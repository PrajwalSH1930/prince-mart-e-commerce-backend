package com.pm.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator validator;
    private final WebClient webClient;

    public AuthenticationFilter(RouteValidator validator, WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.validator = validator;
        this.webClient = webClientBuilder.build();
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            
            // 1. SKIP CORS PREFLIGHT REQUESTS
            // Browsers send OPTIONS requests without headers. If we don't skip them, 
            // the filter returns 401 and the CORS handshake fails.
            if (CorsUtils.isPreFlightRequest(exchange.getRequest())) {
                return chain.filter(exchange);
            }

            // 2. CHECK IF ROUTE IS SECURED
            if (validator.isSecured.test(exchange.getRequest())) {
                
                // Check for Authorization Header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest()
                        .getHeaders()
                        .getFirst(HttpHeaders.AUTHORIZATION);

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);

                // 3. VALIDATE TOKEN WITH IDENTITY SERVICE
                return webClient
                        .get()
                        .uri("http://IDENTITY-SERVICE/auth/validate?token=" + token)
                        .retrieve()
                        .bodyToMono(String.class)
                        .flatMap(userId -> {
                            // 4. ADD USER ID TO HEADERS FOR DOWNSTREAM SERVICES
                            ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
                                @Override
                                public HttpHeaders getHeaders() {
                                    HttpHeaders confHeaders = new HttpHeaders();
                                    confHeaders.addAll(super.getHeaders());
                                    confHeaders.add("X-User-Id", userId);
                                    return HttpHeaders.readOnlyHttpHeaders(confHeaders);
                                }
                            };

                            return chain.filter(exchange.mutate().request(decorator).build());
                        })
                        .onErrorResume(error -> {
                            return onError(exchange, HttpStatus.UNAUTHORIZED);
                        });
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}