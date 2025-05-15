package org.example.businesslogic.ExchnageRateClient;

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

class FrankfurterApiClientTest {
    private RestTemplate restTemplate= mock(RestTemplate.class);
    private MetricsTracker metricsTracker= mock(MetricsTracker.class);
    private FrankfurterApiClient frankfurterApiClient= spy(new FrankfurterApiClient(metricsTracker, restTemplate));

    @Test
    void testFetchSupportedCurrencies_success() {
        Map<String, String> fakeResponse = Map.of(
                "USD", "US Dollar",
                "EUR", "Euro"
        );

        when(restTemplate.getForObject(anyString(), any())).thenReturn(fakeResponse);

        List<String> result = frankfurterApiClient.fetchSupportedCurrencies();

        assertTrue(result.contains("USD"));
        assertTrue(result.contains("EUR"));
    }

    @Test
    void testFetchSupportedCurrencies_nullResponse_returnsEmptyList() {
        when(restTemplate.getForObject(anyString(), any())).thenReturn(null);

        List<String> result = frankfurterApiClient.fetchSupportedCurrencies();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
