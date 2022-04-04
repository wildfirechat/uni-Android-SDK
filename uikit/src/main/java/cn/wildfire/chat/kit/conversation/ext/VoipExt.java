/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.ext;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.view.View;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.annotation.ExtContextMenuItem;
import cn.wildfire.chat.kit.conversation.ConversationFragment;
import cn.wildfire.chat.kit.conversation.ext.core.ConversationExt;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class VoipExt extends ConversationExt {

    @ExtContextMenuItem(tag = ConversationExtMenuTags.TAG_VOIP_VIDEO)
    public void video(View containerView, Conversation conversation) {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!((WfcBaseActivity) activity).checkPermission(permissions)) {
                activity.requestPermissions(permissions, 100);
                return;
            }
        }
        switch (conversation.type) {
            case Single:
                videoChat(conversation.target);
                break;
            case Group:
                ((ConversationFragment) fragment).pickGroupMemberToVoipChat(false);
                break;
            default:
                break;
        }
    }

    @ExtContextMenuItem(tag = ConversationExtMenuTags.TAG_VOIP_AUDIO)
    public void audio(View containerView, Conversation conversation) {
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!((WfcBaseActivity) activity).checkPermission(permissions)) {
                activity.requestPermissions(permissions, 100);
                return;
            }
        }
        switch (conversation.type) {
            case Single:
                audioChat(conversation.target);
                break;
            case Group:
                ((ConversationFragment) fragment).pickGroupMemberToVoipChat(true);
                break;
            default:
                break;
        }
    }

    private void audioChat(String targetId) {
        WfcUIKit.singleCall(activity, targetId, true);
    }

    private void videoChat(String targetId) {
        WfcUIKit.singleCall(activity, targetId, false);
    }

    @Override
    public int priority() {
        return 99;
    }

    @Override
    public int iconResId() {
        return R.mipmap.ic_func_video;
    }

    @Override
    public boolean filter(Conversation conversation) {
        if ((conversation.type == Conversation.ConversationType.Group && AVEngineKit.isSupportMultiCall())) {
            return false;
        }

        if (conversation.type == Conversation.ConversationType.Single) {
            UserInfo userInfo = ChatManager.Instance().getUserInfo(conversation.target, false);
            // robot
            if (userInfo.type == 1) {
                return true;
            }
            return false;
        }
        return true;
    }


    @Override
    public String title(Context context) {
        return "视频通话";
    }


    @Override
    public String contextMenuTitle(Context context, String tag) {
        if (ConversationExtMenuTags.TAG_VOIP_AUDIO.equals(tag)) {
            return "语音通话";
        } else {
            return "视频通话";
        }
    }
}
