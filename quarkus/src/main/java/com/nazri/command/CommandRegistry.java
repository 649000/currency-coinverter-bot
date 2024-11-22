package com.nazri.command;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class CommandRegistry {
    private final Map<String, Command> commands = new HashMap<>();

    @Inject
    Instance<Command> commandInstances;

    @PostConstruct
    void init() {
        commandInstances.forEach(command ->
                commands.put(command.getName().toLowerCase(), command)
        );
    }

    public Command getCommand(String name) {
        return commands.get(name.toLowerCase());
    }
}
