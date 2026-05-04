package ru.yamap.utils

interface Callback<T> {
    fun invoke(arg: T)
}
