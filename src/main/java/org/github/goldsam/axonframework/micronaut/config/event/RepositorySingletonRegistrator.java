package org.github.goldsam.axonframework.micronaut.config.event;

import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.event.ApplicationEventListener;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.modelling.command.Repository;

/**
 * Trac
 */
public class RepositorySingletonRegistrator implements ApplicationEventListener<AxonConfigurationStartedEvent> {
    private final Map<Qualifier<Repository>, AggregateConfiguration<?>> repositoryRegistrations;
    
    public RepositorySingletonRegistrator(Map<Qualifier<Repository>, AggregateConfiguration<?>> repositoryRegistrations) {
        this.repositoryRegistrations = repositoryRegistrations;
    }
            
    private BeanContext beanContext;

    @Override
    public void onApplicationEvent(AxonConfigurationStartedEvent event) {
        registerRepositorySingletons();
    }
    
    private void registerRepositorySingletons() {
        for (Map.Entry<Qualifier<Repository>, AggregateConfiguration<?>> registration : repositoryRegistrations.entrySet()) {
            AggregateConfiguration<?> configuration = registration.getValue(); 
            beanContext.registerSingleton(Repository.class, configuration.repository(), registration.getKey(), true);
        }
        
        repositoryRegistrations.clear();
    }
    
    @Inject
    public void setBeanContext(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
}
