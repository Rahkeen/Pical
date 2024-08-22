package dev.supergooey.caloriesnap

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CameraStore(context: Context) {
  private val resolver = context.contentResolver
  private val contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

  suspend fun saveImageLocally(bitmap: Bitmap): Result<Unit> = withContext(Dispatchers.IO) {
    val timestamp = System.currentTimeMillis()
    val contentValues = ContentValues().apply   {
      put(MediaStore.Images.Media.DISPLAY_NAME, "image-$timestamp.jpg")
      put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
      put(MediaStore.MediaColumns.DATE_TAKEN, timestamp)
      put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/Pical")
      put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val imageMediaStoreUri = resolver.insert(contentUri, contentValues)

    val result: Result<Unit> = imageMediaStoreUri?.let { uri ->
      runCatching {
        resolver.openOutputStream(uri).use { stream ->
          checkNotNull(stream)
          bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
          contentValues.clear()
          contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
          resolver.update(uri, contentValues, null, null)
        }
        Result.success(Unit)
      }.getOrElse { e ->
        resolver.delete(uri, null, null)
        Result.failure(e)
      }
    } ?: Result.failure(Exception("Couldn't create file"))

    result
  }
}

private const val FILENAME_FORMAT = "dd-MM-yyyy hh-mm-ss"