package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.inject.BeanDefinition;
import java.util.Collection;
import javax.inject.Singleton;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.EventProcessingModule;
import org.github.goldsam.axonframework.micronaut.config.event.AxonConfigurationStartedEvent;

/**
 *
 * @author samgo
 */
@Singleton
public class EventHandlerRegistrar implements ApplicationEventListener<AxonConfigurationStartedEvent> {
            
    private final BeanContext beanContext;
    private final EventProcessingModule  eventProcessingModule;
    private final EventProcessingConfigurer eventProcessingConfigurer;
    
    private Collection<BeanDefinition<?>> eventHandlerBeanDefinitions;
            
    public EventHandlerRegistrar(BeanContext beanContext, EventProcessingModule eventProcessingModule, EventProcessingConfigurer eventProcessingConfigurer) {
        this.beanContext = beanContext;
        this.eventProcessingModule = eventProcessingModule;
        this.eventProcessingConfigurer = eventProcessingConfigurer;
    }
    
    public void setEventHandlerBeanDefinitions(Collection<BeanDefinition<?>> eventHandlerBeanDefinitions) {
        this.eventHandlerBeanDefinitions = eventHandlerBeanDefinitions;
    }

    @Override
    public void onApplicationEvent(AxonConfigurationStartedEvent event) {
        eventHandlerBeanDefinitions.forEach(bd -> 
                eventProcessingConfigurer.registerEventHandler(c -> beanContext.getBean(bd.getBeanType())));
        eventProcessingModule.initialize(event.getSource());
        start();
    }
    
    public void start() {
        eventProcessingModule.start();
    }
    
    public void stop() {
        eventProcessingModule.shutdown();
    }
}
