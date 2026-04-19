package com.accenture.franquicia.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor

public class TopProductDTO {

    private long sucursalId;
    private String sucursalName;
    private long productId;
    private String productName;
    private Integer stock;
    
}
