package dev.supergooey.pical.features.paywall

import android.util.Log
import androidx.lifecycle.ViewModel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.models.Period
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PayOptionState(
  val id: String,
  val title: String,
  val priceFormatted: String,
)

interface PaywallFeature {
  data class State(
    val options: List<PayOptionState> = emptyList()
  )
}

class PaywallViewModel() : ViewModel() {
  private val internalState = MutableStateFlow(PaywallFeature.State())
  val state = internalState.asStateFlow()

  init {
    Purchases.sharedInstance.getOfferingsWith(
      onError = { error: PurchasesError ->
        Log.e("RevenueCat Error", "$error")
      },
      onSuccess = { offerings ->
        offerings.current?.let { currentOffering ->
          Log.d("RevenueCat Success", "$currentOffering")
          internalState.update { previousState ->
            previousState.copy(
              options = currentOffering
                .availablePackages
                .map { it.toPayOptionState() }
            )
          }
        }
      }
    )
  }
}

fun Package.toPayOptionState(): PayOptionState {
  val title = if (product.period?.unit == Period.Unit.MONTH) {
    "Monthly"
  } else {
    "Annual"
  }
  return PayOptionState(
    id = product.id,
    title = title,
    priceFormatted = product.price.formatted
  )
}

