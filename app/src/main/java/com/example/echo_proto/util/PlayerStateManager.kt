package com.example.echo_proto.util

import android.content.SharedPreferences
import javax.inject.Inject

class PlayerStateManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun saveLastPlayedPosition(episodeId: String, position: Long) {
        sharedPreferences.edit()
//            .putString(Constants.LAST_EPISODE_ID_KEY, episodeId)
//            .putLong(Constants.LAST_POSITION_KEY, position)
            .putString(Constants.SHARED_PREFERENCE_LAST_EPISODE_ID_KEY, episodeId)
            .putLong(Constants.SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY, position)
            .apply()
    }
}