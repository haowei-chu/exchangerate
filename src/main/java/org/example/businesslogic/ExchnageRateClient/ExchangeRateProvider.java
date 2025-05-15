package org.example.businesslogic.ExchnageRateClient;

import java.util.List;
import java.util.Map;

public interface ExchangeRateProvider {
    String getName();
    List<String> fetchSupportedCurrencies();
    Map<String, Double> fetchExchangeRates(String baseCurrency, List<String> targetCurrencies);
}
