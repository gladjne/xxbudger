package com.example.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.export.DownloadHelper
import com.example.presentation.viewmodel.ExportUiState
import com.example.ui.theme.*

@Composable
fun ExportStatusDialog(
    exportUiState: ExportUiState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (exportUiState !is ExportUiState.Idle) {
        AlertDialog(
            onDismissRequest = { 
                if (exportUiState !is ExportUiState.Loading) {
                    onDismiss()
                }
            },
            title = {
                Text(
                    text = when (exportUiState) {
                        is ExportUiState.Loading -> "Génération de l'export..."
                        is ExportUiState.Success -> "Exportation réussie ! 🎉"
                        is ExportUiState.Error -> "Échec de l'exportation ⚠️"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, 
                        color = TextWhite,
                        fontSize = 18.sp
                    )
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (exportUiState) {
                        is ExportUiState.Loading -> {
                            CircularProgressIndicator(color = PrimaryBlue)
                            Text(
                                text = "Joy prépare son analyse, compile vos transactions et rédige vos conseils personnalisés...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary, 
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        is ExportUiState.Success -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Succès",
                                tint = ColorIncome,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "Fichier téléchargé avec succès ! 📥",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary, 
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Rapport enregistré dans vos téléchargements : \n${exportUiState.destinationPath}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextMuted, 
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        DownloadHelper.openFile(context, exportUiState.file, exportUiState.mimeType)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Launch,
                                            contentDescription = null,
                                            tint = DarkBackground,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ouvrir", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        com.example.data.export.PdfShareHelper.shareFile(
                                            context = context,
                                            file = exportUiState.file,
                                            mimeType = exportUiState.mimeType,
                                            subject = "Export Budget Joy",
                                            text = "Voici mon export de données budgétaires généré par Budget Joy ! 📊"
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    border = BorderStroke(1.dp, PrimaryBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(vertical = 10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Partager", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        is ExportUiState.Error -> {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = "Erreur",
                                tint = ColorExpense,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                text = "Oups ! Impossible de finaliser l'exportation.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = TextPrimary, 
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = exportUiState.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ColorExpense, 
                                    textAlign = TextAlign.Center,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                if (exportUiState !is ExportUiState.Loading) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Fermer", 
                            color = PrimaryBlue, 
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
