package io.sesam.camel;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

/**
 * The Sesam consumer.
 */
public class SesamConsumer extends ScheduledPollConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(SesamConsumer.class);
    private final SesamEndpoint endpoint;
    private final Gson gson = new Gson();
    private RandomAccessFile sinceFile;
    private FileLock lock;
    private String since = null;


    public SesamConsumer(SesamEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }


    @Override
    protected void doStop() throws Exception {
        super.doStop();
        try {
            writeSinceFile();
        } finally {
            LOG.debug("Releasing since lock");
            lock.release();
        }
        sinceFile.close();
    }

    private void writeSinceFile() throws IOException {
        LOG.debug("Writing since value %s for endpoint %s", since, endpoint.getEndpointUri());
        sinceFile.seek(0);
        sinceFile.write(since.getBytes());
    }

    @Override
    public void doStart() throws Exception {
        LOG.debug("Reading since value for endpoint %s", endpoint.getEndpointUri());
        File file = new File(URLEncoder.encode(endpoint.getActualSinceFile(), "utf-8"));
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new RuntimeException("Unable to create since file " + file);
            }
        }
        sinceFile = new RandomAccessFile(file, "rw");
        FileChannel channel = sinceFile.getChannel();
        lock = channel.tryLock();
        if (lock == null) {
            throw new RuntimeException("Unable to lock since file: " + file + ", maybe you need to specify the sinceFile in the endpoint?");
        }
        LOG.debug("Acquired since lock");
        if (sinceFile.length() > 0) {
            LOG.debug("Found since file %s with value %s", sinceFile, since);
            if (!endpoint.isIgnoreSinceFile()) {
                since = sinceFile.readLine();
            } else {
                LOG.info("Found since file %s with value %s, but endpoint is configured to ignore this value", sinceFile, since);
            }
        } else {
            LOG.debug("Found empty since file %s", sinceFile);
        }
        super.doStart();
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
            exchange.getIn().setBody(gson.toJson(entity));
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
        writeSinceFile();
        reader.endArray();
        reader.close();
        return processed;
    }
}
