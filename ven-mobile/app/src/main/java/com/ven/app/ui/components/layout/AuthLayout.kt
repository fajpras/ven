package com.ven.app.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

@Composable
fun AuthLayout(
    title: String,
    modifier: Modifier = Modifier,
    contentMaxWidth: Dp = 304.dp,
    formContent: @Composable () -> Unit,
    footerContent: @Composable () -> Unit,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = contentMaxWidth)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 55.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(VexSpace.s16))

            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = colors.text,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(VexSpace.s12))

            formContent()

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(VexSpace.s8))

            footerContent()
        }
    }
}

@Preview(name = "AuthLayout - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun AuthLayoutDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        AuthLayout(
            title = "Login",
            formContent = {
                Text("Form Slot", color = VexTheme.colors.text)
            },
            footerContent = {
                Text("Footer Slot", color = VexTheme.colors.text)
            },
        )
    }
}

@Preview(name = "AuthLayout - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AuthLayoutLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        AuthLayout(
            title = "Register",
            formContent = {
                Text("Form Slot", color = VexTheme.colors.text)
            },
            footerContent = {
                Text("Footer Slot", color = VexTheme.colors.text)
            },
        )
    }
}
