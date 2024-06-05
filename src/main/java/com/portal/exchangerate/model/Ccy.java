package com.portal.exchangerate.model;

import jakarta.xml.bind.annotation.*;
import org.hibernate.annotations.Index;
import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity
@Table(name = "currency_list", uniqueConstraints = @UniqueConstraint(columnNames = "currency_code"))
@XmlRootElement(name = "CcyNtry")
@XmlAccessorType(XmlAccessType.FIELD)
public class Ccy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @XmlTransient
    private Long id;

    @XmlElement(name = "Ccy")
    @Column(name = "currency_code")
    @Index(name = "idx_currency_code")
    private String currencyCode;

    @XmlElement(name = "CcyNm")
    @Column(name = "currency_name")
    private String currencyName;

    @XmlElement(name = "CcyNbr")
    @Column(name = "currency_number")
    private Integer currencyNumber;

    @XmlElement(name = "CcyMnrUnts")
    @Column(name = "minor_units")
    private String minorUnits;

}