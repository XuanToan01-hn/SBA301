package com.buildings.controller;

import com.buildings.dto.request.maintenance.MaintenanceRequestCreateRequest;
import com.buildings.service.MaintenanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaintenanceService maintenanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getRequestById_InvalidUuid_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/maintenance-requests/invalid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1010)); // ErrorCode.INVALID_UUID
    }

    @Test
    void createRequest_InvalidUuidInBody_ShouldReturnBadRequest() throws Exception {
        String invalidJson = """
            {
              "title": "Test",
              "description": "Test",
              "apartmentId": "invalid-uuid"
            }
            """;

        mockMvc.perform(post("/maintenance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1010)); // ErrorCode.INVALID_UUID
    }
}
