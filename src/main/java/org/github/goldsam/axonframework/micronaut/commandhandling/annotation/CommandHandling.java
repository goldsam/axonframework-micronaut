package org.github.goldsam.axonframework.micronaut.commandhandling.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Indexed;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.axonframework.commandhandling.CommandMessageHandler;

/**
 * Introduces an implementation of the {@link CommandMessageHandler} interface 
 * when applied to a type. 
 * All instance methods annotated with {@link org.axonframework.queryhandling.EnableQueryHandling} 
 * are registered to the {@link org.axonframework.queryhandling.QueryBus} passed to
 * {@link org.axonframework.queryhandling.QueryHandlerAdapter#subscribe(org.axonframework.queryhandling.QueryBus)}.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Introduction(interfaces = CommandMessageHandler.class)
@Type(CommandMessageHandlerIntroduction.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Indexed(CommandMessageHandler.class)
public @interface CommandHandling {
}

 