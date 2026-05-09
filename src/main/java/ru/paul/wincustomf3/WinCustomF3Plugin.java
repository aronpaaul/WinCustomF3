package ru.paul.wincustomf3;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.nio.file.Path;
import org.slf4j.Logger;
import ru.paul.wincustomf3.brand.BrandService;
import ru.paul.wincustomf3.command.WinCustomF3Command;
import ru.paul.wincustomf3.config.ConfigLoadException;
import ru.paul.wincustomf3.config.ConfigManager;
import ru.paul.wincustomf3.config.PluginConfig;
import ru.paul.wincustomf3.listener.BrandListener;

@Plugin(
        id = "wincustomf3",
        name = "WinCustomF3",
        version = "1.0.0",
        description = "Настраиваемый F3 brand для Velocity",
        authors = {"paul"}
)
public final class WinCustomF3Plugin {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public WinCustomF3Plugin(
            final ProxyServer proxyServer,
            final Logger logger,
            @DataDirectory final Path dataDirectory
    ) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(final ProxyInitializeEvent event) {
        final ConfigManager configManager = new ConfigManager(dataDirectory);
        final PluginConfig initialConfig = loadInitialConfig(configManager);
        final BrandService brandService = new BrandService(initialConfig);

        proxyServer.getChannelRegistrar().register(BrandListener.BRAND_CHANNEL);
        proxyServer.getEventManager().register(this, new BrandListener(brandService, logger));

        proxyServer.getCommandManager().register(
                proxyServer.getCommandManager()
                        .metaBuilder("wincustomf3")
                        .plugin(this)
                        .build(),
                new WinCustomF3Command(configManager, brandService, logger)
        );

        logger.info("WinCustomF3 загружен. Текущий F3 brand: {}", brandService.getCurrentBrandText());
    }

    private PluginConfig loadInitialConfig(final ConfigManager configManager) {
        try {
            return configManager.load();
        } catch (final ConfigLoadException exception) {
            logger.error("Не удалось загрузить конфиг WinCustomF3. Будет использована конфигурация по умолчанию.", exception);
            return PluginConfig.defaultConfig();
        }
    }
}

