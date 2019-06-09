package org.github.goldsam.axonframework.micronaut.queryhandling.annotation

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.writer.BeanDefinitionVisitor
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.BeanFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Singleton
import org.axonframework.queryhandling.QueryHandlerAdapter
import spock.lang.Specification
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

/**
 *
 */
public class QueryHandlingAdapterIntoductionSpec extends AbstractTypeElementSpec {

    void "test that @QueryHandling introduces the QueryHandlerAdapter interface to a concrete class. "() {
        when:
        BeanDefinition beanDefinition = buildBeanDefinition('test.Thing' + BeanDefinitionVisitor.PROXY_SUFFIX, '''
package test;

import javax.inject.Singleton;
import org.axonframework.queryhandling.QueryHandler;
import org.github.goldsam.axonframework.micronaut.queryhandling.annotation.QueryHandling;
import org.axonframework.queryhandling.QueryHandlerAdapter;

@Singleton
@QueryHandling
class Thing {
    
    @QueryHandler
    public long lookupValue(String value) {
        return value.hashCode();
    }
}''')
        then:
        beanDefinition != null
        !beanDefinition.isAbstract()

        when:
        def context = ApplicationContext.run()
        def instance = ((BeanFactory) beanDefinition).build(context, beanDefinition)

        then:
        instance instanceof QueryHandlerAdapter

        cleanup:
        context.close()
    }
}
