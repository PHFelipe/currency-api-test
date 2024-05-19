package br.com.ada.currencyapi.domain;


import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CurrencyQuote(
        String code,
        String codein,
        String name,
        BigDecimal high,
        BigDecimal low,
        String varBid,
        String pctChange,
        String bid,
        String ask,
        String timestamp,
        String create_date
) {}
