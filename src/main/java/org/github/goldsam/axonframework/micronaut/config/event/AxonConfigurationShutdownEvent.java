package org.github.goldsam.axonframework.micronaut.config.event;

import org.axonframework.config.Configuration;

/**
 *
 */
public class AxonConfigurationShutdownEvent extends AxonConfigurationEvent {
    public AxonConfigurationShutdownEvent(Configuration configuration) {
        super(configuration);
    }
}
