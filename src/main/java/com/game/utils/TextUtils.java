package main.java.com.game.utils;

public class TextUtils {
    public static String wrapText(String text, int maxLineLength) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int lineLength = 0;

        for (String word : text.split("\\s+")) {
            if (lineLength + word.length() + 1 > maxLineLength) {
                result.append("\n");
                lineLength = 0;
            }

            if (lineLength > 0) {
                result.append(" ");
                lineLength++;
            }

            result.append(word);
            lineLength += word.length();
        }

        return result.toString();
    }
}