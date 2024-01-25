package com.parqour.resiliencedemo.config;


import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HttpClientConfig {

  @Bean
  public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
    SSLContextBuilder builder = new SSLContextBuilder();
    try {
      builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
    } catch (NoSuchAlgorithmException | KeyStoreException ex) {
      log.error("Pooling Connection Manager Initialization failure. Reason: {}", ex.getMessage());
    }

    SSLConnectionSocketFactory sslsf = null;
    try {
      sslsf = new SSLConnectionSocketFactory(builder.build());
    } catch (KeyManagementException | NoSuchAlgorithmException ex) {
      log.error("Pooling Connection Manager Initialization failure. Reason: {}", ex.getMessage());
    }

    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
        .<ConnectionSocketFactory>create().register("https", sslsf)
        .register("http", new PlainConnectionSocketFactory())
        .build();

    PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
        new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    poolingHttpClientConnectionManager.setMaxTotal(300);
    poolingHttpClientConnectionManager.setDefaultMaxPerRoute(10);
    return poolingHttpClientConnectionManager;
  }

  @Bean
  public CloseableHttpClient httpClient() {
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout(Timeout.ofMilliseconds(2000))
        .setResponseTimeout(Timeout.ofMilliseconds(3000))
        .build();

    return HttpClients.custom()
        .setDefaultRequestConfig(requestConfig)
        .setConnectionManager(poolingHttpClientConnectionManager())
        .build();
  }
}
