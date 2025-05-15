package org.example.businesslogic;

import org.example.businesslogic.ExchnageRateClient.ExchangeRateProvider;
import org.example.exception.ApiErrorCode;
import org.example.exception.ApiException;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ExchangeRateService {

    private final List<ExchangeRateProvider> providers;

    // cache: key = "BASE|USD,EUR", value = map of rates
    private final Map<String, Map<String, Double>> cache = new ConcurrentHashMap<>();

    public Map<String, Double> getFilteredExchangeRates(String baseCurrency, List<String> targetCurrencies) {
        if (providers == null || providers.isEmpty()) {
            log.error("No exchange rate providers are registered or available.");
            throw new ApiException(ApiErrorCode.ERROR_NO_AVAILABLE_PROVIDERS);
        }

        String base = baseCurrency.toUpperCase();
        Set<String> targets = targetCurrencies.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toCollection(TreeSet::new)); // sorted for stable key

        String cacheKey = base + "|" + String.join(",", targets);

        // Check cache
        if (cache.containsKey(cacheKey)) {
            log.info("Returning cached exchange rates for {}", cacheKey);
            return cache.get(cacheKey);
        }

        Map<String, List<Double>> aggregated = new HashMap<>();
        boolean foundSupportedProvider = false;

        for (ExchangeRateProvider provider : providers) {
            List<String> supported = provider.fetchSupportedCurrencies();

            if (!supported.contains(base)) {
                log.info("{} does not support base currency {}", provider.getName(), base);
                continue;
            }

            foundSupportedProvider = true;

            try {
                Map<String, Double> rates = provider.fetchExchangeRates(base, List.copyOf(targets));

                rates.forEach((symbol, value) -> {
                    String upperSymbol = symbol.toUpperCase();
                    if (targets.contains(upperSymbol)) {
                        aggregated.computeIfAbsent(upperSymbol, k -> new ArrayList<>()).add(value);
                    }
                });

                log.info("Fetched {} rates from {}", rates.size(), provider.getName());

            } catch (Exception e) {
                log.warn("Failed to fetch rates from provider {}: {}", provider.getName(), e.getMessage());
            }
        }

        if (!foundSupportedProvider) {
            log.warn("None of the providers support base currency: {}", base);
            throw new ApiException(ApiErrorCode.ERROR_UNSUPPORTED_BASE_CURRENCY);
        }

        // Average and cache result
        Map<String, Double> finalRates = targets.stream()
                .filter(aggregated::containsKey)
                .collect(Collectors.toMap(
                        Function.identity(),
                        key -> aggregated.get(key).stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0)
                ));

        cache.put(cacheKey, finalRates);
        return finalRates;
    }
}
