package com.accenture.franquicia.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("sucursales")

public class Sucursal {
    @Id
    private Long id;
    private String name;
    private Long franquiciaId;
    private LocalDateTime createdAt;   
}


