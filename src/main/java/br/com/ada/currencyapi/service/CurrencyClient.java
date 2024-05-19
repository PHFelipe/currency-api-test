package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.CurrencyQuote;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name= "currency-client", url = "https://economia.awesomeapi.com.br/json")
public interface CurrencyClient {
    @GetMapping("/last/{coin}")
    Map<String, CurrencyQuote> getCurrencyQuote(@PathVariable("coin")String code);
}
