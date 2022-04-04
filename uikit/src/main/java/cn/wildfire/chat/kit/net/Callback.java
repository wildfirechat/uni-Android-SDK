/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.net;

/**
 * Created by imndx on 2017/12/15.
 */

public interface Callback<T> {
    void onSuccess(T t);

    void onFailure(int code, String message);
}
