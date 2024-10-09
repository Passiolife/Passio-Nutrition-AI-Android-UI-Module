package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.profile.WaterUnit
import ai.passio.nutrition.uimodule.ui.profile.mlToOz
import android.util.Log
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import java.util.UUID

class WaterRecord {
    val uuid: String = UUID.randomUUID().toString().uppercase(Locale.ROOT)
    var weight: Double = 0.0 ////kg, ml
    var dateTime: Long = 0

    companion object {

        const val QUICK_ADD_GLASS = 8.0 //oz
        const val QUICK_ADD_BOTTLE_SMALL = 16.0 //oz
        const val QUICK_ADD_BOTTLE_LARGE = 24.0 //oz

        fun create(): WaterRecord {
            val weightRecord = WaterRecord()
            weightRecord.dateTime = DateTime.now().millis
            return weightRecord
        }
    }

    fun getWaterInCurrentUnit(): Double {

        if (weight <= 0)
            return 0.0
        return if (UserCache.getProfile().measurementUnit.waterUnit == WaterUnit.Metric) {
            Log.d("measurementUnit::", "unit:aa ${UserCache.getProfile().measurementUnit.waterUnit} === vvv: $weight")
            weight
        } else {
            Log.d("measurementUnit::", "unit:bb ${UserCache.getProfile().measurementUnit.waterUnit} === vvv: ${mlToOz(weight)}")
            mlToOz(weight)
        }
    }

    fun getDisplayDay(): String {
        val dateTime = DateTime(dateTime)
        val dateFormatter = DateTimeFormat.forPattern("MMMM dd, yyyy")
        return dateTime.toString(dateFormatter)
    }

    fun getDisplayTime(): String {
        val dateTime = DateTime(dateTime)
        val timeFormatter = DateTimeFormat.forPattern("hh:mm a")
        return dateTime.toString(timeFormatter).uppercase()
    }

    fun copy(): WaterRecord {
        return passioGson.fromJson(passioGson.toJson(this), WaterRecord::class.java)
    }
}
