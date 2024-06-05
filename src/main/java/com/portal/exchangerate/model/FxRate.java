package com.portal.exchangerate.model;

import lombok.Data;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@XmlRootElement(name = "FxRate")
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "fx_rates")
public class FxRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
    private Long id;

    @XmlElement(name = "FxRate")
    @Column(name = "fx_rate")
    private BigDecimal fxRate;

    @XmlElement(name = "Dt")
    @Column(name = "fx_rate_date")
    private LocalDate fxRateDate;

    @XmlElement(name = "Tp")
    @Column(name = "fx_rate_type")
    private String fxRateType;

    @XmlElement(name = "CcyAmt")
    @OneToMany(mappedBy = "fxRate", cascade = CascadeType.ALL)
    private List<CcyAmt> currencyAmounts;

    @ManyToOne
    @JoinColumn(name = "base_currency", referencedColumnName = "currency_code")
    private Ccy baseCurrency;

    @Transient
    private String transientExchangeRateType;

    @Transient
    public void calculateExchangeRate(BigDecimal realExchangeRate, CcyAmt ccyAmt) {
        if (baseCurrency == null || ccyAmt == null) {
            throw new IllegalStateException("Base currency or target currency is not set");
        }

        BigDecimal targetAmount = ccyAmt.getAmount();
        BigDecimal calculatedRate = realExchangeRate.multiply(targetAmount);
        fxRate = calculatedRate;
    }
}