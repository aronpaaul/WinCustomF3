package ru.paul.wincustomf3.command;

import com.velocitypowered.api.command.SimpleCommand;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BaseCommand implements SimpleCommand {

    protected void sendNoPermission(final Invocation invocation, final String permission) {
        invocation.source().sendRichMessage(
                "<red>Нет прав. Нужна permission <white>" + permission + "</white>.</red>"
        );
    }

    protected void sendUsage(final Invocation invocation, final String usage) {
        invocation.source().sendRichMessage("<red>Использование: <white>" + usage + "</white></red>");
    }

    protected List<String> filterSuggestions(final Collection<String> values, final String input) {
        final String normalizedInput = input.toLowerCase();
        final List<String> suggestions = new ArrayList<>();

        for (final String value : values) {
            if (value.toLowerCase().startsWith(normalizedInput)) {
                suggestions.add(value);
            }
        }

        return suggestions;
    }
}

