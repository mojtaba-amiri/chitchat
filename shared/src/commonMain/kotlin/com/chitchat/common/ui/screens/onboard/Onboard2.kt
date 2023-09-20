package com.chitchat.common.ui.screens.onboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chitchat.common.MR
import com.chitchat.common.ui.components.BodyText
import com.chitchat.common.ui.components.HeaderText
import com.chitchat.common.ui.components.HightlightButton
import com.chitchat.common.ui.navigator.NavigatorViewModel
import com.chitchat.common.ui.navigator.Screen
import dev.icerock.moko.mvvm.compose.getViewModel
import dev.icerock.moko.mvvm.compose.viewModelFactory
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import org.jetbrains.compose.resources.ExperimentalResourceApi


@OptIn(ExperimentalResourceApi::class)
@Composable
fun Onboard2(modifier: Modifier = Modifier,
             navigator: NavigatorViewModel) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeaderText(
            text = stringResource(MR.strings.onboard2_title)
        )
        Image(
            painter = painterResource(MR.images.chatgpt2),
            contentDescription = null
        )
        BodyText(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(MR.strings.onboard2_desc)
        )
        HightlightButton(
            modifier = Modifier
                .width(200.dp)
                .padding(top = 20.dp),
            text = stringResource(MR.strings.start_conversation),
            onClick = { navigator.navigate(Screen.Conversation) }
        )
    }
}