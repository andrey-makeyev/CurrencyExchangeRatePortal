package com.portal.exchangerate.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Component
public class WebServiceClient {

    @Value("${fx.rates.webservice.base-url}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    public String getCurrencyList() {
        return restTemplate.getForObject(baseUrl + "/getCurrencyList", String.class);
    }

    public String getCurrentFxRates(String tp) {
        return restTemplate.getForObject(baseUrl + "/getCurrentFxRates?tp={tp}", String.class, tp);
    }

    public String getFxRates(String tp, LocalDate dt) {
        return restTemplate.getForObject(baseUrl + "/getFxRates?tp={tp}&dt={dt}", String.class, tp, dt);
    }

    public String getFxRatesForCurrency(String tp, String ccy, LocalDate dtFrom, LocalDate dtTo) {
        return restTemplate.getForObject(baseUrl + "/getFxRatesForCurrency?tp={tp}&ccy={ccy}&dtFrom={dtFrom}&dtTo={dtTo}", String.class, tp, ccy, dtFrom, dtTo);
    }
}