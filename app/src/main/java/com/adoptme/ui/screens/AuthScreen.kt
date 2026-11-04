package com.adoptme.ui.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.adoptme.R
import com.adoptme.viewmodel.AccountViewModel

private enum class AuthMode { REGISTER, LOGIN }

/**
 * Registro e inicio de sesión local.
 *
 * La cuenta se guarda en DataStore con contraseña PBKDF2. No hay backend
 * todavía: la sesión y las publicaciones viven en este dispositivo.
 */
@Composable
fun AuthScreen(
    viewModel: AccountViewModel,
    onShowToast: (String) -> Unit = {}
) {
    var mode by rememberSaveable { mutableStateOf(AuthMode.REGISTER) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var termsAccepted by rememberSaveable { mutableStateOf(false) }
    var privacyAccepted by rememberSaveable { mutableStateOf(false) }
    var isAdult by rememberSaveable { mutableStateOf(false) }
    var marketingAccepted by rememberSaveable { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var legalDocument by remember { mutableStateOf<LegalDocument?>(null) }

    val authState by viewModel.authState.collectAsState()
    val isRegister = mode == AuthMode.REGISTER
    val displayedError = localError ?: authState.error

    fun switchMode() {
        mode = if (isRegister) AuthMode.LOGIN else AuthMode.REGISTER
        localError = null
        viewModel.clearAuthError()
    }

    fun submit() {
        val validationError = when {
            isRegister && (firstName.isBlank() || lastName.isBlank()) ->
                "Completa tu nombre y apellido."
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() ->
                "Ingresa un correo electrónico válido."
            password.length < 6 ->
                "La contraseña debe tener al menos 6 caracteres."
            isRegister && password != confirmPassword ->
                "Las contraseñas no coinciden."
            isRegister && (!termsAccepted || !privacyAccepted) ->
                "Debes aceptar los Términos y la Política de Privacidad."
            isRegister && !isAdult ->
                "Debes declarar que eres mayor de 18 años o tienes autorización."
            else -> null
        }

        localError = validationError
        if (validationError == null) {
            if (isRegister) {
                viewModel.register(firstName, lastName, email, password)
            } else {
                viewModel.login(email, password)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_paw),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
            Spacer(Modifier.height(18.dp))

            Text(
                text = if (isRegister) "Crea tu cuenta" else "Inicia sesión",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isRegister) {
                    "Crea tu cuenta para asociar tus mascotas y gestionar sus fichas."
                } else {
                    "Entra para gestionar tus publicaciones, intereses y reportes."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))

            OutlinedButton(
                onClick = {
                    onShowToast("Continuar con Google estará disponible próximamente.")
                },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Continuar con Google",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = if (isRegister) "o regístrate con correo" else "o entra con correo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(18.dp))

            if (isRegister) {
                AuthTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = "Nombre",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(10.dp))
                AuthTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = "Apellido",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(10.dp))
            }

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Correo electrónico",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(10.dp))
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Contraseña",
                keyboardType = KeyboardType.Password,
                imeAction = if (isRegister) ImeAction.Next else ImeAction.Done,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    PasswordVisibilityButton(
                        visible = passwordVisible,
                        onToggle = { passwordVisible = !passwordVisible }
                    )
                }
            )

            if (isRegister) {
                Spacer(Modifier.height(10.dp))
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirmar contraseña",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    visualTransformation = if (confirmVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        PasswordVisibilityButton(
                            visible = confirmVisible,
                            onToggle = { confirmVisible = !confirmVisible }
                        )
                    }
                )

                Spacer(Modifier.height(18.dp))

                LegalCheckRow(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    label = "Acepto los Términos y Condiciones",
                    linkLabel = "Ver términos",
                    onLinkClick = { legalDocument = LegalDocument.TERMS }
                )
                LegalCheckRow(
                    checked = privacyAccepted,
                    onCheckedChange = { privacyAccepted = it },
                    label = "Acepto la Política de Privacidad",
                    linkLabel = "Ver privacidad",
                    onLinkClick = { legalDocument = LegalDocument.PRIVACY }
                )
                LegalCheckRow(
                    checked = isAdult,
                    onCheckedChange = { isAdult = it },
                    label = "Declaro ser mayor de 18 años o contar con autorización de mi tutor legal."
                )
                LegalCheckRow(
                    checked = marketingAccepted,
                    onCheckedChange = { marketingAccepted = it },
                    label = "Acepto recibir comunicaciones sobre adopciones y novedades (opcional)."
                )
            }

            displayedError?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { submit() },
                enabled = !authState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Procesando…", style = MaterialTheme.typography.titleMedium)
                } else {
                    Text(
                        text = if (isRegister) "Crear cuenta" else "Iniciar sesión",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                text = if (isRegister) {
                    "Tus datos se usan para crear tu cuenta y gestionar adopciones. " +
                        "Puedes ejercer tus derechos de acceso, rectificación, cancelación y oposición."
                } else {
                    "Tu cuenta se guarda solo en este dispositivo. Aún no hay sincronización con la nube."
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegister) "¿Ya tienes cuenta? " else "¿No tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isRegister) "Inicia sesión" else "Crea una",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { switchMode() }
                )
            }
        }
    }

    legalDocument?.let { document ->
        LegalDialog(
            document = document,
            onDismiss = { legalDocument = null }
        )
    }
}

private enum class LegalDocument { TERMS, PRIVACY }

@Composable
private fun LegalCheckRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    linkLabel: String? = null,
    onLinkClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (linkLabel != null && onLinkClick != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = linkLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onLinkClick)
                )
            }
        }
    }
}

@Composable
private fun LegalDialog(document: LegalDocument, onDismiss: () -> Unit) {
    val title = when (document) {
        LegalDocument.TERMS -> "Términos y Condiciones"
        LegalDocument.PRIVACY -> "Política de Privacidad"
    }
    val body = when (document) {
        LegalDocument.TERMS ->
            "AdoptMe es una plataforma de adopción responsable. No es un servicio " +
                "de emergencia ni veterinario. No se permite la venta de animales ni " +
                "la publicación de información falsa. Cada publicante declara la " +
                "veracidad de los datos de la mascota y se compromete a coordinar la " +
                "adopción conforme a la Ley 21.020 de tenencia responsable. AdoptMe " +
                "puede ocultar fichas que incumplan estas reglas.\n\n" +
                "Texto base del prototipo; debe ser revisado por asesoría legal antes " +
                "de su publicación en producción."
        LegalDocument.PRIVACY ->
            "Responsable: AdoptMe. Datos tratados: nombre, apellido, correo " +
                "electrónico, comuna y datos de las mascotas que publiques. Finalidad: " +
                "crear y administrar tu cuenta, gestionar publicaciones y coordinar " +
                "adopciones. Base de licitud: consentimiento. Tus datos no se venden " +
                "ni se comparten con terceros ajenos a la adopción.\n\n" +
                "Puedes ejercer tus derechos de acceso, rectificación, cancelación, " +
                "oposición y portabilidad a través del canal de privacidad que se " +
                "habilitará antes de producción. Tratamiento alineado a la Ley 21.719 " +
                "de protección de datos personales.\n\n" +
                "Texto base del prototipo, sujeto a revisión legal."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = Color(0xFF1E1B1A),
            unfocusedTextColor = Color(0xFF1E1B1A)
        )
    )
}

@Composable
private fun PasswordVisibilityButton(visible: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            painter = painterResource(
                if (visible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
            ),
            contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
