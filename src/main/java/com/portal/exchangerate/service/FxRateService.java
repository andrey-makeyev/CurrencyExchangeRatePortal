package com.portal.exchangerate.service;

import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.model.Ccy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FxRateService {

    void saveCurrencyDataFromXml();

    List<CcyDTO> getCurrencies();

    List<FxRateDTO> getCurrentFxRates(ExchangeRateType exchangeRateType, String rate, String currency);

    List<FxRateDTO> getFxRates(ExchangeRateType exchangeRateType, String rate, String currency, String startDate, String endDate);

    BigDecimal calculateCrossRate(String fromCurrency, String toCurrency);

    List<CcyDTO> getAllCurrencies();

    BigDecimal getFxRate(LocalDate fxRateDate, String fxRateType, Ccy baseCurrency);

    BigDecimal getRateForCurrency(String targetCurrencyCode);

    List<CcyDTO> getAvailableCurrencies();

}