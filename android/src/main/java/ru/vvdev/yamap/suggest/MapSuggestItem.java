package ru.vvdev.yamap.suggest;

import javax.annotation.Nullable;

public class MapSuggestItem {
    private String searchText;
    private String title;
    @Nullable
    private String subtitle;
    @Nullable
    private String uri;

    public MapSuggestItem() {
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
    }


    @Nullable
    public String getUri() {
        return uri;
    }

    public void setUri(@Nullable String uri) {
        this.uri = uri;
    }
}
