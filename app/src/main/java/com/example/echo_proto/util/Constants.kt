package com.example.echo_proto.util

object Constants {
    const val URL = "https://feedmaster.umputun.com/rss/echo-msk"

    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val FEED_FILTER_LIST = "feedFilterList"
    const val FILTER_DIALOG_DESCRIPTION = "Добавьте ваш персональный фильтр для ленты, что бы не пропустить интересные вам выпуски."

    const val NO_DATA = "no_data"
    const val NETWORK_STATE_PROBLEM = "Проверьте ваше соединение с интернетом"
    const val DATABASE_EMPTY_TAG ="db_empty"
    const val DATABASE_EMPTY_MESSAGE = "Вероятно, это ваш первый запуск приложения (а может вы просто почистили кэш и сохраненные эпизоды пропали из базы данных). Для обновления ленты в дальнейшем - сделайте свайп вниз или нажмите иконку \"обновления\" в правом верхнем углу экрана."
    const val DATABASE_ERROR_MESSAGE = "Что-то произошло с базой данных. Попробуйте перезапустить прилошение или обновить ленту. Если проблема возникает снова и загрузка не происходит - переустановить приложение."
    const val DATABASE_SEARCH_QUERY_RESULT_IS_EMPTY = "В базе данных нет результатов соответствующих вашему запросу"
    const val DATABASE_QUEUE_EMPTY = "Очередь пуста"

}