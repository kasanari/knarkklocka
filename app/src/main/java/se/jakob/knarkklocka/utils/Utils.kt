package se.jakob.knarkklocka.utils

import java.util.Locale

object Utils {

    val creditsString: String
        get() {
            val emoji = arrayOf("❤️", "🍷", "🔥", "🖥️", "☠️", "☕", "🌼", "👨‍💻", "🧠", """💖""")
            val number = (Math.random() * emoji.size).toInt()
            return String.format(Locale.getDefault(), "Made with %s by Jakob Nyberg", emoji[number])
        }
}
