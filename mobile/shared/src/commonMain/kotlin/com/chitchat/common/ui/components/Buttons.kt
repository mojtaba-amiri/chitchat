package com.chitchat.common.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chitchat.common.MR
import dev.icerock.moko.resources.compose.colorResource


@Composable
fun HightlightButton(modifier: Modifier = Modifier,
                     text: String,
                     onClick: (() -> Unit) = {}) {
    OutlinedButton(
        modifier = modifier
            .height(50.dp),
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults
            .buttonColors(contentColor = Color.White,
                backgroundColor = colorResource(MR.colors.primaryColor)
            )
    ) {
        Text(text = text, fontSize = 15.sp)
    }
}