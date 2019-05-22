/*
 * Copyright 2019 samgo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.github.goldsam.axonframework.micronaut.queryhandling;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Context;
import java.util.Collection;
import javax.annotation.PostConstruct;
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
    
    public QueryHandlerSubscriber(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }
    
    public void setQueryBus(QueryBus queryBus) {
        this.queryBus = queryBus;
    }
        
    public void setQueryHandlers(Collection<QueryHandlerAdapter> queryHandlers) {
        this.queryHandlers = queryHandlers;
    }
    
    @PostConstruct
    void initialize() {
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
