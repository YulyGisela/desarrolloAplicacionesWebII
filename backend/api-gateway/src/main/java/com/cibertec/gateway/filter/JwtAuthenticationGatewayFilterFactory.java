package com.cibertec.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

	private final SecretKey secretKey;

	public JwtAuthenticationGatewayFilterFactory(@Value("${app.jwt.secret}") String secret) {
		super(Config.class);
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return onError(exchange, "Falta cabecera de autenticación", HttpStatus.UNAUTHORIZED);
			}

			String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return onError(exchange, "Formato de token no válido", HttpStatus.UNAUTHORIZED);
			}

			String token = authHeader.substring(7);

			try {
				Claims claims = Jwts.parser()
						.verifyWith(secretKey)
						.build()
						.parseSignedClaims(token)
						.getPayload();

				if (claims.getExpiration().before(new Date())) {
					return onError(exchange, "El token ha expirado", HttpStatus.UNAUTHORIZED);
				}

				String username = claims.getSubject();
				Object role = claims.get("role");

				ServerHttpRequest modifiedRequest = request.mutate()
						.header("X-User", username != null ? username : "")
						.header("X-Role", role != null ? role.toString() : "")
						.build();

				return chain.filter(exchange.mutate().request(modifiedRequest).build());

			} catch (Exception e) {
				return onError(exchange, "Token inválido o expirado", HttpStatus.UNAUTHORIZED);
			}
		};
	}

	private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(httpStatus);
		response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

		String json = "{\"status\":" + httpStatus.value() + ",\"message\":\"" + err + "\",\"errors\":[]}";
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		DataBuffer buffer = response.bufferFactory().wrap(bytes);
		return response.writeWith(Mono.just(buffer));
	}

	public static class Config {
	}
}
