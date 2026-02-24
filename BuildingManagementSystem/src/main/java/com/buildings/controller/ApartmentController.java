package com.buildings.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.buildings.dto.ApiResponse;
import com.buildings.entity.Apartment;
import com.buildings.repository.ApartmentRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/apartments")
@RequiredArgsConstructor
public class ApartmentController {

    private final ApartmentRepository apartmentRepository;

    @GetMapping
    public ApiResponse<List<Apartment>> getAll() {
        List<Apartment> result = apartmentRepository.findAll();
        return ApiResponse.<List<Apartment>>builder()
                .result(result)
                .build();
    }
}
