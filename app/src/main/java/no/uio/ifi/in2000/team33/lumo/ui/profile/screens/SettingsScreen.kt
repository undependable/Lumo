package no.uio.ifi.in2000.team33.lumo.ui.profile.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.turnOn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val backgroundColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.outline
    val switchColor = MaterialTheme.colorScheme.primary
    val switchTrackColor = MaterialTheme.colorScheme.tertiaryContainer

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Innstillinger",
                        style = MaterialTheme.typography.headlineMedium,
                        color = textColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Tilbake",
                            tint = textColor
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 29.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            SettingsItem(
                title = "Mørkt tema",
                subtitle = "Skru på mørkt tema for appen",
                trailing = {
                    Switch(
                        checked = isDark,
                        onCheckedChange = { turnOn() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = switchColor,
                            checkedTrackColor = switchTrackColor,
                            uncheckedThumbColor = switchColor,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()

            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF625B71)
            )
        }
        trailing()
    }
}