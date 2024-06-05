package com.portal.exchangerate.service;

import com.portal.exchangerate.api.WebServiceClient;
import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class XmlDataParserTest {

    @Test
    void parseCurrencyListTest() {

        String xmlData = "<CcyTbl xmlns=\"http://www.lb.lt/WebServices/FxRates\">"
                + "<CcyNtry><Ccy>ADP</Ccy><CcyNm lang=\"LT\">Andoros peseta</CcyNm><CcyNm lang=\"EN\">Andorran peseta</CcyNm><CcyNbr>020</CcyNbr><CcyMnrUnts>0</CcyMnrUnts></CcyNtry>"
                + "</CcyTbl>";

        WebServiceClient webServiceClient = new WebServiceClient();
        XmlDataParser xmlDataParser = new XmlDataParser(webServiceClient);

        List<CcyDTO> result = xmlDataParser.parseCurrencyList(xmlData);

        assertNotNull(result);
        assertEquals(1, result.size());

        CcyDTO currency = result.get(0);
        assertEquals("ADP", currency.getCurrencyCode());
        assertEquals("Andoros peseta", currency.getCurrencyName());
        assertEquals(20, currency.getCurrencyNumber());
        assertEquals("0", currency.getMinorUnits());
    }

    @Test
    void parseExchangeRatesTest() {

        String xmlData = "<FxRates xmlns=\"http://www.lb.lt/WebServices/FxRates\">\n" +
                "    <FxRate>\n" +
                "        <Dt>2024-03-03</Dt>\n" +
                "        <Tp>LT</Tp>\n" +
                "        <CcyAmt>\n" +
                "            <Ccy>EUR</Ccy>\n" +
                "            <Amt>1</Amt>\n" +
                "        </CcyAmt>\n" +
                "        <CcyAmt>\n" +
                "            <Ccy>AED</Ccy>\n" +
                "            <Amt>3.970040</Amt>\n" +
                "        </CcyAmt>\n" +
                "    </FxRate>\n" +
                "</FxRates>";

        WebServiceClient webServiceClient = new WebServiceClient();
        XmlDataParser xmlDataParser = new XmlDataParser(webServiceClient);

        List<FxRateDTO> result = xmlDataParser.parseExchangeRates(xmlData);

        assertNotNull(result);
        assertEquals(1, result.size());

        FxRateDTO fxRate = result.get(0);
        assertEquals(LocalDate.parse("2024-03-03"), fxRate.getDate());
        assertEquals("LT", fxRate.getType());
        assertEquals(2, fxRate.getCurrencyAmounts().size());
        assertEquals(BigDecimal.valueOf(1), fxRate.getCurrencyAmounts().get(0).getAmount());
        assertEquals(BigDecimal.valueOf(3.97004).stripTrailingZeros(), fxRate.getCurrencyAmounts().get(1).getAmount().stripTrailingZeros());
    }
}