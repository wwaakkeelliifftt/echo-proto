package com.example.echo_proto.util

object Constants {
    const val URL = "https://feedmaster.umputun.com/rss/echo-msk"

    const val CHANNEL_ID = "channelId"
    const val EPISODE_DETAIL_ID = "episodeDetailId"

    const val MUSIC_SERVICE = "musicService"
    const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
    const val NOTIFICATION_ID = 1

    const val MEDIA_ROOT_ID = "root_id"
    const val MEDIA_QUEUE_ID = "queue_id"
    const val MEDIA_FEED_ID = "feed_id"
    const val MEDIA_FEED_PERSONAL_ID = "feed_personal_id"
    const val MEDIA_CHANNEL_ID = "channel_id"
    const val MEDIA_DOWNLOADS_ID = "downloads_id"

    const val UPDATE_PLAYER_POSITION_INTERVAL = 500L

    const val SHARED_PREFERENCES_INIT_KEY = "sharedPref"
    const val SHARED_PREFERENCE_EPISODE_DETAIL_ID_KEY = "id_episodeDetail"
    const val SHARED_PREFERENCE_MEDIA_ID_KEY = "id_tag"
    const val SHARED_PREFERENCE_LAST_EPISODE_ID_KEY = "last_episode"
    // ?? udalit?
    const val SHARED_PREFERENCE_LAST_EPISODE_PAUSE_TIME_KEY = "episode_time_key"

    const val FEED_FILTER_DIALOG_TAG = "feedFilterList"
    const val FILTER_DIALOG_DESCRIPTION = "Добавьте ваш персональный фильтр для ленты, что бы не пропустить интересные вам выпуски."

    const val NO_DATA = "no_data"
    const val NETWORK_ERROR = "Проверьте ваше соединение с интернетом"
    const val DATABASE_EMPTY_TAG ="db_empty"
    const val DATABASE_EMPTY_MESSAGE = "Вероятно, это ваш первый запуск приложения (а может вы просто почистили кэш и сохраненные эпизоды пропали из базы данных). Для обновления ленты в дальнейшем - сделайте свайп вниз или нажмите иконку \"обновления\" в правом верхнем углу экрана."
    const val DATABASE_ERROR_MESSAGE = "Что-то произошло с базой данных. Попробуйте перезапустить прилошение или обновить ленту. Если проблема возникает снова и загрузка не происходит - переустановить приложение."
    const val DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY = "В базе данных нет результатов соответствующих вашему запросу"
    const val DATABASE_QUEUE_EMPTY = "Очередь пуста"
    const val CHANNEL_SEARCH_BY_TITLE_ERROR = "Канал отсутствует в базе стандартного формата имен"
    const val EMPTY_SERVER_RESPONSE = "Ответ сервера не содержит эпизодов по вашему запросу"

}