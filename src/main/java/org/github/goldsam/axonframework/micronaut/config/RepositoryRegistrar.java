package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEventListener;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.modelling.command.Repository;
import org.github.goldsam.axonframework.micronaut.config.event.AxonConfigurationStartedEvent;

/**
 */
@Singleton
public class RepositoryRegistrar implements ApplicationEventListener<AxonConfigurationStartedEvent> {
    private final BeanContext beanContext;

    private Map<Qualifier<Repository>, AggregateConfiguration<?>> repositoryRegistrations;
    
    @Inject
    public RepositoryRegistrar(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
            
    public void setRepositoryRegistrations(Map<Qualifier<Repository>, AggregateConfiguration<?>> repositoryRegistrations) {
        this.repositoryRegistrations = repositoryRegistrations;
    }
    
    @Override
    public void onApplicationEvent(AxonConfigurationStartedEvent event) {
        registerRepositorySingletons();
    }
    
    private void registerRepositorySingletons() {
        repositoryRegistrations.entrySet().forEach((registration) -> {
            AggregateConfiguration<?> configuration = registration.getValue(); 
            beanContext.registerSingleton(Repository.class, configuration.repository(), registration.getKey(), true);
        });
        
        repositoryRegistrations.clear();
    }
}
