package com.localgrok.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localgrok.ui.components.chat.ModelOption
import com.localgrok.ui.theme.InterFont
import com.localgrok.ui.theme.LocalAppColors

// ═══════════════════════════════════════════════════════════════════════════
// MODEL OPTIONS DATA
// ═══════════════════════════════════════════════════════════════════════════

val MODEL_OPTIONS = listOf(
    ModelOption(
        id = "standard",
        displayName = "Flash",
        subtitle = "Instant chat",
        icon = Icons.Outlined.DarkMode,
        modelId = "gemma3:1b-it-qat"
    ),
    ModelOption(
        id = "reasoning",
        displayName = "Think",
        subtitle = "Tools & deep thinking",
        icon = Icons.Filled.RocketLaunch,
        modelId = "qwen3:1.7b"
    )
)

val DEFAULT_MODEL = MODEL_OPTIONS.first()

// ═══════════════════════════════════════════════════════════════════════════
// MODEL SELECTOR BOTTOM SHEET
// ═══════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorSheet(
    selectedModel: ModelOption,
    onModelSelected: (ModelOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = {
            // Custom drag handle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.borderGrey)
                )
            }
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Model options list
            MODEL_OPTIONS.forEachIndexed { index, model ->
                ModelOptionItem(
                    model = model,
                    isSelected = model.id == selectedModel.id,
                    onClick = {
                        onModelSelected(model)
                        onDismiss()
                    }
                )

                if (index < MODEL_OPTIONS.size - 1) {
                    HorizontalDivider(
                        color = colors.borderGrey.copy(alpha = 0.3f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Models" footer link
            HorizontalDivider(
                color = colors.borderGrey.copy(alpha = 0.5f),
                thickness = 0.5.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* TODO: Navigate to models settings */ }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Models",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "›",
                    fontSize = 20.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ModelOptionItem(
    model: ModelOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.darkGrey else colors.background)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = model.icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title and subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = model.displayName,
                fontFamily = InterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = model.subtitle,
                fontFamily = InterFont,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
    }
}

