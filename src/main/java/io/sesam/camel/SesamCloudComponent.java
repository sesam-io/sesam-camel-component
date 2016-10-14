package io.sesam.camel;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link SesamCloudEndpoint}.
 */
public class SesamCloudComponent extends UriEndpointComponent {

    public SesamCloudComponent() {
        super(SesamCloudEndpoint.class);
    }

    public SesamCloudComponent(CamelContext context) {
        super(context, SesamCloudEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        SesamCloudEndpoint endpoint = new SesamCloudEndpoint(uri, this);
        setProperties(endpoint, parameters);
        endpoint.setPipe(remaining);
        return endpoint;
    }
}
