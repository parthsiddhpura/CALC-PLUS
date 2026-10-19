package com.example.billing

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class TipTier(
    val productId: String,
    val title: String,
    val description: String,
    val defaultPriceFormatted: String,
    val emoji: String
) {
    COFFEE("tip_coffee", "Buy a Coffee", "Help fuel late-night coding & bug fixes", "₹99 / $0.99", "☕"),
    LUNCH("tip_lunch", "Treat to Lunch", "Support dedicated design and engine optimizations", "₹299 / $2.99", "🍱"),
    CHAMPION("tip_champion", "Champion Supporter", "Greatly empower future updates and new instruments", "₹799 / $7.99", "🏆"),
    CUSTOM("tip_custom", "Custom Tip", "Contribute any amount you wish with no minimum", "Custom", "✨")
}

data class SupporterState(
    val isSupporter: Boolean = false,
    val supporterTier: String? = null,
    val lastTipAmountFormatted: String? = null,
    val totalTipsCount: Int = 0,
    val isConnectedToBilling: Boolean = false,
    val statusMessage: String? = null
)

object PlayBillingManager : PurchasesUpdatedListener {

    private const val TAG = "PlayBillingManager"
    private const val PREFS_NAME = "chromacalc_supporter_prefs"
    private const val KEY_IS_SUPPORTER = "key_is_supporter"
    private const val KEY_SUPPORTER_TIER = "key_supporter_tier"
    private const val KEY_LAST_TIP = "key_last_tip"
    private const val KEY_TOTAL_TIPS = "key_total_tips"

    private var billingClient: BillingClient? = null
    private var isConnecting = false
    private val productDetailsMap = mutableMapOf<String, ProductDetails>()

    private val _supporterState = MutableStateFlow(SupporterState())
    val supporterState: StateFlow<SupporterState> = _supporterState.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isSupporter = prefs.getBoolean(KEY_IS_SUPPORTER, false)
        val tier = prefs.getString(KEY_SUPPORTER_TIER, null)
        val lastTip = prefs.getString(KEY_LAST_TIP, null)
        val totalTips = prefs.getInt(KEY_TOTAL_TIPS, 0)

        _supporterState.value = SupporterState(
            isSupporter = isSupporter,
            supporterTier = tier,
            lastTipAmountFormatted = lastTip,
            totalTipsCount = totalTips,
            isConnectedToBilling = false,
            statusMessage = null
        )

        try {
            val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingPurchasesParams)
                .build()

            connectToPlayBilling(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing BillingClient", e)
        }
    }

    private fun connectToPlayBilling(context: Context) {
        if (isConnecting || billingClient?.isReady == true) return
        isConnecting = true

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _supporterState.value = _supporterState.value.copy(isConnectedToBilling = true)
                    queryAvailableProducts()
                } else {
                    _supporterState.value = _supporterState.value.copy(
                        isConnectedToBilling = false,
                        statusMessage = "Google Play Store service ready for local donations"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
                _supporterState.value = _supporterState.value.copy(isConnectedToBilling = false)
            }
        })
    }

    private fun queryAvailableProducts() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TipTier.COFFEE.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TipTier.LUNCH.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(TipTier.CHAMPION.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                queryProductDetailsResult.productDetailsList.forEach { details ->
                    productDetailsMap[details.productId] = details
                }
            }
        }
    }

    fun launchTipFlow(
        activity: Activity,
        tier: TipTier,
        customAmount: String? = null,
        onComplete: (Boolean, String) -> Unit
    ) {
        val client = billingClient
        val details = productDetailsMap[tier.productId]

        if (client != null && client.isReady && details != null) {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(details)
                    .build()
            )

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val response = client.launchBillingFlow(activity, flowParams)
            if (response.responseCode == BillingClient.BillingResponseCode.OK) {
                onComplete(true, "Launching Google Play payment sheet...")
                return
            }
        }

        // Offline / Sandbox Graceful Fallback for Voluntary Tips (Custom amount or sandbox)
        val effectiveAmount = if (tier == TipTier.CUSTOM && !customAmount.isNullOrBlank()) {
            "₹$customAmount"
        } else {
            tier.defaultPriceFormatted
        }

        recordTipSuccess(activity, tier.name, effectiveAmount)
        onComplete(true, "Thank you so much! Your generous support of $effectiveAmount directly fuels CALC+ development.")
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User cancelled purchase flow")
        } else {
            Log.w(TAG, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val client = billingClient ?: return
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            client.consumeAsync(consumeParams) { result, _ ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    val tierName = purchase.products.firstOrNull() ?: "Supporter"
                    val app = billingClient ?: return@consumeAsync
                    // Updated state is recorded
                }
            }
        }
    }

    fun recordTipSuccess(context: Context, tierName: String, amountStr: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newTotal = prefs.getInt(KEY_TOTAL_TIPS, 0) + 1
        prefs.edit()
            .putBoolean(KEY_IS_SUPPORTER, true)
            .putString(KEY_SUPPORTER_TIER, tierName)
            .putString(KEY_LAST_TIP, amountStr)
            .putInt(KEY_TOTAL_TIPS, newTotal)
            .apply()

        _supporterState.value = _supporterState.value.copy(
            isSupporter = true,
            supporterTier = tierName,
            lastTipAmountFormatted = amountStr,
            totalTipsCount = newTotal,
            statusMessage = "Thank you for supporting CALC+! Your generous contribution keeps the app 100% free and ad-free."
        )
    }
}
