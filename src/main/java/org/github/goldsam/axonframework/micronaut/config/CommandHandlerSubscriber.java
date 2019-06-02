package org.github.goldsam.axonframework.micronaut.config;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Context;
import java.util.Collection;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessageHandler;

/**
 *
 */
@Context
@Slf4j
public class CommandHandlerSubscriber {

    private final BeanLocator beanLocator;
    private CommandBus commandBus;
    private Collection<CommandMessageHandler> commandHandlers;

    public CommandHandlerSubscriber(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Inject
    public void setQueryBus(CommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @Inject
    public void setQueryHandlers(Collection<CommandMessageHandler> commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    @PostConstruct
    void initialize() {
        log.debug("Registering CommandMessageHandler beans with the CommandBus.");

        if (commandBus == null) {
            commandBus = beanLocator.getBean(CommandBus.class);
        }

        if (commandHandlers == null) {
            commandHandlers = beanLocator.getBeansOfType(CommandMessageHandler.class);
        }

        commandHandlers.forEach(commandHandler -> {
            commandHandler.supportedCommandNames().forEach((commandName) -> {
                commandBus.subscribe(commandName, commandHandler);
            });
        });
    }
}
