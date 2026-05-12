package com.example.adminlivria.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.adminlivria.R
import com.example.adminlivria.common.navigation.NavDestinations
import com.example.adminlivria.common.ui.theme.*
import com.example.adminlivria.profilecontext.presentation.SettingsViewModel

/**
 * Muestra el capital del admin. Debe recibir el mismo [settingsViewModel] que el resto del grafo
 * (p. ej. StockScreen) para que [SettingsViewModel.spend] y la API reflejen el mismo estado.
 */
@Composable
fun LivriaTopBar(
    navController: NavController,
    currentRoute: String?,
    settingsViewModel: SettingsViewModel
) {
    val settingsState by settingsViewModel.uiState.collectAsState()

    val isSettingsSelected = currentRoute == NavDestinations.SETTINGS_PROFILE_ROUTE
    val currentCapital = String.format("%.2f", settingsState.capital)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(MaterialTheme.colorScheme.tertiary)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Livria Logo",
                    modifier = Modifier.height(24.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Capital:",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    text = currentCapital,
                    color = LivriaOrange,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = AsapCondensedFontFamily,
                        fontSize = 24.sp
                    )
                )

                Spacer(Modifier.width(16.dp))

                IconButton(
                    onClick = { navController.navigate(NavDestinations.SETTINGS_PROFILE_ROUTE) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "Settings",
                        tint = if (isSettingsSelected) LivriaAmber else Color.White,
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth().height(4.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaOrange))
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaAmber))
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaYellowLight))
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaNavyBlue))
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaLightGray))
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaSoftCyan))
            Box(Modifier.weight(1f).fillMaxHeight().background(LivriaBlue))
        }
    }
}
