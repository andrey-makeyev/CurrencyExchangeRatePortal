package com.portal.exchangerate.enums;

public enum ExchangeRateType {
    LT("LT"),
    EU("EU");

    private final String value;

    ExchangeRateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}