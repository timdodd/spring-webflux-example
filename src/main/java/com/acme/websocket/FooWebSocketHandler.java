package com.acme.websocket;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

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
		
		Flux<String> heartbeat = Flux.interval(Duration.ofSeconds(4)).map(time -> "heartbeat");
		
		Flux<String> multipler = Flux.create(sink -> {
			webSocketSession.receive()
				.map(WebSocketMessage::getPayloadAsText)
				.map(this::toObject)
				.delayElements(Duration.ofSeconds(2L))
				.subscribe(value -> {
					Integer nextValue = value.getValue() * 2;
					
					Optional.of(nextValue)
						.map(CustomMessageDto::new)
						.map(this::toString)
						.ifPresent(sink::next);
				});
			});

		return webSocketSession.send(Flux.merge(heartbeat, multipler)
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
		
		public CustomMessageDto() {
			
		}
		
		public CustomMessageDto(Integer value) {
			this.value = value;
		}

		public Integer getValue() {
			return value;
		}

		public CustomMessageDto setValue(Integer value) {
			this.value = value;
			return this;
		}
	}
}
