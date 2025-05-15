package org.example.businesslogic.ExchnageRateClient;

import org.example.metrics.MetricsTracker;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FrankfurterApiClient implements ExchangeRateProvider {
    private final MetricsTracker metricsTracker;

    private static final String BASE_URL = "https://api.frankfurter.dev/v1";
    private final RestTemplate restTemplate;

    private List<String> supported = List.of();


    @Override
    @PostConstruct
    public List<String> fetchSupportedCurrencies() {
        if (supported.isEmpty()) {
            refreshSupportedCurrencies();
        }
        return supported;
    }
    @Override public String getName() { return "Frankfurter"; }

    private void refreshSupportedCurrencies() {
        String url = BASE_URL + "/currencies";
        try {
            metricsTracker.recordApiRequest("Frankfurter");
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            metricsTracker.recordApiResponse("Frankfurter");

            if (response != null && !response.isEmpty()) {
                supported = response.keySet().stream()
                        .filter(key -> key.length() == 3 && key.chars().allMatch(Character::isLetter))
                        .map(String::toUpperCase)
                        .sorted()
                        .toList();

                log.info("Frankfurter: Loaded {} supported 3-letter currencies", supported.size());
            } else {
                log.warn("Frankfurter: Received empty or null currency list");
            }

        } catch (Exception e) {
            log.error("Frankfurter: Failed to fetch supported currencies", e);
        }
    }


    @Override
    public Map<String, Double> fetchExchangeRates(String baseCurrency, List<String> targetCurrencies) {
        String url = BASE_URL + "/latest?base={base}&symbols={symbols}";
        try {
            String symbolsParam = String.join(",", targetCurrencies).toUpperCase();

            metricsTracker.recordApiRequest("Frankfurter");
            Map<String, Object> fullResponse = restTemplate.getForObject(
                    url, Map.class, baseCurrency.toUpperCase(), symbolsParam
            );
            metricsTracker.recordApiResponse("Frankfurter");

            if (fullResponse == null || !fullResponse.containsKey("rates")) {
                log.warn("No 'rates' found in response for base: {}", baseCurrency);
                return Collections.emptyMap();
            }

            Map<String, Object> rates = (Map<String, Object>) fullResponse.get("rates");

            return rates.entrySet().stream()
                    .filter(e -> e.getValue() instanceof Number)
                    .collect(Collectors.toMap(
                            e -> e.getKey().toUpperCase(),
                            e -> ((Number) e.getValue()).doubleValue()
                    ));

        } catch (Exception e) {
            log.error("Failed to fetch exchange rates for base {} with targets {}",
                    baseCurrency, targetCurrencies, e);
            return Collections.emptyMap();
        }
    }


}
