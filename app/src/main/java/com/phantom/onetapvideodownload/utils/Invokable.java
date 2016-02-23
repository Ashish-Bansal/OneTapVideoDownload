package com.phantom.onetapvideodownload.utils;

public interface Invokable<T, R> {
    public R invoke(T input);
}
