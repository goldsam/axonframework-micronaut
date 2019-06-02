package org.github.goldsam.axonframework.micronaut.eventsourcing.annotation;

import io.micronaut.context.annotation.Prototype;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import org.axonframework.modelling.command.AggregateRoot;

/**
 * Annotation that informs Micronaut that a given type is an Aggregate instance type.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Prototype
@AggregateRoot
public @interface Aggregate {
    
    /**
     * Selects the qualifier name name of the aggregate 
     * {@link org.axonframework.modelling.command.Repository} bean. If left 
     * empty a new repository is created. In that case the name of the 
     * repository will be based on the simple name of the aggregate's class.
     * @return the qualifier name name of the aggregate 
     * {@link org.axonframework.modelling.command.Repository} bean.
     */
    String repository() default "";
    
    /**
     * Selects the qualifier name of the 
     * {@link org.axonframework.modelling.command.CommandTargetResolver} bean. 
     * If left empty, the 
     * {@link org.axonframework.modelling.command.CommandTargetResolver} bean 
     * from the {@link io.micronaut.context.ApplicationContext} will be used. 
     * If the bean is not defined in the application context, 
     * {@link AnnotationCommandTargetResolver} will be used.
     * @return 
     */
    String commandTargetResolver() default "";
}
