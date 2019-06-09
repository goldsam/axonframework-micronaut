package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.MultiParameterResolverFactory;
import org.axonframework.messaging.annotation.ParameterResolverFactory;

@Factory
public class ParameterResolverFactoryFactory {
    private final List<ParameterResolverFactory> factories = new ArrayList<>();
    private final BeanContext beanContext;
    
    public ParameterResolverFactoryFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
    
    @Singleton
    @Requires(missingBeans = ParameterResolverFactory.class)
    public ParameterResolverFactory ParameterResolverFactory() {
        return MultiParameterResolverFactory.ordered(factories);
    }

    /**
     * Defines any additional parameter resolver factories that need to be used to resolve parameters. By default,
     * the ParameterResolverFactories found on the classpath, as well as a BeanContextParameterResolverFactory are
     * registered.
     *
     * @param additionalFactories The extra factories to register
     * @see SpringBeanParameterResolverFactory
     * @see ClasspathParameterResolverFactory
     */
    public void setAdditionalFactories(List<ParameterResolverFactory> additionalFactories) {
        this.factories.addAll(additionalFactories);
    }

    @PostConstruct
    void init() {
        factories.add(ClasspathParameterResolverFactory.forClassLoader(beanContext.getClassLoader()));
        factories.add(new BeanContextParameterResolverFactory(beanContext));
    }
}
