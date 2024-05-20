package br.com.ada.currencyapi.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SimpleTimeZone;

import org.springframework.stereotype.Service;

import br.com.ada.currencyapi.domain.ConvertCurrencyRequest;
import br.com.ada.currencyapi.domain.ConvertCurrencyResponse;
import br.com.ada.currencyapi.domain.Currency;
import br.com.ada.currencyapi.domain.CurrencyRequest;
import br.com.ada.currencyapi.domain.CurrencyResponse;
import br.com.ada.currencyapi.exception.CoinNotFoundException;
import br.com.ada.currencyapi.exception.CurrencyException;
import br.com.ada.currencyapi.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyClient currencyClient;

    public List<CurrencyResponse> get() {
        List<Currency> currencies = currencyRepository.findAll();
        List<CurrencyResponse> dtos = new ArrayList<>();

        currencies.forEach((currency) -> dtos.add(CurrencyResponse.builder()
                .label("%s - %s".formatted(currency.getId(), currency.getName()))
                .build()));

        return dtos;
    }

    public Long create(CurrencyRequest request) throws CurrencyException {

        if (Objects.isNull(request.getName())) {
            throw new CurrencyException("Coin name cannot be null");
        }

        Currency currency = currencyRepository.findByName(request.getName());

        if (Objects.nonNull(currency)) {
            throw new CurrencyException("Coin already exists");
        }

        Currency saved = currencyRepository.save(Currency.builder()
                .name(request.getName())
                .description(request.getDescription())
                .exchanges(request.getExchanges())
                .build());
        return saved.getId();
    }

    public void delete(Long id) {
        Currency Exists = currencyRepository.findById(id).orElseThrow(() -> new CoinNotFoundException("Coin not found"));
        currencyRepository.deleteById(id);
    }

    public ConvertCurrencyResponse convert(ConvertCurrencyRequest request) throws CoinNotFoundException {
        BigDecimal amount = getAmount(request);
        return ConvertCurrencyResponse.builder()
                .amount(amount)
                .build();

    }

    public ConvertCurrencyResponse convertAPI(ConvertCurrencyRequest request) throws CoinNotFoundException {
        BigDecimal amountAPI = getAmountAPI(request);
        return ConvertCurrencyResponse.builder()
                .amount(amountAPI)
                .build();
    }

    private BigDecimal getAmount(ConvertCurrencyRequest request) throws CoinNotFoundException {
        Currency currency = currencyRepository.findByName(request.getFrom());

        if (Objects.isNull(currency)) {
            throw new CoinNotFoundException(String.format("Coin not found: %s", request.getFrom()));
        }

        BigDecimal exchange = currency.getExchanges().get(request.getTo());

        if (Objects.isNull(exchange)) {
            throw new CoinNotFoundException(String.format("Exchange %s not found for %s", request.getTo(), request.getFrom()));
        }

        return request.getAmount().multiply(exchange);
    }

    private BigDecimal getAmountAPI(ConvertCurrencyRequest request) throws CoinNotFoundException {
        Currency from = currencyRepository.findByName(request.getFrom());

        if (Objects.isNull(from)) {
            throw new CoinNotFoundException(String.format("Coin not found: %s", request.getFrom()));
        }

        StringBuilder code = new StringBuilder();
        code.append(request.getFrom());
        code.append("-");
        code.append(request.getTo());

        try {
            BigDecimal AmountExchange = currencyClient.getCurrencyQuote(code.toString()).get(code.toString().replace("-", "")).low();
            return request.getAmount().multiply(AmountExchange);

        }catch(Exception e){
            throw new CoinNotFoundException(String.format("Exchange %s not found for %s", request.getTo(), request.getFrom()));
        }

    }
}
