package com.bustransport.ticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiErrorResponse {

    private Integer status;
    private String error;
    private String message;
    private List<String> details;
    private String path;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
