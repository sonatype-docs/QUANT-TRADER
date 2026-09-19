package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stairs
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExecutionLadderStep
import com.example.data.StepState
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.StatusScanning
import com.example.ui.theme.SunsetOrange

@Composable
fun ExecutionLadderView(
    steps: List<ExecutionLadderStep>,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stairs,
                    contentDescription = "Execution Ladder",
                    tint = SunsetOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Execution Ladder",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Dynamic Bracket",
                color = colors.textMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            // Continuous vertical line behind the nodes
            Box(
                modifier = Modifier
                    .padding(start = 11.dp, top = 14.dp, bottom = 14.dp)
                    .width(2.dp)
                    .height(280.dp)
                    .background(colors.border)
            )

            // Steps Column
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                steps.forEach { step ->
                    LadderStepRow(step = step)
                }
            }
        }
    }
}

@Composable
private fun LadderStepRow(
    step: ExecutionLadderStep,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    when (step.state) {
        StepState.LIVE_MARK -> {
            // Live Mark is highlighted in a cyan container
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PnlPositiveCyan.copy(alpha = 0.10f))
                    .border(1.dp, PnlPositiveCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Node circle
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(PnlPositiveCyan)
                        .border(2.dp, colors.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${step.levelName}: ${step.price}",
                            color = PnlPositiveCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PnlPositiveCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = step.badgeText,
                                color = PnlPositiveCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = step.description,
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Live Mark",
                    tint = PnlPositiveCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        else -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Step Node
                when (step.state) {
                    StepState.PENDING -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(colors.container)
                                .border(2.dp, colors.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusScanning)
                            )
                        }
                    }
                    StepState.FILLED -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PnlPositive)
                                .border(2.dp, colors.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Filled",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    StepState.ENTRY -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SunsetOrange)
                                .border(2.dp, colors.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                    StepState.RISK_FREE_STOP -> {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF059669))
                                .border(2.dp, colors.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Risk Free Lock",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    else -> Unit
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val titleColor = when (step.state) {
                            StepState.PENDING -> StatusScanning
                            StepState.FILLED -> PnlPositive
                            StepState.ENTRY -> colors.textPrimary
                            StepState.RISK_FREE_STOP -> PnlPositive
                            else -> colors.textPrimary
                        }

                        Text(
                            text = "${step.levelName}: ${step.price}",
                            color = titleColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            textDecoration = if (step.isStrikethrough) TextDecoration.LineThrough else TextDecoration.None
                        )

                        val badgeBg = when (step.state) {
                            StepState.PENDING -> StatusScanning.copy(alpha = 0.15f)
                            StepState.FILLED -> PnlPositive.copy(alpha = 0.15f)
                            StepState.ENTRY -> colors.container
                            StepState.RISK_FREE_STOP -> PnlPositive.copy(alpha = 0.15f)
                            else -> colors.container
                        }

                        val badgeColor = when (step.state) {
                            StepState.PENDING -> StatusScanning
                            StepState.FILLED -> PnlPositive
                            StepState.ENTRY -> colors.textSecondary
                            StepState.RISK_FREE_STOP -> PnlPositive
                            else -> colors.textSecondary
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeBg)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = step.badgeText,
                                color = badgeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = step.description,
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Right trailing icon / value
                when (step.state) {
                    StepState.PENDING -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Pending",
                            tint = colors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    StepState.FILLED -> {
                        Text(
                            text = step.rightValue ?: "",
                            color = PnlPositive,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    StepState.ENTRY -> {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Maker Filled",
                            tint = SunsetOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    StepState.RISK_FREE_STOP -> {
                        Text(
                            text = step.rightValue ?: "",
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}
