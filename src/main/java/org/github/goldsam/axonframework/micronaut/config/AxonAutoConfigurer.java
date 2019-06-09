package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.transaction.NoTransactionManager;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.saga.ResourceInjector;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.queryhandling.LoggingQueryInvocationErrorHandler;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryInvocationErrorHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.serialization.Serializer;
import org.github.goldsam.axonframework.micronaut.eventhandling.annotation.EventHandling;
import org.github.goldsam.axonframework.micronaut.eventsourcing.PrototypeBeanAggregateFactory;
import org.github.goldsam.axonframework.micronaut.eventsourcing.annotation.Aggregate;
import org.github.goldsam.axonframework.micronaut.modeling.saga.annotation.Saga;

@Factory
@Slf4j
public class AxonAutoConfigurer {
    
    private final BeanContext beanContext;

    public AxonAutoConfigurer(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
    
    @Singleton
    @Requires(missingBeans = CommandBus.class)
    public SimpleCommandBus commandBus(TransactionManager txManager, AxonConfiguration axonConfiguration) {
        SimpleCommandBus commandBus = SimpleCommandBus.builder()
                .transactionManager(txManager)
                .messageMonitor(axonConfiguration.messageMonitor(CommandBus.class, "commandBus"))
                .build();
        commandBus.registerHandlerInterceptor(
                new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders())
        );
        return commandBus;
    }
    
    @Singleton
    @Requires(missingBeans = {EventStorageEngine.class, EventBus.class})
    public SimpleEventBus eventBus(AxonConfiguration configuration) {
        return SimpleEventBus.builder()
                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                .build();
    }
    
    @Singleton
    @Requires(beans = QueryInvocationErrorHandler.class)
    @Requires(missingBeans = QueryBus.class)
    public SimpleQueryBus queryBusForQueryInvocationErrorHandler(AxonConfiguration axonConfiguration,
                                                                 TransactionManager transactionManager,
                                                                 QueryInvocationErrorHandler eh) {
        return SimpleQueryBus.builder()
                .messageMonitor(axonConfiguration.messageMonitor(QueryBus.class, "queryBus"))
                .transactionManager(transactionManager)
                .errorHandler(eh)
                .queryUpdateEmitter(axonConfiguration.getComponent(QueryUpdateEmitter.class))
                .build();
    }
    
    @Singleton
    @Requires(missingBeans = {QueryBus.class, QueryInvocationErrorHandler.class})
    public SimpleQueryBus queryBus(AxonConfiguration axonConfiguration, TransactionManager transactionManager) {
        return SimpleQueryBus.builder()
                .messageMonitor(axonConfiguration.messageMonitor(QueryBus.class, "queryBus"))
                .transactionManager(transactionManager)
                .errorHandler(axonConfiguration.getComponent(
                        QueryInvocationErrorHandler.class,
                        () -> LoggingQueryInvocationErrorHandler.builder().build()
                ))
                .queryUpdateEmitter(axonConfiguration.getComponent(QueryUpdateEmitter.class))
                .build();
    }
    
    @Singleton
    @Requires(missingBeans = EventProcessingConfigurer.class)
    public EventProcessingConfigurer eventProcessingConfigurer() {
        return new EventProcessingModule();
    }

    @Singleton
    @Primary
    @Requires(missingBeans = EventProcessingModule.class)
    public EventProcessingModule eventProcessingModule() {
        return new EventProcessingModule();
    }
    
    @Singleton
    @Requires(missingBeans = EventBus.class, beans = EventStorageEngine.class)
    public EmbeddedEventStore eventStore(EventStorageEngine storageEngine, AxonConfiguration configuration) {
        return EmbeddedEventStore.builder()
                .storageEngine(storageEngine)
                .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                .build();
    }
    
    @Singleton
    @Requires(missingBeans = TransactionManager.class)
    public TransactionManager axonTransactionManager() {
        return NoTransactionManager.INSTANCE;
    }
            
    @Singleton
    public Configurer configurer() {
        Configurer configurer = DefaultConfigurer.defaultConfiguration(false);
        configurer.configureResourceInjector(c -> beanContext.getBean(ResourceInjector.class));

        registerAggregateBeanDefinitions(configurer);
        
        configurer.registerComponent(ParameterResolverFactory.class, c -> 
                beanContext.getBean(ParameterResolverFactory.class));

        // TODO: Figure this out:
//        RuntimeBeanReference handlerDefinition =
//                SpringContextHandlerDefinitionBuilder.getBeanReference(registry);
//        configurer.registerHandlerDefinition((c, clazz) -> beanFactory
//                .getBean(handlerDefinition.getBeanName(), HandlerDefinition.class));

        if (beanContext.containsBean(CommandBus.class)) {
            configurer.configureCommandBus(c -> beanContext.getBean(CommandBus.class));
        }
        if (beanContext.containsBean(QueryBus.class)) {
            configurer.configureQueryBus(c -> beanContext.getBean(QueryBus.class));
        }
        if (beanContext.containsBean(QueryUpdateEmitter.class)) {
            configurer.configureQueryUpdateEmitter(c -> beanContext.getBean(QueryUpdateEmitter.class));
        }
        if (beanContext.containsBean(EventStorageEngine.class)) {
            configurer.configureEmbeddedEventStore(c -> beanContext.getBean(EventStorageEngine.class));
        }        
        if (beanContext.containsBean(EventBus.class)) {
            configurer.configureEventBus(c -> beanContext.getBean(EventBus.class));
        }
        if (beanContext.containsBean(Serializer.class)) {
            configurer.configureSerializer(c -> beanContext.getBean(Serializer.class));
        }
        Qualifier<Serializer> eventSerializerQualifier = Qualifiers.byName("eventSerializer");
        if (beanContext.containsBean(Serializer.class, eventSerializerQualifier)) {
            configurer.configureEventSerializer(c -> beanContext.getBean(Serializer.class, eventSerializerQualifier));
        }
        Qualifier<Serializer> messageSerializerQualifier = Qualifiers.byName("messageSerializer");
        if (beanContext.containsBean(Serializer.class, messageSerializerQualifier)) {
            configurer.configureMessageSerializer(c -> beanContext.getBean(Serializer.class, messageSerializerQualifier));
        }
        if (beanContext.containsBean(TokenStore.class)) {
            configurer.registerComponent(TokenStore.class, c -> beanContext.getBean(TokenStore.class));
        }
        
        try {
            EventProcessingConfigurer eventProcessingConfigurer = configurer.eventProcessing();
            registerSagaBeanDefinitions(eventProcessingConfigurer);
            registerEventHandlerRegistrar();
        } catch (AxonConfigurationException ace) {
            log.warn(
                    "There are several EventProcessingConfigurers registered, Axon will not automatically register sagas and event handlers.",
                    ace);
        }

        return configurer;
    }

    @SuppressWarnings("unchecked")
    private void registerSagaBeanDefinitions(EventProcessingConfigurer configurer) {
        Collection<BeanDefinition<?>> beanDefinitions = beanContext
                .getBeanDefinitions(Qualifiers.byAnnotation(AnnotationMetadata.EMPTY_METADATA, Saga.class));
        for (BeanDefinition<?> beanDefinition : beanDefinitions) {
            Class<?> sagaType = beanDefinition.getBeanType();

            beanDefinition.findAnnotation(ProcessingGroup.class).ifPresent(processingGroupAnnotation -> {
                String processingGroup = processingGroupAnnotation.getRequiredValue("value", String.class);
                if (StringUtils.isNotEmpty(processingGroup)) {
                    configurer.assignHandlerTypesMatching(processingGroup, sagaType::equals);
                }
            });

            configurer.registerSaga(sagaType, sagaConfigurer -> {
                beanDefinition.findAnnotation(Saga.class).ifPresent(sagaStoreAnnotation -> {
                    sagaStoreAnnotation.get("sagaStore", String.class).ifPresent(sagaStore ->
                            sagaConfigurer.configureSagaStore(c -> 
                                    beanContext.getBean(SagaStore.class, Qualifiers.byName(sagaStore))));
                });
            });
        }
    }

    private void registerEventHandlerRegistrar() {
        List<BeanDefinition<?>> eventHandlerBeanDefinitions = beanContext
                .getBeanDefinitions(Qualifiers.byStereotype(EventHandling.class))
                .stream()
                    .filter(bd -> bd.isSingleton())
                    .collect(Collectors.toList());
        beanContext.getBean(EventHandlerRegistrar.class)
                .setEventHandlerBeanDefinitions(eventHandlerBeanDefinitions);
    }
    
    @SuppressWarnings("unchecked")
    private void registerAggregateBeanDefinitions(Configurer configurer) {
        Collection<BeanDefinition<?>> beanDefinitions = beanContext
                .getBeanDefinitions(Qualifiers.byAnnotation(AnnotationMetadata.EMPTY_METADATA, Aggregate.class));
        Map<Qualifier<Repository>, AggregateConfiguration<?>> repositoryRegistrations = new HashMap<>();
        for (BeanDefinition<?> beanDefinition : beanDefinitions) {
            Class<?> aggregateType = beanDefinition.getBeanType();

            AnnotationValue<?> aggregateAnnotation = beanDefinition.getAnnotation(Aggregate.class);
            AggregateConfigurer<?> aggregateConf = AggregateConfigurer.defaultConfiguration(beanDefinition.getBeanType());
            final Optional<String> configuredRepositoryName = aggregateAnnotation.get("repository", String.class);
            if (!configuredRepositoryName.isPresent()) {
                String repositoryName = lcFirst(aggregateType.getSimpleName()) + "Repository";
                final Qualifier<Repository> repositoryBeanQualifier = Qualifiers.byName(repositoryName);
                if (beanContext.containsBean(Repository.class, repositoryBeanQualifier)) {
                    aggregateConf.configureRepository(c -> beanContext.getBean(Repository.class, repositoryBeanQualifier));
                } else {
                    String aggregateFactoryName = lcFirst(beanDefinition.getName()) + "AggregateFactory";
                    Qualifier<AggregateFactory> aggregateFactoryQualifier = Qualifiers.byName(aggregateFactoryName);
                    repositoryRegistrations.put(repositoryBeanQualifier, aggregateConf);
                    if (!beanContext.containsBean(AggregateFactory.class, aggregateFactoryQualifier)) {
                        beanContext.registerSingleton(AggregateFactory.class, new PrototypeBeanAggregateFactory<>(aggregateType), aggregateFactoryQualifier, true);
                    }

                    aggregateConf.configureAggregateFactory(c -> beanContext.getBean(AggregateFactory.class, aggregateFactoryQualifier));
                    
                    final Optional<String> configuredSnapshotTriggerDefinition = 
                            aggregateAnnotation.get("snapshotTriggerDefinition", String.class);
                    configuredSnapshotTriggerDefinition.ifPresent(std ->
                            aggregateConf.configureSnapshotTrigger(c ->
                                    beanContext.getBean(SnapshotTriggerDefinition.class, Qualifiers.byName(std))));
                }
            } else {
                aggregateConf.configureRepository(
                        c -> beanContext.getBean(Repository.class, Qualifiers.byName(configuredRepositoryName.get())));
            }

            Optional<String> commandTargetResolverName = aggregateAnnotation.get("commandTargetResolver", String.class);
            if (commandTargetResolverName.isPresent()) {
                aggregateConf.configureCommandTargetResolver(c
                        -> beanContext.getBean(CommandTargetResolver.class, Qualifiers.byName(commandTargetResolverName.get())));
            } else if (beanContext.containsBean(CommandTargetResolver.class)) {
                aggregateConf.configureCommandTargetResolver(c -> beanContext.getBean(CommandTargetResolver.class));
            }

            configurer.configureAggregate(aggregateConf);
        }

        beanContext.getBean(RepositoryRegistrar.class).setRepositoryRegistrations(repositoryRegistrations);
    }

    /**
     * Return the given {@code string}, with its first character lowercase
     *
     * @param string The input string
     * @return The input string, with first character lowercase
     */
    private String lcFirst(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }
}
