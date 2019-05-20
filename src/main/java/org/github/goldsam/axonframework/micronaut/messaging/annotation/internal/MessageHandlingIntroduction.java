package org.github.goldsam.axonframework.micronaut.messaging.annotation.internal;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryHandlerAdapter;

/**
 *
 */
public class MessageHandlingIntroduction implements MethodInterceptor<Object,Object> {
    
    ConcurrentHashMap<Object, QueryHandlerAdapter> queryHandlerAdapters = new ConcurrentHashMap<>();
        
    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Class<?> declaringType = context.getDeclaringType();
        final Object target = context.getTarget();
            
        if (QueryHandlerAdapter.class == declaringType) {
            QueryHandlerAdapter adapter = resolveQueryHandlerAdapter(target);
            QueryBus queryBus = (QueryBus)context.getParameterValues()[0];
            return adapter.subscribe(queryBus); 
        } else if (Closeable.class == declaringType || AutoCloseable.class == declaringType) {
            remove(target);
            return null;
        }
        
        return context.proceed();
    }
    
    private void remove(Object target) {
        queryHandlerAdapters.remove(target);
    }
    
    private QueryHandlerAdapter resolveQueryHandlerAdapter(Object target) {
        return queryHandlerAdapters.computeIfAbsent(target, t -> {
            ParameterResolverFactory prf = resolveParameterResolverFactory(target);

            
            return null;
        });
    }
    
    private ParameterResolverFactory resolveParameterResolverFactory(Object target) {
        Class<?> targetType = target.getClass();
        return ClasspathParameterResolverFactory.forClass(targetType);
    }
}
 