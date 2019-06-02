/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.github.goldsam.axonframework.micronaut.eventhandling.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import javax.inject.Singleton;

/**
 *
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Singleton
public @interface EventHandling {
    
}

