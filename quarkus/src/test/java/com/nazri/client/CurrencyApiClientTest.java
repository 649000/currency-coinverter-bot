package com.nazri.client;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@QuarkusTest
public class CurrencyApiClientTest {

    @InjectMock
    @RestClient
    CurrencyApiClient currencyApiClient;

    @Test
    public void testGetExchangeRates() {
        // Given
        String currency = "usd";
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> rates = new HashMap<>();
        rates.put("eur", 0.85);
        rates.put("gbp", 0.75);
        mockResponse.put("usd", rates);
        
        when(currencyApiClient.getExchangeRates(currency)).thenReturn(mockResponse);

        // When
        Map<String, Object> result = currencyApiClient.getExchangeRates(currency);
        
        // Then
        Mockito.verify(currencyApiClient, Mockito.times(1)).getExchangeRates(currency);
        assertNotNull(result);
        assertNotNull(result.get("usd"));
    }

    @Test
    public void testGetExchangeRatesWithDifferentCurrency() {
        // Given
        String currency = "eur";
        Map<String, Object> mockResponse = new HashMap<>();
        Map<String, Object> rates = new HashMap<>();
        rates.put("usd", 1.18);
        rates.put("gbp", 0.88);
        mockResponse.put("eur", rates);
        
        when(currencyApiClient.getExchangeRates(currency)).thenReturn(mockResponse);

        // When
        Map<String, Object> result = currencyApiClient.getExchangeRates(currency);
        
        // Then
        Mockito.verify(currencyApiClient, Mockito.times(1)).getExchangeRates(currency);
        assertNotNull(result);
        assertNotNull(result.get("eur"));
    }
}
