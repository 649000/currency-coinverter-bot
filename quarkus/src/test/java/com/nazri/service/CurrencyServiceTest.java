package com.nazri.service;

import com.nazri.client.CurrencyApiClient;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyApiClient currencyApiClient;

    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyService();
        currencyService.currencyApiClient = currencyApiClient;
        currencyService.currencyListapiUrl = "https://api.example.com/currencies";
    }

    @Test
    void convertCurrency_ShouldReturnConvertedAmounts_WhenValidInputs() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR", "GBP");
        
        Map<String, Object> apiResponse = createMockApiResponse(fromCurrency, 
                Map.of("eur", 0.85, "gbp", 0.75));
        when(currencyApiClient.getExchangeRates("usd")).thenReturn(apiResponse);

        // When
        Map<String, BigDecimal> result = currencyService.convertCurrency(amount, fromCurrency, toCurrencies);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("85.00"), result.get("EUR"));
        assertEquals(new BigDecimal("75.00"), result.get("GBP"));
        verify(currencyApiClient).getExchangeRates("usd");
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenAmountIsNull() {
        // Given
        BigDecimal amount = null;
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenAmountIsZero() {
        // Given
        BigDecimal amount = BigDecimal.ZERO;
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenAmountIsNegative() {
        // Given
        BigDecimal amount = new BigDecimal("-10.00");
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Amount must be positive", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenFromCurrencyIsNull() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = null;
        List<String> toCurrencies = Arrays.asList("EUR");

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Source currency required", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenFromCurrencyIsEmpty() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "";
        List<String> toCurrencies = Arrays.asList("EUR");

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Source currency required", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenToCurrenciesIsNull() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        List<String> toCurrencies = null;

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Target currencies required", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenToCurrenciesIsEmpty() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList();

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Target currencies required", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowBadRequest_WhenFromCurrencyIsInvalid() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "INVALID";
        List<String> toCurrencies = Arrays.asList("EUR");

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Invalid source currency: INVALID", exception.getMessage());
    }

    @Test
    void convertCurrency_ShouldThrowServiceUnavailable_WhenApiClientThrowsException() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");
        
        when(currencyApiClient.getExchangeRates(anyString()))
                .thenThrow(new RuntimeException("API connection failed"));

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.convertCurrency(amount, fromCurrency, toCurrencies));
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Failed to fetch exchange rates", exception.getMessage());
    }

    @Test
    void fetchExchangeRates_ShouldReturnRates_WhenApiResponseIsValid() {
        // Given
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR", "GBP");
        
        Map<String, Object> apiResponse = createMockApiResponse(fromCurrency, 
                Map.of("eur", 0.85, "gbp", 0.75));
        when(currencyApiClient.getExchangeRates("usd")).thenReturn(apiResponse);

        // When
        Map<String, BigDecimal> result = currencyService.fetchExchangeRates(fromCurrency, toCurrencies);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("0.85"), result.get("EUR"));
        assertEquals(new BigDecimal("0.75"), result.get("GBP"));
    }

    @Test
    void fetchExchangeRates_ShouldThrowNotFound_WhenNoCurrencyDataInResponse() {
        // Given
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");
        
        Map<String, Object> apiResponse = new HashMap<>();
        when(currencyApiClient.getExchangeRates("usd")).thenReturn(apiResponse);

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.fetchExchangeRates(fromCurrency, toCurrencies));
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Failed to fetch exchange rates", exception.getMessage());
    }

    @Test
    void fetchExchangeRates_ShouldThrowNotFound_WhenNoRatesFoundInResponse() {
        // Given
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");
        
        Map<String, Object> apiResponse = createMockApiResponse(fromCurrency, Map.of());
        when(currencyApiClient.getExchangeRates("usd")).thenReturn(apiResponse);

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.fetchExchangeRates(fromCurrency, toCurrencies));
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Failed to fetch exchange rates", exception.getMessage());
    }

    @Test
    void fetchExchangeRates_ShouldThrowServiceUnavailable_WhenApiThrowsException() {
        // Given
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");
        
        when(currencyApiClient.getExchangeRates("usd"))
                .thenThrow(new RuntimeException("Network error"));

        // When & Then
        WebApplicationException exception = assertThrows(WebApplicationException.class,
                () -> currencyService.fetchExchangeRates(fromCurrency, toCurrencies));
        assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), exception.getResponse().getStatus());
        assertEquals("Failed to fetch exchange rates", exception.getMessage());
    }

    @Test
    void getCurrencyCode_ShouldReturnCurrencyCode_WhenValidCurrencyCodeProvided() {
        // When
        String result = currencyService.getCurrencyCode("USD");

        // Then
        assertEquals("USD", result);
    }

    @Test
    void getCurrencyCode_ShouldReturnCurrencyCode_WhenValidCountryCodeProvided() {
        // When
        String result = currencyService.getCurrencyCode("US");

        // Then
        assertEquals("USD", result);
    }

    @Test
    void getCurrencyCode_ShouldReturnNull_WhenInvalidInputProvided() {
        // When
        String result = currencyService.getCurrencyCode("INVALID");

        // Then
        assertNull(result);
    }

    @Test
    void getCurrencyCode_ShouldReturnNull_WhenNullInputProvided() {
        // When
        String result = currencyService.getCurrencyCode(null);

        // Then
        assertNull(result);
    }

    @Test
    void getCurrencyCode_ShouldReturnNull_WhenEmptyInputProvided() {
        // When
        String result = currencyService.getCurrencyCode("");

        // Then
        assertNull(result);
    }

    @Test
    void getCurrencyCode_ShouldReturnNull_WhenWhitespaceInputProvided() {
        // When
        String result = currencyService.getCurrencyCode("   ");

        // Then
        assertNull(result);
    }

    @Test
    void convertCurrency_ShouldHandleLowercaseInputs() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "usd";
        List<String> toCurrencies = Arrays.asList("eur", "gbp");
        
        Map<String, Object> apiResponse = createMockApiResponse("usd", 
                Map.of("eur", 0.85, "gbp", 0.75));
        when(currencyApiClient.getExchangeRates("usd")).thenReturn(apiResponse);

        // When
        Map<String, BigDecimal> result = currencyService.convertCurrency(amount, fromCurrency, toCurrencies);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("85.00"), result.get("EUR"));
        assertEquals(new BigDecimal("75.00"), result.get("GBP"));
    }

    @Test
    void convertCurrency_ShouldRoundToTwoDecimalPlaces() {
        // Given
        BigDecimal amount = new BigDecimal("100.00");
        String fromCurrency = "USD";
        List<String> toCurrencies = Arrays.asList("EUR");
        
        Map<String, Object> apiResponse = createMockApiResponse(fromCurrency, 
                Map.of("eur", 0.123456789));
        when(currencyApiClient.getExchangeRates("usd")).thenReturn(apiResponse);

        // When
        Map<String, BigDecimal> result = currencyService.convertCurrency(amount, fromCurrency, toCurrencies);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("12.35"), result.get("EUR"));
    }

    private Map<String, Object> createMockApiResponse(String fromCurrency, Map<String, Object> rates) {
        Map<String, Object> response = new HashMap<>();
        response.put(fromCurrency.toLowerCase(), rates);
        return response;
    }
}
