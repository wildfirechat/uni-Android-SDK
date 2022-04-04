/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.group.manage;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfirechat.model.GroupInfo;

public class GroupManageActivity extends WfcBaseActivity {

    @Override
    protected void afterViews() {
        GroupInfo groupInfo = getIntent().getParcelableExtra("groupInfo");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, GroupManageFragment.newInstance(groupInfo))
                .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }
}
