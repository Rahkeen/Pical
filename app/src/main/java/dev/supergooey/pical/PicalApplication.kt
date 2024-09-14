package dev.supergooey.pical

import android.app.Application
import android.content.Context

class PicalApplication: Application() {
  lateinit var cameraStore: CameraStore
  override fun onCreate() {
    super.onCreate()

    cameraStore = CameraStore(applicationContext)
  }
}

fun Context.cameraStore(): CameraStore {
  return (this.applicationContext as PicalApplication).cameraStore
}