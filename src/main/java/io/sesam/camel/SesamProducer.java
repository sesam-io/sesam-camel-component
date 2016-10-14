package io.sesam.camel;

import java.io.IOException;
import java.util.UUID;

import org.apache.camel.CamelException;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The Sesam producer.
 */
public class SesamProducer extends DefaultProducer {
    private static final Logger LOG = LoggerFactory.getLogger(SesamProducer.class);
    private final SesamEndpoint endpoint;
    private final Gson gson = new Gson();
    private final JsonParser jsonParser = new JsonParser();

    public SesamProducer(SesamEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        Object messageBody = exchange.getIn().getBody();
        try {
            doProcess(exchange, messageBody);
        } catch (Exception e) {
            exchange.setException(e);
        }
    }

    private void doProcess(Exchange exchange, Object messageBody) throws CamelException, IOException {
        String producerUrl = endpoint.createProducerUrl();
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(producerUrl);
        if (messageBody == null) {
            LOG.debug("Message body was null in exchange %s", exchange.getExchangeId());
            return;
        }
        JsonElement parsedBody = parseBody(messageBody);
        JsonObject entity = convertToEntity(parsedBody);
        if (entity.has("_id")) {
            JsonElement idElement = entity.get("_id");
            if (!idElement.isJsonPrimitive() && !idElement.getAsJsonPrimitive().isString()) {
                throw new CamelException("Message body attribute '_id' must be a string but was: " + idElement.getClass());
            }
        } else {
            String id = exchange.getIn().getHeader(endpoint.getIdHeader(), null, String.class);
            if (id == null) {
                if (!endpoint.isGenerateId()) {
                    throw new RuntimeException("Could not find id in message header and endpoint is not configured to generate ids");
                }
                id = UUID.randomUUID().toString();
            }
            LOG.debug("Found entity id: %s for exchange: %s", id, exchange.getExchangeId());
            entity.addProperty("_id", id);
        }
        post.setEntity(new StringEntity(gson.toJson(wrapInArray(entity))));
        post.setHeader("Content-type", "application/json");
        HttpResponse response = httpClient.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new RuntimeException(String.format("Failed to post to '%s', status was '%s'", producerUrl, statusCode));
        }
    }

    private JsonArray wrapInArray(JsonObject entity) {
        JsonArray entityArray = new JsonArray();
        entityArray.add(entity);
        return entityArray;
    }

    private JsonObject convertToEntity(JsonElement parsedBody) {
        if (!parsedBody.isJsonObject()) {
            JsonObject entity = new JsonObject();
            entity.add(endpoint.getBodyAttributeName(), parsedBody);
            return entity;
        } else {
            return parsedBody.getAsJsonObject();
        }
    }

    private JsonElement parseBody(Object messageBody) throws CamelException {
        if (messageBody instanceof JsonObject) {
            return (JsonObject) messageBody;
        } else if (messageBody instanceof byte[]) {
            return tryParse(new String((byte[]) messageBody));
        } else if (messageBody instanceof String) {
            return tryParse((String) messageBody);
        } else {
            throw new CamelException("Type of message body must be a string or byte[] and be valid json but was: " + messageBody.getClass());
        }
    }

    private JsonElement tryParse(String json) {
        try {
            return jsonParser.parse(json);
        } catch (JsonParseException e) {
            throw new RuntimeException("Message body is not valid json", e);
        }
    }
}
