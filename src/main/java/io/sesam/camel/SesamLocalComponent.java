package io.sesam.camel;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import org.apache.camel.impl.UriEndpointComponent;

/**
 * Represents the component that manages {@link SesamLocalEndpoint}.
 */
public class SesamLocalComponent extends UriEndpointComponent {
    
    public SesamLocalComponent() {
        super(SesamLocalEndpoint.class);
    }

    public SesamLocalComponent(CamelContext context) {
        super(context, SesamLocalEndpoint.class);
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        SesamLocalEndpoint endpoint = new SesamLocalEndpoint(uri, this);
        setProperties(endpoint, parameters);
        endpoint.setPipe(remaining);
        return endpoint;
    }
}
