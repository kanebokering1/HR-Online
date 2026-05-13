package com.binahr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.binahr.data.api.model.FieldChange
import com.binahr.ui.components.*
import com.binahr.ui.theme.*
import com.binahr.ui.viewmodel.ProfileChangeRequestViewModel
import com.binahr.ui.viewmodel.ProfileViewModel

@Composable
fun DataDiriEditScreen(
    onBack: () -> Unit,
    profileVm: ProfileViewModel = viewModel(),
    vm: ProfileChangeRequestViewModel = viewModel(),
) {
    // Load current values from ProfileViewModel for pre-fill only
    val employee by profileVm.employee.collectAsStateWithLifecycle()
    val isLoading by vm.isLoading.collectAsStateWithLifecycle()
    val submitResult by vm.submitResult.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    var phone by remember(employee) { mutableStateOf(employee?.phone ?: "") }
    var address by remember(employee) { mutableStateOf(employee?.address ?: "") }
    var submitted by remember { mutableStateOf(false) }

    LaunchedEffect(submitResult) {
        submitResult?.onSuccess {
            vm.clearSubmitResult()
            submitted = true
        }
        submitResult?.onFailure {
            vm.clearSubmitResult()
        }
    }

    if (submitted) {
        FormScaffold(
            title = "Edit Data Diri",
            onBack = onBack,
            submitLabel = "Kembali",
            isSubmitting = false,
            submitEnabled = true,
            onSubmit = onBack,
        ) {
            InfoCallout(
                message = "Permintaan perubahan data telah dikirim ke HR untuk diverifikasi.",
                type = CalloutType.INFO,
            )
        }
        return
    }

    FormScaffold(
        title = "Edit Data Diri",
        onBack = onBack,
        submitLabel = "Kirim Permintaan",
        isSubmitting = isLoading,
        submitEnabled = phone.isNotBlank() && !isLoading,
        onSubmit = {
            val changes = buildList {
                if (phone.isNotBlank()) add(FieldChange(fieldKey = "phone", newValue = phone))
                if (address.isNotBlank()) add(FieldChange(fieldKey = "address", newValue = address))
            }
            if (changes.isNotEmpty()) vm.submit(changes)
        },
    ) {
        error?.let {
            InfoCallout(message = it, type = CalloutType.ERROR)
            Spacer(Modifier.height(8.dp))
        }

        InfoCallout(
            message = "Perubahan data diri memerlukan verifikasi oleh HR sebelum diterapkan.",
            type = CalloutType.INFO,
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("No. HP") },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                focusedLabelColor = OrangePrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                focusedLabelColor = OrangePrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
