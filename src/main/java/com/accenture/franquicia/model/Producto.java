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
@Table("productos")


public class Producto {
    @Id
    private Long id;
    private String name;
    private long sucursalId;
    private LocalDateTime created_at;
    
}

