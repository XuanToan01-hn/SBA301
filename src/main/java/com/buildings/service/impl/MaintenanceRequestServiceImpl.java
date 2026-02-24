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
        entity.setCode(generateCode());
        entity.setRequestStatus(RequestStatus.PENDING);

        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId()).orElse(null);
            entity.setBuilding(building);
        }

        if (request.getScope() == RequestScope.PRIVATE && request.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(request.getApartmentId()).orElse(null);
            entity.setApartment(apartment);
        }

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
    public PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<MaintenanceRequest> pageResult = maintenanceRequestRepository.findAll(buildKeywordSpec(keyword), pageable);
        List<MaintenanceRequestResponse> responseList = pageResult.getContent().stream()
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(page, size, pageResult.getTotalPages(), pageResult.getTotalElements(), responseList);
    }

    @Override
    public List<MaintenanceRequestResponse> getAllRequests(String keyword) {
        return maintenanceRequestRepository.findAll(buildKeywordSpec(keyword)).stream()
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
        logAction(saved.getId(), "ASSIGN_REQUEST", "Giao cho nhan vien: " + staff.getFullName());
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

    private void logAction(UUID requestId, String action, String note) {
        maintenanceLogRepository.save(MaintenanceLog.builder()
                .requestId(requestId).action(action).note(note).build());
    }

    private String generateCode() {
        return "REQ-" + System.currentTimeMillis();
    }
}
