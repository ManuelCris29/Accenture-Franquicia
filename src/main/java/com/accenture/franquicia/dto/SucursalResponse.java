package com.accenture.franquicia.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SucursalResponse {
    private Long id;
    private String name;
    private Long franquiciaId;
    private LocalDateTime createdAt;
}
