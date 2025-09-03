package no.uio.ifi.in2000.team33.lumo.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SolarPower
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import no.uio.ifi.in2000.team33.lumo.R
import no.uio.ifi.in2000.team33.lumo.ui.utility.data.OnboardingPage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            "Ta det på Lumo",
            "Alt du trenger for å finne ut hvor mye solenergi ditt tak kan produsere",
            Icons.Outlined.SolarPower,
            R.drawable.lumo_logo
        ),
        OnboardingPage(
            "Finn din bolig",
            "I kartet kan du søke opp adresse og legge til takflater.",
            Icons.Outlined.Home,
            R.drawable.my_house
        ),
        OnboardingPage(
            "Se strømoversikt",
            "Få oversikt over estimert strømproduksjon og besparelser.",
            Icons.Outlined.Bolt,
            R.drawable.my_production
        ),
        OnboardingPage(
            "Få nøyaktige estimater",
            "Bruk strømforbruk for bedre estimat.",
            Icons.Outlined.AccountCircle,
            R.drawable.my_estimates
        ),
    )

    var currentPage by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 30.dp, end = 30.dp, bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Spacer(modifier = Modifier.height(20.dp))
            GlideImage(
                model = pages[currentPage].icon!!,
                contentDescription = "image",
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
            )


            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = pages[currentPage].title,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = pages[currentPage].description,
                    fontSize = 17.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column {
                // Indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    pages.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (index == currentPage) 12.dp else 8.dp)
                                .background(
                                    if (index == currentPage) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Forrige
                    if (currentPage > 0) {
                        // Next or Start
                        Button(
                            onClick = {
                                currentPage--
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(text = "Forrige")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // Next or Start
                    Button(
                        onClick = { if (currentPage < pages.lastIndex) currentPage++ else onFinish() },
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(text = if (currentPage == pages.lastIndex) "Start" else "Fortsett")
                    }
                }
            }
        }
    }
}