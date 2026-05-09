package ru.paul.wincustomf3.brand;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import ru.paul.wincustomf3.config.PluginConfig;
import ru.paul.wincustomf3.format.BrandFormatter;

public final class BrandService {

    private final AtomicReference<PluginConfig> configReference;
    private final BrandFormatter brandFormatter;
    private final BrandPayloadEncoder payloadEncoder;

    public BrandService(final PluginConfig initialConfig) {
        this.configReference = new AtomicReference<>(Objects.requireNonNull(initialConfig, "initialConfig"));
        this.brandFormatter = new BrandFormatter();
        this.payloadEncoder = new BrandPayloadEncoder();
    }

    public void updateConfig(final PluginConfig pluginConfig) {
        configReference.set(Objects.requireNonNull(pluginConfig, "pluginConfig"));
    }

    public String getCurrentBrandText() {
        final PluginConfig currentConfig = configReference.get();
        final String rawBrand = currentConfig.resolveBrand(System.currentTimeMillis());
        return brandFormatter.format(rawBrand);
    }

    public byte[] createPayload() {
        return payloadEncoder.encode(getCurrentBrandText());
    }
}

