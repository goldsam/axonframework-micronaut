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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.saga.ResourceInjector;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.github.goldsam.axonframework.micronaut.eventhandling.annotation.EventHandling;
import org.github.goldsam.axonframework.micronaut.eventsourcing.PrototypeBeanAggregateFactory;
import org.github.goldsam.axonframework.micronaut.eventsourcing.annotation.Aggregate;
import org.github.goldsam.axonframework.micronaut.modeling.saga.annotation.Saga;
import org.github.goldsam.axonframework.micronaut.queryhandling.annotation.QueryHandling;

@Factory
@Slf4j
public class AxonAutoConfigurer {
    
    private final BeanContext beanContext;

    public AxonAutoConfigurer(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
//    
//    @Bean
//    public <T> AggregateFactory<T> aggregateFactory(Class<T> aggregateType, Ag) { 
//        
//    }
    
    
    @Singleton
    public EventProcessingConfigurer eventProcessingConfigurer() {
        return new EventProcessingModule();
    }

    @PostConstruct
    void init() {
        Configurer configurer = DefaultConfigurer.defaultConfiguration();

    }
    
    @Singleton
    @Primary
    @Requires(missingBeans = EventProcessingModule.class)
    public EventProcessingModule eventProcessingModule() {
        return new EventProcessingModule();
    }

    @Singleton
    public Configurer configurer() {
        Configurer configurer = DefaultConfigurer.defaultConfiguration(false);
        configurer.configureResourceInjector(c -> beanContext.getBean(ResourceInjector.class));

        registerAggregateBeanDefinitions(configurer);
                
        
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

    public void registerEventHandlerRegistrar() {
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
            final String configuredRepositoryName = aggregateAnnotation.getRequiredValue("repository", String.class);
            if (StringUtils.isEmpty(configuredRepositoryName)) {
                String repositoryName = lcFirst(aggregateType.getSimpleName()) + "Repository";
                final Qualifier<Repository> repositoryBeanQualifier = Qualifiers.byName(repositoryName);
                if (beanContext.containsBean(Repository.class, repositoryBeanQualifier)) {
                    aggregateConf.configureRepository(c -> beanContext.getBean(Repository.class, repositoryBeanQualifier));
                } else {
                    String aggregateFactoryName = lcFirst(beanDefinition.getName()) + "AggregateFactory";
                    Qualifier<AggregateFactory> aggregateFactoryQualifier = Qualifiers.byName(aggregateFactoryName);
                    repositoryRegistrations.put(repositoryBeanQualifier, aggregateConf);
                    if (beanContext.containsBean(AggregateFactory.class, aggregateFactoryQualifier)) {
                        beanContext.registerSingleton(AggregateFactory.class, new PrototypeBeanAggregateFactory<>(aggregateType), aggregateFactoryQualifier, true);
                    }

                    aggregateConf.configureAggregateFactory(c -> beanContext.getBean(AggregateFactory.class, aggregateFactoryQualifier));
                }
            } else {
                aggregateConf.configureRepository(
                        c -> beanContext.getBean(Repository.class, Qualifiers.byName(configuredRepositoryName)));
            }

            String commandTargetResolverName = aggregateAnnotation.getRequiredValue("commandTargetResolver", String.class);
            if (StringUtils.isNotEmpty(commandTargetResolverName)) {
                aggregateConf.configureCommandTargetResolver(c
                        -> beanContext.getBean(CommandTargetResolver.class, Qualifiers.byName(commandTargetResolverName)));
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
