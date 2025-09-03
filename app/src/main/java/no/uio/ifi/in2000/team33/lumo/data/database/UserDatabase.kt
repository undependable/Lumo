package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfo

/**
 * Room database class for storing user information
 *
 * the database stores various entities including user information,
 * mappoints for storing locations, roof surface data for a location
 * and power consumption data
 */

@Database(
    entities = [
        UserInfo::class,
        MapPointEntity::class,
        TakflateDataEntity::class,
        PowerConsumptionEntity::class],
    version = 7,
    exportSchema = false
)
abstract class UserDatabase : RoomDatabase() {

    abstract fun getUserInfoDao(): UserInfoDao

    abstract fun getMapPointDao(): MapPointDao

    abstract fun getPowerConsumptionDao(): PowerConsumptionDao
}