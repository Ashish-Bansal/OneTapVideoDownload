package com.phantom.utils;

public interface Invokable<T, R> {
    R invoke(T input);
}
