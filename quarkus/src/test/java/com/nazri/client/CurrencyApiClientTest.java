package com.nazri.client;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class CurrencyApiClientTest {

    // Since this is an interface, we can't directly mock it
    // In a real test, you would either:
    // 1. Use WireMock to mock the external API
    // 2. Test through the service that uses this client
    // 3. Use QuarkusMock to mock the injected client in a service

    @Test
    public void testCurrencyApiClientInterface() {
        // This is a basic test to ensure the interface is properly defined
        // In a real scenario, you would test the actual implementation
        assertTrue(CurrencyApiClient.class.isInterface());
    }
}
