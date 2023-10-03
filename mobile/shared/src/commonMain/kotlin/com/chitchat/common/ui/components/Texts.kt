package com.chitchat.common.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun HeaderText(modifier: Modifier = Modifier,
               text: String,
               fontWeight: FontWeight = FontWeight.Normal) {
    Text(
        modifier = modifier
            .fillMaxWidth(),
        color = Color.Black,
        fontWeight = fontWeight,
        text = text, fontSize = 32.sp
    )
}

@Composable
fun BodyText(modifier: Modifier = Modifier,
             text: String) {
    Text(
        modifier = modifier,
        color = Color.Black,
        text = text,
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )
}