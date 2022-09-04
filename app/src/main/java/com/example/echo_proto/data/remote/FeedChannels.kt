package com.example.echo_proto.data.remote

data class FeedChannel(val id: Int, val name: String, val url: String, val tabBadgeName: String) {

    companion object {
        val listOfChannels = listOf(
            FeedChannel(0,"Живой Гвоздь", url = "https://worker.feed-master.com/yt/media/UCWAIvx2yYLK_xTYD4F2mUNw.xml", "Эхо"),
            FeedChannel(1, "Дилетант", url = "https://worker.feed-master.com/yt/media/UCuIE7-5QzeAR6EdZXwDRwuQ.xml", "Дилетант"),
            FeedChannel(2, "Ходорковский LIVE", url = "https://worker.feed-master.com/yt/media/UCBzDAjLfvBUBVMMP6-K-y0w.xml", "МБХ Медиа"),
            FeedChannel(3, "Михаил Веллер", url = "https://worker.feed-master.com/yt/media/UCW06e3Y2_HVdxUJGqTa1nVg.xml", "Веллер"),
            FeedChannel(4, "Александр Плющев", url = "https://worker.feed-master.com/yt/media/UCTVk323gzizpujtn2T_BL7w.xml", "Плющев"),
            FeedChannel(5, "Александр Невзоров", url = "https://worker.feed-master.com/yt/media/UC8kI2B-UUv7A5u3AOUnHNMQ.xml", "Невзоров"),
            FeedChannel(6, "Виктор Шендерович", url = "https://worker.feed-master.com/yt/media/UCyYl1mXWoe4z_FcxzMaDHCw.xml", "Шендерович"),
            FeedChannel(7, "Юлия Латынина", url = "https://worker.feed-master.com/yt/media/UCzaqqlriSjVyc795m86GVyg.xml", "Латынина"),
            FeedChannel(8, "Сергей Пархоменко", url = "https://worker.feed-master.com/yt/media/UC5wRw_R3kzwV5zGe7kzec8A.xml", "Пархоменко"),
            FeedChannel(9, "Сергей Алексашенко", url = "https://worker.feed-master.com/yt/media/UCfePVJbfih7dkxrs8AInINg.xml", "Алексашенко"),
            FeedChannel(10, "Дмитрий Потапенко", url = "https://worker.feed-master.com/yt/media/UC54SBo5_usXGEoybX1ZVETQ.xml", "Потапенко"),
            FeedChannel(11, "Сергей Гуриев", url = "https://worker.feed-master.com/yt/media/UCZ-ix1fUTguJvwj6sxgF-6A.xml", "Гуриев"),
            FeedChannel(12, "Татьяна Фельгенгауэр", url = "https://worker.feed-master.com/yt/media/UC0OiowQ-fTqw3Kq3bfopO_Q.xml", "Фельгенгауэр"),
            FeedChannel(13, "Майкл Наки", url = "https://worker.feed-master.com/yt/media/UCgpSieplNxXxLXYAzJLLpng.xml", "Наки"),
            FeedChannel(14, "Марк Солонин", url = "https://worker.feed-master.com/yt/media/UChLpUGaZO35ICTltBP50VSg.xml", "Солонин"),
            FeedChannel(15, "Евгения Альбац", url = "https://worker.feed-master.com/yt/media/UCZdyijiiXyMyrwTYMGOVxKA.xml", "Альбац"),
            FeedChannel(16, "Котрикадзе Дзядко", url = "https://worker.feed-master.com/yt/media/UC0p3rxtSGCnO-JjBz5bU5CQ.xml", "Дождь"),
            FeedChannel(17, "Фишман", url = "https://worker.feed-master.com/yt/media/UCieHwWZXwzIwWEjGtmQ2y6A.xml", "Фишман"),
            FeedChannel(18, "Сергей Асланян", url = "https://worker.feed-master.com/yt/media//UCPemWOA2UiBwhTauetmz95w.xml", "Асланян"),
        )
    }
}


