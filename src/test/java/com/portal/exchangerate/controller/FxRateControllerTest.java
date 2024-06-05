package com.portal.exchangerate.controller;

import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.service.FxRateService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FxRateControllerTest {

    @Mock
    private FxRateService fxRateService;

    @InjectMocks
    private FxRateController fxRateController;

    @Test
    void getCurrencyListTest() {
        // Empty list
        when(fxRateService.getCurrencies()).thenReturn(Collections.emptyList());

        ResponseEntity<CollectionModel<EntityModel<CcyDTO>>> response = fxRateController.getCurrencyList();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Non-empty list
        CcyDTO currency = new CcyDTO("USD", "JAV doleris", 840, "2");
        when(fxRateService.getCurrencies()).thenReturn(List.of(currency));

        response = fxRateController.getCurrencyList();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void getAvailableCurrencyListTest() {
        // Empty list
        when(fxRateService.getAvailableCurrencies()).thenReturn(Collections.emptyList());

        ResponseEntity<CollectionModel<EntityModel<CcyDTO>>> response = fxRateController.getAvailableCurrencyList();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Non-empty list
        CcyDTO currency = new CcyDTO("EUR", "Euro", 978, "2");
        when(fxRateService.getAvailableCurrencies()).thenReturn(List.of(currency));

        response = fxRateController.getAvailableCurrencyList();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());
    }

    @Test
    void getCurrentExchangeRatesTest() {
        // Successful retrieval
        FxRateDTO fxRate = new FxRateDTO();
        fxRate.setBaseCurrency("USD");
        fxRate.setRate(BigDecimal.valueOf(0.8));
        fxRate.setDate(LocalDate.now());
        fxRate.setType("EU");

        when(fxRateService.getCurrentFxRates(any(), any(), any())).thenReturn(List.of(fxRate));

        ResponseEntity<CollectionModel<EntityModel<FxRateDTO>>> response = fxRateController.getCurrentExchangeRates(ExchangeRateType.EU);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        // Unsupported ExchangeRateType
        response = fxRateController.getCurrentExchangeRates(null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getExchangeRatesTest() {
        // Successful retrieval
        FxRateDTO fxRate = new FxRateDTO();
        fxRate.setBaseCurrency("USD");
        fxRate.setRate(BigDecimal.valueOf(0.8));
        fxRate.setDate(LocalDate.now());
        fxRate.setType("EU");

        when(fxRateService.getFxRates(any(), any(), any(), any(), any())).thenReturn(List.of(fxRate));

        LocalDate date = LocalDate.now();
        ResponseEntity<CollectionModel<EntityModel<FxRateDTO>>> response = fxRateController.getExchangeRates(ExchangeRateType.EU, date);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        // Unsupported ExchangeRateType
        response = fxRateController.getExchangeRates(null, date);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getExchangeRatesForCurrencyTest() {
        // Successful retrieval
        FxRateDTO fxRate = new FxRateDTO();
        fxRate.setBaseCurrency("USD");
        fxRate.setRate(BigDecimal.valueOf(0.8));
        fxRate.setDate(LocalDate.now());
        fxRate.setType("EU");

        when(fxRateService.getFxRates(any(), any(), anyString(), anyString(), anyString())).thenReturn(List.of(fxRate));

        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now();
        ResponseEntity<CollectionModel<EntityModel<FxRateDTO>>> response = fxRateController.getExchangeRatesForCurrency(ExchangeRateType.EU, "USD", startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        // Unsupported ExchangeRateType
        response = fxRateController.getExchangeRatesForCurrency(null, "USD", startDate, endDate);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCrossRateTest_SuccessfulCalculation() {
        when(fxRateService.calculateCrossRate("USD", "EUR")).thenReturn(BigDecimal.valueOf(0.8));

        ResponseEntity<BigDecimal> response = fxRateController.getCrossRate("USD", "EUR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(0.8), response.getBody());

        verify(fxRateService, times(1)).calculateCrossRate("USD", "EUR");
    }

    @Test
    void getCrossRateTest_HandleNullRates() {
        when(fxRateService.calculateCrossRate("USD", "EUR")).thenReturn(null);

        ResponseEntity<BigDecimal> response = fxRateController.getCrossRate("USD", "EUR");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(fxRateService, times(1)).calculateCrossRate("USD", "EUR");
    }

    @Test
    void getCrossRateTest_HandleEntityNotFoundException() {
        when(fxRateService.calculateCrossRate("USD", "EUR")).thenThrow(new EntityNotFoundException("Rate not found"));

        ResponseEntity<BigDecimal> response = fxRateController.getCrossRate("USD", "EUR");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        verify(fxRateService, times(1)).calculateCrossRate("USD", "EUR");
    }

    @Test
    void getCrossRateTest_HandleNonUniqueResultException() {
        when(fxRateService.calculateCrossRate("USD", "EUR")).thenThrow(new NonUniqueResultException("Multiple rates found"));

        ResponseEntity<BigDecimal> response = fxRateController.getCrossRate("USD", "EUR");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());

        verify(fxRateService, times(1)).calculateCrossRate("USD", "EUR");
    }
}