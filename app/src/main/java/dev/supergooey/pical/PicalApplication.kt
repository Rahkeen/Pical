package dev.supergooey.pical

import android.app.Application
import android.content.Context
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

class PicalApplication: Application() {
  lateinit var cameraStore: CameraStore
  lateinit var calorieClient: CalorieClient

  override fun onCreate() {
    super.onCreate()

    Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(
      configuration = PurchasesConfiguration.Builder(
        context = this,
        apiKey = BuildConfig.REVENUECAT_KEY
      ).build()
    )

    cameraStore = CameraStore(applicationContext)
    calorieClient = CalorieClient()
  }
}

fun Context.cameraStore(): CameraStore {
  return (this.applicationContext as PicalApplication).cameraStore
}

fun Context.calorieClient(): CalorieClient {
  return (this.applicationContext as PicalApplication).calorieClient
}