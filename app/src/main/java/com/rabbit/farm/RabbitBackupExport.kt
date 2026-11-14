package com.rabbit.farm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/** Exports existing preferences without changing or clearing the installed database. */
fun exportRabbitBackup(context: Context, destination: Uri) {
    val prefs = context.getSharedPreferences("rabbit_prefs", Context.MODE_PRIVATE)
    val data = JSONObject()
    val sets = listOf("rabbit_list", "meat_list", "adult_death_list", "mating_list",
        "young_list", "young_death_history", "adult_sales", "young_sales")
    for (key in sets) data.put(key, JSONArray((prefs.getStringSet(key, emptySet()) ?: emptySet()).toList()))
    for (key in listOf("young_meat_total", "young_death_total")) data.put(key, prefs.getInt(key, 0))
    val photos = JSONObject()
    val missing = JSONArray()
    val uris = listOf("rabbit_list", "meat_list", "adult_death_list")
        .flatMap { (prefs.getStringSet(it, emptySet()) ?: emptySet()).toList() }
        .mapNotNull { it.split('|').getOrNull(4)?.takeIf(String::isNotBlank) }.distinct()
    for (value in uris) {
        try {
            val uri = Uri.parse(value)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            require(bounds.outWidth > 0 && bounds.outHeight > 0)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 1280) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: error("Photo unavailable")
            val scale = minOf(1f, 640f / maxOf(bitmap.width, bitmap.height))
            val resized = Bitmap.createScaledBitmap(bitmap,
                maxOf(1, (bitmap.width * scale).toInt()), maxOf(1, (bitmap.height * scale).toInt()), true)
            try {
                val bytes = ByteArrayOutputStream().use { output ->
                    check(resized.compress(Bitmap.CompressFormat.JPEG, 78, output))
                    output.toByteArray()
                }
                photos.put(value, "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP))
            } finally {
                if (resized !== bitmap) resized.recycle()
                bitmap.recycle()
            }
        } catch (_: Exception) { missing.put(value) }
    }
    val backup = JSONObject().put("format", "rabbitfarm-android-prefs").put("version", 1)
        .put("packageName", context.packageName).put("prefs", data)
        .put("photos", photos).put("unavailablePhotos", missing)
    val output = context.contentResolver.openOutputStream(destination)
        ?: error("Не удалось открыть файл для записи")
    output.bufferedWriter(Charsets.UTF_8).use { it.write(backup.toString(2)) }
}
