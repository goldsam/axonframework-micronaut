package org.github.goldsam.axonframework.micronaut.modeling.saga.annotation;

import io.micronaut.context.annotation.Prototype;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;



/**
 * Annotation that informs Micronaut that a given type is a Saga instance type.
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Prototype
public @interface Saga {
    /**
     * Selects the qualifier name of the {@link org.axonframework.modelling.saga.repository.SagaStore} 
     * bean. If left empty the saga will be stored in the 
     * {@link org.axonframework.modelling.saga.repository.SagaStore} configured in the
     * global Axon Configuration.
     * @return qualifier name of the {@link org.axonframework.modelling.saga.repository.SagaStore} bean. 
     */
    String sagaStore() default "";
}