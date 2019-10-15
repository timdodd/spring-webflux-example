package com.acme.controller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acme.model.Foo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/test")
public class TestController {

	private List<Foo> fakeData = new ArrayList<>();

	{
		fakeData.add(new Foo("Foo 1"));
		fakeData.add(new Foo("Foo 2"));
		fakeData.add(new Foo("Foo 3"));
		fakeData.add(new Foo("Foo 4"));
		fakeData.add(new Foo("Foo 5"));
	}

	@GetMapping(value = "/flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Foo> flux() {
		return Flux.fromIterable(fakeData).delayElements(Duration.ofSeconds(2));
	}

	@GetMapping(value = "/mono", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Mono<Foo> mono() {
		return Mono.just(new Foo("Foo Mono")).delayElement(Duration.ofSeconds(5));
	}

}