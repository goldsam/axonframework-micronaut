package org.github.goldsam.axonframework.micronaut.modeling.saga;

import io.micronaut.context.BeanContext;
import javax.inject.Singleton;
import org.axonframework.modelling.saga.ResourceInjector;

/**
 *
 */
@Singleton
public class BeanContextResourceInjector implements ResourceInjector{

    private final BeanContext beanContext; 
    
    public BeanContextResourceInjector(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
    
    @Override
    public void injectResources(Object saga) {
        beanContext.inject(saga);
    }
}
