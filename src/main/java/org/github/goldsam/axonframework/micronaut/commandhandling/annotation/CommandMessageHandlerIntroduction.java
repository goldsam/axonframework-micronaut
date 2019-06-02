package org.github.goldsam.axonframework.micronaut.commandhandling.annotation;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Singleton;
import org.axonframework.commandhandling.AnnotationCommandHandlerAdapter;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandMessageHandler;
import org.axonframework.messaging.annotation.ClasspathHandlerDefinition;
import org.axonframework.messaging.annotation.ClasspathParameterResolverFactory;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryHandlerAdapter;
import org.axonframework.queryhandling.annotation.AnnotationQueryHandlerAdapter;


/**
 * {@link MethodInterceptor} which introduces an implementation of the 
 * {@link CommandMessageHandler} interface when a class is annotated
 * with {@link CommandHandling}.
 */
@Singleton
@Internal
public class CommandMessageHandlerIntroduction implements MethodInterceptor<Object, Object>{
    
    private final ConcurrentMap<Object, CommandMessageHandler> commandMessageHandlers = new ConcurrentHashMap<>();

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        switch (context.getMethodName()) {
            case "supportedCommandNames":
                return resolveQueryHandlerAdapter(context.getTarget())
                        .supportedCommandNames();
            case "canHandle":
                return resolveQueryHandlerAdapter(context.getTarget())
                        .canHandle((CommandMessage<?>)context.getArguments()[0]);
            case "handle":
                try {
                    return resolveQueryHandlerAdapter(context.getTarget())
                            .handle((CommandMessage<?>)context.getArguments()[0]);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to hanle message.", ex);
                }
            case "getTargetType":
                return resolveQueryHandlerAdapter(context.getTarget())
                        .getTargetType();
            default:
                return context.proceed();
        }
   }
    
    private CommandMessageHandler resolveQueryHandlerAdapter(Object target) {
        return commandMessageHandlers.computeIfAbsent(target, t -> {
            Class<?> targetClass = target.getClass();
            ParameterResolverFactory parameterResolverFactory = ClasspathParameterResolverFactory.forClass(targetClass);
            HandlerDefinition handlerDefinition = ClasspathHandlerDefinition.forClass(targetClass);
            return new AnnotationCommandHandlerAdapter(target, parameterResolverFactory, handlerDefinition);  
        });
    } 
}
