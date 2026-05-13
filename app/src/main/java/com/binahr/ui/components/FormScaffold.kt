package com.binahr.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Scaffold pattern for data-entry screens.
 * - BinaTopBar at the top
 * - Scrollable content area with keyboard-aware insets
 * - Sticky HRButton "bottomBar" at the bottom
 *
 * Usage:
 *   FormScaffold(
 *     title = "Ajukan Cuti",
 *     onBack = { navController.popBackStack() },
 *     submitLabel = "Kirim Pengajuan",
 *     isSubmitting = viewModel.isLoading,
 *     onSubmit = { viewModel.submit() },
 *   ) {
 *     // form fields
 *   }
 */
@Composable
fun FormScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    submitLabel: String = "Simpan",
    isSubmitting: Boolean = false,
    submitEnabled: Boolean = true,
    onSubmit: () -> Unit,
    actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            BinaTopBar(title = title, onBack = onBack, actions = actions)
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                HRButton(
                    text = submitLabel,
                    onClick = onSubmit,
                    enabled = submitEnabled && !isSubmitting,
                    isLoading = isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            content()
        }
    }
}
