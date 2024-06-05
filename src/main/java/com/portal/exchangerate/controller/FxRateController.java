package com.portal.exchangerate.controller;

import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.service.FxRateService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NonUniqueResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fx-rate")
public class FxRateController {

    private static final Logger logger = LoggerFactory.getLogger(FxRateController.class);

    private final FxRateService fxRateService;

    @Autowired
    public FxRateController(FxRateService fxRateService) {
        this.fxRateService = fxRateService;
    }

    @GetMapping(value = "/currency-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<CcyDTO>>> getCurrencyList() {

        List<CcyDTO> currencyList = fxRateService.getCurrencies();
        if (currencyList == null || currencyList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<EntityModel<CcyDTO>> currencyModels = currencyList.stream()
                .map(currency -> EntityModel.of(currency,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FxRateController.class).getCurrencyList()).withSelfRel()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(CollectionModel.of(currencyModels), HttpStatus.OK);
    }

    @GetMapping(value = "/available-currency-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<CcyDTO>>> getAvailableCurrencyList() {
        List<CcyDTO> availableCurrencyList = fxRateService.getAvailableCurrencies();
        if (availableCurrencyList == null || availableCurrencyList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<EntityModel<CcyDTO>> currencyModels = availableCurrencyList.stream()
                .map(currency -> EntityModel.of(currency,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FxRateController.class).getAvailableCurrencyList()).withSelfRel()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(CollectionModel.of(currencyModels), HttpStatus.OK);
    }

    @GetMapping(value = "/current-exchange-rates/{exchangeRateType}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<FxRateDTO>>> getCurrentExchangeRates(@PathVariable ExchangeRateType exchangeRateType) {
        if (exchangeRateType != ExchangeRateType.EU && exchangeRateType != ExchangeRateType.LT) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            List<FxRateDTO> fxRateDTOList = fxRateService.getCurrentFxRates(exchangeRateType, null, null);
            List<EntityModel<FxRateDTO>> fxRateModels = fxRateDTOList.stream()
                    .map(fxRateDTO -> {
                        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FxRateController.class)
                                .getCurrentExchangeRates(exchangeRateType)).withSelfRel();
                        return EntityModel.of(fxRateDTO, selfLink);
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<>(CollectionModel.of(fxRateModels), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/exchange-rates/{exchangeRateType}/{date}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<FxRateDTO>>> getExchangeRates(@PathVariable ExchangeRateType exchangeRateType,
                                                                                    @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        if (exchangeRateType != ExchangeRateType.EU && exchangeRateType != ExchangeRateType.LT) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            List<FxRateDTO> fxRateDTOList = fxRateService.getFxRates(exchangeRateType, null, null, date.toString(), date.toString());
            List<EntityModel<FxRateDTO>> fxRateModels = fxRateDTOList.stream()
                    .map(fxRateDTO -> {
                        Link selfLink = WebMvcLinkBuilder.linkTo(FxRateController.class, exchangeRateType, date).withSelfRel();
                        return EntityModel.of(fxRateDTO, selfLink);
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<>(CollectionModel.of(fxRateModels), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/exchange-rates/{exchangeRateType}/{currency}/{startDate}/{endDate}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CollectionModel<EntityModel<FxRateDTO>>> getExchangeRatesForCurrency(@PathVariable ExchangeRateType exchangeRateType,
                                                                                               @PathVariable String currency,
                                                                                               @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                                                               @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        if (exchangeRateType != ExchangeRateType.EU && exchangeRateType != ExchangeRateType.LT) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            List<FxRateDTO> fxRateDTOList = fxRateService.getFxRates(exchangeRateType, null, currency, startDate.toString(), endDate.toString());
            List<EntityModel<FxRateDTO>> fxRateModels = fxRateDTOList.stream()
                    .map(fxRateDTO -> EntityModel.of(fxRateDTO,
                            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(FxRateController.class).getExchangeRatesForCurrency(exchangeRateType, currency, startDate, endDate)).withSelfRel()))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(CollectionModel.of(fxRateModels), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/cross-rate/{fromCurrency}/{toCurrency}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BigDecimal> getCrossRate(@PathVariable String fromCurrency, @PathVariable String toCurrency) {
        try {
            BigDecimal crossRate = fxRateService.calculateCrossRate(fromCurrency, toCurrency);
            if (crossRate == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(crossRate, HttpStatus.OK);
        } catch (NonUniqueResultException e) {
            logger.error("Multiple rates found for given parameters: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (EntityNotFoundException e) {
            logger.error("Error calculating cross rate: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Error calculating cross rate: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}