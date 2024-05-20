package br.com.ada.currencyapi.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.ada.currencyapi.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.ada.currencyapi.service.CurrencyService;
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class CurrencyControllerUnitTest {

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private CurrencyController currencyController;

    private MockMvc mockMvc;

    private final List<CurrencyResponse> coinsOfTest = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyController).build();

        coinsOfTest.add(CurrencyResponse.builder()
                .label("1 - USD")
                .build());
        coinsOfTest.add(CurrencyResponse.builder()
                .label("2 - R$")
                .build());
        coinsOfTest.add(CurrencyResponse.builder()
                .label("3 - EUR")
                .build());
    }

    @Test
    void GetCurrencies() throws Exception {
        Mockito.when(currencyService.get()).thenReturn(coinsOfTest);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(3)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].label").value("1 - USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].label").value("2 - R$"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[2].label").value("3 - EUR"))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    void GetCurrenciesEmpty() throws Exception {
        Mockito.when(currencyService.get()).thenReturn(new ArrayList<>());
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(0)))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    void Convert() throws Exception {
        ConvertCurrencyRequest request = new ConvertCurrencyRequest();
        request.setFrom("USD");
        request.setTo("R$");
        request.setAmount(BigDecimal.ONE);

        ConvertCurrencyResponse response = new ConvertCurrencyResponse(BigDecimal.TEN);

        Mockito.when(currencyService.convert(Mockito.any(ConvertCurrencyRequest.class))).thenReturn(response);
        var content = new ObjectMapper().writeValueAsString(request);
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/currency/convert")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.amount").value(BigDecimal.TEN))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void Create() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setName("RUB");

        Mockito.when(currencyService.create(Mockito.any(CurrencyRequest.class))).thenReturn(10L);
        var content = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("$").value(10L))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void CreateInvalid() throws Exception {
        String content = objectMapper.writeValueAsString(null);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/currency")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void Delete() throws Exception {
        Mockito.doNothing().when(currencyService).delete(anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/currency/{id}",1L)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }
}
