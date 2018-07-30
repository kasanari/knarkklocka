package se.jakob.knarkklocka.utils;

import java.util.Locale;

public class Utils {

    public static String getCreditsString() {
        String[] emoji = new String[] {"❤️", "\uD83C\uDF77", "\uD83D\uDD25", "\uD83D\uDDA5️", "☣️", "\uD83D\uDE0E", "\uD83D\uDE2D", "☠️"};
        int number = (int) (Math.random() * emoji.length);
        return String.format(Locale.getDefault(), "Made with %s by Jakob Nyberg", emoji[number]);
    }
}
