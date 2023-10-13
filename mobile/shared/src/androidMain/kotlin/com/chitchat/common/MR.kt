package com.chitchat.common

import com.chitchat.common.R
import dev.icerock.moko.graphics.Color
import dev.icerock.moko.resources.AssetResource
import dev.icerock.moko.resources.ColorResource
import dev.icerock.moko.resources.FileResource
import dev.icerock.moko.resources.FontResource
import dev.icerock.moko.resources.ImageResource
import dev.icerock.moko.resources.PluralsResource
import dev.icerock.moko.resources.ResourceContainer
import dev.icerock.moko.resources.StringResource

public actual object MR {
    public actual object strings : ResourceContainer<StringResource> {
        public actual val getting_answer: StringResource = StringResource(R.string.getting_answer)
        public actual val end_interview: StringResource = StringResource(R.string.end_interview)
        public actual val start_interview: StringResource = StringResource(R.string.start_interview)
        public actual val gpt_answer: StringResource = StringResource(R.string.gpt_answer)
        public actual val gpt_summarize: StringResource = StringResource(R.string.gpt_summarize)
        public actual val start_conversation: StringResource = StringResource(R.string.start_conversation)
        public actual val onboard1_title: StringResource = StringResource(R.string.onboard1_title)
        public actual val onboard2_title: StringResource = StringResource(R.string.onboard2_title)
        public actual val onboard2_desc: StringResource = StringResource(R.string.onboard2_desc)
        public actual val next: StringResource = StringResource(R.string.next)
        public actual val share: StringResource = StringResource(R.string.share)
        public actual val description: StringResource = StringResource(R.string.description)
        public actual val description_chatgpt: StringResource = StringResource(R.string.description_chatgpt)
        public actual val instructions: StringResource = StringResource(R.string.instructions)
        public actual val conversation_is_empty: StringResource = StringResource(R.string.conversation_is_empty)
        public actual val conversation_is_empty_summarize: StringResource = StringResource(R.string.conversation_is_empty_summarize)
    }

    public actual object plurals : ResourceContainer<PluralsResource>

    public actual object images : ResourceContainer<ImageResource> {
        public actual val start_conversation: ImageResource = ImageResource(R.drawable.start_conversation)
        public actual val chatgpt2: ImageResource = ImageResource(R.drawable.chatgpt2)
        public actual val ic_gpt: ImageResource = ImageResource(R.drawable.ic_gpt)
        public actual val ic_share: ImageResource = ImageResource(R.drawable.ic_share)
        public actual val ic_play: ImageResource = ImageResource(R.drawable.ic_play)
        public actual val ic_stop: ImageResource = ImageResource(R.drawable.ic_stop)
        public actual val ic_summarize: ImageResource = ImageResource(R.drawable.ic_summarize)
        public actual val ic_chat: ImageResource = ImageResource(R.drawable.ic_chat)
    }

    public actual object fonts : ResourceContainer<FontResource>

    public actual object files : ResourceContainer<FileResource>

    public actual object colors : ResourceContainer<ColorResource>{
        public actual val primaryColor: ColorResource = ColorResource(R.color.primaryColor)
        public actual val secondaryColor: ColorResource = ColorResource(R.color.secondaryColor)
        public actual val cardColor: ColorResource = ColorResource(R.color.cardColor)
    }

    public actual object assets : ResourceContainer<AssetResource>
}
