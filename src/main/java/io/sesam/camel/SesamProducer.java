package io.sesam.camel;

import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * The Sesam producer.
 */
public class SesamProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SesamProducer.class);
    private final SesamEndpoint endpoint;
    private final Gson gson = new Gson();

    public SesamProducer(SesamEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        String producerUrl = endpoint.createProducerUrl();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(producerUrl);
        post.setEntity(new StringEntity(gson.toJson(Arrays.asList(exchange.getIn().getBody()))));
        post.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new RuntimeException(String.format("Failed to post to '%s', status was '%s'", producerUrl, statusCode));
        }
    }

}
