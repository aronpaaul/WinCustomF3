package ru.paul.wincustomf3.command;

import com.velocitypowered.api.command.SimpleCommand;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MainCommand extends BaseCommand {

    private static final String usage = "/wincustomf3 reload";

    private final Map<String, CommandArgument> commandArguments;

    public MainCommand(final List<CommandArgument> commandArguments) {
        this.commandArguments = new LinkedHashMap<>();

        for (final CommandArgument commandArgument : commandArguments) {
            this.commandArguments.put(commandArgument.name(), commandArgument);
        }
    }

    @Override
    public void execute(final Invocation invocation) {
        final String[] arguments = invocation.arguments();
        if (arguments.length == 0) {
            sendUsage(invocation, usage);
            return;
        }

        final CommandArgument commandArgument = commandArguments.get(arguments[0].toLowerCase());
        if (commandArgument == null) {
            sendUsage(invocation, usage);
            return;
        }

        if (!invocation.source().hasPermission(commandArgument.permission())) {
            sendNoPermission(invocation, commandArgument.permission());
            return;
        }

        commandArgument.execute(invocation);
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        final String[] arguments = invocation.arguments();
        if (arguments.length == 0) {
            return availableArguments(invocation);
        }

        if (arguments.length == 1) {
            return filterSuggestions(availableArguments(invocation), arguments[0]);
        }

        final CommandArgument commandArgument = commandArguments.get(arguments[0].toLowerCase());
        if (commandArgument == null || !invocation.source().hasPermission(commandArgument.permission())) {
            return List.of();
        }

        return commandArgument.suggest(invocation);
    }

    private List<String> availableArguments(final SimpleCommand.Invocation invocation) {
        return commandArguments.values().stream()
                .filter(commandArgument -> invocation.source().hasPermission(commandArgument.permission()))
                .map(CommandArgument::name)
                .toList();
    }
}

