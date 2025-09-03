package no.uio.ifi.in2000.team33.lumo.ui.profile.screens


// ny
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.components.EditableUserInfoField
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.UserInfo
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.Users
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(navController: NavController, profileViewModel: ProfileViewModel) {
    val backgroundColor = MaterialTheme.colorScheme.primary
    val purpleTextColor = MaterialTheme.colorScheme.outline
    val secondaryPurpleColor = MaterialTheme.colorScheme.outline
    val cardColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFFFBF2)

    val userInfoUiState by profileViewModel.userInfo.collectAsState()
    val initialUserInfo = userInfoUiState.user

    // State for user information fields
    var firstName by remember { mutableStateOf(initialUserInfo?.firstName ?: "") }
    var lastName by remember { mutableStateOf(initialUserInfo?.lastName ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Din informasjon",
                        style = MaterialTheme.typography.headlineMedium,
                        color = purpleTextColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = purpleTextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Bottom wave background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color(
                            0xFFFFF2CD
                        ),
                        shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 29.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Editable form fields
                EditableUserInfoField(
                    label = "Fornavn",
                    value = firstName,
                    onValueChange = { firstName = it },
                    color = secondaryPurpleColor,
                    backgroundColor = cardColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                EditableUserInfoField(
                    label = "Etternavn",
                    value = lastName,
                    onValueChange = { lastName = it },
                    color = secondaryPurpleColor,
                    backgroundColor = cardColor
                )

                Spacer(modifier = Modifier.height(200.dp))

                // Save button
                Button(
                    onClick = {
                        // Save the updated user info to the saved state handle
                        profileViewModel.updateUserInfo(
                            UserInfo(
                                firstName = firstName,
                                lastName = lastName
                            )
                        )
                        println(Users(firstName, lastName))
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color(
                            0xFFF5EFF7
                        ),
                        contentColor = secondaryPurpleColor
                    ),
                    shape = RoundedCornerShape(50.dp),
                    border = BorderStroke(2.dp, secondaryPurpleColor)
                ) {
                    Text(
                        text = "Lagre endringer",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}