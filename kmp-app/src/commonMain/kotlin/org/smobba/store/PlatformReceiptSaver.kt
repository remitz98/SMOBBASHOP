package org.smobba.store

import org.smobba.store.models.Order

expect class PlatformReceiptSaver() {
    fun saveReceipt(order: Order, onComplete: (Boolean, String) -> Unit)
}
