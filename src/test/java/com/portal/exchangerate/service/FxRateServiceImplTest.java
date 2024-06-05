package com.portal.exchangerate.service;

import com.portal.exchangerate.api.WebServiceClient;
import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.model.Ccy;
import com.portal.exchangerate.model.CcyAmt;
import com.portal.exchangerate.model.FxRate;
import com.portal.exchangerate.repository.CurrencyRepository;
import com.portal.exchangerate.repository.FxRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FxRateServiceImplTest {

    @Mock
    private WebServiceClient webServiceClient;

    @Mock
    private XmlDataParser xmlDataParser;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private FxRateRepository fxRateRepository;

    @InjectMocks
    private FxRateServiceImpl fxRateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private List<FxRate> createMockFxRates() {
        List<FxRate> fxRates = new ArrayList<>();
        FxRate fxRate1 = new FxRate();
        fxRate1.setFxRateDate(LocalDate.now());
        fxRate1.setFxRateType("Type");

        fxRate1.setCurrencyAmounts(new ArrayList<>());

        List<Ccy> currencies = createMockCurrencies();

        CcyAmt ccyAmt1 = new CcyAmt();
        ccyAmt1.setTargetCurrency(currencies.get(0)); // USD
        ccyAmt1.setAmount(BigDecimal.ONE);
        fxRate1.getCurrencyAmounts().add(ccyAmt1);

        CcyAmt ccyAmt2 = new CcyAmt();
        ccyAmt2.setTargetCurrency(currencies.get(1)); // EUR
        ccyAmt2.setAmount(BigDecimal.ONE);
        fxRate1.getCurrencyAmounts().add(ccyAmt2);

        CcyAmt ccyAmt3 = new CcyAmt();
        ccyAmt3.setTargetCurrency(currencies.get(2)); // GBP
        ccyAmt3.setAmount(BigDecimal.ONE);
        fxRate1.getCurrencyAmounts().add(ccyAmt3);

        fxRates.add(fxRate1);
        return fxRates;
    }

    private List<Ccy> createMockCurrencies() {
        List<Ccy> currencies = new ArrayList<>();
        currencies.add(new Ccy());
        currencies.get(0).setCurrencyCode("USD");
        currencies.get(0).setCurrencyName("JAV doleris");
        currencies.get(0).setCurrencyNumber(840);
        currencies.get(0).setMinorUnits("2");

        currencies.add(new Ccy());
        currencies.get(1).setCurrencyCode("EUR");
        currencies.get(1).setCurrencyName("Euras");
        currencies.get(1).setCurrencyNumber(978);
        currencies.get(1).setMinorUnits("2");

        currencies.add(new Ccy());
        currencies.get(2).setCurrencyCode("GBP");
        currencies.get(2).setCurrencyName("Didžiosios Britanijos svaras sterlingų");
        currencies.get(2).setCurrencyNumber(826);
        currencies.get(2).setMinorUnits("2");

        return currencies;
    }

    @Test
    void getAvailableCurrenciesTest() {

        List<CcyDTO> expectedCurrencies = Arrays.asList(
                new CcyDTO("USD", "JAV doleris", 840, "2"),
                new CcyDTO("EUR", "Euras", 978, "2"),
                new CcyDTO("GBP", "Didžiosios Britanijos svaras sterlingų", 826, "2")
        );

        when(fxRateRepository.findAll()).thenReturn(createMockFxRates());
        when(currencyRepository.findAll()).thenReturn(createMockCurrencies());

        List<CcyDTO> result = fxRateService.getAvailableCurrencies();

        assertFalse(result.isEmpty());
        assertEquals(expectedCurrencies.size(), result.size());

        for (CcyDTO expectedCurrency : expectedCurrencies) {
            boolean containsExpectedCurrency = result.stream()
                    .anyMatch(currency ->
                            currency.getCurrencyCode().equals(expectedCurrency.getCurrencyCode()) &&
                                    currency.getCurrencyName().equals(expectedCurrency.getCurrencyName()) &&
                                    Objects.equals(currency.getCurrencyNumber(), expectedCurrency.getCurrencyNumber()) &&
                                    Objects.equals(currency.getMinorUnits(), expectedCurrency.getMinorUnits())
                    );
            assertTrue(containsExpectedCurrency);
        }
    }

    @Test
    void saveCurrencyDataFromXmlTest() {
        String mockedXmlData = "<CcyTbl xmlns=\"http://www.lb.lt/WebServices/FxRates\">...</CcyTbl>";
        List<CcyDTO> ccyDTOList = Collections.singletonList(new CcyDTO());

        when(webServiceClient.getCurrencyList()).thenReturn(mockedXmlData);
        when(xmlDataParser.parseCurrencyList(mockedXmlData)).thenReturn(ccyDTOList);
        when(currencyRepository.saveAll(anyIterable())).thenReturn(Collections.singletonList(new Ccy())); // Fixed

        fxRateService.saveCurrencyDataFromXml();

        verify(currencyRepository, times(1)).saveAll(anyIterable());
    }

    @Test
    void getCurrenciesTest() {

        List<Ccy> currencies = Collections.singletonList(new Ccy());
        when(currencyRepository.findAll()).thenReturn(currencies);

        List<CcyDTO> result = fxRateService.getCurrencies();

        assertFalse(result.isEmpty());
        assertEquals(currencies.size(), result.size());
    }


    @Test
    void getCurrentFxRatesTest() {

        ExchangeRateType exchangeRateType = ExchangeRateType.LT;
        String currency = "EUR";
        String fxRatesData = "<FxRates xmlns=\"http://www.lb.lt/WebServices/FxRates\">...</FxRates>";
        List<FxRateDTO> fxRateDTOList = Collections.singletonList(new FxRateDTO());

        when(webServiceClient.getFxRatesForCurrency(anyString(), eq(currency), any(), any())).thenReturn(fxRatesData);
        when(xmlDataParser.parseExchangeRates(fxRatesData)).thenReturn(fxRateDTOList);

        List<FxRateDTO> result = fxRateService.getCurrentFxRates(exchangeRateType, null, currency);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(fxRateDTOList.size(), result.size());
    }

    @Test
    void getFxRatesTest() {

        ExchangeRateType exchangeRateType = ExchangeRateType.LT;
        String currency = "EUR";
        String startDate = "2024-01-01";
        String endDate = "2024-01-31";
        String fxRatesData = "<FxRates xmlns=\"http://www.lb.lt/WebServices/FxRates\">...</FxRates>";
        List<FxRateDTO> fxRateDTOList = Collections.singletonList(new FxRateDTO());

        when(webServiceClient.getFxRatesForCurrency(anyString(), eq(currency), any(), any())).thenReturn(fxRatesData);
        when(xmlDataParser.parseExchangeRates(fxRatesData)).thenReturn(fxRateDTOList);

        List<FxRateDTO> result = fxRateService.getFxRates(exchangeRateType, null, currency, startDate, endDate);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(fxRateDTOList.size(), result.size());
    }

    @Test
    void getAllCurrenciesTest() {

        List<Ccy> currencies = Collections.singletonList(new Ccy());
        when(currencyRepository.findAll()).thenReturn(currencies);

        List<CcyDTO> result = fxRateService.getAllCurrencies();

        assertFalse(result.isEmpty());
        assertEquals(currencies.size(), result.size());
    }

}