package org.example.omnibegateway.common.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.example.omnibegateway.common.apiPayload.ApiResult;
import org.example.omnibegateway.common.apiPayload.code.status.ErrorStatus;
import org.example.omnibegateway.common.util.GatewayResponseUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@Slf4j
public class JWTGatewayFilter implements GlobalFilter, Ordered {

    private final JWTUtil jwtUtil;
    AntPathMatcher matcher = new AntPathMatcher();

    private static final List<String> WHITELIST_PATTERNS = List.of(
            "/**/auth/reissue",
            "/**/auth/login",
            "/**/auth/logout",
            "/**/auth/signup/**",
            "/**/v3/api-docs/**",
            "/**/swagger-ui/**",
            "/**/swagger-ui.html",
            "/**/webjars/**"
    );

    public JWTGatewayFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "";

        // OPTIONS 요청은 CORS preflight로 인증 없이 허용
        if ("OPTIONS".equalsIgnoreCase(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // 화이트리스트 패턴에 매칭되면 JWT 검증 없이 통과
        boolean isWhitelisted = WHITELIST_PATTERNS.stream()
                .anyMatch(pattern -> matcher.match(pattern, requestPath))
                || ("/sponsor/v1/categories".equals(requestPath) && "GET".equalsIgnoreCase(method))
                || ("/sponsor/v1/products".equals(requestPath) && "GET".equalsIgnoreCase(method))
                || (matcher.match("/sponsor/v1/products/*", requestPath) && "GET".equalsIgnoreCase(method));

        if (isWhitelisted) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        String authorizationHeader = headers.getFirst("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return GatewayResponseUtil.unauthorized(exchange, ErrorStatus._NULL_ACCESS_TOKEN);
        }
        String accessToken = authorizationHeader.substring("Bearer ".length());

        log.debug("AccessToken: {}", accessToken);

        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            return GatewayResponseUtil.unauthorized(exchange, ErrorStatus._EXFIRED_ACCESS_TOKEN);
        } catch (SignatureException e) {
            return GatewayResponseUtil.unauthorized(exchange, ErrorStatus._INVALID_ACCESS_TOKEN);
        } catch (JwtException e) {
            return GatewayResponseUtil.unauthorized(exchange, ErrorStatus._INVALID_ACCESS_TOKEN);

        }

        String category = jwtUtil.getCategory(accessToken);
        if (!"access".equals(category)) {
            return GatewayResponseUtil.unauthorized(exchange, ErrorStatus._NOTFOUND_ACCESS_TOKEN);
        }

        // 헤더에 loginId 추가
        Long memberId = jwtUtil.getMemberId(accessToken);
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .header("X-Authorization-Id", String.valueOf(memberId))
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());

    }

    @Override
    public int getOrder() {
        return -100;
    }
}
