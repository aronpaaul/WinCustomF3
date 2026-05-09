package ru.paul.wincustomf3.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import ru.paul.wincustomf3.brand.BrandService;

public final class BrandListener {

    public static final MinecraftChannelIdentifier BRAND_CHANNEL =
            MinecraftChannelIdentifier.from("minecraft:brand");

    private final BrandService brandService;
    private final Logger logger;

    public BrandListener(final BrandService brandService, final Logger logger) {
        this.brandService = brandService;
        this.logger = logger;
    }

    @Subscribe
    public void onPluginMessage(final PluginMessageEvent event) {
        if (!BRAND_CHANNEL.equals(event.getIdentifier())) {
            return;
        }

        if (!(event.getSource() instanceof ServerConnection) || !(event.getTarget() instanceof Player player)) {
            return;
        }

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!player.sendPluginMessage(BRAND_CHANNEL, brandService.createPayload())) {
            logger.warn("Не удалось отправить custom brand игроку {}.", player.getUsername());
        }
    }
}

