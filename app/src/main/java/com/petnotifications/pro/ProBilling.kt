package com.petnotifications.pro

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.petnotifications.Accessory
import com.petnotifications.Outfit
import com.petnotifications.PetType
import com.petnotifications.Prefs
import com.petnotifications.Shop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

/**
 * Compras con Google Play: Pro (un producto de pago único, [PRODUCT_ID]), los accesorios de
 * temporada del vestidor (uno por accesorio, Accessory.productId) y los packs de pelajes (uno por
 * mascota, Prefs.coatPackId), todos de pago único. La compra la hace la
 * app Play Store; acá solo se pide, se confirma y se guarda el resultado en [Prefs.proPurchased],
 * así Pro funciona sin conexión (los accesorios, en Outfit.KEY_BOUGHT). Cada vez que se abre la app se vuelve a consultar a Play (eso
 * también es "Restaurar compras": en un celular nuevo aparece la compra hecha con la misma cuenta).
 */
object ProBilling {
    const val PRODUCT_ID = "pro_lifetime"

    enum class Status { IDLE, WORKING, UNAVAILABLE, PENDING, ERROR }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mutex = Mutex()
    private var client: BillingClient? = null
    private var product: ProductDetails? = null
    private var accProducts: Map<String, ProductDetails> = emptyMap()

    private val _status = MutableStateFlow(Status.IDLE)
    val status: StateFlow<Status> = _status

    /** Precio en la moneda del usuario, tal como lo muestra Play ("US$ 4,99"), o null si no se sabe. */
    private val _price = MutableStateFlow<String?>(null)
    val price: StateFlow<String?> = _price

    /** Precio de cada accesorio a la venta (por productId), con la moneda y el precio del país del usuario. */
    private val _accPrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val accPrices: StateFlow<Map<String, String>> = _accPrices

    /** Aumenta cada vez que cambia si el usuario tiene Pro, para que la pantalla se actualice. */
    private val _changes = MutableStateFlow(0)
    val changes: StateFlow<Int> = _changes

    /** Volver a armar las pantallas con el plan actual (por ejemplo, tras cambiar el plan de dueño). */
    fun notifyChanged() {
        _changes.value++
    }

    /** Consulta a Play si el usuario compró Pro y el precio. Seguro llamarla seguido. */
    fun refresh(ctx: Context) {
        val app = ctx.applicationContext
        scope.launch { refreshNow(app) }
    }

    private suspend fun refreshNow(ctx: Context): Boolean = mutex.withLock {
        val c = connected(ctx) ?: run {
            _status.value = Status.UNAVAILABLE
            return false
        }
        val (result, purchases) = suspendCancellableCoroutine { cont ->
            c.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            ) { r, list -> cont.resume(r to list) }
        }
        // Solo con una respuesta válida de Play se cambia lo guardado (sin conexión se conserva).
        if (result.responseCode == BillingResponseCode.OK) handlePurchases(ctx, purchases, fromQuery = true)
        if (product == null) loadProduct(c)
        if (_status.value == Status.UNAVAILABLE) _status.value = Status.IDLE
        true
    }

    /** Abre la pantalla de pago de Google Play (de Pro, o del accesorio con ese [productId]). */
    fun buy(activity: Activity, productId: String = PRODUCT_ID) {
        scope.launch {
            _status.value = Status.WORKING
            val c = mutex.withLock { connected(activity) }
            fun known() = if (productId == PRODUCT_ID) product else accProducts[productId]
            val details = known() ?: c?.let { loadProduct(it); known() }
            if (c == null || details == null) {
                _status.value = Status.UNAVAILABLE
                return@launch
            }
            val params = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).build()),
                )
                .build()
            val r = c.launchBillingFlow(activity, params)
            if (r.responseCode != BillingResponseCode.OK) {
                _status.value = if (r.responseCode == BillingResponseCode.ITEM_ALREADY_OWNED) {
                    refreshNow(activity.applicationContext)
                    Status.IDLE
                } else Status.ERROR
            }
            // Si abrió bien, el resultado llega a onPurchasesUpdated.
        }
    }

    /** "Restaurar compras": vuelve a preguntar a Play. */
    fun restore(ctx: Context) {
        val app = ctx.applicationContext
        scope.launch {
            _status.value = Status.WORKING
            if (refreshNow(app)) _status.value = Status.IDLE
        }
    }

    private suspend fun connected(ctx: Context): BillingClient? {
        client?.takeIf { it.isReady }?.let { return it }
        val app = ctx.applicationContext
        val c = client ?: BillingClient.newBuilder(app)
            .setListener { result, purchases -> onPurchasesUpdated(app, result, purchases) }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
            .also { client = it }
        val ok = suspendCancellableCoroutine { cont ->
            c.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (cont.isActive) cont.resume(result.responseCode == BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
        if (!ok) {
            runCatching { c.endConnection() }
            client = null
        }
        return c.takeIf { ok }
    }

    /** Pide a Play Pro y lo que se vende aparte (si está a la venta, ver Shop) juntos; un producto que no está cargado en Play Console no vuelve. */
    private suspend fun loadProduct(c: BillingClient): ProductDetails? {
        val ids = listOf(PRODUCT_ID) +
            (if (Shop.SEASONAL_FOR_SALE) Accessory.forSale.mapNotNull { it.productId } else emptyList()) +
            (if (Shop.COATS_FOR_SALE) PetType.launch.map(Prefs::coatPackId) else emptyList())
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                ids.map {
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(it)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                },
            )
            .build()
        val list = suspendCancellableCoroutine { cont ->
            c.queryProductDetailsAsync(params) { _, result -> cont.resume(result.productDetailsList) }
        }
        val details = list.firstOrNull { it.productId == PRODUCT_ID }
        product = details
        _price.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
        accProducts = list.filter { it.productId != PRODUCT_ID }.associateBy { it.productId }
        _accPrices.value = accProducts.mapNotNull { (id, d) -> d.oneTimePurchaseOfferDetails?.formattedPrice?.let { id to it } }.toMap()
        return details
    }

    private fun onPurchasesUpdated(ctx: Context, result: BillingResult, purchases: List<Purchase>?) {
        _status.value = when (result.responseCode) {
            BillingResponseCode.OK -> Status.IDLE
            BillingResponseCode.USER_CANCELED -> Status.IDLE
            BillingResponseCode.ITEM_ALREADY_OWNED -> {
                refresh(ctx)
                Status.IDLE
            }
            else -> Status.ERROR
        }
        if (result.responseCode == BillingResponseCode.OK && purchases != null) {
            scope.launch { handlePurchases(ctx, purchases, fromQuery = false) }
        }
    }

    /**
     * Marca Pro solo con compras confirmadas (no "pendientes", como un pago en efectivo que todavía
     * no se hizo). Play reembolsa solo si no se "reconoce" la compra en 3 días: se reconoce acá.
     */
    private suspend fun handlePurchases(ctx: Context, purchases: List<Purchase>, fromQuery: Boolean) {
        val pro = purchases.filter { PRODUCT_ID in it.products }
        val owned = pro.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        if (pro.any { it.purchaseState == Purchase.PurchaseState.PENDING } && !owned) _status.value = Status.PENDING
        val prefs = Prefs(ctx)
        // Una consulta completa puede sacar Pro (reembolso); una compra nueva solo lo agrega.
        if (fromQuery || owned) {
            if (prefs.proPurchased != owned) {
                if (owned && prefs.triesBeforePurchase < 0) prefs.triesBeforePurchase = prefs.tryCount
                prefs.proPurchased = owned
                _changes.value++
            }
        }
        // Accesorios: la consulta completa deja exactamente lo que Play dice; una compra nueva lo suma y se lo pone.
        val accs = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap { it.products }.mapNotNull(Accessory::ofProduct).toSet()
        // Packs de pelajes: igual que los accesorios.
        val packs = purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .flatMap { it.products }.mapNotNull(Prefs::coatPackPet).toSet()
        val packsBefore = prefs.sp.getStringSet(Prefs.KEY_COAT_PACKS, emptySet()).orEmpty().mapNotNull(Prefs::coatPackPet).toSet()
        val packsNow = if (fromQuery) packs else packsBefore + packs
        if (packsNow != packsBefore) {
            prefs.setCoatPacks(packsNow)
            _changes.value++
        }
        if (!fromQuery && purchases.any { p ->
                p.purchaseState == Purchase.PurchaseState.PENDING &&
                    p.products.any { Accessory.ofProduct(it) != null || Prefs.coatPackPet(it) != null }
            }) {
            _status.value = Status.PENDING
        }
        val before = Prefs(ctx).sp.getStringSet(Outfit.KEY_BOUGHT, emptySet()).orEmpty().mapNotNull(Accessory::ofProduct).toSet()
        val now = if (fromQuery) accs else before + accs
        if (now != before) {
            Outfit.setBought(ctx, now)
            if (!fromQuery) (accs - before).forEach { Outfit.wear(ctx, it) }
            _changes.value++
        }
        val c = client ?: return
        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }.forEach { p ->
            suspendCancellableCoroutine { cont ->
                c.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(p.purchaseToken).build()) {
                    cont.resume(Unit)
                }
            }
        }
    }
}
