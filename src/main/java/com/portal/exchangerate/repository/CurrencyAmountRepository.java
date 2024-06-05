package com.portal.exchangerate.repository;

import com.portal.exchangerate.model.CcyAmt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyAmountRepository extends JpaRepository<CcyAmt, Long> {

}