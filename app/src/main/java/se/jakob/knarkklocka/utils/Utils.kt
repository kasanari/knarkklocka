package se.jakob.knarkklocka.utils

import android.content.Context
import android.os.PowerManager
import android.util.Log
import se.jakob.knarkklocka.BuildConfig
import java.util.*

object Utils {

    private const val TAG = "Utils"

    val creditsString: String
        get() {
            val emoji = arrayOf("❤️", "🍷", "🔥", "🖥️", "☠️", "☕", "🌼", "👨‍💻", "🧠", """💖""")
            val number = (Math.random() * emoji.size).toInt()
            return String.format(Locale.getDefault(), "Made with %s by Jakob Nyberg", emoji[number])
        }

    fun checkIfWhiteListed(context: Context) {
        val isWhiteListed = (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
        if (isWhiteListed) {
            Log.d(TAG, "Application is ignoring battery optimizations.")
        } else {
            Log.d(TAG, "Application is NOT ignoring battery optimizations.")
        }
    }
}
