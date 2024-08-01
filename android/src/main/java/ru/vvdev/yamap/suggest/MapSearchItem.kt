package ru.vvdev.yamap.suggest

class MapSearchItemComponent {
    var kind: String? = null
    var name: String? = null
}

class MapSearchItem {
    var formatted: String? = null
    var country_code: String? = null
    var Components: ArrayList<MapSearchItemComponent>? = null
}