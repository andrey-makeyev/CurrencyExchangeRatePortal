package com.portal.exchangerate.repository;

import com.portal.exchangerate.enums.ExchangeRateType;
import com.portal.exchangerate.model.Ccy;
import com.portal.exchangerate.model.FxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FxRateRepository extends JpaRepository<FxRate, Long> {

    List<FxRate> findAll();

    List<FxRate> findByFxRateDateAndFxRateType(LocalDate fxRateDate, String fxRateType);

    Optional<FxRate> findByFxRateTypeAndBaseCurrencyOrderByFxRateDateDesc(ExchangeRateType fxRateType, Ccy baseCurrency);

}