package com.buildings.dto.response.role;
import lombok.*;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private String code;
    private String name;
    private String description;
}
