package com.pm.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

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

public static class Config {}

@Override
public GatewayFilter apply(Config config) {

    return (exchange, chain) -> {

        if (validator.isSecured.test(exchange.getRequest())) {

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

            return webClient
                    .get()
                    .uri("http://IDENTITY-SERVICE/auth/validate?token=" + token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(userId -> {
                        System.out.println("Identity Service returned User ID: " + userId);

                        // Use a Decorator to override the headers method
                        org.springframework.http.server.reactive.ServerHttpRequestDecorator decorator = 
                            new org.springframework.http.server.reactive.ServerHttpRequestDecorator(exchange.getRequest()) {
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
                        error.printStackTrace();
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
