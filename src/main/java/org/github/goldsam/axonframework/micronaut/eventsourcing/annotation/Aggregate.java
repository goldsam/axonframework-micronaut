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
     * Selects the name of the AggregateRepository bean. If left empty a new repository is created. In that case the
     * name of the repository will be based on the simple name of the aggregate's class.
     */
    String repository() default "";
    
    /**
     * Selects the name of the {@link CommandTargetResolver} bean. If left empty,
     * {@link CommandTargetResolver} bean from application context will be used. If
     * the bean is not defined in the application context, {@link AnnotationCommandTargetResolver}
     * will be used.
     */
    String commandTargetResolver() default "";
    
}
