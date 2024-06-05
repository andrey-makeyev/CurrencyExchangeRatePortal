package com.portal.exchangerate.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FxRateDTO {
    private String type;
    private LocalDate date;
    private String baseCurrency;
    private BigDecimal rate;
    private List<CcyAmtDTO> currencyAmounts;

}