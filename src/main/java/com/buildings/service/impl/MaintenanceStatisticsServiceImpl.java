package com.buildings.service.impl;

import com.buildings.dto.response.maintenance.MaintenanceRequestResponse;
import com.buildings.dto.response.maintenance.MaintenanceStatisticsResponse;
import com.buildings.dto.response.maintenance.StaffWorkloadResponse;
import com.buildings.entity.MaintenanceRequest;
import com.buildings.entity.MaintenanceReview;
import com.buildings.entity.User;
import com.buildings.entity.enums.RequestStatus;
import com.buildings.mapper.MaintenanceMapper;
import com.buildings.repository.MaintenanceRequestRepository;
import com.buildings.repository.MaintenanceReviewRepository;
import com.buildings.service.MaintenanceStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceStatisticsServiceImpl implements MaintenanceStatisticsService {

    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final MaintenanceReviewRepository maintenanceReviewRepository;
    private final MaintenanceMapper maintenanceMapper;

    @Override
    public MaintenanceStatisticsResponse getStatistics(String from, String to, UUID buildingId) {
        Specification<MaintenanceRequest> spec = (root, query, cb) -> cb.conjunction();

        if (buildingId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("building").get("id"), buildingId));
        }

        if (StringUtils.hasText(from) && StringUtils.hasText(to)) {
            LocalDateTime fromDate = LocalDateTime.parse(from + "T00:00:00");
            LocalDateTime toDate = LocalDateTime.parse(to + "T23:59:59");
            spec = spec.and((root, query, cb) -> cb.between(root.get("createdAt"), fromDate, toDate));
        }

        List<MaintenanceRequest> allRequests = maintenanceRequestRepository.findAll(spec);

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
        List<MaintenanceRequest> allRequests = maintenanceRequestRepository.findAllWithStaff();
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
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return maintenanceRequestRepository.findByRequestStatusAndStartedAtBefore(RequestStatus.IN_PROGRESS, sevenDaysAgo)
                .stream()
                .map(maintenanceMapper::toMaintenanceRequestResponse)
                .collect(Collectors.toList());
    }
}
