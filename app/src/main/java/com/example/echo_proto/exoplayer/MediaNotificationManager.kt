package com.example.echo_proto.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.example.echo_proto.R
import com.example.echo_proto.util.Constants
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager

class MediaNotificationManager(
    context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener
) {
    private val notificationManager: PlayerNotificationManager

    init {
        val mediaController = MediaControllerCompat(context, sessionToken)
        notificationManager = PlayerNotificationManager.Builder(
            context, Constants.NOTIFICATION_ID, Constants.NOTIFICATION_CHANNEL_ID
        )
            .setChannelNameResourceId(R.string.notification_channel_name)
            .setChannelDescriptionResourceId(R.string.notification_channel_description)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .setSmallIconResourceId(R.drawable.ic_menu_full_tab_feed)
            .build().apply {
                setMediaSessionToken(sessionToken)
            }
    }

    fun showNotification(player: Player): Unit = notificationManager.setPlayer(player)

    private inner class DescriptionAdapter(
        private val mediaController: MediaControllerCompat
        ): PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: Player): CharSequence =
            mediaController.metadata.description.title.toString()

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            mediaController.sessionActivity

        override fun getCurrentContentText(player: Player): CharSequence =
            mediaController.metadata.description.description.toString()

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? =
            null
    }

}
