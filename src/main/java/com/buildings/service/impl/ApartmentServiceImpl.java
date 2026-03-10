package com.buildings.service.impl;

import com.buildings.dto.request.apartment_resident.ApartmentResidentRequest;
import com.buildings.dto.response.apartment.ApartmentResponse;
import com.buildings.dto.response.apartment_resident.ApartmentResidentResponse;
import com.buildings.entity.Apartment;
import com.buildings.entity.ApartmentResident;
import com.buildings.entity.User;
import com.buildings.entity.enums.ApartmentStatus;
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
import java.util.List;
import java.util.UUID;

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

        ApartmentResident resident = ApartmentResident.builder()
                .apartment(apartment)
                .user(user)
                .residentType(request.getResidentType())
                .idCardNumber(request.getIdCardNumber())
                .contractDetails(request.getContractDetails())
                .ownershipCertificate(request.getOwnershipCertificate())
                .legalDocs(request.getLegalDocs())
                .note(request.getNote())
                .assignedAt(LocalDateTime.now())
                .build();

        residentRepository.save(resident);

        return ApartmentResidentResponse.builder()
                .id(resident.getId()) // Thêm ID của bản ghi vừa tạo
                .apartmentId(apartment.getId()) // Thêm ID căn hộ
                .apartmentCode(apartment.getCode())
                .userId(user.getId()) // Thêm ID người dùng
                .fullName(user.getFullName())
                .residentType(resident.getResidentType())
                .assignedAt(resident.getAssignedAt())
                .isCurrent(true) // Gán là true vì đây là cư dân đang hoạt động (movedOutAt is null)
                .build();
    }
    @Override
    public List<ApartmentResponse> getAllApartments() {
        return apartmentMapper.toResponseList(apartmentRepository.findAll());
    }

    @Override
    public ApartmentResponse getById(UUID apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APARTMENT_NOT_FOUND));

        return apartmentMapper.toResponse(apartment);
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
    public Page<ApartmentResponse> searchWithFilters(UUID buildingId, Integer floorNumber, ApartmentStatus status, Integer bedroomCount, Pageable pageable) {
        return apartmentRepository.findWithFilters(buildingId, floorNumber, status, bedroomCount, pageable).map(apartmentMapper::toResponse);
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
    @Transactional
    public void deleteByBuilding(UUID buildingId) {
        apartmentRepository.deleteByBuildingId(buildingId);
    }
}
