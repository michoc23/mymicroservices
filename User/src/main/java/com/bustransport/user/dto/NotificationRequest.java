package com.bustransport.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
public class NotificationRequest {
    @NotNull
    private Long userId;
    @NotNull
    private String title;
    @NotNull
    private String message;
    private String type;
}
