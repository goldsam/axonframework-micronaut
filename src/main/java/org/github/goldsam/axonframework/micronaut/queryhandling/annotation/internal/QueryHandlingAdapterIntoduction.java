package org.github.goldsam.axonframework.micronaut.queryhandling.annotation.internal;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.axonframework.common.Registration;
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
public class QueryHandlingAdapterIntoduction implements MethodInterceptor<Object, Object>{
    
    private final ConcurrentMap<Object, QueryHandlerAdapter> queryHandlerAdapters = new ConcurrentHashMap<>();

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        Class<?> declaringType = context.getDeclaringType();
        if (QueryHandlerAdapter.class == declaringType) {
            QueryHandlerAdapter adapter = resolveQueryHandlerAdapter(context.getTarget());
            QueryBus queryBus = (QueryBus)context.getParameterValues()[0];
            return adapter.subscribe(queryBus); 
        } else {
            return context.proceed();
        }
   }
    
    private QueryHandlerAdapter resolveQueryHandlerAdapter(Object target) {
        return queryHandlerAdapters.computeIfAbsent(target, t -> {
            Class<?> targetClass = target.getClass();
            ParameterResolverFactory parameterResolverFactory = ClasspathParameterResolverFactory.forClass(targetClass);
            HandlerDefinition handlerDefinition = ClasspathHandlerDefinition.forClass(targetClass);
            return new AnnotationQueryHandlerAdapter<>(t, parameterResolverFactory, handlerDefinition);
        });
    } 
}
