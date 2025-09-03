package no.uio.ifi.in2000.team33.lumo.ui.utility.data

import no.uio.ifi.in2000.team33.lumo.data.database.PowerConsumptionEntity

data class PowerConsumptionUiState(
    val consumption: PowerConsumptionEntity = PowerConsumptionEntity(),
    val isLoading: Boolean = true
)