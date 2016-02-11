package com.georgepapas.pact

import au.com.dius.pact.consumer.PactVerified$
import au.com.dius.pact.consumer.VerificationResult
import au.com.dius.pact.consumer.groovy.PactBodyBuilder
import au.com.dius.pact.consumer.groovy.PactBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

import java.util.regex.Matcher

class FooClientPactTest {

  private FooServiceClient client

  @Before
  void setup() {
    client = new FooServiceClient()
  }

  @Ignore
  @Test
  void "request to create a foo"() {

    PactBuilder fooService = new PactBuilder()

    Foo foo = new Foo(priceAsCents: '1000')

    fooService {
      serviceConsumer 'TestCaseConsumer'
      hasPactWith 'FooService'
      port 1234

      uponReceiving('a create foo request')
      withAttributes(
              method: 'post',
              path: '/foo',
              headers: ['Accept': regexp(~/.*application\/json.*/, 'application/json')]
      )
      withBody {
        priceAsCents regexp(~/\d{4}/, foo.priceAsCents)
      }
      willRespondWith(
              status: 202,
              headers: ['Content-Type': regexp(~/.*application\/json.*/, 'application/json')]
      )
      withBody {
        status 'hello foo'
      }
    }

    VerificationResult result = fooService.run {
      ResponseEntity<FooResponse> response = client.createFoo(foo)
      assert response.statusCode == HttpStatus.ACCEPTED
    }

    assert result == PactVerified$.MODULE$
  }


  @Ignore
  @Test()
  void "request to create another foo"() {

    PactBuilder fooService = new PactBuilder()

    AnotherFoo anotherFoo = new AnotherFoo(priceInCents: '1000')

    PactBodyBuilder bodyBuilder = new PactBodyBuilder()
    bodyBuilder.priceInCents = '1000'

    fooService {
      serviceConsumer 'TestCaseConsumer'
      hasPactWith 'FooService'
      port 1234

      uponReceiving('a create another foo request')
      withAttributes(
              method: 'post',
              path: '/anotherFoo',
              body: bodyBuilder,
              headers: ['Accept': regexp(~/.*application\/json.*/, 'application/json')],
      )
      withBody {
        priceInCents "1000"
      }
      willRespondWith(
              status: 202,
              headers: ['Content-Type': 'application/json']
      )
      withBody {
        status 'hello foo'
      }
    }

    VerificationResult result = fooService.run {
      ResponseEntity<FooResponse> response = client.createAnotherFoo(anotherFoo)
      assert response.statusCode == HttpStatus.ACCEPTED
    }

    assert result == PactVerified$.MODULE$
  }


  @Test
  void "consumer with alpha"() {

    def alice_service = new PactBuilder() // Create a new PactBuilder
    alice_service {
      serviceConsumer "Consumer"  // Define the service consumer by name
      hasPactWith "Alice Service"   // Define the service provider that it has a pact with
      port 1234                       // The port number for the service. It is optional, leave it out to
      // to use a random one

      given('there is some good mallory') // defines a provider state. It is optional.
      uponReceiving('a retrieve Mallory request') // upon_receiving starts a new interaction
      withAttributes(
              method: 'post',
              path: '/mallory',
              headers: ['Accept': regexp(~/.*application\/json.*/, 'application/json')]
      )     // define the request, a GET request to '/mallory'
      withBody {
        priceInCents 'xx'
      }
      willRespondWith(                        // define the response we want returned
              status: 200,
              headers: ['Content-Type': regexp(~/.*application\/json.*/, 'application/json')],
              body: '"That is some good Mallory."'
      )
    }

    // Execute the run method to have the mock server run.
    // It takes a closure to execute your requests and returns a Pact VerificationResult.
    VerificationResult result = alice_service.run() {
      def client = new RestTemplate()
      ResponseEntity<String> alice_response = client.postForEntity('http://localhost:1234/mallory', new AnotherFoo(priceInCents: 'xx'), String)

      assert alice_response.statusCode.value() == 200

      def data = alice_response.body
      assert data == '"That is some good Mallory."'
    }
    assert result == PactVerified$.MODULE$  // This means it is all good in weird Scala speak.

  }

  @Test
  void 'assert regex matches'() {
    Matcher matcher = 'application/json;charset=UTF-8' =~ /.*application\/json.*/
    assert matcher.matches()
  }

  @Test
  void "consumer with digits"() {

    def alice_service = new PactBuilder() // Create a new PactBuilder
    alice_service {
      serviceConsumer "Consumer"  // Define the service consumer by name
      hasPactWith "Alice Service"   // Define the service provider that it has a pact with
      port 1234                       // The port number for the service. It is optional, leave it out to
      // to use a random one

      given('there is some good mallory') // defines a provider state. It is optional.
      uponReceiving('a retrieve Mallory request') // upon_receiving starts a new interaction
      withAttributes(
              method: 'post',
              path: '/mallory',
              headers: ['Accept': regexp(~'.*application/json.*', 'application/json')]
      )
      withBody {
        priceInCents 'x0x'
      }
      willRespondWith(                        // define the response we want returned
              status: 200,
              headers: ['Content-Type': regexp(~'.*application/json.*', 'application/json')],
              body: '"That is some good Mallory."'
      )
    }

    // Execute the run method to have the mock server run.
    // It takes a closure to execute your requests and returns a Pact VerificationResult.
    VerificationResult result = alice_service.run() {
      def client = new RestTemplate()
      ResponseEntity<String> alice_response = client.postForEntity('http://localhost:1234/mallory', new AnotherFoo(priceInCents: 'x0x'), String)

      assert alice_response.statusCode.value() == 200

      def data = alice_response.body
      assert data == '"That is some good Mallory."'
    }
    assert result == PactVerified$.MODULE$  // This means it is all good in weird Scala speak.

  }

}
