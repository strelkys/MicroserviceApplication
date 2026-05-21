package ru.streltsov.microserviceapplication.apigateway.config;

import java.util.List;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThickServiceLoadBalancerConfig {

    @Bean
    public ServiceInstanceListSupplier thickServiceInstanceSupplier() {
        return new StaticServiceInstanceListSupplier(
                "thick-service",
                List.of(
                        new DefaultServiceInstance(
                                "thick-service-1",
                                "thick-service",
                                "localhost",
                                8085,
                                false
                        )
                )
        );
    }
}
