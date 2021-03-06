package au.com.dius.pact.provider.broker

import au.com.dius.pact.provider.ConsumerInfo
import groovy.json.JsonSlurper
import groovy.transform.Canonical
import groovyx.net.http.HTTPBuilder

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.PUT

/**
 * Client for the pact broker service
 */
@Canonical
class PactBrokerClient {

  String pactBrokerUrl
  Map options = [:]

  List fetchConsumers(String provider) {
    List consumers = []

    HalClient halClient = new HalClient(pactBrokerUrl, options)
    halClient.navigate('pb:latest-provider-pacts', provider: provider).pacts { pact ->
      consumers << new ConsumerInfo(pact.name, new URL(pact.href))
    }

    consumers
  }

  def uploadPactFile(File pactFile, String version) {
    def pact = new JsonSlurper().parse(pactFile)
    def http = new HTTPBuilder(pactBrokerUrl)
    def response = http.request(PUT) {
      uri.path = "/pacts/provider/${pact.provider.name}/consumer/${pact.consumer.name}/version/$version"
      requestContentType = JSON
      body = pactFile.text
    }
    response.statusLine
  }
}
