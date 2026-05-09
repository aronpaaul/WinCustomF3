package ru.paul.wincustomf3.config;

public final class PluginConfig {

    public static final String DEFAULT_BRAND = "&b&lWin&d&lProxy";

    private final String brand;
    private final AnimationConfig animationConfig;

    public PluginConfig(final String brand, final AnimationConfig animationConfig) {
        this.brand = brand;
        this.animationConfig = animationConfig;
    }

    public String resolveBrand(final long currentTimeMillis) {
        final String animatedFrame = animationConfig.resolveFrame(currentTimeMillis);
        return animatedFrame != null ? animatedFrame : brand;
    }

    public static PluginConfig defaultConfig() {
        return new PluginConfig(DEFAULT_BRAND, new AnimationConfig(false, 500L, java.util.List.of(DEFAULT_BRAND)));
    }
}

