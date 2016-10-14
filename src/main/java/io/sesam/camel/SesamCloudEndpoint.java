package io.sesam.camel;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * Represents a Sesam endpoint in the cloud. Can be used as producer and consumer. Consumer is a scheduled polling consumer.
 */
@UriEndpoint(scheme = "sesam", title = "Sesam", syntax="sesam:pipe", consumerClass = SesamConsumer.class, label = "Sesam")
public class SesamCloudEndpoint extends SesamEndpoint {
    @UriParam
    private String subscription;
    @UriParam
    private String service;

    // TODO security parameters

    public SesamCloudEndpoint() {
    }

    public SesamCloudEndpoint(String uri, Component component) {
        super(uri, component);
    }

    public SesamCloudEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected String createConsumerUrl(String since) {
        throw new UnsupportedOperationException("Cloud consumer not implemented yet");
    }

    @Override
    protected String createProducerUrl() {
        throw new UnsupportedOperationException("Cloud producer not implemented yet");
    }

    /**
     * Subscription ID to connect to.
     *
     * @return the subscription id
     */
    public String getSubscription() {
        return subscription;
    }

    /**
     * Service instance ID to connect to.
     *
     * @return the service instance id
     */
    public String getService() {
        return service;
    }
}
