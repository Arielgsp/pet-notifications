package com.petnotifications.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.petnotifications.PetType
import com.petnotifications.Prefs
import com.petnotifications.PetView
import com.petnotifications.R
import com.petnotifications.petPainter

/** Largo máximo del nombre: entra en el globo y en la notificación. */
const val PET_NAME_MAX = 16

/**
 * "¿Cómo se llama tu perro?": se pregunta la primera vez que se elige cada mascota y se puede
 * cambiar después. Vacío o "Ahora no" = sin nombre (se dice "tu perro").
 */
@Composable
fun PetNameDialog(pet: PetType, initial: String, onDone: (String?) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    val species = stringResource(pet.nameRes).lowercase()
    fun save() = onDone(name.trim().takeIf { it.isNotEmpty() })
    AlertDialog(
        onDismissRequest = { onDone(initial.ifBlank { null }) },
        icon = { Image(petPainter(pet, PetView.FRONT, Prefs(LocalContext.current).coat(pet)), contentDescription = null, modifier = Modifier.size(64.dp)) },
        title = { Text(stringResource(R.string.name_title, species)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.name_body))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(PET_NAME_MAX) },
                    placeholder = { Text(stringResource(R.string.name_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = { TextButton(onClick = { save() }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = { onDone(initial.ifBlank { null }) }) { Text(stringResource(R.string.ob_not_now)) } },
    )
}
