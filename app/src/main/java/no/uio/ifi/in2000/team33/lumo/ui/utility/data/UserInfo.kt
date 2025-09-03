package no.uio.ifi.in2000.team33.lumo.ui.utility.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

// user info entity so that it can be stored in roomDB
@Parcelize
@Entity
data class UserInfo(
    @PrimaryKey val userId: Int = 1, // only 1 profile can exist at a time
    @ColumnInfo(name = "first_name") val firstName: String = "",
    @ColumnInfo(name = "last_name") val lastName: String = "",
    @ColumnInfo(name = "onboarding_completed") val onboardingCompleted: Boolean = false
) : Parcelable