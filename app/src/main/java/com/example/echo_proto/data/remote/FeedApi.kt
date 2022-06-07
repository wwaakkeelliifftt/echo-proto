package com.example.echo_proto.data.remote

import com.example.echo_proto.util.Constants
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import javax.inject.Inject

class FeedApi @Inject constructor(private val parser: Parser) {

    suspend fun getChannelsFeed(): Channel = parser.getChannel(Constants.URL)

    suspend fun getChannelFeed(url: String) = parser.getChannel(url)
}

object AllFeeds {
    const val feeds = """Живой Гвоздь
    Александр Плющев
    Дилетант
    Майкл Наки
    Татьяна Фельгенгауэр
    Юлия Латынина
    Сергей Пархоменко
    Сергей Алексашенко
    Евгения Альбац
    Виктор Шендерович
    Александр Невзоров
    Ходорковский LIVE
    Сергей Гуриев
    Котрикадзе Дзядко
    Сергей Асланян
    Марк Солонин
    Михаил Веллер
    Дмитрий Потапенко
    Фишман"""

}