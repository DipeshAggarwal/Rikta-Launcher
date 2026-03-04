package com.lumina.rikta.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lumina.rikta.R
import com.lumina.core.ui.theme.CardContainerColor
import com.lumina.core.ui.theme.ContentColor
import com.lumina.core.ui.theme.primaryContentColor
import com.lumina.rikta.utils.InstalledApp
import com.lumina.core.ui.components.text.AutoResizingText

@Composable
fun SponsorBox(
    text: String,
    secondText: String,
    onSponsorClick: () -> Unit = {},
    onBackgroundClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .padding(vertical = 1.dp)
            .fillMaxWidth()
            .clickable(onClick = {
                onBackgroundClick()
            }),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardContainerColor,
            contentColor = ContentColor
        )
    ) {
        Column(
            Modifier
                .padding(vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(R.drawable.ic_launcher_monochrome),
                "Escape Launcher Icon",
                Modifier
                    .padding(3.dp),
                tint = ContentColor
            )

            Spacer(
                Modifier.height(10.dp)
            )

            AutoResizingText(
                text = text,
                modifier = Modifier,
                color = ContentColor,
            )

            Spacer(Modifier.height(10.dp))

            AutoResizingText(
                text = secondText,
                modifier = Modifier,
                color = ContentColor,
            )

            Spacer(Modifier.height(15.dp))

            Button(
                onClick = {
                    onSponsorClick()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = primaryContentColor,
                    contentColor = CardContainerColor
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, "", tint = CardContainerColor)
                    Spacer(Modifier.width(5.dp))
                    AutoResizingText(
                        text = stringResource(R.string.sponsor),
                        color = CardContainerColor
                    )
                }
            }

        }
    }
}

@Preview
@Composable
fun SponsorBoxPreview() {
    SponsorBox("Escape Launcher", "Testing")
}

/**
 * Weather app picker
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppPicker(
    apps: List<InstalledApp>,
    onAppSelected: (InstalledApp) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(apps.sortedBy { it.displayName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(onClick = { onAppSelected(app) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = app.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryContentColor
                        )
                    }
                }
            }
        }
    }
}