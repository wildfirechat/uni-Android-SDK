/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.channel.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.wildfire.chat.kit.R2;
import cn.wildfirechat.model.ChannelInfo;

public class ChannelViewHolder extends RecyclerView.ViewHolder {
    @BindView(R2.id.portraitImageView)
    ImageView portraitImageView;
    @BindView(R2.id.channelNameTextView)
    TextView channelNameTextView;

    public ChannelViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(ChannelInfo channelInfo) {
        channelNameTextView.setText(channelInfo.name == null ? "< " + channelInfo.channelId + "> " : channelInfo.name);
        Glide.with(itemView.getContext()).load(channelInfo.portrait).into(portraitImageView);
    }
}
