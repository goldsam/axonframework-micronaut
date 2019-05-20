
package org.github.goldsam.axonframework.micronaut.messaging.annotation;


import io.micronaut.aop.Adapter;
import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Type;
import io.micronaut.core.annotation.Indexed;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;
import javax.inject.Singleton;
import org.axonframework.messaging.MessageHandler;

/**
 *
 */
@Documented
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE, TYPE})
@Introduction
@Indexed(MessageHandler.class)
@Singleton
public @interface MessageHandling {
}
