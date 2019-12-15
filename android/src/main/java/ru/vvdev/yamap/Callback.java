package ru.vvdev.yamap;

public interface Callback<T> {
    public void invoke(T arg);
}
