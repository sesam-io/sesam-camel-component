Camel Component Project For Sesam
=================================

This project is a Camel Component for `Sesam <http://sesam.io>`_.

Only local (on premise) installations are currently supported. Cloud support is under construction, and will be using the url scheme ``sesam://<pipe>``.

Security tokens (JWT) are not yet implemented.

Since values are persisted in local files.

The url pattern (and defaults) for the local consumer endpoint is:
::

    sesam-local://<pipe-name>?useHttps=false&host=localhost&port=9042&apiPath=/api&initialSince=<blank>

And the url pattern (and defaults) for the local producer endpoint is:
::

    sesam-local://<pipe-name>?useHttps=false&host=localhost&port=9042&apiPath=/api

This example shows how you pull data from an http_endpoint sink, and push it back
to an http_endpoint source.

::

    return new RouteBuilder() {
            public void configure() {
                from("sesam-local://my-publisher-pipe")
                  .to("sesam-local://my-receiver-pipe");
            }
        };

To build this project first set up a Sesam on localhost:9042 then run:

::

    $ src/test/resources/prep.sh
    $ mvn install

Maven users can include this dependency with:

::

    <dependency>
        <groupId>io.sesam.camel</groupId>
        <artifactId>camel-sesam</artifactId>
        <version>0.1-SNAPSHOT</version>
    </dependency>

For more help see the `Apache Camel documentation <http://camel.apache.org/writing-components.html>`_.
    
