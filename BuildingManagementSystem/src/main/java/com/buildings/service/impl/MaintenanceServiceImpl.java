package com.buildings.service.impl;

import com.buildings.dto.PageResponse;
import com.buildings.dto.request.maintenance.*;
import com.buildings.dto.response.maintenance.*;
import com.buildings.entity.*;
import com.buildings.entity.enums.*;
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
import java.util.*;
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
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceReviewRepository maintenanceReviewRepository;
    private final MaintenanceProgressRepository maintenanceProgressRepository;
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

        if (request.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Apartment not found"));
            entity.setApartment(apartment);
        }

        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Building not found"));
            entity.setBuilding(building);
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

    // ==================== Quotation ====================

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

    // ==================== Resource ====================

    @Override
    @Transactional
    public MaintenanceResourceResponse addResource(UUID requestId, MaintenanceResourceRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        MaintenanceResource resource = maintenanceMapper.toMaintenanceResource(request);
        resource.setMaintenanceRequest(maintenanceRequest);
        MaintenanceResource saved = maintenanceResourceRepository.save(resource);
        logAction(requestId, "ADD_RESOURCE", "Dinh kem tai nguyen: " + resource.getName());
        return maintenanceMapper.toMaintenanceResourceResponse(saved);
    }

    @Override
    public List<MaintenanceResourceResponse> getResourcesByRequestId(UUID requestId) {
        findRequestOrThrow(requestId);
        return maintenanceResourceRepository.findByMaintenanceRequestId(requestId).stream()
                .map(maintenanceMapper::toMaintenanceResourceResponse)
                .collect(Collectors.toList());
    }

    // ==================== Log ====================

    @Override
    public List<MaintenanceLogResponse> getLogs(UUID requestId) {
        findRequestOrThrow(requestId);
        return maintenanceLogRepository.findByRequestId(requestId).stream()
                .map(maintenanceMapper::toMaintenanceLogResponse)
                .collect(Collectors.toList());
    }

    // ==================== Schedule ====================

    @Override
    @Transactional
    public MaintenanceScheduleResponse proposeSchedule(UUID requestId, MaintenanceScheduleRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        MaintenanceSchedule schedule = maintenanceMapper.toMaintenanceSchedule(request);
        schedule.setMaintenanceRequest(maintenanceRequest);
        schedule.setStatus(ScheduleStatus.PROPOSED);
        schedule.setProposedByRole(ScheduleProposedBy.RESIDENT);
        MaintenanceSchedule saved = maintenanceScheduleRepository.save(schedule);
        logAction(requestId, "PROPOSE_SCHEDULE", "De xuat lich sua chua: " + request.getProposedTime());
        return maintenanceMapper.toMaintenanceScheduleResponse(saved);
    }

    @Override
    public List<MaintenanceScheduleResponse> getSchedulesByRequestId(UUID requestId) {
        findRequestOrThrow(requestId);
        return maintenanceScheduleRepository.findByMaintenanceRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(maintenanceMapper::toMaintenanceScheduleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MaintenanceScheduleResponse respondToSchedule(UUID requestId, UUID scheduleId, ScheduleRespondRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
        MaintenanceSchedule schedule = maintenanceScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Schedule not found"));

        String action = request.getAction().toUpperCase();
        switch (action) {
            case "ACCEPT" -> {
                schedule.setStatus(ScheduleStatus.CONFIRMED);
                maintenanceScheduleRepository.save(schedule);
                maintenanceRequest.setRequestStatus(RequestStatus.IN_PROGRESS);
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
                        .proposedByRole(ScheduleProposedBy.STAFF)
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

    // ==================== Progress ====================

    @Override
    @Transactional
    public MaintenanceProgressResponse addProgress(UUID requestId, MaintenanceProgressRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
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
        findRequestOrThrow(requestId);
        return maintenanceProgressRepository.findByMaintenanceRequestIdOrderByCreatedAtAsc(requestId).stream()
                .map(maintenanceMapper::toMaintenanceProgressResponse)
                .collect(Collectors.toList());
    }

    // ==================== Review ====================

    @Override
    @Transactional
    public MaintenanceReviewResponse submitReview(UUID requestId, MaintenanceReviewRequest request) {
        MaintenanceRequest maintenanceRequest = findRequestOrThrow(requestId);
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
        findRequestOrThrow(requestId);
        MaintenanceReview review = maintenanceReviewRepository.findByMaintenanceRequestId(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Review not found for this request"));
        return maintenanceMapper.toMaintenanceReviewResponse(review);
    }

    // ==================== Statistics ====================

    @Override
    public MaintenanceStatisticsResponse getStatistics(String from, String to, UUID buildingId) {
        List<MaintenanceRequest> allRequests;
        if (buildingId != null) {
            Specification<MaintenanceRequest> spec = (root, query, cb) ->
                    cb.equal(root.get("building").get("id"), buildingId);
            allRequests = maintenanceRequestRepository.findAll(spec);
        } else {
            allRequests = maintenanceRequestRepository.findAll();
        }

        if (StringUtils.hasText(from) && StringUtils.hasText(to)) {
            LocalDateTime fromDate = LocalDateTime.parse(from + "T00:00:00");
            LocalDateTime toDate = LocalDateTime.parse(to + "T23:59:59");
            allRequests = allRequests.stream()
                    .filter(r -> r.getCreatedAt() != null
                            && !r.getCreatedAt().isBefore(fromDate)
                            && !r.getCreatedAt().isAfter(toDate))
                    .collect(Collectors.toList());
        }

        Map<String, Long> byStatus = allRequests.stream()
                .filter(r -> r.getRequestStatus() != null)
                .collect(Collectors.groupingBy(r -> r.getRequestStatus().name(), Collectors.counting()));
        Map<String, Long> byCategory = allRequests.stream()
                .filter(r -> r.getCategory() != null)
                .collect(Collectors.groupingBy(r -> r.getCategory().name(), Collectors.counting()));
        Map<String, Long> byPriority = allRequests.stream()
                .filter(r -> r.getPriority() != null)
                .collect(Collectors.groupingBy(r -> r.getPriority().name(), Collectors.counting()));

        double avgResolutionDays = allRequests.stream()
                .filter(r -> r.getRequestStatus() == RequestStatus.RESIDENT_ACCEPTED
                        && r.getCreatedAt() != null && r.getClosedAt() != null)
                .mapToLong(r -> java.time.Duration.between(r.getCreatedAt(), r.getClosedAt()).toDays())
                .average().orElse(0.0);

        List<UUID> requestIds = allRequests.stream().map(MaintenanceRequest::getId).collect(Collectors.toList());
        double avgRating = maintenanceReviewRepository.findAll().stream()
                .filter(rev -> requestIds.contains(rev.getMaintenanceRequest().getId()) && rev.getRating() != null)
                .mapToInt(MaintenanceReview::getRating).average().orElse(0.0);

        long overdueCount = allRequests.stream()
                .filter(r -> r.getRequestStatus() == RequestStatus.IN_PROGRESS
                        && r.getStartedAt() != null
                        && r.getStartedAt().isBefore(LocalDateTime.now().minusDays(7)))
                .count();

        return MaintenanceStatisticsResponse.builder()
                .totalRequests(allRequests.size())
                .byStatus(byStatus).byCategory(byCategory).byPriority(byPriority)
                .avgResolutionDays(avgResolutionDays).avgRating(avgRating).overdueCount(overdueCount)
                .pendingCount(byStatus.getOrDefault("PENDING", 0L))
                .inProgressCount(byStatus.getOrDefault("IN_PROGRESS", 0L))
                .completedCount(byStatus.getOrDefault("RESIDENT_ACCEPTED", 0L))
                .cancelledCount(byStatus.getOrDefault("CANCELLED", 0L))
                .build();
    }

    @Override
    public List<StaffWorkloadResponse> getStaffWorkload() {
        List<MaintenanceRequest> allRequests = maintenanceRequestRepository.findAll();
        List<MaintenanceReview> allReviews = maintenanceReviewRepository.findAll();

        return allRequests.stream()
                .filter(r -> r.getStaff() != null)
                .collect(Collectors.groupingBy(MaintenanceRequest::getStaff))
                .entrySet().stream()
                .map(entry -> {
                    User staff = entry.getKey();
                    List<MaintenanceRequest> staffReqs = entry.getValue();
                    List<UUID> staffReqIds = staffReqs.stream().map(MaintenanceRequest::getId).collect(Collectors.toList());

                    double avgRating = allReviews.stream()
                            .filter(rev -> staffReqIds.contains(rev.getMaintenanceRequest().getId()) && rev.getRating() != null)
                            .mapToInt(MaintenanceReview::getRating).average().orElse(0.0);

                    return StaffWorkloadResponse.builder()
                            .staffId(staff.getId()).staffName(staff.getFullName())
                            .totalAssigned((long) staffReqs.size())
                            .inProgress(staffReqs.stream().filter(r -> r.getRequestStatus() == RequestStatus.IN_PROGRESS).count())
                            .completed(staffReqs.stream().filter(r -> r.getRequestStatus() == RequestStatus.RESIDENT_ACCEPTED).count())
                            .cancelled(staffReqs.stream().filter(r -> r.getRequestStatus() == RequestStatus.CANCELLED).count())
                            .avgRating(avgRating == 0.0 ? null : avgRating)
                            .overdueCount(staffReqs.stream()
                                    .filter(r -> r.getRequestStatus() == RequestStatus.IN_PROGRESS
                                            && r.getStartedAt() != null
                                            && r.getStartedAt().isBefore(LocalDateTime.now().minusDays(7)))
                                    .count())
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    public List<MaintenanceRequestResponse> getOverdueRequests() {
        return maintenanceRequestRepository.findAll().stream()
                .filter(r -> r.getRequestStatus() == RequestStatus.IN_PROGRESS
                        && r.getStartedAt() != null
                        && r.getStartedAt().isBefore(LocalDateTime.now().minusDays(7)))
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());
    }

    // ==================== Private helpers ====================

    private MaintenanceRequest findRequestOrThrow(UUID id) {
        return maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Maintenance request not found"));
    }

    private MaintenanceQuotation findQuotationOrThrow(UUID id) {
        return maintenanceQuotationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "Quotation not found"));
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
