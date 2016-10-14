package io.sesam.camel;

import java.io.InputStreamReader;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * The Sesam consumer.
 */
public class SesamConsumer extends ScheduledPollConsumer {
    private final SesamEndpoint endpoint;
    private final Gson gson = new Gson();
    private String since = null;

    public SesamConsumer(SesamEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        String consumerUrl = endpoint.createConsumerUrl(since);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(consumerUrl);
        HttpResponse response = client.execute(get);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new RuntimeException(String.format("Unable to read endpoint '%s', status was '%s'", consumerUrl, statusCode));
        }

        JsonReader reader = new JsonReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        reader.beginArray();
        int processed = 0;
        while (reader.hasNext()) {
            JsonObject entity = gson.fromJson(reader, JsonObject.class);
            since = entity.getAsJsonPrimitive("_updated").getAsString();
            Exchange exchange = endpoint.createExchange();
            exchange.getIn().setBody(entity);
            try {
                // send message to next processor in the route
                getProcessor().process(exchange);
                processed++;
            } finally {
                // log exception if an exception occurred and was not handled
                if (exchange.getException() != null) {
                    getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                }
            }
        }
        reader.endArray();
        reader.close();
        return processed;
    }
}
