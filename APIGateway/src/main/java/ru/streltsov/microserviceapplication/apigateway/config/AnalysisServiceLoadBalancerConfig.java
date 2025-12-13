package ru.streltsov.microserviceapplication.apigateway.config;

import java.util.List;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalysisServiceLoadBalancerConfig {

    @Bean
    public ServiceInstanceListSupplier analysisServiceInstanceSupplier() {
        return new StaticServiceInstanceListSupplier(
                "analysis-service",
                List.of(
                        new DefaultServiceInstance(
                                "analysis-service-1",
                                "analysis-service",
                                "localhost",
                                8000,
                                false
                        ),
                        new DefaultServiceInstance(
                                "analysis-service-2",
                                "analysis-service",
                                "localhost",
                                8001,
                                false
                        )
                )
        );
    }
}
