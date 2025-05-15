package org.example.businesslogic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.example.businesslogic.ExchnageRateClient.FawazApiClient;
import org.example.businesslogic.ExchnageRateClient.FrankfurterApiClient;
import org.example.exception.ApiErrorCode;
import org.example.exception.ApiException;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeRateServiceTest {
    private final FawazApiClient fawazApiClient = mock(FawazApiClient.class);
    private final FrankfurterApiClient frankfurterApiClient = mock(FrankfurterApiClient.class);
    private final ExchangeRateService exchangeRateService = spy(new ExchangeRateService(List.of(frankfurterApiClient, fawazApiClient)));

    @BeforeEach
    void setUp() {
        when(frankfurterApiClient.getName()).thenReturn("frankfurter");
        when(fawazApiClient.getName()).thenReturn("fawaz");
    }

    @Test
    void returnsAverageRatesFromMultipleProviders() {
        // Given
        when(fawazApiClient.fetchSupportedCurrencies()).thenReturn(List.of("EUR", "USD", "NZD"));
        when(frankfurterApiClient.fetchSupportedCurrencies()).thenReturn(List.of("EUR", "USD", "NZD"));

        when(fawazApiClient.fetchExchangeRates(any(), anyList()))
                .thenReturn(Map.of("USD", 1.1, "NZD", 1.5));

        when(frankfurterApiClient.fetchExchangeRates(any(),anyList()))
                .thenReturn(Map.of("USD", 1.3, "NZD", 1.7));

        // When
        Map<String, Double> result = exchangeRateService.getFilteredExchangeRates("EUR", List.of("USD", "NZD"));

        // Then
        assertEquals(2, result.size());
        assertEquals(1.2, result.get("USD"), 0.0001);
        assertEquals(1.6, result.get("NZD"), 0.0001);
    }

    @Test
    void skipsProvidersThatDontSupportBase() {
        when(fawazApiClient.fetchSupportedCurrencies()).thenReturn(List.of("EUR", "USD"));
        when(frankfurterApiClient.fetchSupportedCurrencies()).thenReturn(List.of("USD")); // does NOT support EUR

        when(fawazApiClient.fetchExchangeRates(any(), anyList()))
                .thenReturn(Map.of("USD", 1.2));

        Map<String, Double> result = exchangeRateService.getFilteredExchangeRates("EUR", List.of("USD"));

        assertEquals(1, result.size());
        assertEquals(1.2, result.get("USD"));
    }

    @Test
    void throwsWhenNoProviderSupportsBaseCurrency() {
        when(fawazApiClient.fetchSupportedCurrencies()).thenReturn(List.of("USD"));
        when(frankfurterApiClient.fetchSupportedCurrencies()).thenReturn(List.of("USD"));

        ApiException exception = assertThrows(ApiException.class, () ->
                exchangeRateService.getFilteredExchangeRates("XXX", List.of("USD"))
        );

        assertEquals(ApiErrorCode.ERROR_UNSUPPORTED_BASE_CURRENCY.getCode(), exception.getCode());
    }

    @Test
    void throwsWhenNoProvidersAvailable() {
        ExchangeRateService emptyService = new ExchangeRateService(List.of());

        ApiException exception = assertThrows(ApiException.class, () ->
                emptyService.getFilteredExchangeRates("USD", List.of("EUR"))
        );

        assertEquals(ApiErrorCode.ERROR_NO_AVAILABLE_PROVIDERS.getCode(), exception.getCode());
    }
}
