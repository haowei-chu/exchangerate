package org.example.businesslogic.ExchnageRateClient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.example.metrics.MetricsTracker;

import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FawazApiClientTest {
    private RestTemplate restTemplate = mock(RestTemplate.class);
    private MetricsTracker metricsTracker = mock(MetricsTracker.class);
    private FawazApiClient fawazApiClient = spy(new FawazApiClient(metricsTracker, restTemplate));


    @Test
    void testFetchSupportedCurrencies_success() {
        Map<String, String> fakeResponse = Map.of(
                "usd", "US Dollar",
                "eur", "Euro",
                "btc", "Bitcoin",
                "aave", "Aave"  // will be excluded if not 3-letter alpha
        );

        when(restTemplate.getForObject(anyString(), any())).thenReturn(fakeResponse);

        List<String> result = fawazApiClient.fetchSupportedCurrencies();

        assertTrue(result.contains("USD"));
        assertTrue(result.contains("EUR"));
        assertFalse(result.contains("AAVE"));
    }

    @Test
    void testFetchSupportedCurrencies_nullResponse_returnsEmptyList() {
        // Simulate API returning null
        when(restTemplate.getForObject(any(), any())).thenReturn(null);

        List<String> result = fawazApiClient.fetchSupportedCurrencies();

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Should return empty list when response is null");
    }
}
