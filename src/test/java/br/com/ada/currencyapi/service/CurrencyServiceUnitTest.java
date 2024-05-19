package br.com.ada.currencyapi.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;

@ExtendWith(MockitoExtension.class)
public class CurrencyServiceUnitTest {

    @InjectMocks
    private CurrencyService currencyService;

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
    void testCreateCurrencyThrowsCurrencyException() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(Currency.builder().build());

        CurrencyException exception = Assertions.assertThrows(CurrencyException.class, () -> currencyService.create(CurrencyRequest.builder().build()));

        Assertions.assertEquals("Coin already exists", exception.getMessage());

    }

    @Test
    void testDeleteCurrency() {
        doNothing().when(currencyRepository).deleteById(anyLong());
        currencyService.delete(1L);
        verify(currencyRepository, times(1)).deleteById(anyLong());
        verifyNoMoreInteractions(currencyRepository);
    }

    @Test
    void testConvertCurrency() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(
                Currency.builder()
                        .exchanges(Map.of("EUR", new BigDecimal("2")))
                        .build()
        );

        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .to("EUR")
                .amount(BigDecimal.TEN)
                .build();

        ConvertCurrencyResponse response = currencyService.convert(request);
        Assertions.assertEquals(new BigDecimal("20"), response.getAmount());

    }

    @Test
    void textConvertCurrencyThrowsCoinNotFoundException() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(null);
        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.TEN)
                .build();

        CoinNotFoundException exception = Assertions.assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));

        Assertions.assertEquals("Coin not found: USD", exception.getMessage());

    }

    @Test
    void textConvertCurrencyThrowsCoinNotFoundExceptionForExchange() {
        Mockito.when(currencyRepository.findByName(any())).thenReturn(Currency.builder()
                .exchanges(Map.of("BRL", new BigDecimal("2")))
                .build());
        ConvertCurrencyRequest request = ConvertCurrencyRequest
                .builder()
                .from("USD")
                .to("EUR")
                .amount(BigDecimal.TEN)
                .build();

        CoinNotFoundException exception = Assertions.assertThrows(CoinNotFoundException.class, () -> currencyService.convert(request));

        Assertions.assertEquals("Exchange EUR not found for USD", exception.getMessage());

    }
}