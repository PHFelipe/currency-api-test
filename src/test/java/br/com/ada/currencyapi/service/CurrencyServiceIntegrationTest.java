package br.com.ada.currencyapi.service;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@WebAppConfiguration
public class CurrencyServiceIntegrationTest {
    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CurrencyRepository currencyRepository;


    @BeforeEach
     public void setUp(){
        List<Currency> coinsOfTest = new ArrayList<>();
        coinsOfTest.add(Currency.builder().id(1L).name("USD").exchanges(Map.of("EUR", BigDecimal.valueOf(2.0))).build());
        coinsOfTest.add(Currency.builder().id(2L).name("EUR").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(3L).name("JPY").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(4L).name("BRL").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(5L).name("CNY").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(6L).name("RUB").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(7L).name("MXN").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(8L).name("INR").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(9L).name("KRW").exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder().id(10L).name("AUD").exchanges(new HashMap<>()).build());
        currencyRepository.saveAll(coinsOfTest);
    }

    @Test
    void get() {
        List<CurrencyResponse> coins = currencyService.get();

        Assertions.assertThat(coins).isNotNull();
        Assertions.assertThat(coins.size()).isEqualTo(10);
        assertThat(coins.get(0).getLabel()).isEqualTo("1 - USD");
        assertThat(coins.get(1).getLabel()).isEqualTo("2 - EUR");
        assertThat(coins.get(2).getLabel()).isEqualTo("3 - JPY");
        assertThat(coins.get(3).getLabel()).isEqualTo("4 - BRL");
        assertThat(coins.get(4).getLabel()).isEqualTo("5 - CNY");
        assertThat(coins.get(5).getLabel()).isEqualTo("6 - RUB");
        assertThat(coins.get(6).getLabel()).isEqualTo("7 - MXN");
        assertThat(coins.get(7).getLabel()).isEqualTo("8 - INR");
        assertThat(coins.get(8).getLabel()).isEqualTo("9 - KRW");
        assertThat(coins.get(9).getLabel()).isEqualTo("10 - AUD");
    }

    @Test
    void create(){

        CurrencyRequest request = new CurrencyRequest();
        request.setName("R$");
        request.setDescription("Real coin of Brazil");
        request.setExchanges(new HashMap<>());

        Long responseID = currencyService.create(request);

        List<Currency> coins = currencyRepository.findAll();

        Assertions.assertThat(coins.size()).isEqualTo(11);
        Assertions.assertThat(responseID).isEqualTo(coins.get(10).getId());
        Assertions.assertThat(coins.get(10).getName()).isEqualTo("R$");
        Assertions.assertThat(coins.get(10).getDescription()).isEqualTo("Real coin of Brazil");

    }

    @Test
    void createNull(){

        CurrencyRequest request = new CurrencyRequest();
        request.setName(null);
        request.setExchanges(null);

        assertThatThrownBy(()-> currencyService.create(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin name cannot be null");
    }

    @Test
    void createAlreadyExists() {

        CurrencyRequest request = new CurrencyRequest();
        request.setName("USD");
        request.setExchanges(new HashMap<>());

        assertThatThrownBy(()-> currencyService.create(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin already exists");

    }

    @Test
    void delete(){
        currencyService.delete(1L);

        List<Currency> coins = currencyRepository.findAll();
        Assertions.assertThat(coins.get(0).getName()).isEqualTo("EUR");
        Assertions.assertThat(coins.size()).isEqualTo(9);
    }

    @Test
    void deleteNotFound(){
        assertThatThrownBy(()-> currencyService.delete(11L))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Coin not found");

        Assertions.assertThat(currencyRepository.count()).isEqualTo(10);
    }

    @Test
    void convert() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("EUR");
        request.setFrom("USD");
        request.setAmount(BigDecimal.ONE);

        ConvertCurrencyResponse response = currencyService.convert(request);

        Assertions.assertThat(response.getAmount().toString()).isEqualTo("2.00");
    }

    @Test
    void convertExchangeNotFound() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("EUR");
        request.setFrom("RUB");
        request.setAmount(BigDecimal.ONE);

        assertThatThrownBy(()-> currencyService.convert(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Exchange EUR not found for RUB");
    }

    @Test
    void convertWithAPI(){
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("EUR");
        request.setFrom("USD");
        request.setAmount(BigDecimal.ONE);

        ConvertCurrencyResponse response = currencyService.convertAPI(request);

        assertThat(response).isNotNull();
    }

    @Test
    void convertWithAPICoinNotFound() {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("USD");
        request.setFrom("ETH");
        request.setAmount(BigDecimal.ONE);

        assertThatThrownBy(() -> currencyService.convertAPI(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Coin not found: ETH");
    }

    @Test
    void convertWithAPIExchangeNotFound () {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("ETH");
        request.setFrom("USD");
        request.setAmount(BigDecimal.ONE);

        assertThatThrownBy(() -> currencyService.convertAPI(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Exchange ETH not found for USD");
    }


}
