package com.portal.exchangerate.repository;

import com.portal.exchangerate.model.Ccy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRepository extends JpaRepository<Ccy, Long> {

    List<Ccy> findAll();

    Ccy findByCurrencyCode(String currencyCode);

}