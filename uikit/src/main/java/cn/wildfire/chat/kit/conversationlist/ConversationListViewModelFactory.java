/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import cn.wildfirechat.model.Conversation;

public class ConversationListViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private List<Conversation.ConversationType> types;
    private List<Integer> lines;

    public ConversationListViewModelFactory(List<Conversation.ConversationType> types, List<Integer> lines) {
        this.types = types;
        this.lines = lines;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ConversationListViewModel(this.types, this.lines);
    }
}
