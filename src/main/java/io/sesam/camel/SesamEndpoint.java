package io.sesam.camel;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * Represents a Sesam endpoint.
 */
public abstract class SesamEndpoint extends DefaultEndpoint {
    @UriPath
    @Metadata(required = "true")
    private String pipe;

    @UriParam(defaultValue = "null")
    private String initialSince = null;

    public SesamEndpoint() {
    }

    public SesamEndpoint(String uri, Component component) {
        super(uri, component);
    }

    public SesamEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public final Producer createProducer() throws Exception {
        return new SesamProducer(this);
    }

    public final Consumer createConsumer(Processor processor) throws Exception {
        return new SesamConsumer(this, processor);
    }

    public final boolean isSingleton() {
        return true;
    }

    protected abstract String createConsumerUrl(String since);

    protected abstract String createProducerUrl();

    public void setPipe(String pipe) {
        this.pipe = pipe;
    }

    /**
     * Name of pipe inside this sesam instance
     *
     * @return the name of the pipe
     */
    public String getPipe() {
        return pipe;
    }

    /**
     * Initial value of since. If not set the stream starts reading from
     * the beginning without passing a since value.
     *
     * @return the initial value of since
     */
    public String getInitialSince() {
        return initialSince;
    }

    public void setInitialSince(String initialSince) {
        this.initialSince = initialSince;
    }
}
