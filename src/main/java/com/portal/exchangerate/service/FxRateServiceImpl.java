package com.portal.exchangerate.service;

import com.portal.exchangerate.api.WebServiceClient;
import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.model.Ccy;
import com.portal.exchangerate.model.FxRate;
import com.portal.exchangerate.repository.CurrencyRepository;
import com.portal.exchangerate.repository.FxRateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FxRateServiceImpl implements FxRateService {

    private static final Logger logger = LoggerFactory.getLogger(FxRateServiceImpl.class);

    private final XmlDataParser xmlDataParser;
    private final CurrencyRepository currencyRepository;
    private final WebServiceClient webServiceClient;
    private final FxRateRepository fxRateRepository;

    @Autowired
    public FxRateServiceImpl(XmlDataParser xmlDataParser, CurrencyRepository currencyRepository,
                             WebServiceClient webServiceClient, FxRateRepository fxRateRepository) {
        this.xmlDataParser = xmlDataParser;
        this.currencyRepository = currencyRepository;
        this.webServiceClient = webServiceClient;
        this.fxRateRepository = fxRateRepository;
    }

    @Override
    public void saveCurrencyDataFromXml() {
        String xmlData = webServiceClient.getCurrencyList();
        List<CcyDTO> ccyDTOList = xmlDataParser.parseCurrencyList(xmlData);
        List<Ccy> currencyEntities = ccyDTOList.stream()
                .map(this::convertCcyDTOToCurrencyEntity)
                .collect(Collectors.toList());

        currencyRepository.saveAll(currencyEntities);

        for (Ccy ccy : currencyEntities) {
            List<FxRate> existingRecords = fxRateRepository.findByFxRateDateAndFxRateType(
                    LocalDate.now(), ccy.getCurrencyCode());
            if (existingRecords.isEmpty()) {
                logger.info("Currency {} saved.", ccy.getCurrencyCode());
            } else {
                logger.info("Currency {} already exists, skipping saving.", ccy.getCurrencyCode());
            }
        }
    }

    private Ccy convertCcyDTOToCurrencyEntity(CcyDTO ccyDTO) {
        Ccy ccy = new Ccy();
        ccy.setCurrencyCode(ccyDTO.getCurrencyCode());
        ccy.setCurrencyName(ccyDTO.getCurrencyName());
        if (ccyDTO.getCurrencyNumber() != null) {
            ccy.setCurrencyNumber(Integer.parseInt(ccyDTO.getCurrencyNumber().toString()));
        }
        ccy.setMinorUnits(ccyDTO.getMinorUnits());
        return ccy;
    }

    @Override
    public List<CcyDTO> getCurrencies() {
        List<Ccy> ccyList = currencyRepository.findAll();
        return ccyList.stream()
                .map(this::convertCurrencyEntityToCcyDTO)
                .collect(Collectors.toList());
    }

    public List<CcyDTO> getAvailableCurrencies() {
        Set<String> availableCurrencyCodes = fxRateRepository.findAll().stream()
                .flatMap(fxRate -> fxRate.getCurrencyAmounts().stream())
                .map(ccyAmt -> ccyAmt.getTargetCurrency().getCurrencyCode())
                .collect(Collectors.toSet());

        return currencyRepository.findAll().stream()
                .filter(ccy -> availableCurrencyCodes.contains(ccy.getCurrencyCode()))
                .map(ccy -> new CcyDTO(ccy.getCurrencyCode(), ccy.getCurrencyName(), ccy.getCurrencyNumber(), ccy.getMinorUnits()))
                .collect(Collectors.toList());
    }

    private CcyDTO convertCurrencyEntityToCcyDTO(Ccy ccy) {
        CcyDTO ccyDTO = new CcyDTO();
        ccyDTO.setCurrencyCode(ccy.getCurrencyCode());
        ccyDTO.setCurrencyName(ccy.getCurrencyName());

        Integer currencyNumber = ccy.getCurrencyNumber();
        if (currencyNumber != null) {
            ccyDTO.setCurrencyNumber(Integer.valueOf(currencyNumber.toString()));
        } else {
            ccyDTO.setCurrencyNumber(null);
        }

        ccyDTO.setMinorUnits(ccy.getMinorUnits());
        return ccyDTO;
    }

    @Override
    public List<FxRateDTO> getCurrentFxRates(ExchangeRateType exchangeRateType, String rate, String currency) {
        try {
            String fxRatesData = webServiceClient.getFxRatesForCurrency(exchangeRateType.toString(), currency, null, null);
            List<FxRateDTO> fxRateDTOList = xmlDataParser.parseExchangeRates(fxRatesData);
            return fxRateDTOList;
        } catch (Exception e) {
            logger.error("Error while fetching current exchange rates: {}", e.getMessage());
            throw new RuntimeException("Error while fetching current exchange rates", e);
        }
    }

    @Override
    public List<FxRateDTO> getFxRates(ExchangeRateType exchangeRateType, String rate, String currency, String startDate, String endDate) {
        try {
            LocalDate startDateObj = LocalDate.parse(startDate);
            LocalDate endDateObj = LocalDate.parse(endDate);

            String fxRatesData = webServiceClient.getFxRatesForCurrency(exchangeRateType.toString(), currency, startDateObj, endDateObj);
            List<FxRateDTO> fxRateDTOList = xmlDataParser.parseExchangeRates(fxRatesData);
            return fxRateDTOList;
        } catch (Exception e) {
            logger.error("Error while fetching exchange rates: {}", e.getMessage());
            throw new RuntimeException("Error while fetching exchange rates", e);
        }
    }

    @Override
    public List<CcyDTO> getAllCurrencies() {
        List<Ccy> currencies = currencyRepository.findAll();
        return currencies.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    private CcyDTO convertToDto(Ccy currency) {
        CcyDTO dto = new CcyDTO();
        dto.setCurrencyCode(currency.getCurrencyCode());
        dto.setCurrencyName(currency.getCurrencyName());
        return dto;
    }

    @Override
    public BigDecimal getRateForCurrency(String targetCurrencyCode) {
        try {
            String fxRatesData = webServiceClient.getFxRatesForCurrency(String.valueOf(ExchangeRateType.LT), targetCurrencyCode, null, null);
            List<FxRateDTO> fxRateDTOList = xmlDataParser.parseExchangeRates(fxRatesData);

            if (!fxRateDTOList.isEmpty()) {
                FxRateDTO fxRateDTO = fxRateDTOList.get(0);
                return fxRateDTO.getRate();
            } else {
                throw new EntityNotFoundException("FxRate not found for target currency " + targetCurrencyCode);
            }
        } catch (Exception e) {
            logger.error("Error while fetching exchange rate for currency {}: {}", targetCurrencyCode, e.getMessage());
            throw new RuntimeException("Error while fetching exchange rate", e);
        }
    }

    @Override
    public BigDecimal getFxRate(LocalDate fxRateDate, String fxRateType, Ccy baseCurrency) {
        Optional<Ccy> baseCurrencyOptional = Optional.ofNullable(currencyRepository.findByCurrencyCode(String.valueOf(baseCurrency)));

        if (baseCurrencyOptional.isPresent()) {
            Ccy baseCurrencyObj = baseCurrencyOptional.get();
            Optional<FxRate> fxRateOptional = fxRateRepository.findByFxRateTypeAndBaseCurrencyOrderByFxRateDateDesc(ExchangeRateType.valueOf(fxRateType), baseCurrencyObj);

            if (fxRateOptional.isPresent()) {
                return fxRateOptional.get().getFxRate();
            } else {
                throw new EntityNotFoundException("FxRate not found for type " + fxRateType + " and base currency " + baseCurrency);
            }
        } else {
            throw new EntityNotFoundException("Currency " + baseCurrency + " not found");
        }
    }

    public BigDecimal calculateCrossRate(String fromCurrency, String toCurrency) {
        if ("EUR".equals(fromCurrency)) {
            return getRateForCurrency(toCurrency);
        } else {
            BigDecimal baseToEurRate = getRateForCurrency(fromCurrency);
            BigDecimal eurToTargetRate = getRateForCurrency(toCurrency);

            if (baseToEurRate != null && eurToTargetRate != null) {
                return eurToTargetRate.divide(baseToEurRate, MathContext.DECIMAL128);
            } else {
                return null;
            }
        }
    }
}