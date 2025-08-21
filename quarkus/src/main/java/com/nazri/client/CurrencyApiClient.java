package com.nazri.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * REST Client for currency exchange rate API.
 * Provides built-in fault tolerance with retry, circuit breaker, and timeout.
 */
@RegisterRestClient(configKey = "currency-api")
public interface CurrencyApiClient {

    /**
     * Fetches exchange rates for a specific base currency.
     * 
     * @param currency The base currency code (e.g., "usd", "eur")
     * @return Map containing the API response with exchange rates
     */
    @GET
    @Path("/v1/currencies/{currency}.json")
    @Produces(MediaType.APPLICATION_JSON)
    @Retry(maxRetries = 3, delay = 1000, delayUnit = ChronoUnit.MILLIS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 5000, delayUnit = ChronoUnit.MILLIS)
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    Map<String, Object> getExchangeRates(@PathParam("currency") String currency);
}
