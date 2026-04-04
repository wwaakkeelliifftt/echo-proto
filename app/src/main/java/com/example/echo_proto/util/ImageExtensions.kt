package com.example.echo_proto.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.echo_proto.R

/**
 * Универсальное расширение для ImageView для загрузки обложек эпизодов
 */
fun ImageView.loadEpisodeImage(
    url: String?,
    fallbackUrl: String? = null,
    size: Int? = null,
    cornerRadius: Int = 24 
) {
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

/**
 * Загружает фоновое изображение на весь экран (CenterCrop без скругления)
 */
fun ImageView.loadFullScreenBackground(url: String?) {
    if (url.isNullOrEmpty()) return
    
    Glide.with(this)
        .load(url)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade(500))
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Загружает Bitmap из Uri для системных нужд (например, для нотификации)
 */
fun Context.loadBitmapFromUri(uri: Uri?, callback: (Bitmap) -> Unit) {
    if (uri == null) return
    
    Glide.with(this)
        .asBitmap()
        .load(uri)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                callback(resource)
            }
            override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
}
