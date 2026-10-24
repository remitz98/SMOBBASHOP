package org.smobba.store

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    val state = rememberWindowState(
        width = 1200.dp,
        height = 840.dp,
        position = WindowPosition.PlatformDefault
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "SMO BBA SHOP - สโมสรนักศึกษา คณะบริหารธุรกิจและการจัดการ มรภ.อุบลฯ",
        icon = painterResource("icon.png"),
        state = state
    ) {
        App()
    }
}
