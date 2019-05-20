package org.github.goldsam.axonframework.micronaut.queryhandling;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryHandlerAdapter;
import org.axonframework.queryhandling.annotation.AnnotationQueryHandlerAdapter;

/**
 *
 */
@Singleton
public class QueryHandlerAdapterInterceptor implements MethodInterceptor<Object, Object>  {
    
    private final Map<Object, AnnotationQueryHandlerAdapter<?>> adapters = new ConcurrentHashMap<>();
    
    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Class<?> declaringType = context.getDeclaringType();
        final Object target = context.getTarget();
            
        
        if (QueryHandlerAdapter.class == declaringType) {
            AnnotationQueryHandlerAdapter<?> adapter = resolveAdapter(target);
            QueryBus queryBus = (QueryBus)context.getParameterValues()[0];
            return adapter.subscribe(queryBus); 
        } else if (Closeable.class == declaringType || AutoCloseable.class == declaringType) {
            adapters.remove(target);
            return null;
        }
        
        return context.proceed();
   }
    
    private AnnotationQueryHandlerAdapter<?> resolveAdapter(Object target) {
        return adapters.computeIfAbsent(target, t -> {
            Class<?> targetClass = target.getClass();
            ParameterResolverFactory parameterResolverFactory = ClasspathParameterResolverFactory.forClass(targetClass);
            HandlerDefinition handlerDefinition = ClasspathHandlerDefinition.forClass(targetClass);
            return new AnnotationQueryHandlerAdapter<>(t, parameterResolverFactory, handlerDefinition);
        });
    }
}
