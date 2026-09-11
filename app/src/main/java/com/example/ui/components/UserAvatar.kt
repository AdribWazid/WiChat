package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage

@Composable
fun UserAvatar(
    displayName: String,
    avatarUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color? = null,
    borderWidth: Dp = 2.dp,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
) {
    val initialLetter = displayName.trim().take(1).uppercase()
    val borderModifier = if (borderColor != null) {
        Modifier.border(borderWidth, borderColor, CircleShape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .then(borderModifier)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUri.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = avatarUri,
                contentDescription = "$displayName's profile picture",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        InitialLetterOrIcon(
                            initialLetter = initialLetter,
                            contentColor = contentColor,
                            textStyle = textStyle,
                            size = size
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(containerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        InitialLetterOrIcon(
                            initialLetter = initialLetter,
                            contentColor = contentColor,
                            textStyle = textStyle,
                            size = size
                        )
                    }
                }
            )
        } else {
            InitialLetterOrIcon(
                initialLetter = initialLetter,
                contentColor = contentColor,
                textStyle = textStyle,
                size = size
            )
        }
    }
}

@Composable
private fun InitialLetterOrIcon(
    initialLetter: String,
    contentColor: Color,
    textStyle: TextStyle,
    size: Dp
) {
    if (initialLetter.isNotEmpty()) {
        Text(
            text = initialLetter,
            style = textStyle,
            color = contentColor
        )
    } else {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
