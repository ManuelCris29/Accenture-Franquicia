package com.accenture.franquicia.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductoResponse {
    private Long id;
    private String name;
    private Integer stock;
    private Long sucursalId;
    private LocalDateTime createdAt;
}
