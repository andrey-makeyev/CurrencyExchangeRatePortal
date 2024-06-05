package com.portal.exchangerate.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CcyAmtDTO {
    private String targetCurrency;
    private BigDecimal amount;
}