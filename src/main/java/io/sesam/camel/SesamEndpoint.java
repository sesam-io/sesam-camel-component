package io.sesam.camel;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

public abstract class SesamEndpoint extends DefaultEndpoint {
    @UriPath
    @Metadata(required = "true")
    private String pipe;

    @UriParam(label="consumer", defaultValue = "null")
    private String initialSince = null;

    @UriParam(label="producer", defaultValue = "body")
    private String bodyAttributeName = "body";

    @UriParam(label="producer", defaultValue = "id")
    private String idHeader = "id";

    @UriParam(label="producer", defaultValue = "true")
    private boolean generateId = true;

    @UriParam(label="producer", defaultValue = "false")
    private boolean overrideId = false;

    @UriParam(label="consumer", defaultValue = "null")
    private String sinceFile = null;

    @UriParam(label="consumer", defaultValue = "false")
    private boolean ignoreSinceFile = false;

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

    /**
     * Name of entity attribute to store the message body in case the message is not a JSON object
     *
     * @return name of attribute where message body is stored
     */
    public String getBodyAttributeName() {
        return bodyAttributeName;
    }

    public void setBodyAttributeName(String bodyAttributeName) {
        this.bodyAttributeName = bodyAttributeName;
    }

    /**
     * Message header that contains the value to be used as "_id". If header is empty an id will be generated.
     *
     * @return name of header that contains id
     */
    public String getIdHeader() {
        return idHeader;
    }

    public void setIdHeader(String idHeader) {
        this.idHeader = idHeader;
    }

    /**
     * If "_id" should be automatically generated in case message header that should contain the id is empty.
     *
     * @return true if we allow generation of random entity ids, false otherwise
     */
    public boolean isGenerateId() {
        return generateId;
    }

    public void setGenerateId(boolean generateId) {
        this.generateId = generateId;
    }

    /**
     * If "_id" of message could be overwritten if it is not a json primite
     * @return
     */
    public boolean isOverrideId() {
        return overrideId;
    }

    public void setOverrideId(boolean overrideId) {
        this.overrideId = overrideId;
    }

    /**
     * The the file where the consumers will store the latest since value. The consumer need to store this value
     * otherwise you will end up reading the same entities again if you restart Camel (and the consumer).
     *
     * @return the path to the file
     */
    public String getSinceFile() {
        return sinceFile;
    }

    public void setSinceFile(String sinceFile) {
        this.sinceFile = sinceFile;
    }

    public String getActualSinceFile() {
        if (getSinceFile() != null) {
            return getSinceFile();
        } else {
            return String.format(".sesam-since-%s.txt", getEndpointUri());
        }
    }

    /**
     * If the persistent since file should be ignored or not. Added for testing purposes.
     *
     * @return true if since should should be ignored, false otherwise
     */
    public boolean isIgnoreSinceFile() {
        return ignoreSinceFile;
    }

    public void setIgnoreSinceFile(boolean ignoreSinceFile) {
        this.ignoreSinceFile = ignoreSinceFile;
    }
}
