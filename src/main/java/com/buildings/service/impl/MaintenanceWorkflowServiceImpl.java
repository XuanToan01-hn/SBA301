package com.buildings.service.impl;

import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.*;
import com.buildings.entity.Role;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.entity.enums.RequestScope;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.entity.enums.ScheduleProposedBy;
import com.buildings.entity.enums.ScheduleStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MaintenanceMapper;
import com.buildings.repository.*;
import com.buildings.service.MaintenanceWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceWorkflowServiceImpl implements MaintenanceWorkflowService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceResourceRepository maintenanceResourceRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceReviewRepository maintenanceReviewRepository;
    private final MaintenanceProgressRepository maintenanceProgressRepository;
    private final MaintenanceQuotationRepository maintenanceQuotationRepository;
    private final MonthlyBillsRepository monthlyBillsRepository;
    private final BillDetailRepository billDetailRepository;
    private final UserRepository userRepository;
    private final MaintenanceMapper maintenanceMapper;
    private final com.buildings.mapper.MonthlyBillMapper monthlyBillMapper;

    @Override
    @Transactional
    public MaintenanceResourceResponse addResource(UUID requestId, MaintenanceResourceRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, false);
        MaintenanceResource resource = maintenanceMapper.toMaintenanceResource(request);
        resource.setMaintenanceRequest(maintenanceRequest);
        resource.setUser(getCurrentUserOrThrow());
        MaintenanceResource saved = maintenanceResourceRepository.save(resource);
        logAction(requestId, "ADD_RESOURCE", "Dinh kem tai nguyen: " + resource.getName());
        return maintenanceMapper.toMaintenanceResourceResponse(saved);
    }

    @Override
    public List<MaintenanceResourceResponse> getResourcesByRequestId(UUID requestId) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, false);
        return maintenanceResourceRepository.findByMaintenanceRequestId(requestId).stream()
                .map(maintenanceMapper::toMaintenanceResourceResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceLogResponse> getLogs(UUID requestId) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, false);
        return maintenanceLogRepository.findByRequestId(requestId).stream()
                .map(maintenanceMapper::toMaintenanceLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaintenanceScheduleResponse proposeSchedule(UUID requestId, MaintenanceScheduleRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, true);

        User actor = getCurrentUserOrThrow();
        MaintenanceSchedule schedule = maintenanceMapper.toMaintenanceSchedule(request);
        schedule.setMaintenanceRequest(maintenanceRequest);
        schedule.setStatus(ScheduleStatus.PROPOSED);
        schedule.setProposedByRole(resolveScheduleProposedBy(actor));
        MaintenanceSchedule saved = maintenanceScheduleRepository.save(schedule);
        logAction(requestId, "PROPOSE_SCHEDULE", "De xuat lich sua chua: " + request.getProposedTime());
        return maintenanceMapper.toMaintenanceScheduleResponse(saved);
    }

    @Override
    public List<MaintenanceScheduleResponse> getSchedulesByRequestId(UUID requestId) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, false);
        return maintenanceScheduleRepository.findByMaintenanceRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(maintenanceMapper::toMaintenanceScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaintenanceScheduleResponse respondToSchedule(UUID requestId, UUID scheduleId, ScheduleRespondRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, true);
        User actor = getCurrentUserOrThrow();
        MaintenanceSchedule schedule = maintenanceScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Schedule not found"));

        String action = request.getAction().toUpperCase();
        switch (action) {
            case "ACCEPT" -> {
                schedule.setStatus(ScheduleStatus.CONFIRMED);
                maintenanceScheduleRepository.save(schedule);
                maintenanceRequest.setRequestStatus(RequestStatus.IN_PROGRESS);
                maintenanceRequest.setPreferredTime(schedule.getProposedTime());
                maintenanceRequest.setStartedAt(schedule.getProposedTime());
                maintenanceRequestRepository.save(maintenanceRequest);
                logAction(requestId, "CONFIRM_SCHEDULE", "Xac nhan lich sua chua: " + schedule.getProposedTime());
            }
            case "REJECT" -> {
                schedule.setStatus(ScheduleStatus.REJECTED);
                maintenanceScheduleRepository.save(schedule);
                logAction(requestId, "REJECT_SCHEDULE",
                        "Tu choi lich" + (StringUtils.hasText(request.getNote()) ? ": " + request.getNote() : ""));
            }
            case "COUNTER_PROPOSE" -> {
                if (request.getCounterProposedTime() == null) {
                    throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "counterProposedTime la bat buoc khi COUNTER_PROPOSE");
                }
                schedule.setStatus(ScheduleStatus.COUNTER_PROPOSED);
                maintenanceScheduleRepository.save(schedule);
                MaintenanceSchedule counter = MaintenanceSchedule.builder()
                        .maintenanceRequest(maintenanceRequest)
                        .proposedTime(request.getCounterProposedTime())
                        .estimatedDuration(request.getCounterEstimatedDuration())
                        .note(request.getNote())
                        .status(ScheduleStatus.PROPOSED)
                    .proposedByRole(resolveScheduleProposedBy(actor))
                        .parentSchedule(schedule)
                        .build();
                MaintenanceSchedule savedCounter = maintenanceScheduleRepository.save(counter);
                logAction(requestId, "COUNTER_PROPOSE_SCHEDULE", "De xuat lai lich: " + request.getCounterProposedTime());
                return maintenanceMapper.toMaintenanceScheduleResponse(savedCounter);
            }
            default -> throw new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                    "Action khong hop le. Cac gia tri: ACCEPT, REJECT, COUNTER_PROPOSE");
        }
        return maintenanceMapper.toMaintenanceScheduleResponse(schedule);
    }

    @Override
    @Transactional
    public MaintenanceProgressResponse addProgress(UUID requestId, MaintenanceProgressRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, true);
        MaintenanceProgress progress = maintenanceMapper.toMaintenanceProgress(request);
        progress.setMaintenanceRequest(maintenanceRequest);

        if (request.getProgressPercent() != null && request.getProgressPercent() >= 100) {
            maintenanceRequest.setRequestStatus(RequestStatus.COMPLETED);
            maintenanceRequest.setFinishedAt(LocalDateTime.now());
            maintenanceRequestRepository.save(maintenanceRequest);
            logAction(requestId, "COMPLETE_REQUEST", "Hoan thanh sua chua (tien do 100%)");
        }

        MaintenanceProgress saved = maintenanceProgressRepository.save(progress);
        logAction(requestId, "UPDATE_PROGRESS", "Cap nhat tien do: " + request.getProgressPercent() + "%");
        return maintenanceMapper.toMaintenanceProgressResponse(saved);
    }

    @Override
    public List<MaintenanceProgressResponse> getProgressByRequestId(UUID requestId) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, false);
        return maintenanceProgressRepository.findByMaintenanceRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(maintenanceMapper::toMaintenanceProgressResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaintenanceReviewResponse submitReview(UUID requestId, MaintenanceReviewRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, true);
        if (maintenanceRequest.getRequestStatus() != RequestStatus.COMPLETED) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Chi co the danh gia khi yeu cau o trang thai COMPLETED");
        }
        if (maintenanceReviewRepository.existsByMaintenanceRequestId(requestId)) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Yeu cau nay da duoc danh gia roi");
        }

        MaintenanceReview review = maintenanceMapper.toMaintenanceReview(request);
        review.setMaintenanceRequest(maintenanceRequest);
        MaintenanceReview saved = maintenanceReviewRepository.save(review);

        switch (request.getOutcome()) {
            case ACCEPTED, PARTIAL_ACCEPT -> {
                maintenanceRequest.setRequestStatus(RequestStatus.RESIDENT_ACCEPTED);
                maintenanceRequest.setFinishedAt(LocalDateTime.now());
                maintenanceRequest.setClosedAt(LocalDateTime.now());
                createOrAttachMaintenanceBill(maintenanceRequest);
                maintenanceRequestRepository.save(maintenanceRequest);
                logAction(requestId, "RESIDENT_ACCEPTED", "Cu dan nghiem thu: " + request.getOutcome() + " - " + request.getRating() + " sao");
            }
            case REDO -> {
                maintenanceRequest.setRequestStatus(RequestStatus.IN_PROGRESS);
                maintenanceRequestRepository.save(maintenanceRequest);
                logAction(requestId, "REDO_REQUESTED", "Cu dan yeu cau lam lai: " + request.getComment());
            }
        }
        return maintenanceMapper.toMaintenanceReviewResponse(saved);
    }

    @Override
    public MaintenanceReviewResponse getReviewByRequestId(UUID requestId) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        ensureWorkflowAccess(maintenanceRequest, false);
        MaintenanceReview review = maintenanceReviewRepository.findByMaintenanceRequestId(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Review not found for this request"));
        return maintenanceMapper.toMaintenanceReviewResponse(review);
    }

    private MaintenanceRequest findRequestOrThrow(UUID id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));
    }

    private void logAction(UUID requestId, String action, String note) {
        maintenanceLogRepository.save(MaintenanceLog.builder()
                .requestId(requestId).action(action).note(note).build());
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !StringUtils.hasText(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return userRepository.findByEmailWithRoles(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private void ensureWorkflowAccess(MaintenanceRequest request, boolean writeAction) {
        User currentUser = getCurrentUserOrThrow();
        if (request.getScope() != RequestScope.PUBLIC) {
            return;
        }

        if (isAdminActor(currentUser)) {
            return;
        }

        if (isAssignedStaff(currentUser, request)) {
            return;
        }

        if (isRequester(currentUser, request)) {
            throw new AppException(ErrorCode.UNAUTHORIZED,
                    writeAction
                            ? "Resident cannot perform workflow actions for PUBLIC requests"
                            : "Resident cannot access workflow details for PUBLIC requests");
        }

        throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    private ScheduleProposedBy resolveScheduleProposedBy(User actor) {
        if (isAdminActor(actor)) {
            return ScheduleProposedBy.MANAGER;
        }
        if (hasRole(actor, Role.ROLE_STAFF)) {
            return ScheduleProposedBy.STAFF;
        }
        return ScheduleProposedBy.RESIDENT;
    }

    private boolean isAssignedStaff(User currentUser, MaintenanceRequest request) {
        return request.getStaff() != null
                && request.getStaff().getEmail() != null
                && request.getStaff().getEmail().equalsIgnoreCase(currentUser.getEmail());
    }

    private boolean isRequester(User currentUser, MaintenanceRequest request) {
        return request.getRequester() != null
                && request.getRequester().getEmail() != null
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

    private void createOrAttachMaintenanceBill(MaintenanceRequest maintenanceRequest) {
        if (maintenanceRequest.getScope() != RequestScope.PRIVATE || maintenanceRequest.getApartment() == null) {
            return;
        }

        MaintenanceQuotation approvedQuotation = maintenanceQuotationRepository
                .findFirstByMaintenanceRequestIdAndStatusOrderByCreatedAtDesc(
                        maintenanceRequest.getId(),
                        QuotationStatus.APPROVED)
                .orElse(null);

        if (approvedQuotation == null || approvedQuotation.getTotalAmount() == null) {
            return;
        }

        String requestCode = maintenanceRequest.getCode();
        if (requestCode != null
                && billDetailRepository.existsMaintenanceChargeByApartmentAndRequestCode(
                maintenanceRequest.getApartment().getId(),
                requestCode)) {
            return;
        }

        YearMonth now = YearMonth.now();
        String periodCode = now.toString();
        MonthlyBills bill = monthlyBillsRepository
                .findFirstByApartmentIdAndPeriodCode(maintenanceRequest.getApartment().getId(), periodCode)
                .orElseGet(() -> MonthlyBills.builder()
                        .apartment(maintenanceRequest.getApartment())
                        .periodFrom(now.atDay(1).atStartOfDay())
                        .periodTo(now.atEndOfMonth().atTime(23, 59, 59))
                        .periodCode(periodCode)
                        .status("UNPAID")
                        .issuedAt(LocalDateTime.now())
                        .dueDate(LocalDateTime.now().plusDays(10))
                        .locked(false)
                        .details(new ArrayList<>())
                        .build());

        if (bill.getDetails() == null) {
            bill.setDetails(new ArrayList<>());
        }

        List<BillDetail> maintenanceDetails;
        if (approvedQuotation.getItems() != null && !approvedQuotation.getItems().isEmpty()) {
            maintenanceDetails = approvedQuotation.getItems().stream()
                    .map(item -> monthlyBillMapper.toBillDetail(item, requestCode))
                    .toList();
        } else {
            maintenanceDetails = List.of(monthlyBillMapper.toBillDetail(approvedQuotation));
        }

        for (BillDetail detail : maintenanceDetails) {
            detail.setBill(bill);
            bill.getDetails().add(detail);
        }

        double subtotal = bill.getDetails().stream().mapToDouble(d -> d.getAmount() != null ? d.getAmount() : 0.0).sum();
        double total = bill.getDetails().stream().mapToDouble(d -> d.getTotalLine() != null ? d.getTotalLine() : 0.0).sum();
        bill.setSubtotal(subtotal);
        bill.setTaxTotal(total - subtotal);
        bill.setTotalAmount(total);

        monthlyBillsRepository.save(bill);
    }
}
