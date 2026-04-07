package com.cts.ecotrack.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Integer userId;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String status;
}
