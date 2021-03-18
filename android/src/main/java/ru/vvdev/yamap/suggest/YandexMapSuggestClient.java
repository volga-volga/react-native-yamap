package ru.vvdev.yamap.suggest;

import android.content.Context;

import androidx.annotation.NonNull;

import com.yandex.mapkit.geometry.BoundingBox;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.search.SearchFactory;
import com.yandex.mapkit.search.SearchManager;
import com.yandex.mapkit.search.SearchManagerType;
import com.yandex.mapkit.search.SearchType;
import com.yandex.mapkit.search.SuggestItem;
import com.yandex.mapkit.search.SuggestOptions;
import com.yandex.mapkit.search.SuggestSession;
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

    @Override
    public void suggest(final String text, final Callback<List<MapSuggestItem>> onSuccess, final Callback<Throwable> onError) {
        if (suggestSession == null) {
            suggestSession = searchManager.createSuggestSession();
        }
        suggestSession.suggest(
                text,
                defaultGeometry,
                suggestOptions,
                new SuggestSession.SuggestListener() {
                    @Override
                    public void onResponse(@NonNull List<SuggestItem> list) {
                        List<MapSuggestItem> result = new ArrayList<>(list.size());
                        for (int i = 0; i < list.size(); i++) {
                            SuggestItem rawSuggest = list.get(i);
                            MapSuggestItem suggest = new MapSuggestItem();
                            suggest.setSearchText(rawSuggest.getSearchText());
                            suggest.setTitle(rawSuggest.getTitle().getText());
                            suggest.setSubtitle(rawSuggest.getSubtitle().getText());
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
    public void resetSuggest() {
        if (suggestSession != null) {
            suggestSession.reset();
            suggestSession = null;
        }
    }
}
