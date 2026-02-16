package com.buildings.service.impl;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.*;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MaintenanceMapper;
import com.buildings.repository.*;
import com.buildings.service.MaintenanceService;
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
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceQuotationRepository maintenanceQuotationRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceResourceRepository maintenanceResourceRepository;
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

        // Validate and set Apartment
        if (request.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Apartment not found"));
            entity.setApartment(apartment);
        }

        // Validate and set Building
        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Building not found"));
            entity.setBuilding(building);
        }

        // TODO: Set Requester from Security Context
        // entity.setRequester(currentUser);

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);

        logAction(saved.getId(), "CREATE_REQUEST", "Created maintenance request");

        return maintenanceMapper.toMaintenanceRequestResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceRequestResponse updateRequest(UUID id, MaintenanceRequestUpdateRequest request) {
        log.info("Updating maintenance request: {}", id);
        MaintenanceRequest entity = maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));

        maintenanceMapper.updateMaintenanceRequest(entity, request);

        if (request.getStaffId() != null) {
            User staff = userRepository.findById(request.getStaffId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Staff not found"));
            entity.setStaff(staff);
        }

        MaintenanceRequest saved = maintenanceRequestRepository.save(entity);
        logAction(saved.getId(), "UPDATE_REQUEST", "Updated maintenance request details");

        return maintenanceMapper.toMaintenanceRequestResponse(saved);
    }

    @Override
    public PageResponse<MaintenanceRequestResponse> getRequests(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Specification<MaintenanceRequest> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.get("code")), likePattern)
            );
        };

        Page<MaintenanceRequest> pageResult = maintenanceRequestRepository.findAll(spec, pageable);

        List<MaintenanceRequestResponse> responseList = pageResult.getContent().stream()
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(
                page,
                size,
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                responseList
        );
    }

    @Override
    public List<MaintenanceRequestResponse> getAllRequests(String keyword) {
         Specification<MaintenanceRequest> spec = (root, query, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.get("code")), likePattern)
            );
        };

        List<MaintenanceRequest> list = maintenanceRequestRepository.findAll(spec);
        return list.stream()
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceRequestResponse getRequestById(UUID id) {
        MaintenanceRequest entity = maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));
        return maintenanceMapper.toMaintenanceRequestResponse(entity);
    }

    @Override
    @Transactional
    public MaintenanceQuotationResponse createQuotation(UUID requestId, MaintenanceQuotationRequest request) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(requestId)
                 .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));

        MaintenanceQuotation quotation = maintenanceMapper.toMaintenanceQuotation(request);
        quotation.setMaintenanceRequest(maintenanceRequest);
        quotation.setStatus(QuotationStatus.DRAFT); // Default status
        quotation.setCode("Q-" + System.currentTimeMillis()); // Simple code generation

        MaintenanceQuotation savedQuotation = maintenanceQuotationRepository.save(quotation);

        if (request.getItems() != null) {
            List<MaintenanceItem> items = request.getItems().stream()
                    .map(itemRequest -> {
                        MaintenanceItem item = maintenanceMapper.toMaintenanceItem(itemRequest);
                        item.setQuotation(savedQuotation);
                        return item;
                    }).collect(Collectors.toList());
            maintenanceItemRepository.saveAll(items);
            savedQuotation.setItems(items);
        }

        logAction(requestId, "CREATE_QUOTATION", "Created quotation: " + savedQuotation.getCode());

        return maintenanceMapper.toMaintenanceQuotationResponse(savedQuotation);
    }

    @Override
    @Transactional
    public MaintenanceQuotationResponse updateQuotationStatus(UUID quotationId, QuotationStatus status) {
        MaintenanceQuotation quotation = maintenanceQuotationRepository.findById(quotationId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quotation not found"));

        quotation.setStatus(status);
        MaintenanceQuotation saved = maintenanceQuotationRepository.save(quotation);

        logAction(quotation.getMaintenanceRequest().getId(), "UPDATE_QUOTATION_STATUS",
                "Updated quotation " + quotation.getCode() + " status to " + status);

        return maintenanceMapper.toMaintenanceQuotationResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceResourceResponse addResource(UUID requestId, MaintenanceResourceRequest request) {
        MaintenanceRequest maintenanceRequest = maintenanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));

        MaintenanceResource resource = maintenanceMapper.toMaintenanceResource(request);
        resource.setMaintenanceRequest(maintenanceRequest);

        MaintenanceResource saved = maintenanceResourceRepository.save(resource);

        logAction(requestId, "ADD_RESOURCE", "Added resource: " + resource.getName());

        return maintenanceMapper.toMaintenanceResourceResponse(saved);
    }

    @Override
    public List<MaintenanceLogResponse> getLogs(UUID requestId) {
        return maintenanceLogRepository.findByRequestId(requestId).stream()
                .map(maintenanceMapper::toMaintenanceLogResponse)
                .collect(Collectors.toList());
    }

    private void logAction(UUID requestId, String action, String note) {
        MaintenanceLog logEntry = MaintenanceLog.builder()
                .requestId(requestId)
                .action(action)
                .note(note)
                .build();
        // Set Actor ID from context if available
        maintenanceLogRepository.save(logEntry);
    }

    private String generateCode() {
        return "REQ-" + System.currentTimeMillis();
    }
}
