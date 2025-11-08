//package com.example.echo_proto.download_class_mass
//
//import java.io.File
//
//data class DownloadTask(
//    val episodeId: String,
//    val url: String,
//    val onProgress: (Int) -> Unit,
//    val onSuccess: (File) -> Unit,
//    val onError: (Exception) -> Unit
//)
//
//data class DownloadProgress(
//    val progress: Int,
//    val notificationId: Int
//)