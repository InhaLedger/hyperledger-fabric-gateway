package com.inha.coinkaraoke.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class ExceptionHandler extends AbstractErrorWebExceptionHandler {


    /**
     * Create a new {@code AbstractErrorWebExceptionHandler}.
     *
     * @param g    the error attributes
     * @param serverCodecConfigurer codec configuration
     * @param applicationContext the application context
     * @since 2.4.0
     */
    public ExceptionHandler(GlobalErrorAttributes g, ApplicationContext applicationContext,
            ServerCodecConfigurer serverCodecConfigurer) {
        super(g, new WebProperties.Resources(), applicationContext);
        super.setMessageWriters(serverCodecConfigurer.getWriters());
        super.setMessageReaders(serverCodecConfigurer.getReaders());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @NonNull
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {

        final Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());

        return ServerResponse
                .status((Integer) errorPropertiesMap.getOrDefault("status", HttpStatus.BAD_REQUEST))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
