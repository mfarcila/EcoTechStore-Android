package com.ecotech.store

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import java.text.NumberFormat
import java.util.Locale

data class ProductItem(val id: String, val name: String, val price: Int, val specs: String, var qty: Int = 1)
data class SalesTransaction(val txId: String, val itemsSummary: String, val totalAmount: Int, var method: String)
data class PendingProduct(val id: String, val name: String, val vendor: String)

object StoreRepository {
    val cart = mutableListOf<ProductItem>()

    val catalog = mutableListOf(
        ProductItem("MACBOOK", "MacBook Air M1 256GB (Eco-Certificado)", 2450000, "• Estado: Reacondicionado Grado A\n• Dimensiones: 304.1 x 212.4 x 16.1 mm\n• Garantia: 12 meses (Ley 1480)"),
        ProductItem("PIXEL", "Google Pixel 8 Pro 128GB - Grado A", 2100000, "• Estado: Grado A (Bateria 96%)\n• Dimensiones: 162.6 x 76.5 x 8.8 mm\n• Garantia: 12 meses (Ley 1480)"),
        ProductItem("SOLAR", "Cargador Solar Portatil EcoCharge 20W", 185000, "• Estado: Nuevo Sustentable\n• Dimensiones: 150 x 80 x 15 mm\n• Garantia: 24 meses (Ley 1480)")
    )

    val pendingModeration = mutableListOf(
        PendingProduct("PEND_1", "iPhone 13 Reacondicionado", "TechCircular (Vendedor Externo)")
    )

    val transactions = mutableListOf(
        SalesTransaction("5501", "1x Google Pixel 8 Pro 128GB", 2100000, "PSE (Completada - Decreto 587)")
    )

    fun addToCart(item: ProductItem) {
        val existing = cart.find { it.id == item.id }
        if (existing != null) {
            existing.qty += 1
        } else {
            cart.add(item.copy(qty = 1))
        }
    }

    fun formatCop(amount: Int): String {
        val format = NumberFormat.getNumberInstance(Locale("es", "CO"))
        return "$ " + format.format(amount) + " COP"
    }

    fun getTotal(): Int = cart.sumOf { it.price * it.qty }
}

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.btnBiometric).setOnClickListener {
            Toast.makeText(this, "BiometricPrompt: Huella autenticada", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnGoogleOAuth).setOnClickListener {
            Toast.makeText(this, "OAuth 2.0: Cuenta vinculada", Toast.LENGTH_SHORT).show()
        }
        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
        findViewById<Button>(R.id.btnEnterBuyer).setOnClickListener { openRole("BUYER") }
        findViewById<Button>(R.id.btnEnterSeller).setOnClickListener { openRole("SELLER") }
        findViewById<Button>(R.id.btnEnterAdmin).setOnClickListener { openRole("ADMIN") }
    }

    private fun openRole(role: String) {
        val intent = Intent(this, RoleHostActivity::class.java)
        intent.putExtra("ROLE", role)
        startActivity(intent)
    }
}

class RecoverPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)
        findViewById<Button>(R.id.btnSendRecover).setOnClickListener {
            Toast.makeText(this, "Enlace de recuperacion enviado", Toast.LENGTH_SHORT).show()
            finish()
        }
        findViewById<Button>(R.id.btnBackLogin).setOnClickListener { finish() }
    }
}

class RoleHostActivity : AppCompatActivity() {
    private lateinit var container: FrameLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private var currentRole: String = "BUYER"

    override fun onCreate(savedInstanceState: Bundle?) {
        currentRole = intent.getStringExtra("ROLE") ?: "BUYER"
        when (currentRole) {
            "BUYER" -> setTheme(R.style.Theme_EcoTech_Buyer)
            "SELLER" -> setTheme(R.style.Theme_EcoTech_Seller)
            "ADMIN" -> setTheme(R.style.Theme_EcoTech_Admin)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_host)

        container = findViewById(R.id.fragmentContainer)
        toolbar = findViewById(R.id.topAppBar)
        bottomNav = findViewById(R.id.bottomNavigation)
        toolbar.setNavigationOnClickListener { finish() }

        when (currentRole) {
            "BUYER" -> setupBuyer()
            "SELLER" -> setupSeller()
            "ADMIN" -> setupAdmin()
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentRole == "ADMIN" && bottomNav.selectedItemId == R.id.nav_admin_products) {
            loadView(R.layout.view_admin_products)
        } else if (currentRole == "ADMIN" && bottomNav.selectedItemId == R.id.nav_admin_sales) {
            loadView(R.layout.view_admin_sales)
        }
    }

    private fun setupBuyer() {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.buyer_primary))
        toolbar.title = getString(R.string.buyer_home_title)
        bottomNav.inflateMenu(R.menu.menu_buyer_bottom)
        loadView(R.layout.view_buyer_home)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_buyer_home -> { toolbar.title = getString(R.string.buyer_home_title); loadView(R.layout.view_buyer_home) }
                R.id.nav_buyer_catalog -> { toolbar.title = getString(R.string.buyer_catalog_title); loadView(R.layout.view_buyer_catalog) }
                R.id.nav_buyer_cart -> { toolbar.title = getString(R.string.buyer_cart_title); loadView(R.layout.view_buyer_cart) }
                R.id.nav_buyer_profile -> { toolbar.title = getString(R.string.buyer_profile_title); loadView(R.layout.view_buyer_profile) }
            }
            true
        }
    }

    private fun setupSeller() {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.seller_primary))
        toolbar.title = getString(R.string.seller_panel_title)
        bottomNav.inflateMenu(R.menu.menu_seller_bottom)
        loadView(R.layout.view_seller_products)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_seller_products -> { toolbar.title = getString(R.string.seller_panel_title); loadView(R.layout.view_seller_products) }
                R.id.nav_seller_orders -> { toolbar.title = getString(R.string.seller_orders_title); loadView(R.layout.view_seller_orders) }
                R.id.nav_seller_profile -> { toolbar.title = getString(R.string.seller_profile_title); loadView(R.layout.view_seller_profile) }
            }
            true
        }
    }

    private fun setupAdmin() {
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.admin_primary))
        toolbar.title = getString(R.string.admin_dash_title)
        bottomNav.inflateMenu(R.menu.menu_admin_bottom)
        loadView(R.layout.view_admin_dashboard)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_admin_dash -> { toolbar.title = getString(R.string.admin_dash_title); loadView(R.layout.view_admin_dashboard) }
                R.id.nav_admin_users -> { toolbar.title = getString(R.string.admin_users_title); loadView(R.layout.view_admin_users) }
                R.id.nav_admin_products -> { toolbar.title = getString(R.string.admin_products_title); loadView(R.layout.view_admin_products) }
                R.id.nav_admin_sales -> { toolbar.title = getString(R.string.admin_sales_title); loadView(R.layout.view_admin_sales) }
            }
            true
        }
    }

    private fun openProductDetail(product: ProductItem) {
        val intent = Intent(this, ProductDetailActivity::class.java).apply {
            putExtra("ID", product.id)
            putExtra("NAME", product.name)
            putExtra("PRICE", product.price)
            putExtra("SPECS", product.specs)
        }
        startActivity(intent)
    }

    private fun loadView(layoutRes: Int) {
        container.removeAllViews()
        val view = LayoutInflater.from(this).inflate(layoutRes, container, false)
        container.addView(view)

        // 1. Home Comprador
        view.findViewById<Button>(R.id.btnOpenDetailFromHome)?.setOnClickListener {
            if(StoreRepository.catalog.isNotEmpty()) openProductDetail(StoreRepository.catalog[0])
        }

        // 2. Catalogo Comprador
        val catalogContainer = view.findViewById<LinearLayout>(R.id.catalogItemsContainer)
        if (catalogContainer != null) {
            renderCatalogItems(catalogContainer)
        }

        // 3. Carrito Comprador
        val cartContainer = view.findViewById<LinearLayout>(R.id.cartItemsContainer)
        val tvTotal = view.findViewById<TextView>(R.id.tvCartTotal)
        if (cartContainer != null && tvTotal != null) {
            renderCartItems(cartContainer, tvTotal)
        }
        view.findViewById<Button>(R.id.btnProceedToPayment)?.setOnClickListener {
            if (StoreRepository.cart.isEmpty()) {
                Toast.makeText(this, "El carrito esta vacio", Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this, PaymentGatewayActivity::class.java))
            }
        }

        // 4. Vendedor CRUD
        val sellerListContainer = view.findViewById<LinearLayout>(R.id.sellerProductsContainer)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProduct)
        val btnCamera = view.findViewById<Button>(R.id.btnOpenCamera)
        val etName = view.findViewById<TextInputEditText>(R.id.etSellerName)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etSellerPrice)
        val etSpecs = view.findViewById<TextInputEditText>(R.id.etSellerSpecs)

        if (sellerListContainer != null) {
            renderSellerProducts(sellerListContainer)
        }

        btnCamera?.setOnClickListener {
            Toast.makeText(this, "CameraX: Foto capturada y comprimida", Toast.LENGTH_SHORT).show()
        }

        btnSave?.setOnClickListener {
            val nameTxt = etName?.text.toString().trim()
            val priceTxt = etPrice?.text.toString().trim()
            val specsTxt = etSpecs?.text.toString().trim()

            if (nameTxt.isEmpty() || priceTxt.isEmpty()) {
                Toast.makeText(this, "Ingresa nombre y precio", Toast.LENGTH_SHORT).show()
            } else {
                val priceInt = priceTxt.toIntOrNull() ?: 500000
                val newPending = PendingProduct(
                    id = "PEND_${System.currentTimeMillis()}",
                    name = nameTxt,
                    vendor = "Vendedor Local (Pendiente validación Ley 1480)"
                )
                StoreRepository.pendingModeration.add(newPending)
                Toast.makeText(this, "¡Enviado a moderación del Administrador!", Toast.LENGTH_LONG).show()
                etName?.setText("")
                etPrice?.setText("")
                etSpecs?.setText("")
                if (sellerListContainer != null) {
                    renderSellerProducts(sellerListContainer)
                }
            }
        }

        // 5. Admin - Moderación de Productos
        val adminProductsContainer = view.findViewById<LinearLayout>(R.id.adminProductsListContainer)
        if (adminProductsContainer != null) {
            renderAdminProducts(adminProductsContainer)
        }

        // 6. Admin - Reporte de Ventas Permanente y Reversión (Decreto 587)
        val salesContainer = view.findViewById<LinearLayout>(R.id.salesListContainer)
        if (salesContainer != null) {
            renderAdminSales(salesContainer)
        }
    }

    private fun renderCatalogItems(container: LinearLayout) {
        container.removeAllViews()
        if (StoreRepository.catalog.isEmpty()) {
            container.addView(TextView(this).apply { text = "No hay productos disponibles en este momento." })
        } else {
            StoreRepository.catalog.forEach { prod ->
                val card = MaterialCardView(this).apply {
                    radius = 20f
                    cardElevation = 4f
                    setContentPadding(24, 24, 24, 24)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                }
                val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                inner.addView(TextView(this).apply { text = prod.name; textSize = 17f; setTextColor(ContextCompat.getColor(context, R.color.eco_text_primary)); setTypeface(null, android.graphics.Typeface.BOLD) })
                inner.addView(TextView(this).apply { text = "${StoreRepository.formatCop(prod.price)} | ${prod.specs.take(40)}..."; setTextColor(ContextCompat.getColor(context, R.color.buyer_primary)); setPadding(0, 6, 0, 12) })

                val btnDetail = Button(this).apply {
                    text = "Ver Detalle y Garantia"
                    setBackgroundColor(ContextCompat.getColor(context, R.color.buyer_primary))
                    setTextColor(ContextCompat.getColor(context, R.color.eco_white))
                    setOnClickListener { openProductDetail(prod) }
                }
                inner.addView(btnDetail)
                card.addView(inner)
                container.addView(card)
            }
        }
    }


    private fun renderCartItems(cartContainer: LinearLayout, tvTotal: TextView) {
        cartContainer.removeAllViews()
        if (StoreRepository.cart.isEmpty()) {
            cartContainer.addView(TextView(this).apply { text = "Tu carrito esta vacio."; setPadding(16, 16, 16, 16) })
        } else {
            StoreRepository.cart.forEachIndexed { index, item ->
                val card = MaterialCardView(this).apply {
                    radius = 20f
                    cardElevation = 4f
                    setContentPadding(24, 24, 24, 24)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                }
                val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                inner.addView(TextView(this).apply { text = "${item.qty}x ${item.name}"; textSize = 16f; setTextColor(ContextCompat.getColor(context, R.color.eco_text_primary)) })
                inner.addView(TextView(this).apply { text = "Subtotal: ${StoreRepository.formatCop(item.price * item.qty)}"; setTextColor(ContextCompat.getColor(context, R.color.buyer_primary)) })
                
                val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 12, 0, 0) } }
                actions.addView(Button(this).apply { text = "+1 Cantidad"; setOnClickListener { item.qty += 1; renderCartItems(cartContainer, tvTotal) } })
                actions.addView(Button(this).apply { text = "Eliminar"; setOnClickListener { StoreRepository.cart.removeAt(index); renderCartItems(cartContainer, tvTotal) } })
                inner.addView(actions)
                card.addView(inner)
                cartContainer.addView(card)
            }
        }
        tvTotal.text = "Total a Pagar: ${StoreRepository.formatCop(StoreRepository.getTotal())}"
    }

    private fun renderSellerProducts(container: LinearLayout) {
        container.removeAllViews()
        if (StoreRepository.catalog.isEmpty()) {
            container.addView(TextView(this).apply { text = "No hay productos publicados." })
        } else {
            StoreRepository.catalog.forEachIndexed { index, prod ->
                val card = MaterialCardView(this).apply {
                    radius = 20f
                    cardElevation = 4f
                    setContentPadding(24, 24, 24, 24)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                }
                val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                inner.addView(TextView(this).apply { text = prod.name; textSize = 16f; setTextColor(ContextCompat.getColor(context, R.color.eco_text_primary)); setTypeface(null, android.graphics.Typeface.BOLD) })
                inner.addView(TextView(this).apply { text = "${StoreRepository.formatCop(prod.price)} | Stock disponible"; setTextColor(ContextCompat.getColor(context, R.color.seller_primary)) })

                val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 12, 0, 0) } }
                actions.addView(Button(this).apply { text = "Editar"; setOnClickListener { Toast.makeText(context, "Modo edicion activo", Toast.LENGTH_SHORT).show() } })
                actions.addView(Button(this).apply { 
                    text = "Eliminar" 
                    setTextColor(android.graphics.Color.parseColor("#D32F2F"))
                    setOnClickListener { 
                        StoreRepository.catalog.removeAt(index)
                        renderSellerProducts(container)
                        Toast.makeText(context, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show() 
                    } 
                })
                inner.addView(actions)
                card.addView(inner)
                container.addView(card)
            }
        }
    }

    private fun renderAdminProducts(container: LinearLayout) {
        container.removeAllViews()
        if (StoreRepository.pendingModeration.isEmpty()) {
            container.addView(TextView(this).apply { text = "No hay productos pendientes de moderación."; setPadding(16, 16, 16, 16) })
        } else {
            StoreRepository.pendingModeration.forEachIndexed { index, pending ->
                val card = MaterialCardView(this).apply {
                    radius = 20f
                    cardElevation = 4f
                    setContentPadding(24, 24, 24, 24)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                }
                val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                inner.addView(TextView(this).apply { text = "${pending.name} - Pendiente"; textSize = 17f; setTextColor(ContextCompat.getColor(context, R.color.admin_primary_dark)); setTypeface(null, android.graphics.Typeface.BOLD) })
                inner.addView(TextView(this).apply { text = "Vendedor: ${pending.vendor}\nCumple campos Ley 1480: Sí"; setTextColor(ContextCompat.getColor(context, R.color.eco_text_primary)); setPadding(0, 6, 0, 12) })

                val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT) }
                
                val btnApprove = Button(this).apply {
                    text = "Aprobar"
                    setBackgroundColor(ContextCompat.getColor(context, R.color.seller_primary))
                    setTextColor(ContextCompat.getColor(context, R.color.eco_white))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { setMargins(0, 0, 8, 0) }
                    setOnClickListener {
                        StoreRepository.catalog.add(ProductItem(pending.id, pending.name, 980000, "• Estado: Reacondicionado Certificado\n• Garantía: 12 meses (Ley 1480)"))
                        StoreRepository.pendingModeration.removeAt(index)
                        renderAdminProducts(container)
                        Toast.makeText(context, "¡Producto aprobado y publicado en el Catálogo!", Toast.LENGTH_LONG).show()
                    }
                }

                val btnReject = Button(this).apply {
                    text = "Rechazar"
                    setTextColor(ContextCompat.getColor(context, R.color.eco_error))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    setOnClickListener {
                        StoreRepository.pendingModeration.removeAt(index)
                        renderAdminProducts(container)
                        Toast.makeText(context, "Producto rechazado y eliminado", Toast.LENGTH_SHORT).show()
                    }
                }

                actions.addView(btnApprove)
                actions.addView(btnReject)
                inner.addView(actions)
                card.addView(inner)
                container.addView(card)
            }
        }
    }

    private fun renderAdminSales(container: LinearLayout) {
        container.removeAllViews()
        if (StoreRepository.transactions.isEmpty()) {
            container.addView(TextView(this).apply { text = "No hay transacciones registradas."; setPadding(16, 16, 16, 16) })
        } else {
            StoreRepository.transactions.forEach { tx ->
                val card = MaterialCardView(this).apply {
                    radius = 20f
                    cardElevation = 4f
                    setContentPadding(24, 24, 24, 24)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { setMargins(0, 0, 0, 16) }
                }
                val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
                inner.addView(TextView(this).apply { text = "Transaccion #${tx.txId} (${StoreRepository.formatCop(tx.totalAmount)})"; textSize = 17f; setTextColor(ContextCompat.getColor(context, R.color.admin_primary_dark)); setTypeface(null, android.graphics.Typeface.BOLD) })
                inner.addView(TextView(this).apply { text = "Items: ${tx.itemsSummary}\nEstado: ${tx.method}"; setTextColor(ContextCompat.getColor(context, R.color.eco_text_primary)); setPadding(0, 6, 0, 12) })

                val btnReversal = Button(this).apply {
                    text = "Ejecutar Reversion de Pago (Decreto 587)"
                    setBackgroundColor(ContextCompat.getColor(context, R.color.admin_primary))
                    setTextColor(ContextCompat.getColor(context, R.color.eco_white))
                    setOnClickListener {
                        tx.method = "ANULADA / Reversión Aplicada (Decreto 587)"
                        renderAdminSales(container)
                        Toast.makeText(context, "¡Reversión aplicada! Transacción marcada como reversada en el historial.", Toast.LENGTH_LONG).show()
                    }
                }
                inner.addView(btnReversal)
                card.addView(inner)
                container.addView(card)
            }
        }
    }
}

class ProductDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_EcoTech_Buyer)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val id = intent.getStringExtra("ID") ?: "MACBOOK"
        val name = intent.getStringExtra("NAME") ?: "MacBook Air M1"
        val price = intent.getIntExtra("PRICE", 2450000)
        val specs = intent.getStringExtra("SPECS") ?: "Grado A"

        findViewById<TextView>(R.id.tvDetailName).text = name
        findViewById<TextView>(R.id.tvDetailPrice).text = "${StoreRepository.formatCop(price)} (IVA Incluido)"
        findViewById<TextView>(R.id.tvDetailSpecs).text = specs

        findViewById<Button>(R.id.btnAddToCart).setOnClickListener {
            StoreRepository.addToCart(ProductItem(id, name, price, specs))
            Toast.makeText(this, "$name agregado al carrito!", Toast.LENGTH_SHORT).show()
            finish()
        }
        findViewById<Button>(R.id.btnBackFromDetail).setOnClickListener { finish() }
    }
}

class PaymentGatewayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_EcoTech_Buyer)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_gateway)

        val btnConfirm = findViewById<Button>(R.id.btnConfirmPayment)
        btnConfirm.text = "Confirmar y Pagar ${StoreRepository.formatCop(StoreRepository.getTotal())}"

        btnConfirm.setOnClickListener {
            val summary = StoreRepository.cart.joinToString(", ") { "${it.qty}x ${it.name}" }
            val total = StoreRepository.getTotal()
            val newTxId = (1000..9999).random().toString()
            
            StoreRepository.transactions.add(SalesTransaction(newTxId, summary, total, "Completada (Plazo Decreto 587 activo)"))
            StoreRepository.cart.clear()

            Toast.makeText(this, "¡Pago exitoso! Orden registrada en el sistema.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
