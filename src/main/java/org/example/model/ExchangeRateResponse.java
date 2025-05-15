package org.example.model;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class ExchangeRateResponse {
    private String base;
    private Map<String, Double> rates;
}
