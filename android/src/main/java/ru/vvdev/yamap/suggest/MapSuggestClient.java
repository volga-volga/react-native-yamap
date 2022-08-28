package ru.vvdev.yamap.suggest;

import com.facebook.react.bridge.ReadableMap;
import com.yandex.mapkit.search.SuggestOptions;

import java.util.List;

import ru.vvdev.yamap.utils.Callback;

public interface MapSuggestClient {
    /**
     * Получить саджесты по тексту {@code text}.
     * Вернуть результат в метод {@code onSuccess} в случае успеха, в случае неудачи в {@code onError}
     */
    void suggest(final String text, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError);
    void suggest(final String text, final ReadableMap options, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError);
    /**
     * Остановить сессию поиска саджестов
     */
    void resetSuggest();
}
