package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.ModuleConfiguration;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.saga.ResourceInjector;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.queryhandling.DefaultQueryGateway;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.github.goldsam.axonframework.micronaut.config.event.AxonConfigurationStartedEvent;
import org.github.goldsam.axonframework.micronaut.config.event.AxonConfigurationShutdownEvent;

/**
 *
 */
@Factory
public class AxonConfiguration implements Configuration {
    
    private final Configurer configurer; 
    private Configuration config;
    
    private ApplicationContext applicationContext;
    
    @Inject
    public AxonConfiguration(Configurer configurer) {
        this.configurer = configurer;
    }
    
    @Inject
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public CommandBus commandBus() {
        return config.commandBus();
    }

    @Override
    public QueryBus queryBus() {
        return config.queryBus();
    }

    @Override
    public EventBus eventBus() {
        return config.eventBus();
    }

    @Override
    public QueryUpdateEmitter queryUpdateEmitter() {
        return config.queryUpdateEmitter();
    }

    @Bean
    @Primary
    public QueryBus defaultQueryBus() {
        return config.queryBus();
    }

    @Bean
    @Primary
    public QueryUpdateEmitter defaultQueryUpdateEmitter() {
        return config.queryUpdateEmitter();
    }

    @Bean
    @Primary
    public CommandBus defaultCommandBus() {
        return commandBus();
    }

    @Bean
    @Primary
    public EventBus defaultEventBus() {
        return eventBus();
    }
    
    
    @Override
    public ResourceInjector resourceInjector() {
        return config.resourceInjector();
    }

    @Override
    public EventProcessingConfiguration eventProcessingConfiguration() {
        return config.eventProcessingConfiguration();
    }
    
    /**
     * Returns the CommandGateway used to send commands to command handlers.
     *
     * @param commandBus the command bus to be used by the gateway
     * @return the CommandGateway used to send commands to command handlers
     */
    @Bean
    @Primary
    public CommandGateway commandGateway(CommandBus commandBus) {
        return DefaultCommandGateway.builder().commandBus(commandBus).build();
    }

    @Bean
    @Primary
    public QueryGateway queryGateway(QueryBus queryBus) {
        return DefaultQueryGateway.builder().queryBus(queryBus).build();
    }
    
    @Override
    public <T> Repository<T> repository(Class<T> aggregateType) {
        return config.repository(aggregateType);
    }

    @Override
    public <T> T getComponent(Class<T> componentType, Supplier<T> defaultImpl) {
        return config.getComponent(componentType, defaultImpl);
    }

    @Override
    public <M extends Message<?>> MessageMonitor<? super M> messageMonitor(Class<?> componentType, String componentName) {
        return config.messageMonitor(componentType, componentName);
    }

    @Override
    public Serializer eventSerializer() {
        return config.eventSerializer();
    }

    @Override
    public Serializer messageSerializer() {
        return config.messageSerializer();
    }

    @Override
    public List<CorrelationDataProvider> correlationDataProviders() {
        return config.correlationDataProviders();
    }

    @Override
    public HandlerDefinition handlerDefinition(Class<?> inspectedType) {
        return config.handlerDefinition(inspectedType);
    }
    
    @Override
    public EventUpcasterChain upcasterChain() {
        return config.upcasterChain();
    }
    
    @Override
    public List<ModuleConfiguration> getModules() {
        return config.getModules();
    }
    
    @Override
    public void onStart(int i, Runnable r) {
        config.onStart(i, r);
    }

    @Override
    public void onShutdown(int i, Runnable r) {
        config.onShutdown(i, r);
    }
    
    @Override
    public void start() {
        config.start();
        applicationContext.publishEvent(new AxonConfigurationStartedEvent(this));
    }

    @PreDestroy
    @Override
    public void shutdown() {
        config.shutdown();
        applicationContext.publishEvent(new AxonConfigurationShutdownEvent(this));
    }
    
    @PostConstruct
    void initialize() {
        this.config = configurer.buildConfiguration();
    }
}