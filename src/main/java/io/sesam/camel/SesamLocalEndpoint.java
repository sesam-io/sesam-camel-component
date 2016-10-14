package io.sesam.camel;

import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.http.client.utils.URIBuilder;

/**
 * Represents a Sesam endpoint on premise. Can be used as producer and consumer. Consumer is a scheduled polling consumer.
 */
@UriEndpoint(scheme = "sesam-local", title = "Sesam", syntax="sesam-local:pipe", consumerClass = SesamConsumer.class, label = "Sesam")
public class SesamLocalEndpoint extends SesamEndpoint {
    @UriParam(defaultValue = "9042")
    private int port = 9042;
    @UriParam(defaultValue = "localhost")
    private String host = "localhost";
    @UriParam(defaultValue = "/api")
    private String apiPath = "/api";
    @UriParam(defaultValue = "false")
    private boolean useHttps = false;

    // TODO add security parameters

    public SesamLocalEndpoint() {
    }

    public SesamLocalEndpoint(String uri, SesamLocalComponent component) {
        super(uri, component);
    }

    public SesamLocalEndpoint(String endpointUri) {
        super(endpointUri);
    }

    @Override
    protected String createProducerUrl() {
        URIBuilder url = createUrl(String.format("%s/receivers/%s/entities", getStrippedApiPath(), this.getPipe()));
        return url.toString();
    }

    @Override
    protected String createConsumerUrl(String since) {
        URIBuilder url = createUrl(String.format("%s/publishers/%s/entities", getStrippedApiPath(), this.getPipe()));
        if (since != null) {
            url.setParameter("since", since);
        } else if (getInitialSince() != null) {
            url.setParameter("since", getInitialSince());
        }
        return url.toString();
    }

    private URIBuilder createUrl(String path) {
        URIBuilder url = new URIBuilder();
        if (useHttps) {
            url.setScheme("https");
        } else {
            url.setScheme("http");
        }
        url.setHost(host);
        url.setPort(port);
        url.setPath(path);
        return url;
    }

    private String getStrippedApiPath() {
        if (apiPath.endsWith("/")) {
            // strip trailing slash to normalize the parameter
            return apiPath.substring(0, apiPath.length() - 1);
        }
        return apiPath;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public void setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
    }

    /**
     * Specify which port to connect to.
     *
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Specifiy which host to connect to.
     *
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Specify the path of the api base url in case you put Sesam behind a proxy.
     *
     * @return the api path
     */
    public String getApiPath() {
        return apiPath;
    }

    /**
     * Specify if you have secured Sesam behind HTTPS.
     *
     * @return if the connection should be made with HTTPS instead of HTTP
     */
    public boolean isUseHttps() {
        return useHttps;
    }
}
