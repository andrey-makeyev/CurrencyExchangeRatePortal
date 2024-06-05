package com.portal.exchangerate.service;

import com.portal.exchangerate.api.WebServiceClient;
import com.portal.exchangerate.dto.CcyAmtDTO;
import com.portal.exchangerate.dto.CcyDTO;
import com.portal.exchangerate.dto.FxRateDTO;
import com.portal.exchangerate.schedule.DataUpdateScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlDataParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlDataParser.class);

    private static final String NAMESPACE_URI = "http://www.lb.lt/WebServices/FxRates";

    private final WebServiceClient webServiceClient;

    @Autowired
    public XmlDataParser(WebServiceClient webServiceClient) {
        this.webServiceClient = webServiceClient;
    }

    public List<CcyDTO> parseCurrencyList(String xmlData) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            List<CcyDTO> currencyList = new ArrayList<>();

            NodeList nodeList = doc.getElementsByTagNameNS(NAMESPACE_URI, "CcyNtry");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String currencyCode = getContentByTagName(element, NAMESPACE_URI, "Ccy");
                    String currencyName = getContentByTagName(element, NAMESPACE_URI, "CcyNm");
                    String currencyNumberStr = getContentByTagName(element, NAMESPACE_URI, "CcyNbr");
                    String minorUnits = getContentByTagName(element, NAMESPACE_URI, "CcyMnrUnts");

                    if (!"N/A".equals(currencyNumberStr)) {
                        int currencyNumber = Integer.parseInt(currencyNumberStr);

                        CcyDTO ccyDTO = new CcyDTO();
                        ccyDTO.setCurrencyCode(currencyCode);
                        ccyDTO.setCurrencyName(currencyName);
                        ccyDTO.setCurrencyNumber(currencyNumber);
                        ccyDTO.setMinorUnits(minorUnits);
                        currencyList.add(ccyDTO);
                    } else {
                        logger.info("Currency number is N/A for currency: {}", currencyCode);
                    }
                }
            }

            return currencyList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<FxRateDTO> parseExchangeRates(String xmlData) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();

            List<FxRateDTO> fxRateDTOList = new ArrayList<>();
            NodeList nodeList = doc.getElementsByTagNameNS(NAMESPACE_URI, "FxRate");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String date = getContentByTagName(element, NAMESPACE_URI, "Dt");
                    String type = getContentByTagName(element, NAMESPACE_URI, "Tp");

                    NodeList ccyAmtNodes = element.getElementsByTagNameNS(NAMESPACE_URI, "CcyAmt");
                    List<CcyAmtDTO> currencyAmounts = new ArrayList<>();
                    BigDecimal rate = BigDecimal.ZERO;

                    for (int j = 0; j < ccyAmtNodes.getLength(); j++) {
                        Element ccyAmtElement = (Element) ccyAmtNodes.item(j);
                        String currency = getContentByTagName(ccyAmtElement, NAMESPACE_URI, "Ccy");
                        String amountStr = getContentByTagName(ccyAmtElement, NAMESPACE_URI, "Amt");

                        if (currency != null && amountStr != null) {
                            BigDecimal amount = new BigDecimal(amountStr);

                            CcyAmtDTO ccyAmtDTO = new CcyAmtDTO();
                            ccyAmtDTO.setAmount(amount);
                            ccyAmtDTO.setTargetCurrency(currency);

                            if (!currency.equals("EUR")) {
                                rate = amount;
                            }

                            currencyAmounts.add(ccyAmtDTO);
                        } else {
                            System.out.println("Currency or amount is null");
                        }
                    }

                    FxRateDTO fxRateDTO = new FxRateDTO();
                    fxRateDTO.setType(type);
                    fxRateDTO.setDate(LocalDate.parse(date));
                    fxRateDTO.setBaseCurrency("EUR");
                    fxRateDTO.setCurrencyAmounts(currencyAmounts);
                    fxRateDTO.setRate(rate);

                    fxRateDTOList.add(fxRateDTO);
                }
            }

            return fxRateDTOList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getContentByTagName(Element element, String namespaceURI, String tagName) {
        NodeList nodeList = element.getElementsByTagNameNS(namespaceURI, tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }
}