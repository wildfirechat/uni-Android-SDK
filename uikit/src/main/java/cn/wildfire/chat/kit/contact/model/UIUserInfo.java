/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.wildfire.chat.kit.utils.PinyinUtils;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class UIUserInfo {
    private String category = "";
    private String desc = "";
    // 用来排序的字段
    private String sortName;
    private boolean showCategory;
    private UserInfo userInfo;
    private boolean isChecked;
    private boolean isCheckable = true;
    private String extra;

    public UIUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setShowCategory(boolean showCategory) {
        this.showCategory = showCategory;
    }

    public String getCategory() {
        return category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isShowCategory() {
        return showCategory;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public String getSortName() {
        return sortName;
    }

    public void setSortName(String displayNamePinyin) {
        this.sortName = displayNamePinyin;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isCheckable() {
        return isCheckable;
    }

    public void setCheckable(boolean checkable) {
        isCheckable = checkable;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public static UIUserInfo fromUserInfo(UserInfo userInfo) {
        UIUserInfo info = new UIUserInfo(userInfo);
        String indexLetter;
        String displayName = ChatManager.Instance().getUserDisplayName(userInfo);
        if (!TextUtils.isEmpty(displayName)) {
            String pinyin = PinyinUtils.getPinyin(displayName);
            char c = pinyin.toUpperCase().charAt(0);
            if (c >= 'A' && c <= 'Z') {
                indexLetter = c + "";
                info.setSortName(pinyin);
            } else {
                indexLetter = "#";
                // 为了让排序排到最后
                info.setSortName("{" + pinyin);
            }
            info.setCategory(indexLetter);
        } else {
            info.setSortName("");
        }
        return info;
    }

    public static List<UIUserInfo> fromUserInfos(List<UserInfo> userInfos) {
        return fromUserInfos(userInfos, false);
    }

    public static List<UIUserInfo> fromUserInfos(List<UserInfo> userInfos, boolean isFavUser) {
        if (userInfos != null && !userInfos.isEmpty()) {
            List<UIUserInfo> uiUserInfos = new ArrayList<>(userInfos.size());
            String indexLetter;
            for (UserInfo userInfo : userInfos) {
                uiUserInfos.add(fromUserInfo(userInfo));
            }
            Collections.sort(uiUserInfos, (o1, o2) -> o1.getSortName().compareToIgnoreCase(o2.getSortName()));

            if (isFavUser) {
                UIUserInfo uiUserInfo = uiUserInfos.get(0);
                uiUserInfo.setShowCategory(true);
                uiUserInfo.setCategory("星标朋友");
            } else {
                String preIndexLetter = null;
                for (UIUserInfo info : uiUserInfos) {
                    indexLetter = info.getCategory();
                    if (preIndexLetter == null || !preIndexLetter.equals(indexLetter)) {
                        info.setShowCategory(true);
                    }
                    preIndexLetter = indexLetter;
                }
            }
            return uiUserInfos;
        } else {
            return null;
        }
    }
}
