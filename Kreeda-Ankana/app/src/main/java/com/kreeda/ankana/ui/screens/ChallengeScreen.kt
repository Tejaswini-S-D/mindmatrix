package com.kreeda.ankana.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kreeda.ankana.R
import com.kreeda.ankana.data.model.Challenge
import com.kreeda.ankana.data.model.ChallengeReply
import com.kreeda.ankana.ui.theme.*
import com.kreeda.ankana.viewmodel.ChallengeViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeScreen(viewModel: ChallengeViewModel = viewModel()) {
    val challenges by viewModel.openChallenges.collectAsState()
    val showPostDialog by viewModel.showPostDialog.collectAsState()
    val context = LocalContext.current

    // Post challenge form state
    var teamName by remember { mutableStateOf("") }
    var sport by remember { mutableStateOf("Cricket") }
    var date by remember { mutableStateOf("") }
    var timePreference by remember { mutableStateOf("Evening") }
    var message by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Header ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(SecondaryOrange, SecondaryOrangeDark)
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
                        text = "⚔️ ${stringResource(R.string.challenge_board)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.challenge_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                FloatingActionButton(
                    onClick = { viewModel.togglePostDialog() },
                    containerColor = Color.White,
                    contentColor = SecondaryOrange,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.post_challenge))
                }
            }
        }

        // ── Challenge Feed ──────────────────────────────────────────────
        if (challenges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚔️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_challenges),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.be_first_challenge),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(challenges, key = { it.challengeId }) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        onReply = { cId, tName, msg ->
                            viewModel.replyToChallenge(cId, tName, msg)
                        },
                        onAccept = { cId ->
                            viewModel.acceptChallenge(cId, "My Team")
                            Toast.makeText(context, "✅ Challenge Accepted!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // ── Post Challenge Dialog ───────────────────────────────────────────
    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.togglePostDialog() },
            title = {
                Text(
                    text = stringResource(R.string.post_new_challenge),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = teamName,
                        onValueChange = { teamName = it },
                        label = { Text(stringResource(R.string.team_name)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Sport chips
                    Text(
                        text = stringResource(R.string.select_sport),
                        style = MaterialTheme.typography.labelMedium
                    )
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
                        value = date,
                        onValueChange = { date = it },
                        label = { Text(stringResource(R.string.match_date_hint)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Time preference chips
                    Text(
                        text = stringResource(R.string.time_preference),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Morning", "Afternoon", "Evening").forEach { t ->
                            FilterChip(
                                selected = timePreference == t,
                                onClick = { timePreference = t },
                                label = { Text(t) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text(stringResource(R.string.challenge_message)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamName.isNotBlank() && message.isNotBlank()) {
                            viewModel.postChallenge(teamName, sport, date, timePreference, message)
                            teamName = ""; message = ""; date = ""
                        } else {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.post_challenge))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.togglePostDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    onReply: (String, String, String) -> Unit,
    onAccept: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showReplyInput by remember { mutableStateOf(false) }
    var replyTeam by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf("") }

    val sportColor = when (challenge.sport.lowercase()) {
        "cricket" -> CricketColor
        "volleyball" -> VolleyballColor
        "kabaddi" -> KabaddiColor
        else -> TertiaryBlue
    }

    val sportEmoji = when (challenge.sport.lowercase()) {
        "cricket" -> "🏏"
        "volleyball" -> "🏐"
        "kabaddi" -> "🤼"
        else -> "⚽"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(sportColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sportEmoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = challenge.challengerTeamName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = challenge.sport,
                            style = MaterialTheme.typography.labelMedium,
                            color = sportColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (challenge.status) {
                        "OPEN" -> SuccessGreen.copy(alpha = 0.15f)
                        "ACCEPTED" -> TertiaryBlue.copy(alpha = 0.15f)
                        else -> Color.Gray.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = challenge.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (challenge.status) {
                            "OPEN" -> SuccessGreen
                            "ACCEPTED" -> TertiaryBlue
                            else -> Color.Gray
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Time
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = challenge.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = challenge.timePreference,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message
            Text(
                text = challenge.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showReplyInput = !showReplyInput },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Reply,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reply", style = MaterialTheme.typography.labelMedium)
                    }
                    if (challenge.replies.isNotEmpty()) {
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(
                                "${challenge.replies.size} replies",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Icon(
                                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (challenge.status == "OPEN") {
                    Button(
                        onClick = { onAccept(challenge.challengeId) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sportColor
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Accept ⚡", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Replies section
            AnimatedVisibility(
                visible = expanded && challenge.replies.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    challenge.replies.forEach { reply ->
                        ReplyItem(reply)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // Reply input
            AnimatedVisibility(
                visible = showReplyInput,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replyTeam,
                        onValueChange = { replyTeam = it },
                        label = { Text("Your Team Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyMessage,
                            onValueChange = { replyMessage = it },
                            label = { Text("Your reply...") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (replyTeam.isNotBlank() && replyMessage.isNotBlank()) {
                                    onReply(challenge.challengeId, replyTeam, replyMessage)
                                    replyTeam = ""
                                    replyMessage = ""
                                    showReplyInput = false
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Send,
                                contentDescription = "Send reply",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReplyItem(reply: ChallengeReply) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = reply.teamName.take(1).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = reply.teamName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = reply.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
