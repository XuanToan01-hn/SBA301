package com.buildings.service.impl;

import com.buildings.dto.request.maintenance.MaintenanceQuotationRequest;
import com.buildings.dto.request.maintenance.MaintenanceQuotationUpdateRequest;
import com.buildings.dto.response.maintenance.MaintenanceQuotationResponse;
import com.buildings.entity.MaintenanceItem;
import com.buildings.entity.MaintenanceLog;
import com.buildings.entity.MaintenanceQuotation;
import com.buildings.entity.MaintenanceRequest;
import com.buildings.entity.enums.QuotationStatus;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.MaintenanceMapper;
import com.buildings.repository.MaintenanceItemRepository;
import com.buildings.repository.MaintenanceLogRepository;
import com.buildings.repository.MaintenanceQuotationRepository;
import com.buildings.repository.MaintenanceRequestRepository;
import com.buildings.service.MaintenanceQuotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceQuotationServiceImpl implements MaintenanceQuotationService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceQuotationRepository maintenanceQuotationRepository;
    private final MaintenanceItemRepository maintenanceItemRepository;
    private final MaintenanceLogRepository maintenanceLogRepository;
    private final MaintenanceMapper maintenanceMapper;

    @Override
    @Transactional
    public MaintenanceQuotationResponse createQuotation(UUID requestId, MaintenanceQuotationRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);

        MaintenanceQuotation quotation = maintenanceMapper.toMaintenanceQuotation(request);
        quotation.setMaintenanceRequest(maintenanceRequest);
        quotation.setStatus(QuotationStatus.DRAFT);
        quotation.setCode("Q-" + System.currentTimeMillis());

        MaintenanceQuotation savedQuotation = maintenanceQuotationRepository.save(quotation);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            List<MaintenanceItem> items = request.getItems().stream()
                    .map(itemRequest -> {
                        MaintenanceItem item = maintenanceMapper.toMaintenanceItem(itemRequest);
                        item.setQuotation(savedQuotation);
                        return item;
                    }).collect(Collectors.toList());
            maintenanceItemRepository.saveAll(items);
            savedQuotation.setItems(items);
        }

        maintenanceRequest.setRequestStatus(RequestStatus.QUOTING);
        maintenanceRequestRepository.save(maintenanceRequest);
        logAction(requestId, "CREATE_QUOTATION", "Tao bao gia: " + savedQuotation.getCode());
        return maintenanceMapper.toMaintenanceQuotationResponse(savedQuotation);
    }

    @Override
    public List<MaintenanceQuotationResponse> getQuotationsByRequestId(UUID requestId) {
        findRequestOrThrow(requestId);
        return maintenanceQuotationRepository.findByMaintenanceRequestId(requestId).stream()
                .map(maintenanceMapper::toMaintenanceQuotationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MaintenanceQuotationResponse getQuotationById(UUID quotationId) {
        return maintenanceMapper.toMaintenanceQuotationResponse(findQuotationOrThrow(quotationId));
    }

    @Override
    @Transactional
    public MaintenanceQuotationResponse updateQuotation(UUID quotationId, MaintenanceQuotationUpdateRequest request) {
        MaintenanceQuotation quotation = findQuotationOrThrow(quotationId);
        if (quotation.getStatus() != QuotationStatus.DRAFT) {
            throw new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Chi co the chinh sua bao gia o trang thai DRAFT");
        }
        maintenanceMapper.updateMaintenanceQuotation(quotation, request);

        if (request.getItems() != null) {
            if (quotation.getItems() != null) {
                maintenanceItemRepository.deleteAll(quotation.getItems());
            }
            List<MaintenanceItem> newItems = request.getItems().stream()
                    .map(itemReq -> {
                        MaintenanceItem item = maintenanceMapper.toMaintenanceItem(itemReq);
                        item.setQuotation(quotation);
                        return item;
                    }).collect(Collectors.toList());
            quotation.setItems(maintenanceItemRepository.saveAll(newItems));
        }

        MaintenanceQuotation saved = maintenanceQuotationRepository.save(quotation);
        logAction(quotation.getMaintenanceRequest().getId(), "UPDATE_QUOTATION", "Cap nhat bao gia: " + quotation.getCode());
        return maintenanceMapper.toMaintenanceQuotationResponse(saved);
    }

    @Override
    @Transactional
    public MaintenanceQuotationResponse updateQuotationStatus(UUID quotationId, QuotationStatus status) {
        MaintenanceQuotation quotation = findQuotationOrThrow(quotationId);
        MaintenanceRequest request = quotation.getMaintenanceRequest();

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
            default -> { /* CANCELLED, EXPIRED */ }
        }

        logAction(request.getId(), "UPDATE_QUOTATION_STATUS",
                "Cap nhat trang thai bao gia " + quotation.getCode() + " -> " + status);
        return maintenanceMapper.toMaintenanceQuotationResponse(quotation);
    }

    private MaintenanceRequest findRequestOrThrow(UUID id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));
    }

    private MaintenanceQuotation findQuotationOrThrow(UUID id) {
        return maintenanceQuotationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quotation not found"));
    }

    private void logAction(UUID requestId, String action, String note) {
        maintenanceLogRepository.save(MaintenanceLog.builder()
                .requestId(requestId).action(action).note(note).build());
    }
}
