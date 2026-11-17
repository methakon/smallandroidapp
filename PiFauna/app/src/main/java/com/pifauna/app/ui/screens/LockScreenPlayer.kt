package com.pifauna.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pifauna.app.audio.ToneMode
import com.pifauna.app.data.SpeciesProfiles
import com.pifauna.app.mantra.MantraRuntime
import com.pifauna.app.playback.PlaybackRuntime
import com.pifauna.app.playback.PlayerController
import com.pifauna.app.util.TimeFormat
import `in`.berhampore.dhar.layaswarna.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 01 · Amethyst glass (Figma node 279:48)
 *
 * Full-screen lock-screen media player featuring:
 * - Ambient Amethyst (#151024 -> #302342 -> #100e1b) atmosphere with subtle glows
 * - Lock-screen chrome: Status time, lock indicator, battery, full-size clock (94sp) & live date
 * - Frosted glass playback card (350 x 332 dp) with 30dp radius and 1px border
 * - Active pill badge, tone details, live scrub progression (elapsed & remaining)
 * - Three dedicated controls: Play (52dp), Pause (64dp primary gold CTA), and Stop (52dp)
 * - Lock-screen footer with safety disclosure and swipe-up to return/unlock
 */
@Composable
fun LockScreenPlayerScreen(
    controller: PlayerController,
    onUnlock: () -> Unit
) {
    val context = LocalContext.current
    val playerState by controller.state.collectAsState()
    val mantraPlaying by MantraRuntime.isPlaying.collectAsState()
    val mantraPaused by MantraRuntime.isPaused.collectAsState()
    val mantraState by MantraRuntime.sessionState.collectAsState()

    val isMantraActive = mantraPlaying || mantraPaused
    val isAnyAudioPlaying = playerState.isPlaying || mantraPlaying
    val isAnyAudioPaused = playerState.isPaused || mantraPaused

    // Real-time clock & date update
    var currentTimeString by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var currentDateString by remember { mutableStateOf(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDateString = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(now)
            delay(1000L)
        }
    }

    // Playback countdown & progress tick
    var remainingMs by remember { mutableLongStateOf(playerState.remainingMs()) }
    LaunchedEffect(playerState.isPlaying, playerState.isPaused, playerState.startedAtElapsed) {
        while (true) {
            remainingMs = playerState.remainingMs()
            delay(500L)
        }
    }

    val totalDurationMs = playerState.durationMinutes * 60_000L
    val elapsedMs = (totalDurationMs - remainingMs).coerceAtLeast(0L)
    val progressFraction = if (totalDurationMs > 0L) {
        (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val profile = SpeciesProfiles.byId(playerState.profileId)

    // Palette tokens from Figma 279:48
    val bgGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF151024),
            Color(0xFF302342),
            Color(0xFF100E1B)
        )
    )
    val frostedGlassFill = Color(0x12FFFFFF)
    val frostedGlassBorder = Color(0x33FFFFFF)
    val goldAccent = Color(0xFFDEC588)
    val goldText = Color(0xFFE6CCA0)
    val plumDark = Color(0xFF271F32)
    val lightPurpleText = Color(0xFFE4DCEB)
    val activePillGreen = Color(0xFF8BE6C6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // Swiping up unlocks / returns to main player
                    if (dragAmount < -20f) {
                        onUnlock()
                    }
                }
            }
    ) {
        // Ambient Light glow (top-left amethyst glow)
        Box(
            modifier = Modifier
                .size(450.dp)
                .offset(x = (-100).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x369F7EC1), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        // Warm Light glow (bottom warm gold glow)
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-80).dp, y = 420.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x28BC965C), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))

            // 1. Status Bar Chrome (Time 10:08, Lock icon, Battery indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentTimeString,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Lock icon
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.2.dp, Color.White, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                    }
                    // Battery indicator
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(12.dp)
                            .border(1.dp, Color(0xA6FFFFFF), RoundedCornerShape(2.5.dp))
                            .padding(1.5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(playerState.snapshot.batteryPercent / 100f)
                                .height(8.dp)
                                .background(Color.White, RoundedCornerShape(1.dp))
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Center Lock Icon
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(1.6.dp, Color.White, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color.White, CircleShape)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Date (e.g. Friday, 6 November)
            Text(
                text = currentDateString,
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            // Large Digital Clock (94sp)
            Text(
                text = currentTimeString,
                color = Color.White,
                fontSize = 86.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-3).sp,
                lineHeight = 98.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            // 2. Frosted Glass Playback Panel (Figma 279:65, 350x332 dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(frostedGlassFill)
                    .border(1.dp, frostedGlassBorder, RoundedCornerShape(30.dp))
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row: App Tile + Audible Tones + Active/Paused Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 32x32 App Tile
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0x29E6CCA0), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .border(1.2.dp, goldAccent, CircleShape)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isMantraActive) "Mantra Japa Studio" else "Audible tones",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Built-in speaker",
                                    color = lightPurpleText,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Active / Paused Pill Badge
                        val pillColor = if (isAnyAudioPlaying) activePillGreen else goldAccent
                        Box(
                            modifier = Modifier
                                .border(1.dp, pillColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                .background(pillColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isAnyAudioPlaying) "Active" else if (isAnyAudioPaused) "Paused" else "Idle",
                                color = pillColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // Session Title
                    val sessionTitle = if (isMantraActive) {
                        mantraState?.title?.ifBlank { null } ?: "Mantra Japa Practice"
                    } else {
                        playerState.displayName ?: "Fixed ${playerState.startHz} Hz · ${profile.name}"
                    }

                    val sessionSubtitle = if (isMantraActive) {
                        val c = (mantraState?.completedChants ?: 0) + 1
                        val total = mantraState?.totalChants ?: 108
                        val set = mantraState?.currentSet ?: 1
                        val sets = mantraState?.sets ?: 1
                        "Chant $c of $total · Set $set of $sets"
                    } else {
                        "${if (playerState.mode == ToneMode.FIXED) "Pure tone" else playerState.mode.name.lowercase().replaceFirstChar { it.uppercase() }} · ${(playerState.gain * 100).toInt()}% volume"
                    }

                    Text(
                        text = sessionTitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = sessionSubtitle,
                        color = goldText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(Modifier.height(18.dp))

                    // Real-time Scrubber Track & Elapsed / Remaining Timers
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Color(0x1FFFFFFF), RoundedCornerShape(3.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(6.dp)
                                .background(goldAccent, RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val elapsedClock = if (isMantraActive) {
                            "${(mantraState?.completedChants ?: 0)} chants done"
                        } else {
                            TimeFormat.clock(elapsedMs)
                        }
                        val remainingClock = if (isMantraActive) {
                            val left = (mantraState?.totalChants ?: 108) - (mantraState?.completedChants ?: 0)
                            "$left chants left"
                        } else {
                            "${TimeFormat.clock(remainingMs)} left"
                        }

                        Text(text = elapsedClock, color = lightPurpleText, fontSize = 11.sp)
                        Text(text = remainingClock, color = lightPurpleText, fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(18.dp))

                    // Three Control Buttons (Play, Pause, Stop)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Play Button (Left)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, Color(0x2EFFFFFF), CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (isMantraActive) {
                                            MantraRuntime.resume(context)
                                        } else {
                                            if (playerState.isPaused) {
                                                PlaybackRuntime.resume(context)
                                            } else if (!playerState.isPlaying) {
                                                PlaybackRuntime.start(context, true)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_media_play),
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Play",
                                color = Color(0xFFF1EAF6),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // 2. Pause Button (Center, Primary 64dp CTA)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .shadow(elevation = 6.dp, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(goldAccent)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (isMantraActive) {
                                            if (mantraPlaying) MantraRuntime.pause(context)
                                            else MantraRuntime.resume(context)
                                        } else {
                                            if (playerState.isPlaying) PlaybackRuntime.pause(context)
                                            else PlaybackRuntime.resume(context)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(
                                        if (isAnyAudioPlaying) R.drawable.ic_media_pause else R.drawable.ic_media_play
                                    ),
                                    contentDescription = if (isAnyAudioPlaying) "Pause" else "Resume",
                                    tint = plumDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (isAnyAudioPlaying) "Pause" else "Resume",
                                color = goldText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // 3. Stop Button (Right)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, Color(0x2EFFFFFF), CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (isMantraActive) {
                                            MantraRuntime.stop(context, "Stopped from lock screen.")
                                        } else {
                                            PlaybackRuntime.stop(context, "Stopped from lock screen.")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_media_stop),
                                    contentDescription = "Stop",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Stop",
                                color = Color(0xFFF1EAF6),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Panel Footer Text
                    Text(
                        text = if (isMantraActive) {
                            "Japa repetition practice · Stops safely upon completion"
                        } else {
                            "${playerState.durationMinutes}-minute session · Ends automatically"
                        },
                        color = lightPurpleText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // 3. Lock-Screen Footer (Figma 279:66)
            Text(
                text = "Only your started session plays while locked.\nStop or safety warnings end sound immediately.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Swipe Up / Tap to Return
            Text(
                text = "Swipe up to unlock",
                color = Color.White.copy(alpha = 0.80f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable { onUnlock() }
            )

            Spacer(Modifier.height(10.dp))

            // Home indicator bar (126 x 5 dp)
            Box(
                modifier = Modifier
                    .width(126.dp)
                    .height(5.dp)
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
                    .clickable { onUnlock() }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}
