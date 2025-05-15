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
public class FawazApiClient implements ExchangeRateProvider {

    private final MetricsTracker metricsTracker;
    private final RestTemplate restTemplate;
    private static final String BASE_URL = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1";

    private List<String> supported = List.of();


    @Override
    @PostConstruct
    public List<String> fetchSupportedCurrencies() {
        if (supported.isEmpty()) {
            refreshSupportedCurrencies();
        }
        return supported;
    }
    @Override public String getName() { return "fawaz"; }

    private void refreshSupportedCurrencies() {
        String url = BASE_URL + "/currencies.json";
        try {
            metricsTracker.recordApiRequest("fawaz");
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            metricsTracker.recordApiResponse("fawaz");

            if (response != null && !response.isEmpty()) {
                supported = response.keySet().stream()
                        .filter(key -> key.length() == 3 && key.chars().allMatch(Character::isLetter))
                        .map(String::toUpperCase)
                        .sorted()
                        .toList();

                log.info("Fawaz: Loaded {} supported 3-letter currencies", supported.size());
            }
        } catch (Exception e) {
            log.error("Fawaz: Failed to fetch supported currencies", e);
        }
    }


    @Override
    public Map<String, Double> fetchExchangeRates(String baseCurrency, List<String> targetCurrencies) {
        String url = BASE_URL + "/currencies/{base}.json";
        try {
            // Only supports lower case
            metricsTracker.recordApiRequest("fawaz");
            Map<String, Object> fullResponse = restTemplate.getForObject(url, Map.class, baseCurrency.toLowerCase());
            metricsTracker.recordApiResponse("fawaz");

            if (fullResponse == null || !fullResponse.containsKey(baseCurrency.toLowerCase())) {
                return Collections.emptyMap();
            }

            Map<String, Object> rates = (Map<String, Object>) fullResponse.get(baseCurrency.toLowerCase());

            return rates.entrySet().stream()
                    .filter(e -> e.getKey().length() == 3 && e.getKey().chars().allMatch(Character::isLetter))
                    .filter(e -> e.getValue() instanceof Number)
                    .collect(Collectors.toMap(
                            e -> e.getKey().toUpperCase(),
                            e -> ((Number) e.getValue()).doubleValue()
                    ));

        } catch (Exception e) {
            log.error("Fawaz: Failed to fetch exchange rates for {}", baseCurrency, e);
            return Collections.emptyMap();
        }
    }


}

