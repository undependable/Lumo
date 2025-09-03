package no.uio.ifi.in2000.team33.lumo.ui.profile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team33.lumo.ui.profile.ProfileViewModel
import no.uio.ifi.in2000.team33.lumo.ui.profile.components.ProfileCard
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark

@Composable
fun ProfileScreen(navController: NavController, profileViewModel: ProfileViewModel) {
    // Use the current dark mode state from the theme
    val backgroundColor = MaterialTheme.colorScheme.primary
    val purpleTextColor = MaterialTheme.colorScheme.outline
    val secondaryPurpleColor =
        if (isDark) MaterialTheme.colorScheme.secondary else Color(0xFF4F378A)

    // dataflow from profileVM
    val userInfoUiState by profileViewModel.userInfo.collectAsState()
    val userInfo = userInfoUiState.user

    // checks if valid user info exists
    val hasUserInfo =
        userInfo?.firstName?.isNotEmpty() == true && userInfo.lastName.isNotEmpty()

    val powerConsumptionState by profileViewModel.powerConsumption.collectAsState()
    val consumption = powerConsumptionState.consumption

    Scaffold(
        containerColor = backgroundColor,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background curve at the bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(topStart = 170.dp, topEnd = 0.dp)
                    )
            )

            // Profile content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 29.dp)
            ) {
                Spacer(modifier = Modifier.height(85.dp))

                // Profile Title
                Text(
                    text = "Profil",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(60.dp))

                // User Information Card
                ProfileCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(72.dp)
                        )
                    },
                    title = "Din informasjon",
                    value = if (hasUserInfo) "${userInfo?.firstName} ${userInfo?.lastName}" else "Legg til informasjon",
                    onClick = {
                        navController.navigate("userinfoscreen")
                    },
                    titleColor = secondaryPurpleColor,
                    valueColor = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Power Consumption Card
                ProfileCard(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Power",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(72.dp)
                        )
                    },
                    title = "Mitt strømforbruk",
                    value = "${consumption.yearlyConsumption} kWh årlig",
                    onClick = {
                        navController.navigate("powerconsumptionscreen")
                    },
                    titleColor = secondaryPurpleColor,
                    valueColor = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(50.dp))

                // Settings Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(67.dp)
                        .clickable { navController.navigate("settingsscreen") },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = purpleTextColor,
                            modifier = Modifier.size(38.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Innstillinger",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = secondaryPurpleColor
                        )
                    }
                }
            }
        }
    }
}