package ru.paul.wincustomf3.command;

import com.velocitypowered.api.command.SimpleCommand;
import java.util.List;

public interface CommandArgument {

    String name();

    String permission();

    void execute(SimpleCommand.Invocation invocation);

    default List<String> suggest(final SimpleCommand.Invocation invocation) {
        return List.of();
    }
}

