package br.com.ada.currencyapi.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.ada.currencyapi.domain.*;
import br.com.ada.currencyapi.domain.Currency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceUnitTest {

    @InjectMocks
    private CurrencyService currencyService;

    @Mock
    private CurrencyClient currencyClient;

    @Mock
    private CurrencyRepository currencyRepository;

    private final List<Currency> coinsOfTest = new ArrayList<>();

    @BeforeEach
    void setUp(){
        coinsOfTest.add(Currency.builder()
                .id(1L)
                .name("EUR")
                .description("Euro is a member of the eurozone")
                .exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder()
                .id(2L)
                .name("USD")
                .description("US Dollar coin of the United States of America")
                .exchanges(new HashMap<>()).build());
        coinsOfTest.add(Currency.builder()
                .id(3L)
                .name("R$")
                .description("Real coin of Brazil")
                .exchanges(new HashMap<>()).build());
    }

    @Test
    void GetCurrencies() {
        when(currencyRepository.findAll()).thenReturn(coinsOfTest);

        List<CurrencyResponse> responses = currencyService.get();

        Assertions.assertNotNull(responses);
        assertThat(responses).hasSize(3);
        Assertions.assertEquals("1 - EUR", responses.get(0).getLabel());
        Assertions.assertEquals("2 - USD", responses.get(1).getLabel());
        Assertions.assertEquals("3 - R$", responses.get(2).getLabel());
    }

    @Test
    void getCurrenciesEmpty() {
        when(currencyRepository.findAll()).thenReturn(new ArrayList<>());

        List<CurrencyResponse> responses = currencyService.get();
        Assertions.assertNotNull(responses);
        assertThat(responses).hasSize(0);
    }

    @Test
    void createCoin(){
        when(currencyRepository.findByName(Mockito.anyString())).thenReturn(null);
        when(currencyRepository.save(Mockito.any(Currency.class))).thenReturn(coinsOfTest.get(2));

        CurrencyRequest request = new CurrencyRequest();
        request.setName("R$");
        request.setDescription("Real coin of Brazil");
        request.setExchanges(new HashMap<>());

        Long id = currencyService.create(request);
        Assertions.assertNotNull(id);

        assertThat(id).isEqualTo(3L);

        verify(currencyRepository, times(1)).findByName(Mockito.anyString());
        verify(currencyRepository, times(1)).save(Mockito.any(Currency.class));
        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void createCoinAlreadyExists() {
        Mockito.when(currencyRepository.findByName(Mockito.anyString())).thenReturn(coinsOfTest.get(1));

        CurrencyRequest request = new CurrencyRequest();
        request.setName("USD");
        request.setDescription("US Dollar coin of the United States of America");
        request.setExchanges(new HashMap<>());

        CurrencyException exception = Assertions.assertThrows(CurrencyException.class, () -> currencyService.create(request));
        Assertions.assertEquals("Coin already exists", exception.getMessage());
    }

    @Test
    void createCurrency() {
        Mockito.when(currencyRepository.findByName(anyString())).thenReturn(null);
        Mockito.when(currencyRepository.save(any(Currency.class))).thenReturn(Currency.builder().id(3L).build());

        Long id = currencyService.create(CurrencyRequest.builder().name("name").build());
        Assertions.assertNotNull(id);

    }

    @Test
    void createCurrencyWithNullName() {
        CurrencyRequest request = new CurrencyRequest();
        request.setName(null);
        request.setDescription("US Dollar coin of the United States of America");
        request.setExchanges(new HashMap<>());

        assertThatThrownBy(()-> currencyService.create(request))
                .isInstanceOf(CurrencyException.class)
                .hasMessage("Coin name cannot be null");
    }

    @Test
    void deleteCoinNotFound() {
        Mockito.when(currencyRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(()-> currencyService.delete(1L))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Coin not found");
    }

    @Test
    void deleteCoin() {
        Mockito.when(currencyRepository.findById(3L)).thenReturn(Optional.ofNullable(coinsOfTest.get(2)));

        currencyService.delete(3L);
        verify(currencyRepository, times(1)).deleteById(3L);
        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void convertCurrency() {
        Mockito.when(currencyRepository.findByName("USD")).thenReturn(coinsOfTest.get(1));
        coinsOfTest.get(1).setExchanges(Map.of("BRL", new BigDecimal("5")));

        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("BRL")
                .amount(BigDecimal.TEN)
                .build();

        ConvertCurrencyResponse response = currencyService.convert(request);
        Assertions.assertEquals(new BigDecimal(50), response.getAmount());
        verify(currencyRepository, times(1)).findByName("USD");
        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void convertCurrencyFromNotExists() {
        Mockito.when(currencyRepository.findByName("USD")).thenReturn(coinsOfTest.get(1));
        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("R$")
                .amount(BigDecimal.ONE)
                .build();

        assertThatThrownBy(()-> currencyService.convert(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Exchange R$ not found for USD");
    }

    @Test
    void convertCurrencyCoinNotExists() {
        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("R$")
                .amount(BigDecimal.ONE)
                .build();

        assertThatThrownBy(()-> currencyService.convert(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Coin not found: USD");
    }

    @Test
    void convertWithAPI(){
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("EUR");
        request.setFrom("USD");
        request.setAmount(BigDecimal.ONE);

        Map<String, CurrencyQuote> quotes = Map.of("USDEUR", CurrencyQuote.builder().low(BigDecimal.TEN).build());

      when(currencyRepository.findByName(anyString())).thenReturn(coinsOfTest.get(1));
      when(currencyClient.getCurrencyQuote(anyString())).thenReturn(quotes);

      assertThat(currencyService.convertAPI(request).getAmount()).isEqualTo(new BigDecimal(10));
    }

    @Test
    void convertWithAPICoinNotFound (){
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("USD");
        request.setFrom("ETH");
        request.setAmount(BigDecimal.ONE);

        assertThatThrownBy(()-> currencyService.convertAPI(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Coin not found: ETH");
    }

    @Test
    void convertWithAPIExchangeNotFound () {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setTo("R$");
        request.setFrom("USD");
        request.setAmount(BigDecimal.ONE);

        when(currencyRepository.findByName(anyString())).thenReturn(coinsOfTest.get(1));

        assertThatThrownBy(() -> currencyService.convertAPI(request))
                .isInstanceOf(CoinNotFoundException.class)
                .hasMessage("Exchange R$ not found for USD");
    }
}