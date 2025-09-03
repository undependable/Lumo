package no.uio.ifi.in2000.team33.lumo.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfo

/**
 * Data Access Object for user information operations
 *
 * Made for a single user system with fixed userId
 */

@Dao
interface UserInfoDao {

    @Upsert // combines @Insert and @Update annotations
    suspend fun upsertUserInfo(userInfo: UserInfo)

    @Query("SELECT * FROM userInfo WHERE userId = 1")
    fun getUserInfo(): Flow<UserInfo?>
}