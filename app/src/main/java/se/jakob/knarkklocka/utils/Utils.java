package se.jakob.knarkklocka.utils;

import java.util.Locale;

public class Utils {

    public static String getCreditsString() {
        String[] emoji = new String[] {"❤️", "🍷", "🔥", "🖥️", "☣️", "😎", "😭", "☠️", "☕", "🌼"};
        int number = (int) (Math.random() * emoji.length);
        return String.format(Locale.getDefault(), "Made with %s by Jakob Nyberg", emoji[number]);
    }
}
