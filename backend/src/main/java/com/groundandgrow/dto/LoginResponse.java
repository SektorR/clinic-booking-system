package com.groundandgrow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type = "Bearer";
    private String psychologistId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;

    public LoginResponse(String token, String psychologistId, String email, String firstName, String lastName, String role) {
        this.token = token;
        this.type = "Bearer";
        this.psychologistId = psychologistId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
}
