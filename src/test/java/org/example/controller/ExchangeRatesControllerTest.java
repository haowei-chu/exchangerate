package org.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.businesslogic.ExchangeRateService;
import org.example.exception.ApiErrorCode;
import org.example.exception.ApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import org.junit.jupiter.api.Test;

@WebMvcTest(controllers = { ExchangeRatesController.class })
class ExchangeRatesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExchangeRateService exchangeRateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testValidRequest_returnsExchangeRateResponse() throws Exception {
        Map<String, Double> mockRates = Map.of("USD", 1.1, "NZD", 1.6);

        when(exchangeRateService.getFilteredExchangeRates(any(), anyList()))
                .thenReturn(mockRates);

        mockMvc.perform(get("/exchangeRates/EUR")
                        .param("symbols", "USD", "NZD")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.base").value("EUR"))
                .andExpect(jsonPath("$.rates.USD").value(1.1))
                .andExpect(jsonPath("$.rates.NZD").value(1.6));
    }

    @Test
    void testInvalidBase_returns400() throws Exception {
        mockMvc.perform(get("/exchangeRates/123")
                        .param("symbols", "USD", "NZD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testInvalidSymbols_returns400() throws Exception {
        mockMvc.perform(get("/exchangeRates/EUR")
                        .param("symbols", "usd", "###"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testUnsupportedBaseCurrency_returns400() throws Exception {
        when(exchangeRateService.getFilteredExchangeRates(any(), anyList()))
                .thenThrow(new ApiException(ApiErrorCode.ERROR_UNSUPPORTED_BASE_CURRENCY));

        mockMvc.perform(get("/exchangeRates/ZZZ")
                        .param("symbols", "USD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.ERROR_UNSUPPORTED_BASE_CURRENCY.getCode()))
                .andExpect(jsonPath("$.message").exists());
    }
}
