/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.pick;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.utils.PinyinUtils;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class PickUserViewModel extends ViewModel {
    private List<UIUserInfo> users;
    private List<String> uncheckableIds;
    private List<String> initialCheckedIds;
    private MutableLiveData<UIUserInfo> userCheckStatusUpdateLiveData;
    private int maxPickCount = Integer.MAX_VALUE;

    public PickUserViewModel() {
        super();
    }

    public MutableLiveData<UIUserInfo> userCheckStatusUpdateLiveData() {
        if (userCheckStatusUpdateLiveData == null) {
            userCheckStatusUpdateLiveData = new MutableLiveData<>();
        }
        return userCheckStatusUpdateLiveData;
    }

    public void setMaxPickCount(int maxPickCount) {
        this.maxPickCount = maxPickCount;
    }

    public int getMaxPickCount() {
        return maxPickCount;
    }

    public void setUncheckableIds(List<String> uncheckableIds) {
        this.uncheckableIds = uncheckableIds;
        updateUserStatus();
    }

    public void addUncheckableIds(List<String> uncheckableIds) {
        if (uncheckableIds == null || uncheckableIds.isEmpty()) {
            return;
        }
        if (this.uncheckableIds == null) {
            this.uncheckableIds = new ArrayList<>();
        }
        this.uncheckableIds.addAll(uncheckableIds);
    }

    public void setInitialCheckedIds(List<String> checkedIds) {
        this.initialCheckedIds = checkedIds;
        updateUserStatus();
    }

    public List<String> getInitialCheckedIds() {
        return initialCheckedIds;
    }

    public void setUsers(List<UIUserInfo> users) {
        this.users = users;
        updateUserStatus();
    }

    private void updateUserStatus() {
        if (users == null || users.isEmpty()) {
            return;
        }
        for (UIUserInfo info : users) {
            if (initialCheckedIds != null && !initialCheckedIds.isEmpty()) {
                if (initialCheckedIds.contains(info.getUserInfo().uid)) {
                    info.setChecked(true);
                }
            }
            if (uncheckableIds != null && !uncheckableIds.isEmpty()) {
                if (uncheckableIds.contains(info.getUserInfo().uid)) {
                    info.setCheckable(false);
                }
            }
        }
    }

    public List<UIUserInfo> searchUser(String keyword) {
        if (users == null || users.isEmpty()) {
            return null;
        }

        List<UIUserInfo> resultList = new ArrayList<>();
        for (UIUserInfo info : users) {
            UserInfo userInfo = info.getUserInfo();
            String name = ChatManager.Instance().getUserDisplayName(userInfo);
            String pinyin = PinyinUtils.getPinyin(name);
            if (name.contains(keyword) || pinyin.contains(keyword.toUpperCase())) {
                resultList.add(info);
                if (uncheckableIds != null && uncheckableIds.contains(userInfo.uid)) {
                    info.setCheckable(false);
                }
                if (initialCheckedIds != null && initialCheckedIds.contains(userInfo.uid)) {
                    info.setChecked(true);
                }
            }
        }
        if (resultList.isEmpty()) {
            return null;
        }
        resultList.get(0).setShowCategory(true);
        resultList.get(0).setCategory("搜索结果");
        return resultList;
    }

    /**
     * include initial checked users
     *
     * @return
     */
    public @NonNull
    List<UIUserInfo> getCheckedUsers() {
        List<UIUserInfo> checkedUsers = new ArrayList<>();
        if (users == null) {
            return checkedUsers;
        }
        for (UIUserInfo info : users) {
            if (info.isCheckable() && info.isChecked()) {
                checkedUsers.add(info);
            }
        }
        return checkedUsers;
    }

    /**
     * @param userInfo
     * @param checked
     * @return 选择成功，则返回true；否则，失败
     */
    public boolean checkUser(UIUserInfo userInfo, boolean checked) {
        if (checked && getCheckedUsers() != null && getCheckedUsers().size() >= maxPickCount) {
            return false;
        }
        userInfo.setChecked(checked);
        if (userCheckStatusUpdateLiveData != null) {
            userCheckStatusUpdateLiveData.setValue(userInfo);
        }
        return true;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
