package ru.paul.wincustomf3.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GradientFormatter {

    private static final String openingTag = "<gradient:";
    private static final String closingTag = "</gradient>";

    public GradientToken tryReadToken(final String input, final int startIndex) {
        if (!input.regionMatches(true, startIndex, openingTag, 0, openingTag.length())) {
            return null;
        }

        final int openingEnd = input.indexOf('>', startIndex);
        if (openingEnd == -1) {
            return null;
        }

        final String rawColors = input.substring(startIndex + openingTag.length(), openingEnd);
        final List<String> colors = parseColors(rawColors);
        if (colors.size() < 2) {
            return null;
        }

        final int closingStart = input.toLowerCase(Locale.ROOT).indexOf(closingTag, openingEnd + 1);
        if (closingStart == -1) {
            return null;
        }

        return new GradientToken(
                colors,
                input.substring(openingEnd + 1, closingStart),
                closingStart + closingTag.length()
        );
    }

    public FormattingState appendGradient(
            final StringBuilder output,
            final String content,
            final List<String> colors,
            final FormattingState inheritedState,
            final FormattingState emittedState
    ) {
        final List<StyledCharacter> styledCharacters = parseStyledCharacters(content, inheritedState);
        if (styledCharacters.isEmpty()) {
            return emittedState;
        }

        FormattingState currentEmittedState = emittedState;
        final int characterCount = styledCharacters.size();

        for (int index = 0; index < characterCount; index++) {
            final StyledCharacter styledCharacter = styledCharacters.get(index);
            final String currentColor = interpolateColor(colors, index, characterCount);
            final FormattingState targetState = styledCharacter.formattingState().withDisplayColor(currentColor);

            output.append(targetState.transitionFrom(currentEmittedState));
            output.append(styledCharacter.value());
            currentEmittedState = targetState;
        }

        return currentEmittedState;
    }

    private List<StyledCharacter> parseStyledCharacters(final String content, final FormattingState inheritedState) {
        final List<StyledCharacter> characters = new ArrayList<>();
        FormattingState currentState = inheritedState;

        int index = 0;
        while (index < content.length()) {
            final int hexLength = readHexTokenLength(content, index);
            if (hexLength > 0) {
                final String hexColor = content.substring(index + 2, index + 8);
                currentState = currentState.withHexColor(hexColor);
                index += hexLength;
                continue;
            }

            final int legacyLength = readLegacyTokenLength(content, index);
            if (legacyLength > 0) {
                final char code = Character.toLowerCase(content.charAt(index + 1));
                currentState = currentState.applyLegacyCode(code);
                index += legacyLength;
                continue;
            }

            characters.add(new StyledCharacter(content.charAt(index), currentState));
            index++;
        }

        return characters;
    }

    private List<String> parseColors(final String rawColors) {
        final String[] parts = rawColors.split(":");
        final List<String> colors = new ArrayList<>();

        for (final String part : parts) {
            final String trimmed = part.trim();
            if (trimmed.matches("#[0-9A-Fa-f]{6}")) {
                colors.add(trimmed);
            }
        }

        return colors;
    }

    private String interpolateColor(final List<String> colors, final int index, final int totalCharacters) {
        if (totalCharacters <= 1) {
            return colors.get(0);
        }

        final double progress = (double) index / (double) (totalCharacters - 1);
        final double scaledProgress = progress * (colors.size() - 1);

        final int segmentIndex = Math.min((int) Math.floor(scaledProgress), colors.size() - 2);
        final double localProgress = scaledProgress - segmentIndex;

        final int startColor = parseColor(colors.get(segmentIndex));
        final int endColor = parseColor(colors.get(segmentIndex + 1));

        final int red = interpolateChannel((startColor >> 16) & 0xFF, (endColor >> 16) & 0xFF, localProgress);
        final int green = interpolateChannel((startColor >> 8) & 0xFF, (endColor >> 8) & 0xFF, localProgress);
        final int blue = interpolateChannel(startColor & 0xFF, endColor & 0xFF, localProgress);

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    private int interpolateChannel(final int start, final int end, final double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }

    private int parseColor(final String hexColor) {
        return Integer.parseInt(hexColor.substring(1), 16);
    }

    private int readHexTokenLength(final String input, final int index) {
        if (index + 7 >= input.length()) {
            return 0;
        }

        final char prefix = input.charAt(index);
        if (prefix != '&' && prefix != '\u00A7') {
            return 0;
        }

        if (input.charAt(index + 1) != '#') {
            return 0;
        }

        for (int offset = index + 2; offset < index + 8; offset++) {
            if (Character.digit(input.charAt(offset), 16) == -1) {
                return 0;
            }
        }

        return 8;
    }

    private int readLegacyTokenLength(final String input, final int index) {
        if (index + 1 >= input.length()) {
            return 0;
        }

        final char prefix = input.charAt(index);
        if (prefix != '&' && prefix != '\u00A7') {
            return 0;
        }

        final char code = Character.toLowerCase(input.charAt(index + 1));
        return "0123456789abcdefklmnor".indexOf(code) >= 0 ? 2 : 0;
    }

    public record GradientToken(List<String> colors, String content, int endIndex) {
    }
}
