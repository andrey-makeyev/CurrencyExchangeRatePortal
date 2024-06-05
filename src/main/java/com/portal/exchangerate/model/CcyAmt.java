package com.portal.exchangerate.model;

import lombok.Data;

import jakarta.persistence.*;
import jakarta.xml.bind.annotation.*;

import java.math.BigDecimal;

@Data
@Entity
@XmlRootElement(name = "CcyAmt")
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "currency_amount")
public class CcyAmt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
    private Long id;

    @XmlElement(name = "Amt")
    @Column(name = "amount")
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "target_currency", referencedColumnName = "currency_code")
    private Ccy targetCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fx_rate_id", referencedColumnName = "id")
    private FxRate fxRate;

}