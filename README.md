# EcoTech Store - Android Nativo 
Aplicación móvil nativa desarrollada en Kotlin para Android Studio, diseñada bajo los lineamientos de la economía circular y el comercio electrónico sostenible en Colombia.

## Autores
* **Desarrollo y Arquitectura:** David Santafé & Mafe Arcila

##  Características Principales
* **Autenticación Transversal:** Simulación de Biometría (`BiometricPrompt`) y OAuth 2.0.
* **Rol Comprador (Tema Azul):** 
  * Catálogo dinámico y sincronizado de productos reacondicionados.
  * Visualización de garantías amparadas por la **Ley 1480**.
  * Carrito de compras y pasarela de pagos con tokenización (PCI-DSS).
* **Rol Vendedor (Tema Verde):**
  * CRUD completo de productos.
  * Integración con **CameraX** para captura y compresión de evidencias fotográficas.
* **Rol Administrador (Tema Púrpura):**
  * Módulo de moderación previa de productos.
  * Historial de ventas permanente y ejecución de reversiones de pago conforme al **Decreto 587 de 2016**.

## Tecnologías Utilizadas
* **Lenguaje:** Kotlin
* **Entorno:** Android Studio (Windows)
* **UI Components:** Material Design 3 (CardView, MaterialToolbar, BottomNavigationView)
* **Arquitectura:** Repositorio en memoria unificado (`StoreRepository`) para sincronización en tiempo real entre roles.

##  Ejecución
1. Clona el repositorio: `git clone https://github.com/mfarcila/EcoTechStore-Android.git`
2. Abre el proyecto en **Android Studio**.
3. Sincroniza las dependencias de Gradle y ejecuta la app en un emulador Android (API 24+).
