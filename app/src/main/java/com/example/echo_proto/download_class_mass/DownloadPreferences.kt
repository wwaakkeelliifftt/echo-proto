//package com.example.echo_proto.download_class_mass
//
//import android.content.SharedPreferences
//import javax.inject.Inject
//
//class DownloadPreferences @Inject constructor(
//    private val sharedPreferences: SharedPreferences
//) {
//    private val DOWNLOAD_CAPACITY = "max_downloads"
//
//    var maxParallelDownloads: Int
//        get() = sharedPreferences.getInt("max_downloads", 3)
//        set(value) {
//            sharedPreferences.edit().apply {
//                putInt(DOWNLOAD_CAPACITY, value.coerceIn(1, 5))
//                apply()
//            }
//        }
//}