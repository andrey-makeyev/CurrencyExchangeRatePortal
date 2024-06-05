package com.portal.exchangerate.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CcyDTO {
    private String currencyCode;
    private String currencyName;
    private Integer currencyNumber;
    private String minorUnits;
}