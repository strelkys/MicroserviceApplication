package ru.streltsov.microserviceapplication.apigateway.config;

import java.util.List;
import reactor.core.publisher.Flux;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

public class StaticServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private final String serviceId;
    private final List<ServiceInstance> instances;

    public StaticServiceInstanceListSupplier(String serviceId, List<ServiceInstance> instances) {
        this.serviceId = serviceId;
        this.instances = instances;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        return Flux.just(instances);
    }
}
