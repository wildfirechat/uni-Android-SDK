/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.contact.pick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.contact.model.UIUserInfo;
import cn.wildfire.chat.kit.contact.pick.viewholder.CheckableUserBlackViewHolder;
import cn.wildfire.chat.kit.contact.pick.viewholder.CheckableUserViewHolder;

public class CheckableUserListBlackAdapter extends CheckableUserListAdapter {

    public CheckableUserListBlackAdapter(Fragment fragment) {
        super(fragment);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContactViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.contact_item_contact_black, parent, false);
        CheckableUserViewHolder viewHolder = new CheckableUserBlackViewHolder(fragment, this, itemView);

        itemView.setOnClickListener(v -> {
            UIUserInfo userInfo = viewHolder.getBindContact();
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(userInfo);
            }
        });
        return viewHolder;
    }
}
