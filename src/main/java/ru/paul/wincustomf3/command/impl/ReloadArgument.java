package ru.paul.wincustomf3.command.impl;

import com.velocitypowered.api.command.SimpleCommand;
import org.slf4j.Logger;
import ru.paul.wincustomf3.brand.BrandService;
import ru.paul.wincustomf3.command.CommandArgument;
import ru.paul.wincustomf3.config.ConfigLoadException;
import ru.paul.wincustomf3.config.ConfigManager;
import ru.paul.wincustomf3.config.PluginConfig;

public final class ReloadArgument implements CommandArgument {

    private static final String reloadPermission = "wincustomf3.command.reload";

    private final ConfigManager configManager;
    private final BrandService brandService;
    private final Logger logger;

    public ReloadArgument(
            final ConfigManager configManager,
            final BrandService brandService,
            final Logger logger
    ) {
        this.configManager = configManager;
        this.brandService = brandService;
        this.logger = logger;
    }

    @Override
    public String name() {
        return "reload";
    }

    @Override
    public String permission() {
        return reloadPermission;
    }

    @Override
    public void execute(final SimpleCommand.Invocation invocation) {
        if (invocation.arguments().length != 1) {
            invocation.source().sendRichMessage("<red>Использование: <white>/wincustomf3 reload</white></red>");
            return;
        }

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

