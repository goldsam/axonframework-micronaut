package org.github.goldsam.axonframework.micronaut.queryhandling.annotation;

import io.micronaut.context.annotation.Type;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import static java.lang.annotation.RetentionPolicy.*;
import javax.inject.Singleton;
import org.github.goldsam.axonframework.micronaut.queryhandling.annotation.internal.QueryHandlingAdapterIntoduction;

        
@Documented
@Retention(RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
//@Introduction(interfaces = QueryHandlingAdapterIntoduction.class)
@Type(QueryHandlingAdapterIntoduction.class) 
@Singleton
public @interface QueryHandling {
}

