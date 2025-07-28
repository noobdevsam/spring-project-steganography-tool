package com.example.springprojectsteganographytool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public JettyServletWebServerFactory jettyFactory() {
        var factory = new JettyServletWebServerFactory();
        factory.addServerCustomizers(server -> {
            server.setHandler(server.getHandler()); // Retain existing handler
            server.addBean(
                    Executors.newVirtualThreadPerTaskExecutor()
            );
        });
        return factory;
    }

    @Bean
    public ExecutorService virtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

}


/*
* Usage Example:
*
@Service
public class MyService {

    private final ExecutorService virtualThreadExecutor;

    public MyService(ExecutorService virtualThreadExecutor) {
        this.virtualThreadExecutor = virtualThreadExecutor;
    }

    public void runTask() {
        virtualThreadExecutor.submit(() -> {
            // Your parallel logic here
            System.out.println("Running in virtual thread: " + Thread.currentThread());
        });
    }
}
* */