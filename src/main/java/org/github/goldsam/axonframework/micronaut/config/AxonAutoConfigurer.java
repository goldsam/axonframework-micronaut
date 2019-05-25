package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configurer;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.saga.ResourceInjector;
import org.github.goldsam.axonframework.micronaut.config.event.AxonConfigurationStartedEvent;
import org.github.goldsam.axonframework.micronaut.config.event.RepositorySingletonRegistrator;
import org.github.goldsam.axonframework.micronaut.eventsourcing.PrototypeBeanAggregateFactory;
import org.github.goldsam.axonframework.micronaut.eventsourcing.annotation.Aggregate;


@Factory
public class AxonAutoConfigurer implements ApplicationEventListener<AxonConfigurationStartedEvent>{
    private final BeanContext beanContext;
    
    public AxonAutoConfigurer(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
//    
//    @Bean
//    public <T> AggregateFactory<T> aggregateFactory(Class<T> aggregateType, Ag) { 
//        
//    }
    
    @PostConstruct
    void init() {
        Configurer configurer = DefaultConfigurer.defaultConfiguration();
        
    }
    
    @Singleton
    public Configurer configurer() {
         Configurer configurer = DefaultConfigurer.defaultConfiguration(false);
         configurer.configureResourceInjector( c -> beanContext.getBean(ResourceInjector.class));
         
         registerAggregateBeanDefinitions(configurer);
         
         return configurer;                
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
                aggregateConf.configureCommandTargetResolver(c -> 
                        beanContext.getBean(CommandTargetResolver.class, Qualifiers.byName(commandTargetResolverName)));
            } else if (beanContext.containsBean(CommandTargetResolver.class)) {
                aggregateConf.configureCommandTargetResolver(c -> beanContext.getBean(CommandTargetResolver.class));
            }
            
            configurer.configureAggregate(aggregateConf);
        }
        
        beanContext.registerSingleton(new RepositorySingletonRegistrator(repositoryRegistrations));
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
//    
//        String repositoryName = lcFirst(aggregateType.getSimpleName()) + "Repository";
//                
//                if (beanFactory.containsBean(repositoryName)) {
//                    
//                } else {
//                    registry.registerBeanDefinition(repositoryName,
//                                                    genericBeanDefinition(RepositoryFactoryBean.class)
//                                                            .addConstructorArgValue(aggregateConf)
//                                                            .getBeanDefinition());
//
//                    if (!registry.isBeanNameInUse(factoryName)) {
//                        registry.registerBeanDefinition(factoryName,
//                                                        genericBeanDefinition(SpringPrototypeAggregateFactory.class)
//                                                                .addConstructorArgValue(aggregate)
//                                                                .getBeanDefinition());
//                    }
//                    aggregateConf
//                            .configureAggregateFactory(c -> beanFactory.getBean(factoryName, AggregateFactory.class));
//                    String triggerDefinition = aggregateAnnotation.snapshotTriggerDefinition();
//                    if (!"".equals(triggerDefinition)) {
//                        aggregateConf.configureSnapshotTrigger(
//                                c -> beanFactory.getBean(triggerDefinition, SnapshotTriggerDefinition.class));
//                    }
//                    if (AnnotationUtils.isAnnotationPresent(aggregateType, "javax.persistence.Entity")) {
//                        aggregateConf.configureRepository(
//                                c -> GenericJpaRepository.builder(aggregateType)
//                                        .parameterResolverFactory(c.parameterResolverFactory())
//                                        .handlerDefinition(c.handlerDefinition(aggregateType))
//                                        .lockFactory(c.getComponent(
//                                                LockFactory.class, () -> NullLockFactory.INSTANCE
//                                        ))
//                                        .entityManagerProvider(c.getComponent(
//                                                EntityManagerProvider.class,
//                                                () -> beanFactory.getBean(EntityManagerProvider.class)
//                                        ))
//                                        .eventBus(c.eventBus())
//                                        .repositoryProvider(c::repository)
//                                        .build());
//                    }
//                }
//    }

    @Override
    public void onApplicationEvent(AxonConfigurationStartedEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
