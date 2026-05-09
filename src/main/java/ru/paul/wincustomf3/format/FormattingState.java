package ru.paul.wincustomf3.format;

import java.util.Objects;

public record FormattingState(
        String colorCode,
        boolean obfuscated,
        boolean bold,
        boolean strikethrough,
        boolean underlined,
        boolean italic
) {

    private static final char SECTION = '\u00A7';

    public static FormattingState empty() {
        return new FormattingState(null, false, false, false, false, false);
    }

    public FormattingState applyLegacyCode(final char code) {
        return switch (code) {
            case '0', '1', '2', '3', '4', '5', '6', '7',
                    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' -> new FormattingState(
                    String.valueOf(SECTION) + code,
                    false,
                    false,
                    false,
                    false,
                    false
            );
            case 'k' -> new FormattingState(colorCode, true, bold, strikethrough, underlined, italic);
            case 'l' -> new FormattingState(colorCode, obfuscated, true, strikethrough, underlined, italic);
            case 'm' -> new FormattingState(colorCode, obfuscated, bold, true, underlined, italic);
            case 'n' -> new FormattingState(colorCode, obfuscated, bold, strikethrough, true, italic);
            case 'o' -> new FormattingState(colorCode, obfuscated, bold, strikethrough, underlined, true);
            case 'r' -> empty();
            default -> this;
        };
    }

    public FormattingState withHexColor(final String hexColor) {
        return new FormattingState(toSectionHex(hexColor), false, false, false, false, false);
    }

    public FormattingState withDisplayColor(final String hexColor) {
        return new FormattingState(toSectionHex(hexColor), obfuscated, bold, strikethrough, underlined, italic);
    }

    public String transitionFrom(final FormattingState previousState) {
        if (equals(previousState)) {
            return "";
        }

        if (isEmpty()) {
            return previousState.isEmpty() ? "" : String.valueOf(SECTION) + 'r';
        }

        if (needsReset(previousState)) {
            return String.valueOf(SECTION) + 'r' + fullCodes();
        }

        if (!Objects.equals(colorCode, previousState.colorCode) && colorCode != null) {
            return colorCode + formatCodes();
        }

        return addedFormatCodes(previousState);
    }

    public boolean isEmpty() {
        return colorCode == null && !obfuscated && !bold && !strikethrough && !underlined && !italic;
    }

    private boolean needsReset(final FormattingState previousState) {
        if (previousState.isEmpty()) {
            return false;
        }

        if (colorCode == null && previousState.colorCode != null) {
            return true;
        }

        if (!Objects.equals(colorCode, previousState.colorCode) && colorCode != null) {
            return false;
        }

        return (previousState.obfuscated && !obfuscated)
                || (previousState.bold && !bold)
                || (previousState.strikethrough && !strikethrough)
                || (previousState.underlined && !underlined)
                || (previousState.italic && !italic);
    }

    private String fullCodes() {
        return (colorCode == null ? "" : colorCode) + formatCodes();
    }

    private String formatCodes() {
        final StringBuilder builder = new StringBuilder();

        if (obfuscated) {
            builder.append(SECTION).append('k');
        }
        if (bold) {
            builder.append(SECTION).append('l');
        }
        if (strikethrough) {
            builder.append(SECTION).append('m');
        }
        if (underlined) {
            builder.append(SECTION).append('n');
        }
        if (italic) {
            builder.append(SECTION).append('o');
        }

        return builder.toString();
    }

    private String addedFormatCodes(final FormattingState previousState) {
        final StringBuilder builder = new StringBuilder();

        if (obfuscated && !previousState.obfuscated) {
            builder.append(SECTION).append('k');
        }
        if (bold && !previousState.bold) {
            builder.append(SECTION).append('l');
        }
        if (strikethrough && !previousState.strikethrough) {
            builder.append(SECTION).append('m');
        }
        if (underlined && !previousState.underlined) {
            builder.append(SECTION).append('n');
        }
        if (italic && !previousState.italic) {
            builder.append(SECTION).append('o');
        }

        return builder.toString();
    }

    private static String toSectionHex(final String hexColor) {
        final String normalized = hexColor.charAt(0) == '#' ? hexColor.substring(1) : hexColor;
        final StringBuilder builder = new StringBuilder(14);
        builder.append(SECTION).append('x');

        for (int index = 0; index < normalized.length(); index++) {
            builder.append(SECTION).append(Character.toLowerCase(normalized.charAt(index)));
        }

        return builder.toString();
    }
}

