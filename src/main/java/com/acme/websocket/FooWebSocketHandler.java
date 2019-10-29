package com.acme.websocket;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FooWebSocketHandler implements WebSocketHandler {

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(final WebSocketSession webSocketSession) {
		Flux<CustomMessageDto> flux = Flux.create(sink -> {
			webSocketSession.receive()
				.map(WebSocketMessage::getPayloadAsText)
				.map(this::toObject)
				.subscribe(value -> {
					Integer nextValue = value.getValue() * 2;
					sink.next(new CustomMessageDto().setValue(nextValue));
				});
			});

		return webSocketSession.send(flux
				.map(this::toString)
				.map(webSocketSession::textMessage));
	}

	private CustomMessageDto toObject(String value) {
		try {
			return objectMapper.readValue(value, CustomMessageDto.class);
		} catch (IOException e) {
			return null;
		}
	}

	private String toString(CustomMessageDto value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (IOException e) {
			return null;
		}
	}

	public static class CustomMessageDto {

		private Integer value;

		public Integer getValue() {
			return value;
		}

		public CustomMessageDto setValue(Integer value) {
			this.value = value;
			return this;
		}
	}
}
