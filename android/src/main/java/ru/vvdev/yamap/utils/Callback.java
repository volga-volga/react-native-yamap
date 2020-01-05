package ru.vvdev.yamap.utils;

public interface Callback<T> {
    void invoke(T arg);
}
