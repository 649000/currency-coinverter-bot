package com.nazri.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CurrencyService {

    private static final Logger log = Logger.getLogger(CurrencyService.class);

    @ConfigProperty(name = "currency.converter.url")
    private String URL;
}
