package org.github.goldsam.axonframework.micronaut.messaging.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collections;
import java.util.List;
import org.github.goldsam.axonframework.micronaut.messaging.annotation.MessageHandling;

/**
 *
 */
public class MessageHandlingIntrospectedAnnotationMapper implements TypedAnnotationMapper<MessageHandling> {

    @Override
    public Class<MessageHandling> annotationType() {
        return MessageHandling.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<MessageHandling> annotation, VisitorContext visitorContext) {
        final AnnotationValueBuilder<Introspected> builder = AnnotationValue.builder(Introspected.class)
                // we are interested in event, command, and query handlers.
                .member("includedAnnotations", 
                    // query handlers
                    "org.axonframework.queryhandling.QueryHandler");
        return Collections.singletonList(
                builder.build()
        );   
    }
}
