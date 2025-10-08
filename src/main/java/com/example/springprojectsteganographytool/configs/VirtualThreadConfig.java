package com.example.springprojectsteganographytool.configs;

import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/**
 * Configuration class for setting up a Jetty web server with virtual thread support.
 * This configuration customizes the Jetty server to use a virtual thread-per-task executor.
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * Creates and configures a `JettyServletWebServerFactory` bean.
     * This factory customizes the Jetty server to use a virtual thread-per-task executor,
     * which allows handling tasks using lightweight virtual threads.
     *
     * @return a configured `JettyServletWebServerFactory` instance.
     */
    @Bean
    public JettyServletWebServerFactory jettyFactory() {
        var factory = new JettyServletWebServerFactory();

        // Add a server customizer to configure the Jetty server
        factory.addServerCustomizers(server -> {
            server.setHandler(server.getHandler()); // Retain the existing handler
            server.addBean(
                    Executors.newVirtualThreadPerTaskExecutor() // Add a virtual thread-per-task executor
            );
        });
        return factory;
    }

}