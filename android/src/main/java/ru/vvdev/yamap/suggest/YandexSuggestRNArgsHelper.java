package ru.vvdev.yamap.suggest;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

public final class YandexSuggestRNArgsHelper {
    public WritableArray createSuggestsMapFrom(List<MapSuggestItem> data) {
        final WritableArray result = Arguments.createArray();

        for (int i = 0; i < data.size(); i++) {
            result.pushMap(createSuggestMapFrom(data.get(i)));
        }

        return result;
    }

    private WritableMap createSuggestMapFrom(MapSuggestItem data) {
        final WritableMap result = Arguments.createMap();
        result.putString("title", data.getTitle());
        result.putString("subtitle", data.getSubtitle());
        result.putString("uri", data.getUri());

        return result;
    }
}
