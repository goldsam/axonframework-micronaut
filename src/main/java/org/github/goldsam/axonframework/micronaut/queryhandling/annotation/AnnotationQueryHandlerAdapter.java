package org.github.goldsam.axonframework.micronaut.queryhandling.annotation;

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import org.axonframework.common.Registration;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryHandlerAdapter;

/**
 *
 * @author samgo
 */
public class AnnotationQueryHandlerAdapter<T> implements QueryHandlerAdapter {
        
    private final T target;
    private final BeanIntrospection<T> beanIntrospection; 

    @SuppressWarnings("unchecked")
    public AnnotationQueryHandlerAdapter(T target, ParameterResolverFactory parameterResolverFactory) {
        beanIntrospection = BeanIntrospection.getIntrospection((Class<T>)target.getClass());
        this.target = target;
    }

    @Override
    public Registration subscribe(QueryBus qb) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
