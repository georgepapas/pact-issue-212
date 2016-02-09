package com.georgepapas.pact

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate

class FooServiceClient {

  private final RestTemplate restTemplate = new RestTemplate()

  private String server = 'http://localhost:1234/'

  private ObjectMapper objectMapper

  FooServiceClient() {
    this.objectMapper = Jackson2ObjectMapperBuilder.json().build()
  }

  ResponseEntity<FooResponse> createFoo(Foo foo) {
    restTemplate.postForEntity("${server}/foo", foo, FooResponse)
  }

  ResponseEntity<FooResponse> createAnotherFoo(AnotherFoo anotherFoo) {
    restTemplate.postForEntity("${server}/anotherFoo", anotherFoo, FooResponse)
  }

}
