package com.adoptme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adoptme.R
import com.adoptme.core.di.AppContainer
import com.adoptme.core.navigation.Screen
import com.adoptme.ui.components.AdoptMeToast
import com.adoptme.viewmodel.PublishViewModel
import kotlinx.coroutines.launch

/**
 * Onboarding local: paso 2 (crear cuenta) y paso 3 (agregar mascota).
 * La cuenta se guarda en el dispositivo y la mascota queda ligada a su id.
 */
@Composable
fun AuthScreen() {
    var isLogin by rememberSaveable { mutableStateOf(false) }
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isLogin) {
                StepIndicator(step = 2, total = 3)
                Spacer(Modifier.height(18.dp))
            }

            Text(
                text = if (isLogin) "Inicia sesión" else "Crea tu cuenta",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isLogin) {
                    "Entra para gestionar tus mascotas y adopciones."
                } else {
                    "Crea tu cuenta para asociar tus mascotas y gestionar sus fichas."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            if (!isLogin) {
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
                imeAction = if (isLogin) ImeAction.Done else ImeAction.Next,
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

            if (!isLogin) {
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
            }

            errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(22.dp))

            Button(
                onClick = {
                    if (isLoading) return@Button
                    errorMessage = null
                    if (!isLogin && password != confirmPassword) {
                        errorMessage = "Las contraseñas no coinciden."
                        return@Button
                    }
                    isLoading = true
                    scope.launch {
                        val result = if (isLogin) {
                            AppContainer.sessionManager.login(email, password)
                        } else {
                            AppContainer.sessionManager.register(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                password = password
                            )
                        }
                        isLoading = false
                        if (!result.success) {
                            errorMessage = result.message ?: "No pudimos completar la operación."
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                } else {
                    Text(
                        text = if (isLogin) "Entrar" else "Crear cuenta",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLogin) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isLogin) "Crea una" else "Inicia sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        isLogin = !isLogin
                        errorMessage = null
                        password = ""
                        confirmPassword = ""
                    }
                )
            }
        }
    }
}

@Composable
fun AddPetOnboardingScreen(onFinished: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var showPublish by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    if (showPublish) {
        val publishViewModel: PublishViewModel = viewModel()
        Box(modifier = Modifier.fillMaxSize()) {
            PublishScreen(
                viewModel = publishViewModel,
                onClose = { showPublish = false },
                onPublished = {
                    scope.launch {
                        AppContainer.sessionManager.markOnboardingCompleted()
                        onFinished(Screen.MyPet.route)
                    }
                },
                onShowToast = { toastMessage = it }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AdoptMeToast(toastMessage) { toastMessage = null }
            }
        }
        return
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
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StepIndicator(step = 3, total = 3)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Agrega tu primera mascota",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Quedará asociada a tu cuenta para que puedas gestionar su ficha de adopción.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(190.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🐶", fontSize = 82.sp)
                }
            }
            Spacer(Modifier.height(30.dp))
            Button(
                onClick = { showPublish = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                )
            ) {
                Text("Agregar mascota", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = {
                    scope.launch {
                        AppContainer.sessionManager.markOnboardingCompleted()
                        onFinished(Screen.Adopt.route)
                    }
                }
            ) {
                Text(
                    text = "Explorar adopciones",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int, total: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Paso $step de $total",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(if (index < step) 30.dp else 16.dp)
                        .background(
                            if (index < step) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            },
                            RoundedCornerShape(50)
                        )
                )
            }
        }
    }
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
