package com.codinginflow.just10minutes2.common.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.google.android.material.progressindicator.CircularProgressIndicator

@Composable
fun CircularProgressIndicatorWithBackground(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.StrokeWidth
) {
    Box {
        CircularProgressIndicator( // background
            progress = 1f,
            strokeWidth = strokeWidth,
            modifier = modifier,
            color = Color.LightGray
        )
        CircularProgressIndicator(
            progress = progress,
            strokeWidth = strokeWidth,
            color = color,
            modifier = modifier
        )
    }
}
