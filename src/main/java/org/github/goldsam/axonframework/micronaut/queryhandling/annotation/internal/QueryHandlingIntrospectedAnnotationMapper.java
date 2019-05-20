package org.github.goldsam.axonframework.micronaut.queryhandling.annotation.internal;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.inject.annotation.TypedAnnotationMapper;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.Collections;
import java.util.List;
import org.github.goldsam.axonframework.micronaut.queryhandling.annotation.QueryHandling;

/**
 *
 */
public class QueryHandlingIntrospectedAnnotationMapper implements TypedAnnotationMapper<QueryHandling> {

    @Override
    public Class<QueryHandling> annotationType() {
        return QueryHandling.class;
    }

    @Override
    public List<AnnotationValue<?>> map(AnnotationValue<QueryHandling> annotation, VisitorContext visitorContext) {
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
