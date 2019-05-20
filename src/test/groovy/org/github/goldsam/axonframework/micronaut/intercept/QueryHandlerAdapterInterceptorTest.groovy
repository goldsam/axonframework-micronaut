//package org.github.goldsam.axonframework.micronaut.intercept;
//
//import io.micronaut.annotation.processing.test.AbstractTypeElementSpec;
//import io.micronaut.aop.MethodInvocationContext;
//import io.micronaut.context.ApplicationContext
//import io.micronaut.inject.BeanDefinition
//import io.micronaut.inject.BeanFactory
//import java.io.ByteArrayOutputStream
//import java.io.IOException
//import java.io.InputStream
//import javax.inject.Singleton
//import org.axonframework.queryhandling.QueryHandler
//import spock.lang.Specification
//import org.github.goldsam.axonframework.micronaut.annotation.QueryHandlerAdapter
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import static org.junit.Assert.*
//
///**
// *
// */
//public class QueryHandlerAdapterInterceptorTest extends AbstractTypeElementSpec {
//
//
//  void "test that ... "() {
//    when:
//      BeanDefinition beanDefinition = buildBeanDefinition('test.Lookups', '''
//package test;
//
//import javax.inject.Singleton;
//import org.axonframework.queryhandling.QueryHandler;
//import org.github.goldsam.axonframework.micronaut.annotation.QueryHandlerAdapter;
//
//@Singleton
//@QueryHandlerAdapter
//class Lookups {
//
//    @QueryHandler
//    public long lookupValue(String value) {
//        return value.hashCode();
//    }
//}
//      ''')
//    then:
//      beanDefinition != null
//      !beanDefinition.isAbstract()
//
////    beanDefinition.postConstructMethods.size() == 1
////    beanDefinition.preDestroyMethods.size() == 1
//
//    when:
//      def context = ApplicationContext.run()
//      def instance = ((BeanFactory) beanDefinition).build(context, beanDefinition)
//
//    then:
//      instance instanceof QueryHandlerAdapter
//
//    cleanup:
//      context.close()
//  }
//}
