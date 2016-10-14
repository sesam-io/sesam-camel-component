package io.sesam.camel;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class SesamLocalComponentTest extends CamelTestSupport {
    @Test
    public void testSesam() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                from("sesam-local://out?ignoreSinceFile=true")
                  .to("sesam-local://in")
                  .to("mock:result");
            }
        };
    }
}
