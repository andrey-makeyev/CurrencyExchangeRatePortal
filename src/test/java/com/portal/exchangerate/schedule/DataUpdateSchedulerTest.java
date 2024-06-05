package com.portal.exchangerate.schedule;

import com.portal.exchangerate.api.WebServiceClient;
import com.portal.exchangerate.dto.CcyAmtDTO;
import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.service.XmlDataParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataUpdateSchedulerTest {

    @Mock
    private XmlDataParser xmlDataParser;

    @Mock
    private WebServiceClient webServiceClient;

    @InjectMocks
    private DataUpdateScheduler dataUpdateScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void scheduledDataUpdateTest() {

        DataUpdateScheduler dataUpdateScheduler = Mockito.mock(DataUpdateScheduler.class);
        doCallRealMethod().when(dataUpdateScheduler).scheduledDataUpdate();

        dataUpdateScheduler.scheduledDataUpdate();

        verify(dataUpdateScheduler).scheduledDataUpdate();
    }

    @Test
    void initialDataLoadTest() {

        DataUpdateScheduler dataUpdateScheduler = Mockito.mock(DataUpdateScheduler.class);
        doCallRealMethod().when(dataUpdateScheduler).initialDataLoad();

        dataUpdateScheduler.initialDataLoad();

        verify(dataUpdateScheduler).initialDataLoad();
    }

    @Test
    void updateDataSuccessTest() {

        when(webServiceClient.getCurrencyList()).thenReturn(getSampleCurrencyXmlData());
        when(webServiceClient.getCurrentFxRates(anyString())).thenReturn(getSampleFxRatesXmlData());

        when(xmlDataParser.parseCurrencyList(any())).thenReturn(getSampleCurrencyList());
        when(xmlDataParser.parseExchangeRates(any())).thenReturn(getSampleFxRateList());

        assertDoesNotThrow(() -> dataUpdateScheduler.updateData());
    }

    @Test
    void updateDataExceptionTest() {

        when(webServiceClient.getCurrencyList()).thenThrow(new RuntimeException("WebServiceClient exception"));

        assertDoesNotThrow(() -> dataUpdateScheduler.updateData());
    }

    private String getSampleCurrencyXmlData() {

        return "<Ccy>...</Ccy>";
    }

    private String getSampleFxRatesXmlData() {

        return "<FxRates>...</FxRates>";
    }

    private List<CcyDTO> getSampleCurrencyList() {

        List<CcyDTO> currencyList = new ArrayList<>();
        currencyList.add(new CcyDTO("USD", "JAV doleris", 840, "2"));

        return currencyList;
    }

    private List<FxRateDTO> getSampleFxRateList() {

        List<FxRateDTO> fxRateList = new ArrayList<>();
        FxRateDTO fxRateDTO = new FxRateDTO();
        fxRateDTO.setBaseCurrency("EUR");
        fxRateDTO.setDate(LocalDate.now());
        fxRateDTO.setType("LT");
        fxRateDTO.setRate(BigDecimal.valueOf(1.0));
        List<CcyAmtDTO> currencyAmounts = new ArrayList<>();

        CcyAmtDTO ccyAmtDTO = new CcyAmtDTO();
        ccyAmtDTO.setTargetCurrency("USD");
        ccyAmtDTO.setAmount(BigDecimal.valueOf(1.18));

        currencyAmounts.add(ccyAmtDTO);
        fxRateDTO.setCurrencyAmounts(currencyAmounts);
        fxRateList.add(fxRateDTO);

        return fxRateList;
    }
}
