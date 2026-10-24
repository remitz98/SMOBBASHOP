package org.smobba.store

expect class PlatformReceiptSaver() {
    fun saveReceipt(orderId: String, textContent: String, onComplete: (Boolean, String) -> Unit)
}
