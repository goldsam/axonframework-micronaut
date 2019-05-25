package org.github.goldsam.axonframework.micronaut.config.event;

import org.axonframework.config.Configuration;

/**
 *
 */
public class AxonConfigurationStartedEvent extends AxonConfigurationEvent {
    public AxonConfigurationStartedEvent(Configuration configuration) {
        super(configuration);
    }
}
