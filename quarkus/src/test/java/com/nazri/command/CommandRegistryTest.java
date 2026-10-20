package com.nazri.command;

import jakarta.enterprise.inject.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandRegistryTest {

    @Mock
    private Instance<Command> commandInstances;

    @Mock
    private Command startCommand;

    @Mock
    private Command convertCommand;

    private CommandRegistry commandRegistry;

    @BeforeEach
    void setUp() {
        commandRegistry = new CommandRegistry();
        commandRegistry.commandInstances = commandInstances;
    }

    @Test
    void init_ShouldPopulateCommandsMapFromInjectedInstances() {
        when(startCommand.getName()).thenReturn("START");
        when(convertCommand.getName()).thenReturn("convert");

        doAnswer(invocation -> {
            Consumer<Command> consumer = invocation.getArgument(0);
            Arrays.asList(startCommand, convertCommand).forEach(consumer);
            return null;
        }).when(commandInstances).forEach(any());

        commandRegistry.init();

        assertEquals(startCommand, commandRegistry.getCommand("start"));
        assertEquals(startCommand, commandRegistry.getCommand("START"));
        assertEquals(convertCommand, commandRegistry.getCommand("convert"));
        assertNull(commandRegistry.getCommand("nonexistent"));
    }

    @Test
    void getCommand_ShouldReturnNull_WhenCommandNotFound() {
        assertNull(commandRegistry.getCommand("unknown"));
    }
}
