package com.phantom.utils;

public interface Invokable<T, R> {
    public R invoke(T input);
}
