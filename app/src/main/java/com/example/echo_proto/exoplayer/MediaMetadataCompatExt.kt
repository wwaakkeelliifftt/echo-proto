package com.example.echo_proto.exoplayer

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.appcompat.widget.TintInfo
import com.example.echo_proto.domain.model.Episode
import timber.log.Timber

fun MediaMetadataCompat.toPlayerInfoEpisode(): Episode? {
    return description?.let { mediaMetaDesc ->
        Episode(
            mediaId = mediaMetaDesc.mediaId ?: "",
            videoLink = mediaMetaDesc.mediaUri.toString(),
            title = mediaMetaDesc.title.toString(),
            description = mediaMetaDesc.description.toString(),
            timestamp =  this.getLong(METADATA_KEY_YEAR),
            duration = this.getLong(METADATA_KEY_DURATION).toInt() / 1000,

            // ...MEDIA_URI = null...
            audioLink = this.getString(METADATA_KEY_MEDIA_URI).toString(),
            // check WAT we need- above or below
            downloadUrl = this.bundle.getString(METADATA_KEY_ART_URI) ?: "pusto?",
            // todo: make mapper for check status
            isDownloaded = this.getLong(METADATA_KEY_DOWNLOAD_STATUS) == 2L,
            rssId = this.getLong(METADATA_KEY_DOWNLOAD_STATUS).toString(),
            id = mediaMetaDesc.mediaId?.toInt() ?: -666,
            indexInQueue = this.bundle.getLong(METADATA_KEY_TRACK_NUMBER).toInt()
        ).also {
            Timber.d(
                "\n\nnewEpisode (prepare from MediaMetadataCompat\n" +
                        "mediaId=${it.mediaId}\n" +
                        "id=${it.id}" +
                        "title=${it.title}\n" +
                        "desc=${it.description.substring(startIndex = 0, endIndex = 33)}\n" +
                        "timestamp=${it.timestamp}\n" +
                        "duration<Int>=${it.duration}\n" +
                        "audioLink=${it.audioLink}\n" +
                        "videoLink=${it.videoLink}\n" +
                        "downloadUrl(ART_URI)=${it.downloadUrl}\n" +
                        "isDownloaded<<getLong==2L>>(DOWNLOAD_STATUS)=${it.isDownloaded}\n" +
                        "isDownloaded<getLong as is>=${it.rssId}\n" +
                        "indexInQueue=${it.indexInQueue}\n" +
                        "\n also:: MediaMetadataCompat try ->\n" +
                        "bundle-timestamp<DATE>=${this.bundle.getString(METADATA_KEY_DATE)}\n" +
                        "obj-timestamp<DATE>=${this.getString(METADATA_KEY_DATE)}\n" +
                        "obj<string>-timestamp<YEAR>=${this.getString(METADATA_KEY_YEAR)}\n" +
                        "obj<long>-timestamp<YEAR>=${this.getLong(METADATA_KEY_YEAR)}\n"
            )
        }
    } ?: null.also { Timber.d("\n\n\n\n\n NULL\t\tNULL\t\tNULL <<<<----return----   toPlayerInfoEpisodeEntity\n\n\n\n\n\n") }
}
