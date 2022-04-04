/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist.viewholder;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import cn.wildfire.chat.kit.GlideApp;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.annotation.ConversationInfoType;
import cn.wildfire.chat.kit.annotation.EnableContextMenu;
import cn.wildfire.chat.kit.third.utils.UIUtils;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;

@ConversationInfoType(type = Conversation.ConversationType.Single, line = 0)
@EnableContextMenu
public class UnknownConversationViewHolder extends ConversationViewHolder {
    public UnknownConversationViewHolder(Fragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    protected void onBindConversationInfo(ConversationInfo conversationInfo) {
        GlideApp
                .with(fragment)
                .load(R.mipmap.avatar_def)
                .transforms(new CenterCrop(), new RoundedCorners(UIUtils.dip2Px(4)))
                .into(portraitImageView);
        nameTextView.setText("未知会话类型(" + conversationInfo.conversation.type.getValue() + ")或线路(" + conversationInfo.conversation.line + ")");
    }

}
