package org.github.goldsam.axonframework.micronaut.config.event;

import io.micronaut.context.event.ApplicationEvent;
import org.axonframework.config.Configuration;

/**
 *
 */
public abstract class AxonConfigurationEvent extends ApplicationEvent {
    AxonConfigurationEvent(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Configuration getSource() {
        return (Configuration)super.getSource();
    }
}
