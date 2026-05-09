package ru.paul.wincustomf3.config;

import java.util.List;

public final class AnimationConfig {

    private final boolean enabled;
    private final long intervalMillis;
    private final List<String> frames;

    public AnimationConfig(final boolean enabled, final long intervalMillis, final List<String> frames) {
        this.enabled = enabled;
        this.intervalMillis = intervalMillis;
        this.frames = List.copyOf(frames);
    }

    public boolean isActive() {
        return enabled && intervalMillis > 0L && !frames.isEmpty();
    }

    public String resolveFrame(final long currentTimeMillis) {
        if (!isActive()) {
            return null;
        }

        final long frameIndex = Math.floorDiv(currentTimeMillis, intervalMillis);
        return frames.get((int) (frameIndex % frames.size()));
    }
}

