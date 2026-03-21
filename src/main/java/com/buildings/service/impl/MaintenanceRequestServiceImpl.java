package com.buildings.service.impl;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.MaintenanceAssignRequest;
import com.buildings.dto.request.maintenance.MaintenanceCancelRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestCreateRequest;
import com.buildings.dto.request.maintenance.MaintenanceRequestUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;
import com.buildings.entity.Apartment;
import com.buildings.entity.Building;
import com.buildings.entity.MaintenanceLog;
import com.buildings.entity.MaintenanceRequest;
import com.buildings.entity.User;
import com.buildings.entity.enums.RequestScope;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MaintenanceMapper;
import com.buildings.repository.ApartmentRepository;
import com.buildings.repository.BuildingRepository;
import com.buildings.repository.MaintenanceLogRepository;
import com.buildings.repository.MaintenanceRequestRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.MaintenanceRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceRequestServiceImpl implements MaintenanceRequestService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceMapper maintenanceMapper;
    private final ApartmentRepository apartmentRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MaintenanceRequestResponse createRequest(MaintenanceRequestCreateRequest request) {
        log.info("Creating maintenance request: {}", request.getTitle());

        MaintenanceRequest entity = maintenanceMapper.toMaintenanceRequest(request);
        if (entity.getScope() == null) {
            entity.setScope(RequestScope.PRIVATE);
        }
        entity.setCode(generateCode());
        entity.setRequestStatus(RequestStatus.PENDING);
        entity.setRequester(getCurrentUserOrThrow());

        applyScopeBindingOnCreate(entity, request);

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);
        logAction(saved.getId(), "CREATE_REQUEST", "Tao yeu cau bao tri: " + saved.getTitle());
        return maintenanceMapper.toMaintenanceRequestResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceRequestResponse updateRequest(UUID id, MaintenanceRequestUpdateRequest request) {
        log.info("Updating maintenance request: {}", id);
        MaintenanceRequest entity = findRequestOrThrow(id);
        maintenanceMapper.updateMaintenanceRequest(entity, request);

        if (entity.getScope() == RequestScope.PUBLIC) {
            entity.setApartment(null);
        }

        if (request.getStaffId() != null) {
            User staff = userRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Staff not found"));
            entity.setStaff(staff);
        }

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);
        logAction(saved.getId(), "UPDATE_REQUEST", "Cap nhat yeu cau bao tri");
        return maintenanceMapper.toMaintenanceRequestResponse(saved);
    }

    @Override
        public PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size, UUID requesterId, RequestScope scope) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Specification<MaintenanceRequest> spec = Specification.where(buildKeywordSpec(keyword))
            .and(buildRequesterIdSpec(requesterId))
            .and(buildScopeSpec(scope));
        Page<MaintenanceRequest> pageResult = maintenanceRequestRepository.findAll(spec, pageable);
        List<MaintenanceRequestResponse> responseList = pageResult.getContent().stream()
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageResult.getTotalPages(), pageResult.getTotalElements(), responseList);
    }

    @Override
        public List<MaintenanceRequestResponse> getAllRequests(String keyword, UUID requesterId, RequestScope scope) {
        Specification<MaintenanceRequest> spec = Specification.where(buildKeywordSpec(keyword))
            .and(buildRequesterIdSpec(requesterId))
            .and(buildScopeSpec(scope));
        return maintenanceRequestRepository.findAll(spec, Sort.by("createdAt").descending()).stream()
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceRequestResponse getRequestById(UUID id) {
        return maintenanceMapper.toMaintenanceRequestResponse(findRequestOrThrow(id));
    }

    @Override
    @Transactional
    public MaintenanceRequestResponse cancelRequest(UUID id, MaintenanceCancelRequest request) {
        MaintenanceRequest entity = findRequestOrThrow(id);
        entity.setRequestStatus(RequestStatus.CANCELLED);
        entity.setClosedAt(LocalDateTime.now());
        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);
        String note = "Huy yeu cau" + (StringUtils.hasText(request.getReason()) ? ": " + request.getReason() : "");
        logAction(saved.getId(), "CANCEL_REQUEST", note);
        return maintenanceMapper.toMaintenanceRequestResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceRequestResponse assignRequest(UUID id, MaintenanceAssignRequest request) {
        MaintenanceRequest entity = findRequestOrThrow(id);
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Staff not found"));
        entity.setStaff(staff);
        entity.setRequestStatus(RequestStatus.VERIFYING);
        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);
        String note = "Giao cho nhan vien: " + staff.getFullName()
            + (StringUtils.hasText(request.getNote()) ? " | Ghi chu: " + request.getNote() : "");
        logAction(saved.getId(), "ASSIGN_REQUEST", note);
        return maintenanceMapper.toMaintenanceRequestResponse(saved);
    }

    private MaintenanceRequest findRequestOrThrow(UUID id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));
    }

    private Specification<MaintenanceRequest> buildKeywordSpec(String keyword) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String like = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("code")), like));
        };
    }

    private Specification<MaintenanceRequest> buildRequesterIdSpec(UUID requesterId) {
        if (requesterId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("requester").get("id"), requesterId);
    }

    private Specification<MaintenanceRequest> buildScopeSpec(RequestScope scope) {
        if (scope == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("scope"), scope);
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

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private String generateCode() {
        return "REQ-" + System.currentTimeMillis();
    }

    private void applyScopeBindingOnCreate(MaintenanceRequest entity, MaintenanceRequestCreateRequest request) {
        if (entity.getScope() == RequestScope.PUBLIC) {
            if (request.getBuildingId() == null) {
                throw new AppException(ErrorCode.INVALID_KEY, "PUBLIC request requires buildingId");
            }

            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));
            entity.setBuilding(building);
            entity.setApartment(null);
            return;
        }

        if (request.getApartmentId() == null) {
            throw new AppException(ErrorCode.INVALID_KEY, "PRIVATE request requires apartmentId");
        }

        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APARTMENT_NOT_FOUND));
        entity.setApartment(apartment);

        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new AppException(ErrorCode.BUILDING_NOT_FOUND));
            if (apartment.getBuilding() != null && !apartment.getBuilding().getId().equals(building.getId())) {
                throw new AppException(ErrorCode.INVALID_KEY, "Apartment does not belong to provided building");
            }
            entity.setBuilding(building);
        } else {
            entity.setBuilding(apartment.getBuilding());
        }
    }
}
