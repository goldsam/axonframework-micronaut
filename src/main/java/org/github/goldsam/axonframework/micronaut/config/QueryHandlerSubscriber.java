package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Context;
import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryHandlerAdapter;

/**
 * Registers beans that implement {@link QueryHandlerAdapter} with the query bus.
 */
@Context
@Slf4j
public class QueryHandlerSubscriber {
    
    private final BeanLocator beanLocator;
    private QueryBus queryBus;
    private Collection<QueryHandlerAdapter> queryHandlers;
    
    public QueryHandlerSubscriber(BeanLocator beanLocator, QueryBus queryBus) {
        this.beanLocator = beanLocator;
    }
    
    @Inject
    public void setQueryBus(QueryBus queryBus) {
        this.queryBus = queryBus;
    }
    
    @Inject    
    public void setQueryHandlers(Collection<QueryHandlerAdapter> queryHandlers) {
        this.queryHandlers = queryHandlers;
    }
    
    @PostConstruct
    void init() {
        log.debug("Registering QueryHandlerAdapter beans with the QueryBus.");
                
        if (queryBus == null) {
            queryBus = beanLocator.getBean(QueryBus.class);
        }
        
        if (queryHandlers == null) {
            queryHandlers = beanLocator.getBeansOfType(QueryHandlerAdapter.class );
        }
        
        queryHandlers.forEach(queryHandler -> queryHandler.subscribe(queryBus));
    }
}
