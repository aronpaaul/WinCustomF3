package ru.paul.wincustomf3.format;

public final class BrandFormatter {

    private final GradientFormatter gradientFormatter;

    public BrandFormatter() {
        this.gradientFormatter = new GradientFormatter();
    }

    public String format(final String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        final StringBuilder output = new StringBuilder(input.length() * 2);
        FormattingState currentState = FormattingState.empty();
        FormattingState emittedState = FormattingState.empty();

        int index = 0;
        while (index < input.length()) {
            final int hexLength = readHexTokenLength(input, index);
            if (hexLength > 0) {
                final String hexColor = input.substring(index + 2, index + 8);
                currentState = currentState.withHexColor(hexColor);
                index += hexLength;
                continue;
            }

            final int legacyLength = readLegacyTokenLength(input, index);
            if (legacyLength > 0) {
                final char code = Character.toLowerCase(input.charAt(index + 1));
                currentState = currentState.applyLegacyCode(code);
                index += legacyLength;
                continue;
            }

            final GradientFormatter.GradientToken gradientToken = gradientFormatter.tryReadToken(input, index);
            if (gradientToken != null) {
                emittedState = gradientFormatter.appendGradient(
                        output,
                        gradientToken.content(),
                        gradientToken.colors(),
                        currentState,
                        emittedState
                );
                index = gradientToken.endIndex();
                continue;
            }

            output.append(currentState.transitionFrom(emittedState));
            output.append(input.charAt(index));
            emittedState = currentState;
            index++;
        }

        return output.toString();
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
}

