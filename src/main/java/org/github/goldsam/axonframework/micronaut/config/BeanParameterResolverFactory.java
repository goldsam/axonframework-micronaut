package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.core.annotation.AnnotationValue;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.inject.Named;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.annotation.AnnotationUtils;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.ParameterResolver;
import org.axonframework.messaging.annotation.ParameterResolverFactory;

/**
 *
 */
@Slf4j
public class BeanParameterResolverFactory implements ParameterResolverFactory {
    
    private final BeanContext beanContext;
    
    public BeanParameterResolverFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }
    
    @Override
    public ParameterResolver createInstance(Executable executable, Parameter[] parameters, int parameterIndex) {
        if (beanContext == null) {
            return null;
        }
        Parameter parameter = parameters[parameterIndex];
        Class<?> parameterType = parameter.getType();
        Collection<BeanRegistration<?>> beansFound = (Collection<BeanRegistration<?>>)beanContext.getBeanRegistrations(parameterType);
        if (beansFound.isEmpty()) {
            return null;
        } else if (beansFound.size() > 1) {
            Optional<ParameterResolver> resolver = findQualifiedBean(beansFound, parameter);
            if (resolver.isPresent()) {
                return resolver.get();
            }
            if (log.isWarnEnabled()) {
                log.warn("{} beans of type {} found, but none was marked as primary and parameter lacks @Qualifier. Ignoring this parameter.",
                        beansFound.size(), parameterType.getSimpleName());
            }
            return null;
        } else {
            return new MicronautBeanParameterResolver(beansFound.iterator().next());
        }
    }
    
    private Optional<ParameterResolver> findQualifiedBean(Collection<BeanRegistration<?>> beansFound, Parameter parameter) {
        // find @Named matching candidate
        final Optional<Map<String, Object>> qualifier = AnnotationUtils.findAnnotationAttributes(parameter, Named.class);
        if (qualifier.isPresent()) {
            String qualifierName = (String) qualifier.get().get("qualifier");
            for (BeanRegistration<?> registration : beansFound) {
                AnnotationValue<Named> annotationValue = registration.getBeanDefinition().getAnnotation(Named.class);
                if (annotationValue != null && Objects.equals(qualifierName, annotationValue.get("value", String.class))) {
                    return Optional.of(new MicronautBeanParameterResolver(registration));
                }
            }
        }
        // find @Primary matching candidate
        for (BeanRegistration<?> registration : beansFound) {
            if (registration.getBeanDefinition().isPrimary()) {
                return Optional.of(new MicronautBeanParameterResolver(registration));
            }
        }
        return Optional.empty();
    }
    
    private static class MicronautBeanParameterResolver implements ParameterResolver<Object> {

        private final BeanRegistration<?> beanRegistration;
                
        public MicronautBeanParameterResolver(BeanRegistration<?> beanRegistration) {
            this.beanRegistration = beanRegistration;
        }
        
        @Override
        public Object resolveParameterValue(Message<?> arg0) {
            return beanRegistration.getBean();
        }

        @Override
        public boolean matches(Message<?> arg0) {
            return true;
        }
    }
}
