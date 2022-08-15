package ru.vvdev.yamap.suggest;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestSession;
import com.yandex.mapkit.search.SuggestType;
import com.yandex.runtime.Error;

import java.util.ArrayList;
import java.util.List;

import ru.vvdev.yamap.utils.Callback;

public class YandexMapSuggestClient implements MapSuggestClient {
    private SearchManager searchManager;
    private SuggestOptions suggestOptions = new SuggestOptions();
    private SuggestSession suggestSession;

    /**
     * Для Яндекса нужно указать географическую область поиска. В дефолтном варианте мы не знаем какие
     * границы для каждого конкретного города, поэтому поиск осуществляется по всему миру.
     * Для `BoundingBox` нужно указать ширину и долготу для юго-западной точки и северо-восточной
     * в градусах. Получается, что координаты самой юго-западной точки, это
     * ширина = -90, долгота = -180, а самой северо-восточной - ширина = 90, долгота = 180
     */
    private BoundingBox defaultGeometry = new BoundingBox(new Point(-90.0, -180.0), new Point(90.0, 180.0));

    public YandexMapSuggestClient(Context context) {
        SearchFactory.initialize(context);
        searchManager = SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED);
        suggestOptions.setSuggestTypes(SearchType.GEO.value);
    }

    public void suggestHandler(final String text, final SuggestOptions options, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        if (suggestSession == null) {
            suggestSession = searchManager.createSuggestSession();
        }

        suggestSession.suggest(
                text,
                defaultGeometry,
                options,
                new SuggestSession.SuggestListener() {
                    @Override
                    public void onResponse(@NonNull List<SuggestItem> list) {
                        List<MapSuggestItem> result = new ArrayList<>(list.size());
                        for (int i = 0; i < list.size(); i++) {
                            SuggestItem rawSuggest = list.get(i);
                            MapSuggestItem suggest = new MapSuggestItem();
                            suggest.setSearchText(rawSuggest.getSearchText());
                            suggest.setTitle(rawSuggest.getTitle().getText());
                            if (rawSuggest.getSubtitle() != null) {
                                suggest.setSubtitle(rawSuggest.getSubtitle().getText());
                            }
                            suggest.setUri(rawSuggest.getUri());
                            result.add(suggest);

                        }
                        onSuccess.invoke(result);
                    }

                    @Override
                    public void onError(@NonNull Error error) {
                        onError.invoke(new IllegalStateException("suggest error: " + error));
                    }
                }
        );
    }

    @Override
    public void suggest(final String text, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        this.suggestHandler(text, this.suggestOptions, onSuccess, onError);
    }

    @Override
    public void suggest(final String text, final ReadableMap options, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        String userPositionKey = "userPosition";
        String lonKey = "lon";
        String latKey = "lat";
        String suggestWordsKey = "suggestWords";
        String suggestTypesKey = "suggestTypes";

        SuggestOptions options_ = new SuggestOptions();

        int suggestType = SuggestType.UNSPECIFIED.value;

        if (options.hasKey(suggestWordsKey) && !options.isNull(suggestWordsKey)) {
            if (options.getType(suggestWordsKey) != ReadableType.Boolean) {
                onError.invoke(new IllegalStateException("suggest error: " + suggestWordsKey + " is not a Boolean"));
                return;
            }
            boolean suggestWords = options.getBoolean(suggestWordsKey);

            options_.setSuggestWords(suggestWords);
        }

        if (options.hasKey(userPositionKey) && !options.isNull(userPositionKey)) {
            if (options.getType(userPositionKey) != ReadableType.Map) {
                onError.invoke(new IllegalStateException("suggest error: " + userPositionKey + " is not an Object"));
                return;
            }
            ReadableMap userPositionMap = options.getMap(userPositionKey);

            if (!userPositionMap.hasKey(latKey) || !userPositionMap.hasKey(lonKey)) {
                onError.invoke(new IllegalStateException("suggest error: " + userPositionKey + " does not have lat or lon"));
                return;
            }

            if (userPositionMap.getType(latKey) != ReadableType.Number || userPositionMap.getType(lonKey) != ReadableType.Number) {
                onError.invoke(new IllegalStateException("suggest error: lat or lon is not a Number"));
                return;
            }

            double lat = userPositionMap.getDouble(latKey);
            double lon = userPositionMap.getDouble(lonKey);
            Point userPosition = new Point(lat, lon);

            options_.setUserPosition(userPosition);
        }

        if (options.hasKey(suggestTypesKey) && !options.isNull(suggestTypesKey)) {
            if (options.getType(suggestTypesKey) != ReadableType.Array) {
                onError.invoke(new IllegalStateException("suggest error: " + suggestTypesKey + " is not an Array"));
                return;
            }
            ReadableArray suggestTypesArray = options.getArray(suggestTypesKey);
            for (int i = 0; i < suggestTypesArray.size(); i++) {
                if(suggestTypesArray.getType(i) != ReadableType.Number){
                    onError.invoke(new IllegalStateException("suggest error: one or more " + suggestTypesKey + " is not an Number"));
                    return;
                }
                int value = suggestTypesArray.getInt(i);
                suggestType = suggestType | value;
            }
            options_.setSuggestTypes(suggestType);
        }

        this.suggestHandler(text, options_, onSuccess, onError);
    }

    @Override
    public void resetSuggest() {
        if (suggestSession != null) {
            suggestSession.reset();
            suggestSession = null;
        }
    }
}
