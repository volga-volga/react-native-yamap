package ru.yamap.search

import com.yandex.mapkit.geometry.Point

class MapSearchItemComponent {
    @JvmField
    var kind: Int? = null
    @JvmField
    var name: String? = null
}

class MapSearchItem {
    @JvmField
    var formatted: String? = null
    @JvmField
    var country_code: String? = null
    @JvmField
    var uri: String? = null
    @JvmField
    var point: Point? = null
    @JvmField
    var Components: ArrayList<MapSearchItemComponent>? = null
}
