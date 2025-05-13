package com.example.wellniaryproject

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@Composable
fun DatePicker(viewModel: UserDataViewModel) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d -> viewModel.dateOfBirth = "$y-${m + 1}-$d" },
        year, month, day
    )

    OutlinedTextField(
        value = "DoB: ${viewModel.dateOfBirth}",
        onValueChange = {},
        readOnly = true,
        label = { Text("Birthday") },
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(Icons.Default.DateRange, contentDescription = null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
