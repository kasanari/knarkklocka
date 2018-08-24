package se.jakob.knarkklocka.utils

import android.content.Context
import se.jakob.knarkklocka.data.AlarmDatabase
import se.jakob.knarkklocka.data.AlarmRepository

class InjetorUtils {

    private fun getPlantRepository(context: Context): AlarmRepository {
        return AlarmRepository.getInstance(AlarmDatabase.getInstance(context).plantDao())
    }

    private fun getAlarmViewModel(context : Context) : Ala {

    }

    fun getAlarmActivityViewmodel
}
