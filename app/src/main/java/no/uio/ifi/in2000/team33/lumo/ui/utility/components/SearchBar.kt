package no.uio.ifi.in2000.team33.lumo.ui.utility.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.FusedLocationProviderClient
import no.uio.ifi.in2000.team33.lumo.R
import no.uio.ifi.in2000.team33.lumo.data.address.model.Adresser
import no.uio.ifi.in2000.team33.lumo.ui.search.viewmodel.SearchScreenViewModel
import no.uio.ifi.in2000.team33.lumo.ui.utility.theme.isDark

/**
 * SearchBar component that handles address search with proper StateFlow integration.
 * All state is managed by SearchScreenViewModel, ensuring consistency and proper recomposition.
 */
@Composable
fun SearchBar(
    showInfo: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onAddressEntered: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    addressText: String,
    searchScreenViewModel: SearchScreenViewModel,
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFound: (Double, Double) -> Unit
) {
    // Collect all state from ViewModels using StateFlow
    val searchUIState by searchScreenViewModel.mapScreenUiState.collectAsState()
    val isLocationLoading by searchScreenViewModel.isLocationLoading.collectAsState()
    val isSearchLoading by searchScreenViewModel.isSearchLoading.collectAsState()
    val mapStyle2D by searchScreenViewModel.mapStyle2D.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        searchScreenViewModel.handleLocationPermissionResult(
            permissions,
            context,
            fusedLocationClient,
            onLocationFound
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search bar row
        SearchFieldRow(
            addressText = addressText,
            onAddressChange = onAddressChange,
            onAddressEntered = onAddressEntered,
            keyboardController = keyboardController,
            isSearchLoading = isSearchLoading
        )

        // Button controls row
        ButtonControlsRow(
            isLocationLoading = isLocationLoading,
            mapStyle2D = mapStyle2D,
            onLocationClick = {
                searchScreenViewModel.handleLocationButtonClick(
                    context,
                    fusedLocationClient,
                    onLocationFound
                )
            },
            onMapStyleToggle = { searchScreenViewModel.toggleMapStyle() },
            onInfoClick = { showInfo(true) }
        )
    }

    // Location Permission Dialog
    if (searchUIState.showLocationPermissionDialog) {
        LocationPermissionDialog(
            onConfirm = {
                searchScreenViewModel.showLocationPermissionDialog(false)
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDismiss = { searchScreenViewModel.showLocationPermissionDialog(false) }
        )
    }
}

@Composable
private fun SearchFieldRow(
    addressText: String,
    onAddressChange: (String) -> Unit,
    onAddressEntered: (String) -> Unit,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?,
    isSearchLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            singleLine = true,
            enabled = !isSearchLoading,
            modifier = Modifier
                .weight(1f)
                .height(53.dp),
            shape = RoundedCornerShape(percent = 50),
            value = addressText,
            placeholder = {
                Text(
                    text = if (isSearchLoading) "Søker..." else "Søk opp adresse",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onPrimary
            ),
            onValueChange = { if (it.length <= 40) onAddressChange(it) },
            leadingIcon = {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu icon",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color.Gray
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (addressText.isNotBlank() && !isSearchLoading) {
                        onAddressEntered(addressText.trim())
                        keyboardController?.hide()
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
                unfocusedBorderColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color.Gray,
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledBorderColor = Color.Gray.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ),
        )
    }
}

@Composable
private fun ButtonControlsRow(
    isLocationLoading: Boolean,
    mapStyle2D: Boolean,
    onLocationClick: () -> Unit,
    onMapStyleToggle: () -> Unit,
    onInfoClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(3.dp))

        // Location Button
        LocationButton(
            isLoading = isLocationLoading,
            onClick = onLocationClick
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 2D/3D Toggle Button
        MapStyleToggleButton(
            is2D = mapStyle2D,
            onClick = onMapStyleToggle
        )

        // Info Button (right-aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoButton(onClick = onInfoClick)
        }
    }
}

@Composable
private fun LocationButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(
                2.dp,
                if (isLoading) MaterialTheme.colorScheme.primary else Color(0xFF79747E),
                RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            SimpleRotatingLoader(
                modifier = Modifier.size(24.dp),
                outerCircleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                middleCircleColor = MaterialTheme.colorScheme.primary,
                innerCircleColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )
        } else {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.location),
                    contentDescription = "Get my location",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun MapStyleToggleButton(
    is2D: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(2.dp, Color(0xFF79747E), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (is2D) "2D" else "3D",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun InfoButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(2.dp, Color(0xFF79747E), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.QuestionMark,
                contentDescription = "Information",
                modifier = Modifier
                    .size(35.dp)
                    .rotate(0.10F)
            )
        }
    }
}

@Composable
private fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Tilgang til lokasjon",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "For å finne din nåværende lokasjon på kartet, trenger appen tilgang til stedsdata. Du kan gi tilgang i innstillingene når som helst.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Gi tilgang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ikke nå")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

/**
 * Address suggestions component that shows a list of suggested addresses.
 * Properly integrated with StateFlow for reactive UI updates.
 */
@Composable
fun AddressSuggestions(
    addresses: List<Adresser>,
    onAddressSelected: (Adresser) -> Unit
) {
    val primaryTextColor = MaterialTheme.colorScheme.onPrimary
    val borderColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else Color.Gray
    MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        modifier = Modifier
            .width(300.dp)
            .heightIn(min = 10.dp, max = 272.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            color = backgroundColor,
            border = BorderStroke(2.dp, borderColor)
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 15.dp)
            ) {
                itemsIndexed(addresses) { index, address ->
                    AddressItem(
                        addressItem = address,
                        onAddressClick = onAddressSelected,
                        textColor = primaryTextColor
                    )
                    if (index < addresses.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 1.dp,
                            color = borderColor
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun AddressItem(
    addressItem: Adresser,
    onAddressClick: (Adresser) -> Unit,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onAddressClick(addressItem) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${addressItem.adressetekst}, ${addressItem.postnummer} ${addressItem.poststed}",
            color = textColor,
            fontSize = 16.sp,
            letterSpacing = 0.5.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
    }
}