package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity class representing a user's monthly and yearly power consumption
 */

@Entity(tableName = "power_consumption")
data class PowerConsumptionEntity(
    @PrimaryKey val userId: Int = 1,
    val januaryConsumption: Int = 0,
    val februaryConsumption: Int = 0,
    val marchConsumption: Int = 0,
    val aprilConsumption: Int = 0,
    val mayConsumption: Int = 0,
    val juneConsumption: Int = 0,
    val julyConsumption: Int = 0,
    val augustConsumption: Int = 0,
    val septemberConsumption: Int = 0,
    val octoberConsumption: Int = 0,
    val novemberConsumption: Int = 0,
    val decemberConsumption: Int = 0,
    val yearlyConsumption: Int = 0
)