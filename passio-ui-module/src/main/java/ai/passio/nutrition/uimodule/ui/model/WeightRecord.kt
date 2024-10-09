package ai.passio.nutrition.uimodule.ui.model

import ai.passio.nutrition.uimodule.data.passioGson
import ai.passio.nutrition.uimodule.ui.activity.UserCache
import ai.passio.nutrition.uimodule.ui.profile.WeightUnit
import ai.passio.nutrition.uimodule.ui.profile.kgToLbs
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import java.util.UUID

class WeightRecord {
    val uuid: String = UUID.randomUUID().toString().uppercase(Locale.ROOT)
    var weight: Double = 0.0 ////kg
    var dateTime: Long = 0

    companion object {
        fun create(): WeightRecord {
            val weightRecord = WeightRecord()
            weightRecord.dateTime = DateTime.now().millis
            return weightRecord
        }
    }

    fun getWightInCurrentUnit(): Double {
        if (weight <= 0)
            return 0.0
        return if (UserCache.getProfile().measurementUnit.weightUnit == WeightUnit.Metric) {
            weight
        } else {
            kgToLbs(weight)
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

    fun copy(): WeightRecord {
        return passioGson.fromJson(passioGson.toJson(this), WeightRecord::class.java)
    }
}
