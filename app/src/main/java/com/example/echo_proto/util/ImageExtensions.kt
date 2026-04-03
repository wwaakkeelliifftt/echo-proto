package com.example.echo_proto.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.echo_proto.R

/**
 * Универсальное расширение для ImageView для загрузки обложек эпизодов
 * @param url Основная ссылка на изображение (эпизода)
 * @param fallbackUrl Резервная ссылка (например, изображение канала)
 * @param size Размер в dp (будет использоваться и для ширины, и для высоты)
 * @param cornerRadius Скругление углов в пикселях (по умолчанию 24)
 */
fun ImageView.loadEpisodeImage(
    url: String?,
    fallbackUrl: String? = null,
    size: Int? = null,
    cornerRadius: Int = 24 
) {
    // Выбираем лучший доступный URL
    val finalUrl = when {
        !url.isNullOrEmpty() -> url
        !fallbackUrl.isNullOrEmpty() -> fallbackUrl
        else -> null
    }

    if (finalUrl == null) {
        setImageResource(R.drawable.ic_image_holder)
        return
    }

    var request = Glide.with(this)
        .load(finalUrl)
        .transition(DrawableTransitionOptions.withCrossFade())
        .placeholder(R.drawable.ic_image_holder)
        .error(R.drawable.ic_image_holder)
        .transform(CenterCrop(), RoundedCorners(cornerRadius))

    if (size != null) {
        val px = (size * resources.displayMetrics.density).toInt()
        request = request.override(px, px)
    }

    request.into(this)
}

/**
 * Специальная версия для больших обложек (например, в плеере или деталях)
 */
fun ImageView.loadLargeCover(url: String?, fallbackUrl: String? = null) {
    loadEpisodeImage(url, fallbackUrl = fallbackUrl, cornerRadius = 32)
}
