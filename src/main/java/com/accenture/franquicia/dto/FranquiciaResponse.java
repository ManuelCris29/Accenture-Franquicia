package com.accenture.franquicia.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class FranquiciaResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
}
