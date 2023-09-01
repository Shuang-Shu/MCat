package com.mdc.mcat.adapter;

import java.net.URI;

public interface HttpExchangeRequest {
    String getRequestMethod();

    URI getRequestURI();
}
