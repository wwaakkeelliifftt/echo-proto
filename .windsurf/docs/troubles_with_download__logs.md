попробовал скачать эпизод, но не получилось

---
## 📝 Пользовательский комментарий + логи (НЕ УДАЛЯТЬ!)

тогда на всякий случай проверь еще раз фаилы и то как у нас реализована загрузка эпизодов. я испытываю постоянные проблемы с визуализацией при скачивании - обычно при нажатии кнопки "download" визуально ничего не происходит, но под капотом идет загрузка и далее визуализация и прогресс все же отрабатывают, но уже в самом конце. обычно это когда загрузилось около 60-80%, прогресс загрузки резко перемещается на это значение, а потом уже плавно "докачивает" и показывает прогресс в ui

---
лог для очень большого файла (387 MB вроде бы)

я посмотрел на таймкоды и выглядит будто бы все ок, но перед тем как полетели в ui обновления прогресса была пауза около 15-20 сек, потом все докачалось почти в момент


17:46:47.363 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:46:47.502 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:48.505 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:49.196 com.example.echo_proto               D  EpisodeDetailViewModel::init::episodeId=752, title=$
17:46:49.292 com.example.echo_proto               D  🎯 WebView: Episode description page finished loading
17:46:49.340 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.349 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.354 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.362 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.366 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.369 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.372 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.374 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.377 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.379 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.380 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.382 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.384 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:46:49.384 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.384 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:46:49.384 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:46:49.384 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:46:49.387 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.388 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.390 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.392 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.394 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
17:46:49.509 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:50.513 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:50.666 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:46:51.309 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 1835/1845 (recycle/alloc) - 17/3668 (fetch/transfer)
17:46:51.519 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:51.547 com.example.echo_proto               D  🕒 4. DOWN::DownloadWorker: doWork() STARTED
17:46:51.549 com.example.echo_proto               D  🕒 5. DOWN::DownloadWorker: Dispatchers.IO context
17:46:51.550 com.example.echo_proto               D  🕒 6. DOWN::DownloadWorker: Got episodeId: 752
17:46:51.552 com.example.echo_proto               D  🕒 7. DOWN::DownloadWorker: Starting download flow...
17:46:51.556 com.example.echo_proto               I  Moving WorkSpec (169aa715-7c50-4bdd-b372-7bf980c244aa) to the foreground
17:46:51.564 com.example.echo_proto               I  Started foreground service Intent { act=ACTION_START_FOREGROUND cmp=com.example.echo_proto/androidx.work.impl.foreground.SystemForegroundService (has extras) }
17:46:51.567 com.example.echo_proto               D  🕒 10. DOWN::DownloadRepositoryImpl: START - Getting episode from DB
17:46:51.571 com.example.echo_proto               D  🕒 11. DOWN::DownloadRepositoryImpl: Got episode: 9 - GitHub падает, Linux 5.8, полвека с Pascal,containers future, DevOps тренды 2020 и DevOps vs SRE
17:46:51.572 com.example.echo_proto               D  DownloadRepositoryImpl: File path: /data/user/0/com.example.echo_proto/files/episodes/devopskitchentalks.podbean.com/085e6a45-133c-5d2d-94c3-1adfd9870ea3.mp3
17:46:51.573 com.example.echo_proto               D  File creation took 1 ms
17:46:51.573 com.example.echo_proto               D  🕒 12. DOWN::DownloadRepositoryImpl: Creating HTTP request...
17:46:51.580 com.example.echo_proto               D  🕒 13. DOWN::DownloadRepositoryImpl: Executing request...
17:46:51.580 com.example.echo_proto               D  🕒 13.1 DOWN::DownloadRepositoryImpl: Request created at 1777474011573
17:46:51.583 com.example.echo_proto               I  --> GET https://mcdn.podbean.com/mf/web/ef852y/DKT9WithEffects_1__71s4m.mp3
17:46:51.584 com.example.echo_proto               I  Accept: audio/*
17:46:51.584 com.example.echo_proto               I  Accept-Encoding: identity
17:46:51.584 com.example.echo_proto               I  --> END GET
17:46:52.525 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:53.308 com.example.echo_proto               I  <-- 200 OK https://s322.podbean.com/pb/98db4b4ccce48b6176675e9021c6b33f/69f18d3c/data1/fs109/7709170/uploads/DKT9WithEffects_1__71s4m.mp3?pbss=b5edf0f1-ac1c-5f35-b5bc-12a0703dd720 (1723ms)
17:46:53.308 com.example.echo_proto               I  Date: Wed, 29 Apr 2026 14:46:53 GMT
17:46:53.308 com.example.echo_proto               I  Content-Type: audio/mpeg
17:46:53.308 com.example.echo_proto               I  Content-Length: 406086016
17:46:53.308 com.example.echo_proto               I  Last-Modified: Thu, 01 Sep 2022 13:26:10 GMT
17:46:53.309 com.example.echo_proto               I  Connection: keep-alive
17:46:53.309 com.example.echo_proto               I  Access-Control-Allow-Origin: *
17:46:53.309 com.example.echo_proto               I  Access-Control-Allow-Credentials: true
17:46:53.309 com.example.echo_proto               I  ETag: "6310b2f2-18346180"
17:46:53.309 com.example.echo_proto               I  Server: Podbean Content Distribution Network
17:46:53.309 com.example.echo_proto               I  Expires: Wed, 29 Apr 2026 15:46:53 GMT
17:46:53.309 com.example.echo_proto               I  Cache-Control: max-age=3600
17:46:53.309 com.example.echo_proto               I  Strict-Transport-Security: max-age=15552000; includeSubDomains
17:46:53.309 com.example.echo_proto               I  Accept-Ranges: bytes
17:46:53.529 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:53.980 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:46:54.533 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:55.550 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:56.553 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:56.557 com.example.echo_proto               D  MediaSource episodes updated: 11 episodes
17:46:56.630 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:2) isEGL=1, mPendingRelease.size()=1, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:46:56.688 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 2104/2114 (recycle/alloc) - 17/4206 (fetch/transfer)
17:46:56.793 com.example.echo_proto               I  Background concurrent mark compact GC freed 7997KB AllocSpace bytes, 18(492KB) LOS objects, 29% free, 58MB/82MB, paused 585us,3.006ms total 154.708ms
17:46:57.227 com.example.echo_proto               D  submitList: oldSize=1721, newSize=1721
17:46:57.352 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:46:58.699 com.example.echo_proto               D  submitList: oldSize=11, newSize=11
17:46:58.761 com.example.echo_proto               D  submitList: oldSize=1721, newSize=1721
17:46:58.764 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:58.818 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:3) isEGL=1, mPendingRelease.size()=2, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:46:58.826 com.example.echo_proto               I  Background young concurrent mark compact GC freed 10MB AllocSpace bytes, 4(136KB) LOS objects, 17% free, 67MB/82MB, paused 424us,6.353ms total 95.830ms
17:46:59.265 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:46:59.331 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:4) isEGL=1, mPendingRelease.size()=3, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:00.270 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:00.721 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:01.276 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:01.313 com.example.echo_proto               I  Background concurrent mark compact GC freed 11MB AllocSpace bytes, 10(304KB) LOS objects, 25% free, 68MB/92MB, paused 504us,2.578ms total 132.416ms
17:47:01.938 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 2366/2376 (recycle/alloc) - 17/4730 (fetch/transfer)
17:47:02.279 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:03.211 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:47:03.211 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:47:03.281 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:03.780 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:47:03.780 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
17:47:04.052 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:04.283 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:05.028 com.example.echo_proto               D  onWindowFocusChanged hasWindowFocus false
17:47:05.284 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:06.287 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:07.277 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 2631/2641 (recycle/alloc) - 17/5260 (fetch/transfer)
17:47:07.293 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:07.410 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:08.230 com.example.echo_proto               I  Background concurrent mark compact GC freed 29KB AllocSpace bytes, 2(104KB) LOS objects, 20% free, 92MB/116MB, paused 530us,3.289ms total 193.461ms
17:47:08.295 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:09.297 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:10.301 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:10.750 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:10.804 com.example.echo_proto               D  onWindowFocusChanged hasWindowFocus true
17:47:11.303 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:12.307 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:12.606 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 2896/2906 (recycle/alloc) - 17/5790 (fetch/transfer)
17:47:13.311 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:13.541 com.example.echo_proto               I  Background young concurrent mark compact GC freed 6998KB AllocSpace bytes, 12(624KB) LOS objects, 6% free, 109MB/116MB, paused 654us,4.368ms total 129.019ms
17:47:14.077 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:14.314 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:15.239 com.example.echo_proto               I  Background concurrent mark compact GC freed 17KB AllocSpace bytes, 2(104KB) LOS objects, 17% free, 116MB/140MB, paused 413us,2.598ms total 156.063ms
17:47:15.319 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:16.324 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:16.398 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:2) isEGL=1, mPendingRelease.size()=1, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:17.327 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:17.405 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:3) isEGL=1, mPendingRelease.size()=2, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:17.406 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:17.898 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 3160/3170 (recycle/alloc) - 17/6318 (fetch/transfer)
17:47:18.330 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:18.394 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:4) isEGL=1, mPendingRelease.size()=3, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:19.333 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:20.336 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:20.723 com.example.echo_proto               I  Background young concurrent mark compact GC freed 7058KB AllocSpace bytes, 12(624KB) LOS objects, 4% free, 133MB/140MB, paused 339us,4.084ms total 132.067ms
17:47:20.812 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:21.341 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:22.090 com.example.echo_proto               I  Background concurrent mark compact GC freed 13KB AllocSpace bytes, 2(104KB) LOS objects, 14% free, 139MB/163MB, paused 423us,2.527ms total 174.802ms
17:47:22.344 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:23.189 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 3423/3433 (recycle/alloc) - 17/6844 (fetch/transfer)
17:47:23.349 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:24.154 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:24.352 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:25.358 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:26.360 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:27.274 com.example.echo_proto               I  Background young concurrent mark compact GC freed 6964KB AllocSpace bytes, 10(520KB) LOS objects, 4% free, 156MB/163MB, paused 298us,3.801ms total 145.560ms
17:47:27.365 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:27.374 com.example.echo_proto               D  MediaSource episodes updated: 11 episodes
17:47:27.480 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:27.668 com.example.echo_proto               I  Background concurrent mark compact GC freed 21KB AllocSpace bytes, 2(104KB) LOS objects, 12% free, 170MB/194MB, paused 2.810ms,5.079ms total 224.311ms
17:47:28.058 com.example.echo_proto               D  submitList: oldSize=1721, newSize=1721
17:47:28.470 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 3686/3696 (recycle/alloc) - 17/7370 (fetch/transfer)
17:47:29.092 com.example.echo_proto               D  submitList: oldSize=1721, newSize=1721
17:47:29.579 com.example.echo_proto               D  submitList: oldSize=11, newSize=11
17:47:29.581 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:29.765 com.example.echo_proto               I  Background young concurrent mark compact GC freed 13MB AllocSpace bytes, 6(168KB) LOS objects, 9% free, 176MB/194MB, paused 336us,3.132ms total 170.881ms
17:47:30.082 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:30.851 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:31.086 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:32.091 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:33.105 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:33.772 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 3950/3960 (recycle/alloc) - 17/7898 (fetch/transfer)
17:47:33.844 com.example.echo_proto               I  Background concurrent mark compact GC freed 14MB AllocSpace bytes, 14(512KB) LOS objects, 11% free, 179MB/203MB, paused 1.121ms,2.690ms total 270.787ms
17:47:34.101 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:34.178 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:35.103 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:36.107 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:37.110 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:37.504 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:38.111 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:38.965 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 4209/4219 (recycle/alloc) - 17/8416 (fetch/transfer)
17:47:39.115 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:39.259 com.example.echo_proto               I  Background young concurrent mark compact GC freed 5648KB AllocSpace bytes, 10(520KB) LOS objects, 2% free, 197MB/203MB, paused 1.043ms,2.899ms total 178.053ms
17:47:40.123 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:40.223 com.example.echo_proto               I  Background concurrent mark compact GC freed 29KB AllocSpace bytes, 2(104KB) LOS objects, 10% free, 203MB/227MB, paused 580us,3.124ms total 221.488ms
17:47:40.877 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:41.125 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:42.128 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:43.131 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:44.134 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:44.170 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 4468/4478 (recycle/alloc) - 17/8934 (fetch/transfer)
17:47:44.194 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:45.138 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:46.024 com.example.echo_proto               I  Background young concurrent mark compact GC freed 7046KB AllocSpace bytes, 12(624KB) LOS objects, 3% free, 219MB/227MB, paused 444us,2.959ms total 204.990ms
17:47:46.141 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:47.148 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:47.218 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:2) isEGL=1, mPendingRelease.size()=1, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:47.568 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:47.773 com.example.echo_proto               I  Background concurrent mark compact GC freed 31KB AllocSpace bytes, 4(208KB) LOS objects, 9% free, 227MB/251MB, paused 460us,3.183ms total 260.281ms
17:47:48.188 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:49.162 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:49.459 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 4731/4741 (recycle/alloc) - 17/9460 (fetch/transfer)
17:47:50.166 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:50.896 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:51.172 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:52.175 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:52.735 com.example.echo_proto               I  Background young concurrent mark compact GC freed 7456KB AllocSpace bytes, 10(520KB) LOS objects, 2% free, 244MB/251MB, paused 375us,3.462ms total 220.631ms
17:47:53.179 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:54.050 com.example.echo_proto               I  Clamp target GC heap from 275MB to 256MB
17:47:54.050 com.example.echo_proto               I  Background concurrent mark compact GC freed 5824B AllocSpace bytes, 2(104KB) LOS objects, 1% free, 251MB/256MB, paused 399us,4.128ms total 290.545ms
17:47:54.185 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:54.250 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:2) isEGL=1, mPendingRelease.size()=1, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:54.283 com.example.echo_proto               D  elapsed: mInputMetEos 0, hasPendingOutputsInClient 1, n 0
17:47:54.715 com.example.echo_proto               D  bufferpool2 0xb400007bc6ff4428 : 5(327680 size) total buffers - 1(65536 size) used buffers - 4992/5002 (recycle/alloc) - 17/9982 (fetch/transfer)
17:47:55.139 com.example.echo_proto               I  Background young concurrent mark compact GC freed 2937KB AllocSpace bytes, 2(104KB) LOS objects, 1% free, 252MB/256MB, paused 282us,3.328ms total 242.942ms
17:47:55.188 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:55.275 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:3) isEGL=1, mPendingRelease.size()=2, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:55.605 com.example.echo_proto               I  Clamp target GC heap from 279MB to 256MB
17:47:55.605 com.example.echo_proto               I  Background concurrent mark compact GC freed 29KB AllocSpace bytes, 2(104KB) LOS objects, 0% free, 255MB/256MB, paused 418us,2.850ms total 276.980ms
17:47:55.619 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:55.846 com.example.echo_proto               I  Background young concurrent mark compact GC freed 1168KB AllocSpace bytes, 0(0B) LOS objects, 0% free, 254MB/256MB, paused 240us,3.354ms total 239.504ms
17:47:55.846 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Background for 226.828ms
17:47:55.931 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:55.958 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:55.997 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.138 com.example.echo_proto               I  Clamp target GC heap from 279MB to 256MB
17:47:56.138 com.example.echo_proto               I  Background concurrent mark compact GC freed 28KB AllocSpace bytes, 0(0B) LOS objects, 0% free, 255MB/256MB, paused 428us,3.058ms total 259.976ms
17:47:56.138 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Background for 206.752ms
17:47:56.138 com.example.echo_proto               I  Forcing collection of SoftReferences for 8208B allocation
17:47:56.138 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 141.603ms
17:47:56.138 com.example.echo_proto               I  Forcing collection of SoftReferences for 16B allocation
17:47:56.138 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 180.551ms
17:47:56.139 com.example.echo_proto               I  Forcing collection of SoftReferences for 192B allocation
17:47:56.139 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.139 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.139 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.139 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.191 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.193 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.480 com.example.echo_proto               I  Clamp target GC heap from 277MB to 256MB
17:47:56.480 com.example.echo_proto               I  Alloc concurrent mark compact GC freed 2396KB AllocSpace bytes, 0(0B) LOS objects, 0% free, 253MB/256MB, paused 402us,2.895ms total 341.556ms
17:47:56.480 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 341.515ms
17:47:56.480 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 341.548ms
17:47:56.481 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 341.567ms
17:47:56.481 com.example.echo_proto               I  Waiting for a blocking GC Alloc
17:47:56.481 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 289.523ms
17:47:56.481 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 288.075ms
17:47:56.483 com.example.echo_proto               D  6) playbackStateObserver -> session speed=1,30
17:47:56.490 com.example.echo_proto               W  Throwing OutOfMemoryError "Failed to allocate a 8208 byte allocation with 2323968 free bytes and 2269KB until OOM, target footprint 268435456, growth limit 268435456; giving up on allocation because <1% of heap free after GC." (VmSize 34992624 kB)
17:47:56.496 com.example.echo_proto               E  Work [ id=169aa715-7c50-4bdd-b372-7bf980c244aa, tags={ com.example.echo_proto.domain.worker.DownloadWorker } ] failed because it threw an exception/error (Ask Gemini)
java.util.concurrent.ExecutionException: java.lang.OutOfMemoryError: Failed to allocate a 8208 byte allocation with 2323968 free bytes and 2269KB until OOM, target footprint 268435456, growth limit 268435456; giving up on allocation because <1% of heap free after GC.
	at androidx.work.impl.utils.futures.AbstractFuture.getDoneValue(AbstractFuture.java:516)
	at androidx.work.impl.utils.futures.AbstractFuture.get(AbstractFuture.java:475)
	at androidx.work.impl.WorkerWrapper$2.run(WorkerWrapper.java:311)
	at androidx.work.impl.utils.SerialExecutor$Task.run(SerialExecutor.java:91)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1154)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:652)
	at java.lang.Thread.run(Thread.java:1564)
Caused by: java.lang.OutOfMemoryError: Failed to allocate a 8208 byte allocation with 2323968 free bytes and 2269KB until OOM, target footprint 268435456, growth limit 268435456; giving up on allocation because <1% of heap free after GC.
	at okio.Segment.<init>(Segment.kt:62)
	at okio.SegmentPool.take(SegmentPool.kt:88)
	at okio.Buffer.writableSegment$okio(Buffer.kt:1444)
	at okio.InputStreamSource.read(JvmOkio.kt:92)
	at okio.AsyncTimeout$source$1.read(AsyncTimeout.kt:125)
	at okio.RealBufferedSource.read(RealBufferedSource.kt:189)
	at okhttp3.internal.http1.Http1ExchangeCodec$AbstractSource.read(Http1ExchangeCodec.kt:331)
	at okhttp3.internal.http1.Http1ExchangeCodec$FixedLengthSource.read(Http1ExchangeCodec.kt:368)
	at okhttp3.internal.connection.Exchange$ResponseBodySource.read(Exchange.kt:276)
	at okio.RealBufferedSource.request(RealBufferedSource.kt:206)
	at okhttp3.logging.HttpLoggingInterceptor.intercept(HttpLoggingInterceptor.kt:247)
	at okhttp3.internal.http.RealInterceptorChain.proceed(RealInterceptorChain.kt:109)
	at okhttp3.internal.connection.RealCall.getResponseWithInterceptorChain$okhttp(RealCall.kt:201)
	at okhttp3.internal.connection.RealCall.execute(RealCall.kt:154)
	at com.example.echo_proto.domain.worker.DownloadRepositoryImpl$downloadEpisodeToDatabase$2.invokeSuspend(DownloadRepository.kt:45)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
	at kotlinx.coroutines.internal.LimitedDispatcher.run(LimitedDispatcher.kt:39)
	at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:95)
	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678)
	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665)
17:47:56.500 com.example.echo_proto               I  Worker result FAILURE for Work [ id=169aa715-7c50-4bdd-b372-7bf980c244aa, tags={ com.example.echo_proto.domain.worker.DownloadWorker } ]
17:47:56.522 com.example.echo_proto               I  Stopping foreground service
17:47:56.575 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:5) isEGL=1, mPendingRelease.size()=3, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
17:47:56.702 com.example.echo_proto               D  getTimestamp_l(827): device stall time corrected using current time 138132849246768
17:47:56.861 com.example.echo_proto               I  Clamp target GC heap from 277MB to 256MB
17:47:56.861 com.example.echo_proto               I  Alloc concurrent mark compact GC freed 384KB AllocSpace bytes, 2(104KB) LOS objects, 0% free, 253MB/256MB, paused 398us,3.824ms total 380.732ms
17:47:56.861 com.example.echo_proto               W  Throwing OutOfMemoryError "Failed to allocate a 16 byte allocation with 2388656 free bytes and 2332KB until OOM, target footprint 268435456, growth limit 268435456; giving up on allocation because <1% of heap free after GC." (VmSize 34993172 kB)
--------- beginning of crash
17:47:56.861 com.example.echo_proto               I  WaitForGcToComplete blocked Alloc on Alloc for 722.447ms
17:47:56.867 com.example.echo_proto               E  FATAL EXCEPTION: 2005-ScoutStateMachine (Ask Gemini)
Process: com.example.echo_proto, PID: 2005
java.lang.OutOfMemoryError: Failed to allocate a 16 byte allocation with 2388656 free bytes and 2332KB until OOM, target footprint 268435456, growth limit 268435456; giving up on allocation because <1% of heap free after GC.
	at java.lang.Integer.valueOf(Integer.java:1197)
	at android.os.ThreadLocalWorkSource.setUid(ThreadLocalWorkSource.java:68)
	at android.os.Looper.loopOnce(Looper.java:209)
	at android.os.Looper.loop(Looper.java:300)
	at android.os.HandlerThread.run(HandlerThread.java:67)
17:47:56.873 com.example.echo_proto               W  Failed to mkdir /data/mqsas/hprof/
17:47:56.868 com.example.echo_proto               W  type=1400 audit(0.0:81184): avc: denied { search } for name="mqsas" dev="dm-7" ino=454 scontext=u:r:untrusted_app:s0:c133,c257,c512,c768 tcontext=u:object_r:mqsas_data_file:s0 tclass=dir permissive=0 app=com.example.echo_proto
17:47:56.868 com.example.echo_proto               W  type=1400 audit(0.0:81185): avc: denied { search } for name="mqsas" dev="dm-7" ino=454 scontext=u:r:untrusted_app:s0:c133,c257,c512,c768 tcontext=u:object_r:mqsas_data_file:s0 tclass=dir permissive=0 app=com.example.echo_proto
17:47:56.868 com.example.echo_proto               W  type=1400 audit(0.0:81186): avc: denied { search } for name="mqsas" dev="dm-7" ino=454 scontext=u:r:untrusted_app:s0:c133,c257,c512,c768 tcontext=u:object_r:mqsas_data_file:s0 tclass=dir permissive=0 app=com.example.echo_proto
17:47:56.872 com.example.echo_proto               W  type=1400 audit(0.0:81187): avc: denied { search } for name="mqsas" dev="dm-7" ino=454 scontext=u:r:untrusted_app:s0:c133,c257,c512,c768 tcontext=u:object_r:mqsas_data_file:s0 tclass=dir permissive=0 app=com.example.echo_proto
17:47:56.872 com.example.echo_proto               W  type=1400 audit(0.0:81188): avc: denied { getattr } for path="/data/mqsas" dev="dm-7" ino=454 scontext=u:r:untrusted_app:s0:c133,c257,c512,c768 tcontext=u:object_r:mqsas_data_file:s0 tclass=dir permissive=0 app=com.example.echo_proto
17:47:56.872 com.example.echo_proto               W  type=1400 audit(0.0:81189): avc: denied { search } for name="mqsas" dev="dm-7" ino=454 scontext=u:r:untrusted_app:s0:c133,c257,c512,c768 tcontext=u:object_r:mqsas_data_file:s0 tclass=dir permissive=0 app=com.example.echo_proto
17:47:56.984 com.example.echo_proto               I  Sending signal. PID: 2005 SIG: 9.

---
лог для файла около 70-80мб

18:11:35.510 com.example.echo_proto               D  🔄 Changing queue status for episode id=973, current isInQueue=false
18:11:35.525 com.example.echo_proto               D  ✅ Repository changeEpisodeQueueStatus completed
18:11:35.545 com.example.echo_proto               D  ✅ Episode updated: id=973, isInQueue=true, indexInQueue=11
18:11:35.613 com.example.echo_proto               D  🎯 WebView: Episode description page finished loading
18:11:35.628 com.example.echo_proto               D  🕒 4. DOWN::DownloadWorker: doWork() STARTED
18:11:35.630 com.example.echo_proto               D  🕒 5. DOWN::DownloadWorker: Dispatchers.IO context
18:11:35.631 com.example.echo_proto               D  🕒 6. DOWN::DownloadWorker: Got episodeId: 973
18:11:35.635 com.example.echo_proto               D  🕒 7. DOWN::DownloadWorker: Starting download flow...
18:11:35.641 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:35.657 com.example.echo_proto               D  🕒 10. DOWN::DownloadRepositoryImpl: START - Getting episode from DB
18:11:35.659 com.example.echo_proto               D  🕒 11. DOWN::DownloadRepositoryImpl: Got episode: Марк Солонин: Как и почему взрываются атомные реакторы ? (2026-28-04)
18:11:35.659 com.example.echo_proto               D  DownloadRepositoryImpl: File path: /data/user/0/com.example.echo_proto/files/episodes/UChLpUGaZO35ICTltBP50VSg::xGndBUO72Ck.mp3
18:11:35.659 com.example.echo_proto               D  File creation took 0 ms
18:11:35.659 com.example.echo_proto               D  🕒 12. DOWN::DownloadRepositoryImpl: Creating HTTP request...
18:11:35.659 com.example.echo_proto               I  Started foreground service Intent { act=ACTION_START_FOREGROUND cmp=com.example.echo_proto/androidx.work.impl.foreground.SystemForegroundService (has extras) }
18:11:35.662 com.example.echo_proto               D  🕒 13. DOWN::DownloadRepositoryImpl: Executing request...
18:11:35.662 com.example.echo_proto               D  🕒 13.1 DOWN::DownloadRepositoryImpl: Request created at 1777475495659
18:11:35.666 com.example.echo_proto               I  --> GET https://worker.feed-master.com/yt/media/c8299ec540ce615b9ff5e574ce03f5e45201d73d.mp3
18:11:35.666 com.example.echo_proto               I  Accept: audio/*
18:11:35.666 com.example.echo_proto               I  Accept-Encoding: identity
18:11:35.666 com.example.echo_proto               I  --> END GET
18:11:36.174 com.example.echo_proto               I  Skipped 46 frames!  The application may be doing too much work on its main thread.
18:11:36.178 com.example.echo_proto               W  PerfMonitor doFrame : time=4ms vsyncFrame=0 latency=517ms procState=-1 historyMsgCount=7 (msgIndex=5 wall=512ms seq=1201 late=5ms h=android.os.Handler c=kotlinx.coroutines.CancellableContinuationImpl)
18:11:36.180 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
18:11:36.184 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
18:11:36.186 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
18:11:36.187 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
18:11:36.188 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
18:11:36.188 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb
18:11:36.188 com.example.echo_proto               E  == MALI DEBUG ===BAD ALLOC from gles_texture_egl_image_get_2d_template err is 0
18:11:36.489 com.example.echo_proto               I  <-- 200 OK https://worker02.feed-master.com/yt/media/c8299ec540ce615b9ff5e574ce03f5e45201d73d.mp3 (822ms)
18:11:36.489 com.example.echo_proto               I  Server: nginx/1.22.1
18:11:36.489 com.example.echo_proto               I  Date: Wed, 29 Apr 2026 15:11:36 GMT
18:11:36.489 com.example.echo_proto               I  Content-Type: audio/mpeg
18:11:36.489 com.example.echo_proto               I  Content-Length: 67877967
18:11:36.489 com.example.echo_proto               I  Last-Modified: Tue, 28 Apr 2026 16:22:25 GMT
18:11:36.489 com.example.echo_proto               I  Connection: keep-alive
18:11:36.489 com.example.echo_proto               I  ETag: "69f0dec1-40bbc4f"
18:11:36.489 com.example.echo_proto               I  Accept-Ranges: bytes
18:11:38.031 com.example.echo_proto               D  submitList: oldSize=11, newSize=11
18:11:38.034 com.example.echo_proto               I  Skipped 166 frames!  The application may be doing too much work on its main thread.
18:11:38.040 com.example.echo_proto               W  PerfMonitor doFrame : time=6ms vsyncFrame=0 latency=1851ms procState=-1 historyMsgCount=8 (msgIndex=1 wall=527ms seq=1208 late=508ms h=android.os.Handler c=androidx.lifecycle.LiveData$1) (msgIndex=2 wall=760ms seq=1209 late=1026ms h=android.os.Handler c=kotlinx.coroutines.CancellableContinuationImpl) (msgIndex=3 wall=562ms seq=1210 late=1782ms h=android.os.Handler c=kotlinx.coroutines.CancellableContinuationImpl)
18:11:38.056 com.example.echo_proto               I  Davey! duration=1866ms; Flags=0, FrameTimelineVsyncId=5745836, IntendedVsync=139552330089375, Vsync=139554174533801, InputEventId=0, HandleInputStart=139554181211853, AnimationStart=139554181213622, PerformTraversalsStart=139554183278007, DrawStart=139554184217930, FrameDeadline=139552361200486, FrameInterval=139554181036776, FrameStartTime=11111111, SyncQueued=139554185007468, SyncStart=139554185066622, IssueDrawCommandsStart=139554185234930, SwapBuffers=139554191014007, FrameCompleted=139554196676160, DequeueBufferDuration=62461, QueueBufferDuration=1726461, GpuCompleted=139554196533930, SwapBuffersCompleted=139554196676160, DisplayPresentTime=0, 
18:11:38.116 com.example.echo_proto               D  submitList: oldSize=1721, newSize=1721
18:11:38.239 com.example.echo_proto               I  Background concurrent mark compact GC freed 15MB AllocSpace bytes, 22(480KB) LOS objects, 35% free, 42MB/66MB, paused 1.808ms,3.312ms total 179.092ms
18:11:44.737 com.example.echo_proto               I  Background concurrent mark compact GC freed 5244KB AllocSpace bytes, 8(144KB) LOS objects, 28% free, 61MB/85MB, paused 361us,2.412ms total 108.939ms
18:11:51.134 com.example.echo_proto               I  Background concurrent mark compact GC freed 28KB AllocSpace bytes, 0(0B) LOS objects, 21% free, 85MB/109MB, paused 370us,2.341ms total 109.778ms
18:11:54.826 com.example.echo_proto               I  <-- END HTTP (binary 67877967-byte body omitted)
18:11:54.828 com.example.echo_proto               D  🕒 13.2 DOWN::DownloadRepositoryImpl: Response received at 1777475514827 (19168 ms)
18:11:54.829 com.example.echo_proto               D  🕒 14. DOWN::DownloadRepositoryImpl: Got response, success: true
18:11:54.830 com.example.echo_proto               D  DownloadRepositoryImpl: response.isSuccessful
18:11:54.832 com.example.echo_proto               D  🕒 15. DOWN::DownloadRepositoryImpl: Total size: 67877967 bytes
18:11:54.833 com.example.echo_proto               D  🕒 16. DOWN::DownloadRepositoryImpl: Starting file copy...
18:11:54.841 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:54.867 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:54.883 com.example.echo_proto               D  DownloadRepositoryImpl: 1
18:11:54.905 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:54.919 com.example.echo_proto               D  DownloadRepositoryImpl: 2
18:11:54.938 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:54.951 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:3) isEGL=1, mPendingRelease.size()=1, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
18:11:54.952 com.example.echo_proto               D  DownloadRepositoryImpl: 3
18:11:54.964 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:3) isEGL=1, mPendingRelease.size()=2, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
18:11:54.965 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:54.975 com.example.echo_proto               D  DownloadRepositoryImpl: 4
18:11:54.986 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.006 com.example.echo_proto               D  DownloadRepositoryImpl: 5
18:11:55.021 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.030 com.example.echo_proto               D  DownloadRepositoryImpl: 6
18:11:55.043 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.052 com.example.echo_proto               D  DownloadRepositoryImpl: 7
18:11:55.059 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.074 com.example.echo_proto               D  DownloadRepositoryImpl: 8
18:11:55.082 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.091 com.example.echo_proto               D  DownloadRepositoryImpl: 9
18:11:55.101 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.112 com.example.echo_proto               D  DownloadRepositoryImpl: 10
18:11:55.125 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.136 com.example.echo_proto               D  DownloadRepositoryImpl: 11
18:11:55.144 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.155 com.example.echo_proto               D  DownloadRepositoryImpl: 12
18:11:55.162 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.168 com.example.echo_proto               D  DownloadRepositoryImpl: 13
18:11:55.176 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.188 com.example.echo_proto               D  DownloadRepositoryImpl: 14
18:11:55.196 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.209 com.example.echo_proto               D  DownloadRepositoryImpl: 15
18:11:55.221 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.233 com.example.echo_proto               D  DownloadRepositoryImpl: 16
18:11:55.246 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.261 com.example.echo_proto               D  DownloadRepositoryImpl: 17
18:11:55.267 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.279 com.example.echo_proto               D  DownloadRepositoryImpl: 18
18:11:55.289 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.307 com.example.echo_proto               D  DownloadRepositoryImpl: 19
18:11:55.316 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.338 com.example.echo_proto               D  DownloadRepositoryImpl: 20
18:11:55.354 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.381 com.example.echo_proto               D  DownloadRepositoryImpl: 21
18:11:55.393 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.411 com.example.echo_proto               D  DownloadRepositoryImpl: 22
18:11:55.429 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.445 com.example.echo_proto               D  DownloadRepositoryImpl: 23
18:11:55.451 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.458 com.example.echo_proto               D  DownloadRepositoryImpl: 24
18:11:55.473 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.483 system_server                        E  Package enqueue rate is 5.1751914. Shedding 0|com.example.echo_proto|2|null|10389. package=com.example.echo_proto
18:11:55.486 com.example.echo_proto               D  DownloadRepositoryImpl: 25
18:11:55.492 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.505 com.example.echo_proto               D  DownloadRepositoryImpl: 26
18:11:55.514 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.525 com.example.echo_proto               D  DownloadRepositoryImpl: 27
18:11:55.530 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.552 com.example.echo_proto               D  DownloadRepositoryImpl: 28
18:11:55.564 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.573 com.example.echo_proto               D  DownloadRepositoryImpl: 29
18:11:55.581 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.592 com.example.echo_proto               D  DownloadRepositoryImpl: 30
18:11:55.599 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.609 com.example.echo_proto               D  DownloadRepositoryImpl: 31
18:11:55.621 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.631 com.example.echo_proto               D  DownloadRepositoryImpl: 32
18:11:55.664 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.676 com.example.echo_proto               D  DownloadRepositoryImpl: 33
18:11:55.687 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.697 com.example.echo_proto               D  DownloadRepositoryImpl: 34
18:11:55.704 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.713 com.example.echo_proto               D  DownloadRepositoryImpl: 35
18:11:55.721 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.729 com.example.echo_proto               D  DownloadRepositoryImpl: 36
18:11:55.737 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.745 com.example.echo_proto               D  DownloadRepositoryImpl: 37
18:11:55.752 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.760 com.example.echo_proto               D  DownloadRepositoryImpl: 38
18:11:55.768 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.775 com.example.echo_proto               D  DownloadRepositoryImpl: 39
18:11:55.783 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.789 com.example.echo_proto               D  DownloadRepositoryImpl: 40
18:11:55.797 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.804 com.example.echo_proto               D  DownloadRepositoryImpl: 41
18:11:55.811 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.819 com.example.echo_proto               D  DownloadRepositoryImpl: 42
18:11:55.825 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.830 com.example.echo_proto               D  DownloadRepositoryImpl: 43
18:11:55.836 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.844 com.example.echo_proto               D  DownloadRepositoryImpl: 44
18:11:55.851 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.857 com.example.echo_proto               D  DownloadRepositoryImpl: 45
18:11:55.863 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.870 com.example.echo_proto               D  DownloadRepositoryImpl: 46
18:11:55.876 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.884 com.example.echo_proto               D  DownloadRepositoryImpl: 47
18:11:55.890 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.898 com.example.echo_proto               D  DownloadRepositoryImpl: 48
18:11:55.905 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.913 com.example.echo_proto               D  DownloadRepositoryImpl: 49
18:11:55.919 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.925 com.example.echo_proto               D  DownloadRepositoryImpl: 50
18:11:55.931 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.937 com.example.echo_proto               D  DownloadRepositoryImpl: 51
18:11:55.944 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.952 com.example.echo_proto               D  DownloadRepositoryImpl: 52
18:11:55.962 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.971 com.example.echo_proto               D  DownloadRepositoryImpl: 53
18:11:55.978 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:55.985 com.example.echo_proto               D  DownloadRepositoryImpl: 54
18:11:55.999 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.006 com.example.echo_proto               D  DownloadRepositoryImpl: 55
18:11:56.019 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.026 com.example.echo_proto               D  DownloadRepositoryImpl: 56
18:11:56.032 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.037 com.example.echo_proto               D  DownloadRepositoryImpl: 57
18:11:56.045 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.054 com.example.echo_proto               D  DownloadRepositoryImpl: 58
18:11:56.060 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.067 com.example.echo_proto               D  DownloadRepositoryImpl: 59
18:11:56.074 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.081 com.example.echo_proto               D  DownloadRepositoryImpl: 60
18:11:56.087 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.095 com.example.echo_proto               D  DownloadRepositoryImpl: 61
18:11:56.102 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.108 com.example.echo_proto               D  DownloadRepositoryImpl: 62
18:11:56.114 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.119 com.example.echo_proto               D  DownloadRepositoryImpl: 63
18:11:56.126 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.132 com.example.echo_proto               D  DownloadRepositoryImpl: 64
18:11:56.142 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.150 com.example.echo_proto               D  DownloadRepositoryImpl: 65
18:11:56.157 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.166 com.example.echo_proto               D  DownloadRepositoryImpl: 66
18:11:56.174 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.180 com.example.echo_proto               D  DownloadRepositoryImpl: 67
18:11:56.187 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.193 com.example.echo_proto               D  DownloadRepositoryImpl: 68
18:11:56.204 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.211 com.example.echo_proto               D  DownloadRepositoryImpl: 69
18:11:56.221 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.226 com.example.echo_proto               D  DownloadRepositoryImpl: 70
18:11:56.240 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.250 com.example.echo_proto               D  DownloadRepositoryImpl: 71
18:11:56.266 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.281 com.example.echo_proto               D  DownloadRepositoryImpl: 72
18:11:56.304 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.313 com.example.echo_proto               D  DownloadRepositoryImpl: 73
18:11:56.321 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.329 com.example.echo_proto               D  DownloadRepositoryImpl: 74
18:11:56.336 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.345 com.example.echo_proto               D  DownloadRepositoryImpl: 75
18:11:56.353 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.363 com.example.echo_proto               D  DownloadRepositoryImpl: 76
18:11:56.371 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.383 com.example.echo_proto               D  DownloadRepositoryImpl: 77
18:11:56.390 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.399 com.example.echo_proto               D  DownloadRepositoryImpl: 78
18:11:56.409 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.418 com.example.echo_proto               D  DownloadRepositoryImpl: 79
18:11:56.426 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.443 com.example.echo_proto               D  DownloadRepositoryImpl: 80
18:11:56.462 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.474 com.example.echo_proto               D  DownloadRepositoryImpl: 81
18:11:56.494 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.508 com.example.echo_proto               D  DownloadRepositoryImpl: 82
18:11:56.530 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.543 com.example.echo_proto               D  DownloadRepositoryImpl: 83
18:11:56.562 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.584 com.example.echo_proto               D  DownloadRepositoryImpl: 84
18:11:56.599 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.623 com.example.echo_proto               D  DownloadRepositoryImpl: 85
18:11:56.635 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.662 com.example.echo_proto               D  DownloadRepositoryImpl: 86
18:11:56.673 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.705 com.example.echo_proto               D  DownloadRepositoryImpl: 87
18:11:56.719 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.727 com.example.echo_proto               D  DownloadRepositoryImpl: 88
18:11:56.740 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.790 com.example.echo_proto               D  DownloadRepositoryImpl: 89
18:11:56.831 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.845 com.example.echo_proto               D  DownloadRepositoryImpl: 90
18:11:56.856 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.903 com.example.echo_proto               D  DownloadRepositoryImpl: 91
18:11:56.924 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.936 com.example.echo_proto               D  DownloadRepositoryImpl: 92
18:11:56.946 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.962 com.example.echo_proto               D  DownloadRepositoryImpl: 93
18:11:56.987 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:56.999 com.example.echo_proto               D  DownloadRepositoryImpl: 94
18:11:57.005 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.016 com.example.echo_proto               D  DownloadRepositoryImpl: 95
18:11:57.024 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.032 com.example.echo_proto               D  DownloadRepositoryImpl: 96
18:11:57.041 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.052 com.example.echo_proto               D  DownloadRepositoryImpl: 97
18:11:57.063 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.074 com.example.echo_proto               D  DownloadRepositoryImpl: 98
18:11:57.081 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.086 com.example.echo_proto               D  DownloadRepositoryImpl: 99
18:11:57.093 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.102 com.example.echo_proto               D  DownloadRepositoryImpl: 100
18:11:57.103 com.example.echo_proto               D  🕒 16.1 DOWN::DownloadRepositoryImpl: File copy completed in 2271 ms
18:11:57.103 com.example.echo_proto               D  🕒 17. DOWN::DownloadRepositoryImpl: Updating database...
18:11:57.114 com.example.echo_proto               I  Moving WorkSpec (5d794e98-5779-48da-aa7a-5ce14e786324) to the foreground
18:11:57.143 com.example.echo_proto               D  🕒 18. DOWN::DownloadRepositoryImpl: DONE
18:11:57.154 com.example.echo_proto               D  🕒 9. DOWN::DownloadWorker: Download COMPLETED
18:11:57.162 com.example.echo_proto               I  Worker result SUCCESS for Work [ id=5d794e98-5779-48da-aa7a-5ce14e786324, tags={ com.example.echo_proto.domain.worker.DownloadWorker } ]
18:11:57.181 com.example.echo_proto               I  Stopping foreground service
18:11:57.519 com.example.echo_proto               I  Background young concurrent mark compact GC freed 27MB AllocSpace bytes, 1(68KB) LOS objects, 15% free, 92MB/109MB, paused 561us,8.845ms total 290.521ms
18:11:58.921 com.example.echo_proto               I  Skipped 138 frames!  The application may be doing too much work on its main thread.
18:11:58.923 com.example.echo_proto               W  PerfMonitor doFrame : time=2ms vsyncFrame=0 latency=1544ms procState=-1 historyMsgCount=4 (msgIndex=2 wall=514ms seq=2103 late=14ms h=android.os.Handler c=kotlinx.coroutines.CancellableContinuationImpl) (msgIndex=3 wall=525ms seq=2104 late=528ms h=android.os.Handler c=kotlinx.coroutines.CancellableContinuationImpl) (msgIndex=4 wall=500ms seq=2105 late=1051ms h=android.os.Handler c=kotlinx.coroutines.CancellableContinuationImpl)
18:11:58.945 com.example.echo_proto               I  Davey! duration=1560ms; Flags=0, FrameTimelineVsyncId=5746548, IntendedVsync=139573524017195, Vsync=139575057350513, InputEventId=0, HandleInputStart=139575068368469, AnimationStart=139575068370623, PerformTraversalsStart=139575068939854, DrawStart=139575069397392, FrameDeadline=139573555128306, FrameInterval=139575068110623, FrameStartTime=11111111, SyncQueued=139575069712777, SyncStart=139575069769008, IssueDrawCommandsStart=139575069908162, SwapBuffers=139575077359854, FrameCompleted=139575084689085, DequeueBufferDuration=82153, QueueBufferDuration=1638539, GpuCompleted=139575084689085, SwapBuffersCompleted=139575083112700, DisplayPresentTime=0, 
18:11:59.419 com.example.echo_proto               D  submitList: oldSize=11, newSize=11
18:11:59.468 com.example.echo_proto               D  submitList: oldSize=1721, newSize=1721
18:11:59.468 com.example.echo_proto               I  Skipped 48 frames!  The application may be doing too much work on its main thread.
18:11:59.470 com.example.echo_proto               W  PerfMonitor doFrame : time=2ms vsyncFrame=0 latency=536ms procState=-1 historyMsgCount=6 (msgIndex=1 wall=495ms seq=2108 late=1536ms h=android.os.Handler c=androidx.lifecycle.LiveData$1)
18:11:59.500 com.example.echo_proto               E  [ViewRootImpl[MainActivity]#0](f:0,a:2) isEGL=1, mPendingRelease.size()=1, mMaxAcquiredBuffers=4, currentMaxAcquiredBufferCount=1
18:12:00.575 com.example.echo_proto               E  [AUX]GuiExtAuxCheckAuxPath:670: Null anb


---


## 🔍 Анализ проблемы с визуализацией прогресса загрузки

### 📊 Архитектура загрузки

**Поток данных:**
1. Пользователь нажимает `download` в EpisodeDetailFragment/EpisodeDetailFragmentV2
2. Создается `WorkRequest` для `DownloadWorker`
3. `WorkManager.enqueue(request)`
4. Fragment вызывает `showDownloadProgress(request.id)` - подписывается на `WorkInfo` по ID
5. `DownloadWorker` запускается, вызывает `downloadRepo.downloadEpisodeToDatabase()`
6. `DownloadRepositoryImpl` эмитит прогресс через `Flow<Int>`
7. `DownloadWorker` обновляет `setProgress()` с данными прогресса
8. Fragment получает progress через `workInfo.progress.getInt()`

### ❌ Проблема #1: OutOfMemoryError (из логов)

**Детали:**
- Файл: 387 MB (406,086,016 bytes)
- Ошибка при попытке выделить 8208 байт
- Heap заполнен до 256MB (limit)
- Стек ошибки: `okio.Segment.<init>` → `okio.SegmentPool.take`

**Корневая причина:**
OkHttp буферизует весь response body в памяти перед тем как отдать его через `byteStream()`. Для файла 387MB это превышает лимит heap и вызывает OutOfMemoryError.

**Доказательство из логов для файла 67MB:**
```
18:11:35.662 - Request created
18:11:36.489 - Response received (822ms) - Content-Length: 67877967 bytes
18:11:54.826 - <-- END HTTP (binary body omitted) - весь body получен
18:11:54.828 - Response received at 19168 ms (19 СЕКУНД после request!)
18:11:54.833 - Starting file copy...
18:11:54.883 - DownloadRepositoryImpl: 1 (первый emit прогресса)
18:11:57.103 - File copy completed in 2271 ms
```

**Анализ:**
- Пауза 19 секунд между получением response и началом копирования файла
- Это время когда OkHttp буферизует весь response body в памяти
- UI не показывает прогресс потому что копирование на диск еще не началось
- Само копирование занимает только 2.3 секунды
- Для файла 387MB буферизация заняла бы ~60+ секунд и вызвала OOM

**Код проблемы:**
```kotlin
// DownloadRepository.kt:65-91
body.byteStream().use { input ->
    FileOutputStream(file).use { output ->
        val buffer = ByteArray(64 * 1024) // 64KB buffer
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            downloadedSize += bytesRead
            // progress emit logic...
        }
    }
}
```

### ❌ Проблема #2: Визуализация прогресса (пользовательский комментарий)

**Симптомы:**
- При нажатии кнопки "download" визуально ничего не происходит
- Прогресс резко перескакивает на 60-80% когда загрузилось около этого значения
- Потом плавно "докачивает" и показывает прогресс в UI

**Дополнительные наблюдения пользователя:**
- Проблема с отображением НЕ зависит от размера файла
- При скачивании файлов 5-10MB и 100-150MB поведение одинаковое
- Прогресс подхватывается в конце (когда скачано 60-80%)
- От размера файла зависит только скорость докачки (5 сек для маленьких, 30-60 сек для больших)
- Для файла 67MB: пауза 15-20 секунд перед обновлениями прогресса, потом все докачалось почти в момент

**Корневая причина (ОБНОВЛЕНО на основе логов):**
Проблема НЕ в логике эмита прогресса. Проблема в том что OkHttp буферизует весь response body в памяти перед тем как отдать его через `byteStream()`.

**Доказательство из логов для файла 67MB:**
```
18:11:35.662 - Request created
18:11:36.489 - Response received (822ms) - Content-Length: 67877967 bytes
18:11:54.826 - <-- END HTTP (binary body omitted) - весь body получен
18:11:54.828 - Response received at 19168 ms (19 СЕКУНД после request!)
18:11:54.833 - Starting file copy...
18:11:54.883 - DownloadRepositoryImpl: 1 (первый emit прогресса)
18:11:57.103 - File copy completed in 2271 ms
```

**Анализ:**
- Пауза 19 секунд между получением response и началом копирования файла
- Это время когда OkHttp буферизует весь response body в памяти
- UI не показывает прогресс потому что копирование на диск еще не началось
- Само копирование занимает только 2.3 секунды
- Эмиты прогресса идут часто (каждые ~10-20мс) - логика эмита работает нормально
- Проблема в том что копирование начинается только ПОСЛЕ полной буферизации response body

**Корневая причина #3: Отсутствие "Queued" статуса**
- Fragment не показывает "Queued" статус сразу после `enqueue`
- Пользователь не видит что задача поставлена в очередь
- Только когда Worker переходит в `RUNNING` состояние, появляется прогресс

**Код проблемы:**
```kotlin
// EpisodeDetailFragment.kt:149-161
private fun showDownloadProgress(id: UUID) {
    WorkManager.getInstance(requireContext())
        .getWorkInfoByIdLiveData(id)
        .observe(this) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.ENQUEUED -> {
                    binding.btnDownload.text = "Queued"  // Только в EpisodeDetailFragment
                }
                WorkInfo.State.RUNNING -> {
                    val progress = workInfo.progress.getInt(DownloadWorker.PROGRESS, 0)
                    binding.btnDownload.text = "$progress%"
                    binding.progressBar.progress = progress
                }
                // ...
            }
        }
}
```

**EpisodeDetailFragmentV2 не имеет ENQUEUED обработки:**
```kotlin
// EpisodeDetailFragmentV2.kt:338-350
private fun showDownloadProgress(id: UUID) {
    WorkManager.getInstance(requireContext())
        .getWorkInfoByIdLiveData(id)
        .observe(viewLifecycleOwner) { workInfo ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> {  // Нет ENQUEUED обработки!
                    val progress = workInfo.progress.getInt(DownloadWorker.PROGRESS, 0)
                    updateDownloadButtonState("$progress%", true)
                    updateProgressBar(progress)
                }
                // ...
            }
        }
}
```

### 💡 Пути решения

#### Для OutOfMemoryError И визуализации прогресса (ОБЩАЯ ПРОБЛЕМА):

**Вариант 1: Использовать Okio source() для потоковой загрузки (РЕКОМЕНДУЕТСЯ)**
```kotlin
// DownloadRepository.kt
override suspend fun downloadEpisodeToDatabase(episodeId: Int): Flow<Int> = flow {
    val episode = db.dao.getEpisodeById(episodeId).toEpisode()
    val file = makeEpisodeFilepath(episode)

    val request = Request.Builder()
        .url(episode.audioLink)
        .header("Accept", "audio/*")
        .build()

    val response = okHttpClient.newCall(request).execute()

    if (response.isSuccessful) {
        val totalSize = response.body?.contentLength() ?: 0L
        var downloadedSize = 0L
        var lastEmitTime = System.currentTimeMillis()
        var progress = 0

        // Используем Okio source для потоковой загрузки
        response.body?.source()?.use { source ->
            file.sink().buffer().use { sink ->
                val buffer = Buffer()
                var bytesRead: Long

                emit(0) // Начальный прогресс

                while (source.read(buffer, 8192).also { bytesRead = it } != -1L) {
                    sink.write(buffer, bytesRead)
                    downloadedSize += bytesRead
                    buffer.clear()

                    val currentTime = System.currentTimeMillis()
                    val currentProgress = if (totalSize > 0) {
                        ((downloadedSize * 100) / totalSize).toInt()
                    } else {
                        0
                    }

                    // Эмитим каждые 100мс или при изменении на 1%
                    if (currentProgress > progress || (currentTime - lastEmitTime) > 100) {
                        progress = currentProgress
                        lastEmitTime = currentTime
                        emit(progress.coerceIn(0, 99))
                    }
                }
            }
        }
    }
}
```

**Преимущества:**
- Okio source читает данные по мере поступления, не буферизует весь response
- Прогресс будет обновляться в реальном времени во время загрузки
- Решает проблему OutOfMemoryError для больших файлов
- Решает проблему визуализации прогресса

**Вариант 2: Настроить OkHttpClient для потоковой загрузки**
```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    })
    .build()
```

**Проблема:** HttpLoggingInterceptor с Level.BODY буферизует весь response body для логирования. Нужно отключить или изменить уровень логирования для загрузки файлов.

**Вариант 3: Использовать системный DownloadManager**
- Системный DownloadManager умеет загружать большие файлы
- Но требует больше изменений в архитектуре

#### Для визуализации прогресса (дополнительные улучшения):

**Вариант 4: Добавить "Queued" статус во все фрагменты**
- Добавить обработку `WorkInfo.State.ENQUEUED` в EpisodeDetailFragmentV2
- Показывать "Queued" или иконку загрузки сразу после enqueue

**Вариант 5: Оптимистичный UI**
- Показывать "Downloading..." сразу после нажатия кнопки
- Не ждать первого эмита от Worker

### 📝 Рекомендуемый план

1. **Срочно:** Исправить OutOfMemoryError и визуализацию прогресса - использовать Okio source() для потоковой загрузки (Вариант 1)
2. **Важно:** Проверить HttpLoggingInterceptor - возможно он буферизует response body (Вариант 2)
3. **Важно:** Добавить ENQUEUED обработку в EpisodeDetailFragmentV2 (Вариант 4)
4. **Желательно:** Оптимистичный UI - показывать "Downloading..." сразу после нажатия (Вариант 5)