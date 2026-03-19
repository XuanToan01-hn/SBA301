package com.buildings.service.impl;

import com.buildings.dto.request.maintenance.MaintenanceQuotationRequest;
import com.buildings.dto.request.maintenance.MaintenanceQuotationUpdateRequest;
import com.buildings.dto.request.maintenance.MaintenanceItemRequest;
import com.buildings.dto.response.maintenance.MaintenanceQuotationResponse;
import com.buildings.entity.Role;
import com.buildings.entity.MaintenanceItem;
import com.buildings.entity.MaintenanceLog;
import com.buildings.entity.MaintenanceQuotation;
import com.buildings.entity.MaintenanceRequest;
import com.buildings.entity.User;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.entity.enums.RequestScope;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MaintenanceMapper;
import com.buildings.repository.MaintenanceItemRepository;
import com.buildings.repository.MaintenanceLogRepository;
import com.buildings.repository.MaintenanceQuotationRepository;
import com.buildings.repository.MaintenanceRequestRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.MaintenanceQuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceQuotationServiceImpl implements MaintenanceQuotationService {

    private static final EnumSet<RequestStatus> ALLOWED_REQUEST_STATUSES_FOR_QUOTATION = EnumSet.of(
            RequestStatus.VERIFYING,
            RequestStatus.QUOTING
    );

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceQuotationRepository maintenanceQuotationRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceMapper maintenanceMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaintenanceQuotationResponse createQuotation(UUID requestId, MaintenanceQuotationRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureQuotationAccess(maintenanceRequest, true);
        validateRequestStatusForQuotation(maintenanceRequest.getRequestStatus());
        validateQuotationPayload(request.getTitle(), request.getValidUntil(), request.getItems());

        MaintenanceQuotation quotation = maintenanceMapper.toMaintenanceQuotation(request);
        quotation.setMaintenanceRequest(maintenanceRequest);
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setCode("Q-" + System.currentTimeMillis());

        MaintenanceQuotation savedQuotation = maintenanceQuotationRepository.save(quotation);

        List<MaintenanceItem> items = mapItemsForQuotation(request.getItems(), savedQuotation);
        savedQuotation.setItems(maintenanceItemRepository.saveAll(items));
        savedQuotation.setTotalAmount(calculateTotalAmount(savedQuotation.getItems()));
        savedQuotation = maintenanceQuotationRepository.save(savedQuotation);

        maintenanceRequest.setRequestStatus(RequestStatus.QUOTING);
        maintenanceRequestRepository.save(maintenanceRequest);
        logAction(requestId, "CREATE_QUOTATION",
                "Tao bao gia: " + savedQuotation.getCode() + ", tong tien=" + savedQuotation.getTotalAmount());
        return maintenanceMapper.toMaintenanceQuotationResponse(savedQuotation);
    }

    @Override
    public List<MaintenanceQuotationResponse> getQuotationsByRequestId(UUID requestId, boolean isResident) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureQuotationAccess(maintenanceRequest, false);
        return maintenanceQuotationRepository.findByMaintenanceRequestId(requestId).stream()
                .filter(quotation -> !isResident || quotation.getStatus() != QuotationStatus.DRAFT)
                .map(maintenanceMapper::toMaintenanceQuotationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceQuotationResponse getQuotationById(UUID quotationId) {
        MaintenanceQuotation quotation = findQuotationOrThrow(quotationId);
        ensureQuotationAccess(quotation.getMaintenanceRequest(), false);
        if (!canCurrentUserViewQuotation(quotation)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quotation not found");
        }
        return maintenanceMapper.toMaintenanceQuotationResponse(quotation);
    }

    @Override
    @Transactional
    public MaintenanceQuotationResponse updateQuotation(UUID quotationId, MaintenanceQuotationUpdateRequest request) {
        MaintenanceQuotation quotation = findQuotationOrThrow(quotationId);
        ensureQuotationAccess(quotation.getMaintenanceRequest(), true);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new AppException(ErrorCode.INVALID_KEY, "Chi co the chinh sua bao gia o trang thai DRAFT");
        }
        validateRequestStatusForQuotation(quotation.getMaintenanceRequest().getRequestStatus());
        validateQuotationPayload(request.getTitle(), request.getValidUntil(), request.getItems());

        maintenanceMapper.updateMaintenanceQuotation(quotation, request);

        if (request.getItems() != null) {
            if (quotation.getItems() != null && !quotation.getItems().isEmpty()) {
                maintenanceItemRepository.deleteAll(quotation.getItems());
            }
            quotation.setItems(maintenanceItemRepository.saveAll(mapItemsForQuotation(request.getItems(), quotation)));
        }
        quotation.setTotalAmount(calculateTotalAmount(quotation.getItems()));

        MaintenanceQuotation saved = maintenanceQuotationRepository.save(quotation);
        logAction(quotation.getMaintenanceRequest().getId(), "UPDATE_QUOTATION",
                "Cap nhat bao gia: " + quotation.getCode() + ", tong tien=" + saved.getTotalAmount());
        return maintenanceMapper.toMaintenanceQuotationResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceQuotationResponse updateQuotationStatus(UUID quotationId, QuotationStatus status) {
        MaintenanceQuotation quotation = findQuotationOrThrow(quotationId);
        MaintenanceRequest request = quotation.getMaintenanceRequest();
        ensureQuotationAccess(request, true);
        ensureStatusUpdateActorAllowed(request, status);
        validateQuotationStatusTransition(quotation.getStatus(), status);

        quotation.setStatus(status);
        maintenanceQuotationRepository.save(quotation);

        switch (status) {
            case SENT -> {
                request.setRequestStatus(RequestStatus.WAITING_APPROVAL);
                maintenanceRequestRepository.save(request);
            }
            case APPROVED -> {
                request.setRequestStatus(RequestStatus.APPROVED);
                maintenanceRequestRepository.save(request);
            }
            case REJECTED -> {
                request.setRequestStatus(RequestStatus.QUOTING);
                maintenanceRequestRepository.save(request);
            }
            case CANCELLED, EXPIRED -> {
                // Only update quotation status.
            }
            default -> throw new AppException(ErrorCode.INVALID_KEY, "Trang thai bao gia khong hop le");
        }

        logAction(request.getId(), "UPDATE_QUOTATION_STATUS",
                "Cap nhat trang thai bao gia " + quotation.getCode() + " -> " + status);
        return maintenanceMapper.toMaintenanceQuotationResponse(quotation);
    }

    private void validateRequestStatusForQuotation(RequestStatus requestStatus) {
        if (!ALLOWED_REQUEST_STATUSES_FOR_QUOTATION.contains(requestStatus)) {
            throw new AppException(
                    ErrorCode.INVALID_KEY,
                    "Khong the tao/cap nhat bao gia khi yeu cau dang o trang thai " + requestStatus
            );
        }
    }

    private void validateQuotationPayload(String title, LocalDateTime validUntil, List<?> items) {
        if (title == null || title.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Tieu de bao gia khong duoc de trong");
        }
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY, "Bao gia phai co it nhat 1 hang muc");
        }
        if (validUntil != null && !validUntil.isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_KEY, "Han hieu luc phai lon hon thoi diem hien tai");
        }
    }

    private void validateQuotationStatusTransition(QuotationStatus current, QuotationStatus target) {
        if (current == target) {
            return;
        }

        boolean isValid = switch (current) {
            case DRAFT -> target == QuotationStatus.SENT || target == QuotationStatus.CANCELLED;
            case SENT -> target == QuotationStatus.APPROVED
                    || target == QuotationStatus.REJECTED
                    || target == QuotationStatus.EXPIRED
                    || target == QuotationStatus.CANCELLED;
            case REJECTED -> target == QuotationStatus.CANCELLED;
            case APPROVED, CANCELLED, EXPIRED -> false;
        };

        if (!isValid) {
            throw new AppException(
                    ErrorCode.INVALID_KEY,
                    "Khong the chuyen trang thai bao gia tu " + current + " sang " + target
            );
        }
    }

    private List<MaintenanceItem> mapItemsForQuotation(List<MaintenanceItemRequest> itemRequests,
                                                       MaintenanceQuotation quotation) {
        return itemRequests.stream()
                .map(itemReq -> {
                    MaintenanceItem item = maintenanceMapper.toMaintenanceItem(itemReq);
                    item.setQuotation(quotation);
                    return item;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalAmount(List<MaintenanceItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private MaintenanceRequest findRequestOrThrow(UUID id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));
    }

    private MaintenanceQuotation findQuotationOrThrow(UUID id) {
        return maintenanceQuotationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quotation not found"));
    }

    private boolean canCurrentUserViewQuotation(MaintenanceQuotation quotation) {
        User currentUser = getCurrentUserWithRoles();
        if (currentUser == null || !StringUtils.hasText(currentUser.getEmail())) {
            return false;
        }
        MaintenanceRequest request = quotation.getMaintenanceRequest();
        if (request == null) {
            return false;
        }

        boolean isRequester = isRequester(currentUser, request);
        boolean isAssignedStaff = isAssignedStaff(currentUser, request);
        boolean isAdmin = isAdminActor(currentUser);

        return isRequester || isAssignedStaff || isAdmin || quotation.getStatus() != QuotationStatus.DRAFT;
    }

    private void ensureQuotationAccess(MaintenanceRequest request, boolean writeAction) {
        User currentUser = getCurrentUserWithRoles();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (request == null || request.getScope() != RequestScope.PUBLIC) {
            return;
        }

        if (isAdminActor(currentUser) || isAssignedStaff(currentUser, request)) {
            return;
        }

        if (isRequester(currentUser, request)) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    writeAction
                            ? "Resident cannot perform quotation actions for PUBLIC requests"
                            : "Resident cannot access quotations for PUBLIC requests");
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private void ensureStatusUpdateActorAllowed(MaintenanceRequest request, QuotationStatus targetStatus) {
        if (request == null || request.getScope() != RequestScope.PUBLIC) {
            return;
        }

        User currentUser = getCurrentUserWithRoles();
        if (currentUser == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (targetStatus == QuotationStatus.APPROVED || targetStatus == QuotationStatus.REJECTED) {
            if (!isAdminActor(currentUser)) {
                throw new AppException(ErrorCode.UNAUTHORIZED,
                        "Only admin/manager can approve or reject quotation for PUBLIC requests");
            }
            return;
        }

        if ((targetStatus == QuotationStatus.SENT || targetStatus == QuotationStatus.CANCELLED)
                && !(isAssignedStaff(currentUser, request) || isAdminActor(currentUser))) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    private User getCurrentUserWithRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            return null;
        }
        return userRepository.findByEmailWithRoles(authentication.getName()).orElse(null);
    }

    private boolean isAssignedStaff(User currentUser, MaintenanceRequest request) {
        return request.getStaff() != null
                && request.getStaff().getEmail() != null
                && currentUser.getEmail() != null
                && request.getStaff().getEmail().equalsIgnoreCase(currentUser.getEmail());
    }

    private boolean isRequester(User currentUser, MaintenanceRequest request) {
        return request.getRequester() != null
                && request.getRequester().getEmail() != null
                && currentUser.getEmail() != null
                && request.getRequester().getEmail().equalsIgnoreCase(currentUser.getEmail());
    }

    private boolean isAdminActor(User user) {
        return hasRole(user, "ADMIN")
                || hasRole(user, "MANAGER")
                || hasRole(user, Role.ROLE_BUILDING_MANAGER);
    }

    private boolean hasRole(User user, String roleCode) {
        if (user == null || user.getUserRoles() == null) {
            return false;
        }
        return user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole() != null && roleCode.equalsIgnoreCase(ur.getRole().getCode()));
    }

    private void logAction(UUID requestId, String action, String note) {
        maintenanceLogRepository.save(MaintenanceLog.builder()
                .requestId(requestId).action(action).note(note).build());
    }
}
