package ru.paul.wincustomf3.command;

import com.velocitypowered.api.command.SimpleCommand;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import ru.paul.wincustomf3.brand.BrandService;
import ru.paul.wincustomf3.config.ConfigLoadException;
import ru.paul.wincustomf3.config.ConfigManager;
import ru.paul.wincustomf3.config.PluginConfig;

public final class WinCustomF3Command implements SimpleCommand {

    private static final String RELOAD_PERMISSION = "wincustomf3.command.reload";

    private final ConfigManager configManager;
    private final BrandService brandService;
    private final Logger logger;

    public WinCustomF3Command(
            final ConfigManager configManager,
            final BrandService brandService,
            final Logger logger
    ) {
        this.configManager = configManager;
        this.brandService = brandService;
        this.logger = logger;
    }

    @Override
    public void execute(final Invocation invocation) {
        if (!invocation.source().hasPermission(RELOAD_PERMISSION)) {
            invocation.source().sendRichMessage("<red>Нет прав. Нужна permission <white>" + RELOAD_PERMISSION + "</white>.</red>");
            return;
        }

        final String[] arguments = invocation.arguments();
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("reload")) {
            reload(invocation);
            return;
        }

        invocation.source().sendRichMessage("<red>Использование: <white>/wincustomf3 reload</white></red>");
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        if (!invocation.source().hasPermission(RELOAD_PERMISSION)) {
            return Collections.emptyList();
        }

        if (invocation.arguments().length == 0) {
            return List.of("reload");
        }

        if (invocation.arguments().length == 1) {
            final String current = invocation.arguments()[0].toLowerCase();
            return "reload".startsWith(current) ? List.of("reload") : Collections.emptyList();
        }

        return Collections.emptyList();
    }

    private void reload(final Invocation invocation) {
        try {
            final PluginConfig pluginConfig = configManager.load();
            brandService.updateConfig(pluginConfig);

            invocation.source().sendRichMessage(
                    "<green>WinCustomF3 перезагружен.</green> " +
                            "<gray>Новый текст F3 появится при следующем входе игрока " +
                            "или повторном подключении к backend-серверу.</gray>"
            );
            logger.info("Конфиг WinCustomF3 успешно перезагружен через команду.");
        } catch (final ConfigLoadException exception) {
            invocation.source().sendRichMessage(
                    "<red>Не удалось перезагрузить конфиг.</red> <gray>Проверь консоль прокси.</gray>"
            );
            logger.error("Ошибка при перезагрузке конфига WinCustomF3.", exception);
        }
    }
}

