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

    private void suggestHandler(final String text, final SuggestOptions options, final BoundingBox boundingBox, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        if (suggestSession == null) {
            suggestSession = searchManager.createSuggestSession();
        }

        suggestSession.suggest(
                text,
                boundingBox,
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

    private Point mapPoint(final ReadableMap readableMap, final String pointKey) {
        final String lonKey = "lon";
        final String latKey = "lat";

        if (readableMap.getType(pointKey) != ReadableType.Map) {
            throw new IllegalStateException("suggest error: " + pointKey + " is not an Object");
        }
        final ReadableMap pointMap = readableMap.getMap(pointKey);

        if (!pointMap.hasKey(latKey) || !pointMap.hasKey(lonKey)) {
            throw new IllegalStateException("suggest error: " + pointKey + " does not have lat or lon");
        }

        if (pointMap.getType(latKey) != ReadableType.Number || pointMap.getType(lonKey) != ReadableType.Number) {
            throw new IllegalStateException("suggest error: lat or lon is not a Number");
        }

        final double lat = pointMap.getDouble(latKey);
        final double lon = pointMap.getDouble(lonKey);

        return new Point(lat, lon);
    }

    @Override
    public void suggest(final String text, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        this.suggestHandler(text, this.suggestOptions, this.defaultGeometry, onSuccess, onError);
    }

    @Override
    public void suggest(final String text, final ReadableMap options, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        final String userPositionKey = "userPosition";
        final String suggestWordsKey = "suggestWords";
        final String suggestTypesKey = "suggestTypes";
        final String boundingBoxKey = "boundingBox";
        final String southWestKey = "southWest";
        final String northEastKey = "northEast";

        SuggestOptions options_ = new SuggestOptions();

        int suggestType = SuggestType.GEO.value;
        BoundingBox boundingBox = this.defaultGeometry;

        if (options.hasKey(suggestWordsKey) && !options.isNull(suggestWordsKey)) {
            if (options.getType(suggestWordsKey) != ReadableType.Boolean) {
                onError.invoke(new IllegalStateException("suggest error: " + suggestWordsKey + " is not a Boolean"));
                return;
            }
            boolean suggestWords = options.getBoolean(suggestWordsKey);

            options_.setSuggestWords(suggestWords);
        }

        if (options.hasKey(boundingBoxKey) && !options.isNull(boundingBoxKey)) {
            if (options.getType(boundingBoxKey) != ReadableType.Map) {
                onError.invoke(new IllegalStateException("suggest error: " + boundingBoxKey + " is not an Object"));
                return;
            }
            final ReadableMap boundingBoxMap = options.getMap(boundingBoxKey);

            if (!boundingBoxMap.hasKey(southWestKey) || !boundingBoxMap.hasKey(northEastKey)) {
                onError.invoke(new IllegalStateException("suggest error: " + boundingBoxKey + " does not have southWest or northEast"));
                return;
            }

            try {
                final Point southWest = mapPoint(boundingBoxMap, southWestKey);
                final Point northEast = mapPoint(boundingBoxMap, northEastKey);
                boundingBox = new BoundingBox(southWest, northEast);
            } catch (Exception bbex) {
                onError.invoke(bbex);
                return;
            }
        }

        if (options.hasKey(userPositionKey) && !options.isNull(userPositionKey)) {
            try {
                final Point userPosition = mapPoint(options, userPositionKey);
                options_.setUserPosition(userPosition);
            } catch (Exception upex) {
                onError.invoke(upex);
                return;
            }
        }

        if (options.hasKey(suggestTypesKey) && !options.isNull(suggestTypesKey)) {
            if (options.getType(suggestTypesKey) != ReadableType.Array) {
                onError.invoke(new IllegalStateException("suggest error: " + suggestTypesKey + " is not an Array"));
                return;
            }
            suggestType = SuggestType.UNSPECIFIED.value;
            ReadableArray suggestTypesArray = options.getArray(suggestTypesKey);
            for (int i = 0; i < suggestTypesArray.size(); i++) {
                if(suggestTypesArray.getType(i) != ReadableType.Number){
                    onError.invoke(new IllegalStateException("suggest error: one or more " + suggestTypesKey + " is not an Number"));
                    return;
                }
                int value = suggestTypesArray.getInt(i);
                suggestType = suggestType | value;
            }
        }

        options_.setSuggestTypes(suggestType);
        this.suggestHandler(text, options_, boundingBox, onSuccess, onError);
    }

    @Override
    public void resetSuggest() {
        if (suggestSession != null) {
            suggestSession.reset();
            suggestSession = null;
        }
    }
}
