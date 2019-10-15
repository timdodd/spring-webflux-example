package com.acme.websocket;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.acme.model.Foo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FooWebSocketHandler implements WebSocketHandler {

    private static final ObjectMapper json = new ObjectMapper();

    private Flux<String> eventFlux = Flux.generate(sink -> {
        Foo event = new Foo(UUID.randomUUID().toString());
        try {
            sink.next(json.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            sink.error(e);
        }
    });

    private Flux<String> intervalFlux = Flux.interval(Duration.ofMillis(1000L))
      .zipWith(eventFlux, (time, event) -> event);

    @Override
    public Mono<Void> handle(final WebSocketSession webSocketSession) {

        return webSocketSession.send(intervalFlux.map(webSocketSession::textMessage))
          .and(webSocketSession.receive().map(WebSocketMessage::getPayloadAsText).log());
    }
}
