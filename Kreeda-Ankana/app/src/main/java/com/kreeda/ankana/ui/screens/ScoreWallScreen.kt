package com.kreeda.ankana.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kreeda.ankana.R
import com.kreeda.ankana.data.model.MatchResult
import com.kreeda.ankana.ui.theme.*
import com.kreeda.ankana.viewmodel.ScoreWallViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreWallScreen(viewModel: ScoreWallViewModel = viewModel()) {
    val matches by viewModel.recentMatches.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val context = LocalContext.current

    // Add score form state
    var team1Name by remember { mutableStateOf("") }
    var team2Name by remember { mutableStateOf("") }
    var team1Score by remember { mutableStateOf("") }
    var team2Score by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("Cricket") }
    var winnerName by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(TertiaryBlue, Color(0xFF1565C0))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🏆 ${stringResource(R.string.score_wall)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.score_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                FloatingActionButton(
                    onClick = { viewModel.toggleAddDialog() },
                    containerColor = Color.White,
                    contentColor = TertiaryBlue,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_score))
                }
            }
        }

        // ── Match Results Feed ──────────────────────────────────────────
        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏆", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_matches),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(matches, key = { it.matchId }) { match ->
                    MatchScoreCard(match)
                }
            }
        }
    }

    // ── Add Score Dialog ────────────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleAddDialog() },
            title = {
                Text(
                    text = stringResource(R.string.add_match_result),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Sport chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Cricket", "Volleyball", "Kabaddi").forEach { s ->
                            FilterChip(
                                selected = sport == s,
                                onClick = { sport = s },
                                label = { Text(s) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = team1Name,
                        onValueChange = { team1Name = it },
                        label = { Text(stringResource(R.string.team_1_name)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = team1Score,
                        onValueChange = { team1Score = it },
                        label = { Text(stringResource(R.string.team_1_score)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = team2Name,
                        onValueChange = { team2Name = it },
                        label = { Text(stringResource(R.string.team_2_name)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = team2Score,
                        onValueChange = { team2Score = it },
                        label = { Text(stringResource(R.string.team_2_score)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = winnerName,
                        onValueChange = { winnerName = it },
                        label = { Text(stringResource(R.string.winner_name)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(stringResource(R.string.match_date_hint)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (team1Name.isNotBlank() && team2Name.isNotBlank() &&
                            team1Score.isNotBlank() && team2Score.isNotBlank()
                        ) {
                            viewModel.addScore(team1Name, team2Name, sport, team1Score, team2Score, winnerName, date)
                            team1Name = ""; team2Name = ""; team1Score = ""; team2Score = ""
                            winnerName = ""; date = ""
                            Toast.makeText(context, "✅ Score added!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.add_score))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.toggleAddDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun MatchScoreCard(match: MatchResult) {
    val sportColor = when (match.sport.lowercase()) {
        "cricket" -> CricketColor
        "volleyball" -> VolleyballColor
        "kabaddi" -> KabaddiColor
        else -> TertiaryBlue
    }

    val sportEmoji = when (match.sport.lowercase()) {
        "cricket" -> "🏏"
        "volleyball" -> "🏐"
        "kabaddi" -> "🤼"
        else -> "⚽"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Sport & Date header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = sportColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "$sportEmoji ${match.sport}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = sportColor
                    )
                }
                Text(
                    text = match.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scoreboard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Team 1
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (match.winnerName == match.team1Name) SuccessGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.team1Name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = if (match.winnerName == match.team1Name) SuccessGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.team1Name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = match.team1Score,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (match.winnerName == match.team1Name) SuccessGreen
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                // VS divider
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }

                // Team 2
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (match.winnerName == match.team2Name) SuccessGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.team2Name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = if (match.winnerName == match.team2Name) SuccessGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = match.team2Name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = match.team2Score,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = if (match.winnerName == match.team2Name) SuccessGreen
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Winner banner
            if (match.winnerName.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${match.winnerName} Won!",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }
        }
    }
}
