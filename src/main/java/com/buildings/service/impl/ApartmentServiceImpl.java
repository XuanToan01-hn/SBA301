package com.buildings.service.impl;

import com.buildings.dto.request.apartment_resident.ApartmentResidentRequest;
import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.dto.response.apartment_resident.ApartmentResidentResponse;
import com.buildings.entity.Apartment;
import com.buildings.entity.ApartmentResident;
import com.buildings.entity.User;
import com.buildings.entity.enums.ApartmentStatus;
import com.buildings.entity.enums.ResidentType;
import com.buildings.exception.AppException;
import com.buildings.exception.ErrorCode;
import com.buildings.mapper.ApartmentMapper;
import com.buildings.repository.ApartmentRepository;
import com.buildings.repository.ApartmentResidentRepository;
import com.buildings.repository.UserRepository;
import com.buildings.service.AparmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements AparmentService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper apartmentMapper;

    private final UserRepository userRepository;
    private final ApartmentResidentRepository residentRepository;

    @Override
    @Transactional
    public ApartmentResidentResponse assignResident(ApartmentResidentRequest request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APARTMENT_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (residentRepository.existsByUserIdAndApartmentIdAndMovedOutAtIsNull(request.getUserId(), request.getApartmentId())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        int maxOccupants = (apartment.getBedroomCount() * 2) + 2;
        if (apartment.getCurrentResidentsCount() >= maxOccupants) {
            throw new AppException(ErrorCode.APARTMENT_FULL, "Apartment full. Maximum residents: " + maxOccupants);
        }
        ApartmentResident resident = ApartmentResident.builder()
                .apartment(apartment)
                .user(user)
                .residentType(request.getResidentType())
                .idCardNumber(request.getIdCardNumber())
                .assignedAt(LocalDateTime.now())
                .build();

        residentRepository.save(resident);

        if (apartment.getStatus() != ApartmentStatus.OCCUPIED) {
            apartment.setStatus(ApartmentStatus.OCCUPIED);
            apartmentRepository.save(apartment);
        }

        return mapToResidentResponse(resident);
    }
    @Override
    public List<ApartmentResponse> getAllApartments() {
        return apartmentMapper.toResponseList(apartmentRepository.findAll());
    }

    @Override
    public List<ApartmentResponse> getByBuildingId(UUID buildingId) {
        return apartmentMapper.toResponseList(apartmentRepository.findByBuildingIdOrderByFloorNumberAscCodeAsc(buildingId));
    }

    @Override
    public Page<ApartmentResponse> getByBuildingIdPaged(UUID buildingId, Pageable pageable) {
        return apartmentRepository.findByBuildingId(buildingId, pageable).map(apartmentMapper::toResponse);
    }

    @Override
    public ApartmentResponse getByBuildingAndCode(UUID buildingId, String code) {
        return apartmentRepository.findByBuildingIdAndCode(buildingId, code)
                .map(apartmentMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("Apartment not found with code: " + code));
    }

    @Override
    public boolean checkExists(UUID buildingId, String code) {
        return apartmentRepository.existsByBuildingIdAndCode(buildingId, code);
    }

    @Override
    public List<ApartmentResponse> getByBuildingAndFloor(UUID buildingId, Integer floorNumber) {
        return apartmentMapper.toResponseList(apartmentRepository.findByBuildingIdAndFloorNumber(buildingId, floorNumber));
    }

    @Override
    public Page<ApartmentResponse> getByBuildingAndStatus(UUID buildingId, ApartmentStatus status, Pageable pageable) {
        return apartmentRepository.findByBuildingIdAndStatus(buildingId, status, pageable).map(apartmentMapper::toResponse);
    }

    @Override
    public Page<ApartmentResponse> getByBuildingAndBedrooms(UUID buildingId, Integer bedroomCount, Pageable pageable) {
        return apartmentRepository.findByBuildingIdAndBedroomCount(buildingId, bedroomCount, pageable).map(apartmentMapper::toResponse);
    }

    @Override
    public Long countTotalInBuilding(UUID buildingId) {
        return apartmentRepository.countByBuildingId(buildingId);
    }

    @Override
    public Long countByStatusInBuilding(UUID buildingId, ApartmentStatus status) {
        return apartmentRepository.countByBuildingIdAndStatus(buildingId, status);
    }

    @Override
    public Page<ApartmentResponse> searchWithFilters(
            UUID buildingId,
            String code,
            Integer floorNumber,
            ApartmentStatus status,
            Integer bedroomCount,
            Pageable pageable) {

        return apartmentRepository.findWithFilters(
                buildingId,
                code,
                floorNumber,
                status,
                bedroomCount,
                pageable
        ).map(apartmentMapper::toResponse);
    }
    @Override
    public Page<ApartmentResponse> getApartmentsWithOwner(UUID buildingId, Pageable pageable) {
        return apartmentRepository.findApartmentsWithOwner(buildingId, pageable).map(apartmentMapper::toResponse);
    }

    @Override
    public Page<ApartmentResponse> getApartmentsWithoutOwner(UUID buildingId, Pageable pageable) {
        return apartmentRepository.findApartmentsWithoutOwner(buildingId, pageable).map(apartmentMapper::toResponse);
    }

    @Override
    public void deleteByBuilding(UUID buildingId) {
        apartmentRepository.deleteByBuildingId(buildingId);
    }

    @Override
    public List<ApartmentResponse> getApartmentsByResidentEmail(String email) {
        return apartmentMapper.toResponseList(apartmentRepository.findByResidentEmail(email));
    }

    @Override
    public ApartmentResponse getById(UUID apartmentId) {
        // 1. Lấy dữ liệu từ Repo
        Apartment apartment = apartmentRepository.findByIdFullInfo(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APARTMENT_NOT_FOUND));

        // 2. Gọi hàm mapping thủ công và trả về
        return mapToApartmentResponse(apartment);
    }

    // --- CÁC HÀM MAPPING THỦ CÔNG ---

    private ApartmentResponse mapToApartmentResponse(Apartment apartment) {
        if (apartment == null) return null;

        // Map thông tin cư dân (chỉ lấy những người đang ở - movedOutAt == null)
        List<ApartmentResidentResponse> residentDtos = new ArrayList<>();
        if (apartment.getResidents() != null) {
            residentDtos = apartment.getResidents().stream()
                    .filter(r -> r.getMovedOutAt() == null) // Chỉ lấy cư dân hiện tại
                    .map(this::mapToResidentResponse)      // Gọi hàm map từng cư dân
                    .collect(Collectors.toList());
        }

        // Map thông tin căn hộ
        return ApartmentResponse.builder()
                .id(apartment.getId())
                .code(apartment.getCode())
                .floorNumber(apartment.getFloorNumber())
                .areaSqm(apartment.getAreaSqm())
                .bedroomCount(apartment.getBedroomCount())
                .status(apartment.getStatus())
                .buildingId(apartment.getBuilding() != null ? apartment.getBuilding().getId() : null)
                .buildingName(apartment.getBuilding() != null ? apartment.getBuilding().getName() : "N/A")
                .residents(residentDtos) // Nhét list cư dân đã map vào đây
                .build();
    }

    @Transactional
    public void moveOut(UUID residentId) {
        ApartmentResident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));
//        if (resident.getMovedOutAt() != null) {
//            throw new AppException(ErrorCode.ALREADY_MOVED_OUT);
//        }
        resident.setMovedOutAt(LocalDateTime.now());
        residentRepository.save(resident);

        // 4. KIỂM TRA TRẠNG THÁI CĂN HỘ
        // Nếu sau khi người này đi, không còn ai ở (active residents = 0), chuyển status về AVAILABLE
        Apartment apartment = resident.getApartment();
        long activeCount = apartment.getResidents().stream()
                .filter(r -> r.getMovedOutAt() == null)
                .count();

        if (activeCount == 0) {
            apartment.setStatus(ApartmentStatus.AVAILABLE);
            apartmentRepository.save(apartment);
        }
    }

    // Trong ApartmentServiceImpl.java

    // Trong ApartmentServiceImpl.java



    @Override
    public Page<ApartmentResidentResponse> getResidencyHistory(UUID apartmentId, ResidentType type, Pageable pageable) {
        // 1. Kiểm tra căn hộ có tồn tại không
        if (!apartmentRepository.existsById(apartmentId)) {
            throw new AppException(ErrorCode.APARTMENT_NOT_FOUND);
        }

        // 2. Gọi Repo để lấy dữ liệu phân trang từ bảng apartment_residents
        // Lưu ý: Pageable từ Frontend sẽ chứa thông tin page, size và sort (ví dụ: assignedAt,desc)
        Page<ApartmentResident> residentPage = residentRepository.findHistoryByApartmentId(apartmentId, type, pageable);

        // 3. Map kết quả sang DTO Response sử dụng hàm mapToResidentResponse bạn đã viết
        return residentPage.map(this::mapToResidentResponse);
    }

    private ApartmentResidentResponse mapToResidentResponse(ApartmentResident resident) {
        if (resident == null) return null;

        User user = resident.getUser(); // Lấy đối tượng User liên kết

        return ApartmentResidentResponse.builder()
                .id(resident.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(user != null ? user.getFullName() : "N/A")
                .email(user != null ? user.getEmail() : "N/A")
                .phone(user != null ? user.getPhone() : "N/A")
                .residentType(resident.getResidentType().name()) // Convert Enum sang String
                .assignedAt(resident.getAssignedAt())
                .isCurrent(resident.getMovedOutAt() == null)
                .build();
    }
}
