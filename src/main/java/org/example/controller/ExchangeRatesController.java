package org.example.controller;

import org.example.businesslogic.ExchangeRateService;
import org.example.metrics.MetricsTracker;
import org.example.model.ExchangeRateResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@Validated
public class ExchangeRatesController {

    private final ExchangeRateService exchangeRateService;
    private final MetricsTracker metricsTracker;

    @GetMapping("/exchangeRates/{base}")
    public ExchangeRateResponse getExchangeRates(
            @PathVariable
            @NotBlank(message = "Base currency must not be blank")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "Base currency must be a 3-letter alphabetic code")
            String base,

            @RequestParam
            @NotEmpty(message = "At least one target currency must be specified")
            List<
                    @NotBlank(message = "Currency code cannot be blank")
                    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Each target currency must be a 3-letter alphabetic code")
                            String> symbols
    ) {
        log.info("Get exchange rates for {} -> {}", base, symbols);
        metricsTracker.incrementTotalQueries();

        Map<String, Double> filteredRates = exchangeRateService.getFilteredExchangeRates(base, symbols);

        return new ExchangeRateResponse().setBase(base.toUpperCase()).setRates(filteredRates);
    }
}
