package org.github.goldsam.axonframework.micronaut.queryhandling.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Indexed;
import org.axonframework.queryhandling.QueryHandlerAdapter;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Singleton;

/**
 * Introduces an implementation of the {@link QueryHandlerAdapter} interface 
 * when applied to a singleton concrete class. All instance methods annotated 
 * with {@link org.axonframework.queryhandling.QueryHandler} are registered  
 * to the {@link org.axonframework.queryhandling.QueryBus} passed to
 * {@link org.axonframework.queryhandling.QueryHandlerAdapter#subscribe(org.axonframework.queryhandling.QueryBus)}.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Introduction(interfaces = QueryHandlerAdapter.class)
@Type(QueryHandlerAdapterIntoduction.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Indexed(QueryHandlerAdapter.class)
@Singleton
public @interface QueryHandling {
}