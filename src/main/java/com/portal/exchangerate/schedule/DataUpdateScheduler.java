package com.portal.exchangerate.schedule;

import com.portal.exchangerate.api.WebServiceClient;
import com.portal.exchangerate.dto.CcyAmtDTO;
import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.model.Ccy;
import com.portal.exchangerate.model.CcyAmt;
import com.portal.exchangerate.model.FxRate;
import com.portal.exchangerate.repository.CurrencyAmountRepository;
import com.portal.exchangerate.repository.CurrencyRepository;
import com.portal.exchangerate.repository.FxRateRepository;
import com.portal.exchangerate.service.XmlDataParser;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataUpdateScheduler.class);

    @Getter
    private LocalDate lastUpdate;
    private final XmlDataParser xmlDataParser;
    private final WebServiceClient webServiceClient;
    private final CurrencyRepository currencyRepository;
    private final FxRateRepository fxRateRepository;
    private final CurrencyAmountRepository currencyAmountRepository;

    @Autowired
    public DataUpdateScheduler(XmlDataParser xmlDataParser, WebServiceClient webServiceClient,
                               CurrencyRepository currencyRepository, FxRateRepository fxRateRepository,
                               CurrencyAmountRepository currencyAmountRepository) {
        this.xmlDataParser = xmlDataParser;
        this.webServiceClient = webServiceClient;
        this.currencyRepository = currencyRepository;
        this.fxRateRepository = fxRateRepository;
        this.currencyAmountRepository = currencyAmountRepository;
    }

    @PostConstruct
    public void initialDataLoad() {
        logger.info("Initial data load started");
        updateData();
        logger.info("Initial data load completed");
    }

    @Scheduled(cron = "0 0 0 * * ?") // Cron schedule every midnight
    public void scheduledDataUpdate() {
        logger.info("Scheduled data update started");
        updateData();
        logger.info("Scheduled data update completed");
    }

    public void updateData() {
        logger.info("Start data update");

        try {
            String currencyXmlData = webServiceClient.getCurrencyList();

            String exchangeRatesXmlDataLT = webServiceClient.getCurrentFxRates(ExchangeRateType.LT.getValue());
            FxRateDTO fxRateDTOLT = xmlDataParser.parseExchangeRates(exchangeRatesXmlDataLT).get(0);
            CcyAmt ccyAmtLT = createCcyAmtObjectBasedOnExchangeRate(fxRateDTOLT);
            updateCurrencyAndExchangeRateAndCurrencyAmount(currencyXmlData, exchangeRatesXmlDataLT, ccyAmtLT);

            String exchangeRatesXmlDataEU = webServiceClient.getCurrentFxRates(ExchangeRateType.EU.getValue());
            FxRateDTO fxRateDTOEU = xmlDataParser.parseExchangeRates(exchangeRatesXmlDataEU).get(0);
            CcyAmt ccyAmtEU = createCcyAmtObjectBasedOnExchangeRate(fxRateDTOEU);
            updateCurrencyAndExchangeRateAndCurrencyAmount(currencyXmlData, exchangeRatesXmlDataEU, ccyAmtEU);

            lastUpdate = LocalDate.now();
        } catch (Exception e) {
            logger.error("Error updating data: {}", e.getMessage());
        }

        logger.info("Completing data update");
    }

    private CcyAmt createCcyAmtObjectBasedOnExchangeRate(FxRateDTO fxRateDTO) {
        if (fxRateDTO.getCurrencyAmounts() != null && !fxRateDTO.getCurrencyAmounts().isEmpty()) {
            CcyAmtDTO ccyAmtDTO = fxRateDTO.getCurrencyAmounts().get(0);

            CcyAmt ccyAmt = new CcyAmt();
            ccyAmt.setAmount(ccyAmtDTO.getAmount());
            return ccyAmt;
        }
        return null;
    }

    private void updateCurrencyAndExchangeRateAndCurrencyAmount(String currencyXmlData, String exchangeRatesXmlData, CcyAmt ccyAmt) {
        List<CcyDTO> currencyList = xmlDataParser.parseCurrencyList(currencyXmlData);
        saveCurrencyList(currencyList);

        List<FxRateDTO> fxRateDTOList = xmlDataParser.parseExchangeRates(exchangeRatesXmlData);
        if (fxRateDTOList != null) {
            List<FxRate> savedFxRates = new ArrayList<>();
            for (FxRateDTO fxRateDTO : fxRateDTOList) {
                if ("N/A".equals(fxRateDTO.getBaseCurrency())) {
                    logger.warn("Skipping saving currency amount due to incorrect currency code");
                    continue;
                }

                CcyDTO ccyDTO = findCcyDTOByCurrencyCode(currencyList, fxRateDTO.getBaseCurrency());
                if (ccyDTO != null) {
                    List<CcyAmtDTO> currencyAmounts = fxRateDTO.getCurrencyAmounts();
                    if (!currencyAmounts.isEmpty()) {
                        CcyAmtDTO ccyAmtDTO = currencyAmounts.get(0);
                    }
                    savedFxRates.addAll(saveExchangeRate(fxRateDTO, ccyDTO, BigDecimal.ONE, ccyAmt));
                }
            }

            for (int i = 0; i < savedFxRates.size(); i++) {
                saveCurrencyAmount(savedFxRates.get(i), fxRateDTOList.get(i));
            }
        }
    }

    private CcyDTO findCcyDTOByCurrencyCode(List<CcyDTO> currencyList, String currencyCode) {
        if (currencyCode == null) {
            return null;
        }

        for (CcyDTO ccyDTO : currencyList) {
            if (currencyCode.equals(ccyDTO.getCurrencyCode())) {
                return ccyDTO;
            }
        }
        return null;
    }

    @Transactional
    protected void saveCurrencyList(List<CcyDTO> currencyList) {
        List<Ccy> currencies = new ArrayList<>();

        for (CcyDTO ccyDTO : currencyList) {
            if ("N/A".equals(ccyDTO.getCurrencyNumber())) {
                logger.warn("Skipping saving currency {} due to incorrect currency number", ccyDTO.getCurrencyCode());
                continue;
            }

            Ccy currency = currencyRepository.findByCurrencyCode(ccyDTO.getCurrencyCode());
            if (currency == null) {
                currency = new Ccy();
                currency.setCurrencyCode(ccyDTO.getCurrencyCode());
            }
            currency.setCurrencyName(ccyDTO.getCurrencyName());
            currency.setCurrencyNumber(ccyDTO.getCurrencyNumber());
            currency.setMinorUnits(ccyDTO.getMinorUnits());
            currencies.add(currency);
        }

        currencyRepository.saveAll(currencies);
        logger.info("Saved or updated {} currencies", currencies.size());
    }

    @Transactional
    protected List<FxRate> saveExchangeRate(FxRateDTO fxRateDTO, CcyDTO ccyDTO, BigDecimal realExchangeRate, CcyAmt ccyAmt) {
        List<FxRate> fxRatesToSave = new ArrayList<>();

        if ("N/A".equals(fxRateDTO.getBaseCurrency())) {
            logger.warn("Skipping saving exchange rate due to incorrect currency code");
            return fxRatesToSave;
        }

        FxRate fxRate = new FxRate();
        String baseCurrencyCode = fxRateDTO.getBaseCurrency();

        if (baseCurrencyCode == null) {
            logger.warn("Currency {} not found in the database, skipping saving exchange rate", baseCurrencyCode);
            return fxRatesToSave;
        }

        Ccy baseCurrencyEntity = currencyRepository.findByCurrencyCode(baseCurrencyCode);

        fxRate.setFxRateDate(fxRateDTO.getDate());
        fxRate.setFxRateType(fxRateDTO.getType());
        fxRate.setBaseCurrency(baseCurrencyEntity);

        fxRate.calculateExchangeRate(realExchangeRate, ccyAmt);

        fxRate.setFxRate(fxRateDTO.getRate());
        fxRatesToSave.add(fxRate);

        return fxRateRepository.saveAll(fxRatesToSave);
    }

    @Transactional
    protected void saveCurrencyAmount(FxRate fxRate, FxRateDTO fxRateDTO) {
        if (fxRateDTO.getCurrencyAmounts() != null && !fxRateDTO.getCurrencyAmounts().isEmpty()) {
            List<CcyAmt> currencyAmounts = new ArrayList<>();
            for (CcyAmtDTO ccyAmtDTO : fxRateDTO.getCurrencyAmounts()) {
                CcyAmt currencyAmount = new CcyAmt();
                currencyAmount.setFxRate(fxRate);
                String currencyCode = ccyAmtDTO.getTargetCurrency();

                Ccy targetCurrencyEntity = currencyRepository.findByCurrencyCode(currencyCode);

                currencyAmount.setTargetCurrency(targetCurrencyEntity);
                currencyAmount.setAmount(ccyAmtDTO.getAmount());

                currencyAmounts.add(currencyAmount);
            }
            currencyAmountRepository.saveAll(currencyAmounts);
            logger.info("Saved {} currency amounts for currency: {}", currencyAmounts.size(), fxRate.getFxRateType());
        }
    }

}