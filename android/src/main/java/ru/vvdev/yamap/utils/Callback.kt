package ru.vvdev.yamap.utils

interface Callback<T> {
    fun invoke(arg: T)
}
