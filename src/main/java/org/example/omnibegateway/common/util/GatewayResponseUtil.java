package org.example.omnibegateway.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.omnibegateway.common.apiPayload.ApiResult;
import org.example.omnibegateway.common.apiPayload.code.status.ErrorStatus;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class GatewayResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Mono<Void> unauthorized(ServerWebExchange exchange, ErrorStatus errorStatus) {

        exchange.getResponse().setStatusCode(errorStatus.getHttpStatus());
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResult<?> apiResult = ApiResult.onFailure(
                errorStatus.getCode(),
                errorStatus.getMessage(),
                null
        );

        String resultJson;
        try {
            resultJson = objectMapper.writeValueAsString(apiResult);
        } catch (JsonProcessingException e) {
            resultJson = "{\"success\":false,\"code\":\"COMMON500\",\"message\":\"JSON 직렬화 오류\",\"data\":null}";
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(resultJson.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

}
