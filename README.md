## Установка

```
yarn add react-native-yamap
```

### Линковка (для react-native версии меньше 60)

```
react-native link react-native-yamap
``` 

## Использование карт

### Инициализировать карты

```
// js
import YaMap from 'react-native-yamap';

YaMap.init('API_KEY');
```

### Использование компонента
```typescript jsx
import React from 'react';
import YaMap from 'react-native-yamap';

const route = {
  start: { lat: 0, lon: 0},
  end: { lat: 10, lon: 10},
};
const markers = [{ lat: 10, lon: 10}];
// ...
class Map extends React.Component {
  handleOnRouteFound(event) {
    this.setState({ routes: event.nativeEvent.routes });
  }
  handleOnMarkerPress(id: number) {
    console.log(`Marker with id ${id} pressed!`);
  }
  // ...
  render() {
    return (
      <YaMap
        vehicles={["bus", "walk"]} // bus, railway, trolleybus, tramway, suburban, underground, walk
        userLocationIcon={{ uri: 'https://www.clipartmax.com/png/middle/180-1801760_pin-png.png' }}
        onRouteFound={this.handleOnRouteFound}
        routeColors={{bus: '#fff', walk: '#f00'}}
        markers={markers}
        route={route}
        onMarkerPress={this.handleOnMarkerPress}
        style={{ flex: 1 }}
      />
    );
  }
}
```

```typescript
export interface Marker {
  lon: number,
  lat: number,
  id?: number,
  zIndex?: number,
  source?: ImageSource,
}
```
```typescript
interface Route {
  start: Point
  end: Point
}
```
```typescript
interface Point {
 lat: double 
 lon: double
}  
```


- Компонент карт стилизуется, как и View из react-native. Если карта не отображается, после инициализации с валидным ключем АПИ, вероятно необходимо прописать стиль, который опишет размеры компонента (height+width или flex)

- Для кастомизации иконки положения пользователя можно использовать userLocationIcon. Может принимать те же значения что и source в компоненте Image в react-native

- Для отображения маркеров, используется props markers, с минимальными параметрами lon и lat.

- Для кастомизации изображения маркера, маркеру можно передать параметр source, как и у Image из react-native.

- Для отображения маркера поверх других, у объекта marker используется параметр zIndex.

- Для обработки нажатия на маркер, используется onMarkerPress. В метод приходит id выбранного маркера. Если для маркеров не передается id, то вместо него будет использоваться индекс в массиве маркеров. Не рекомендуется передавать id только для части маркеров - необходимо либо передавать во все маркеры, либо не передавать нигде.

- Для центрированиия карты можно использовать методы по ref: fitAllMarkers() и setCenter(center), где center { lat, lon, zoom }. fitAllMarkers подбирает положение камеры, чтобы вместить все маркеры (если возможно)

### Замечание
При использовании изображений из js (через require('./img.png')) в дебаге и релизе на андроиде могут быть разные размеры маркера. В текущей версии рекомендуется проверять рендер в релизной сборке. Будет исправлено в следующих релизах

## Отображение примитивов

### Marker

```
import { Polyline } from 'react-native-yamap';

...
<MapView>
    <Marker point={{lat: 50, lon: 50}}/>
</MapView>
```

Доступные props:
```typescript
interface MarkerProps {
  scale?: number; // масштабирование иконки маркера. Не работает если использовать children у маркера
  point: Point; // координаты точки для отображения маркера
  source?: ImageSource; // данные для изображения маркера
  children?: React.ReactElement; // рендер маркера как компонента (не рекомендуется) 
  onPress?: () => void;
  zIndex?: number;
}
```

### Polyline
```
import { Polyline } from 'react-native-yamap';

...
<MapView>
    <Polyline points={[
      {lat: 50, lon: 50},
      {lat: 50, lon: 20},
      {lat: 20, lon: 20},
    ]}/>
</MapView>
```

Доступные props:
```typescript
interface PolylineProps {
  strokeColor?: string; // цвет линии
  outlineColor?: string; // цвет обводки
  strokeWidth?: number; // толщина линии
  outlineWidth?: number; // толщина обвотки (0 по умолчанию)
  dashLength?: number; // длина штриха
  dashOffset?: number; // отступ первого штриха от начала полилинии
  gapLength?: number; // длина разрыва между штрихами (0 по умолчанию - сплошная линия)
  points: Point[]; // массив точек линии
  zIndex?: number;
  onPress?: () => void;
}
```

### Polygon
```
import { Polygon } from 'react-native-yamap';

...
<MapView>
    <Polygon points={[
      {lat: 50, lon: 50},
      {lat: 50, lon: 20},
      {lat: 20, lon: 20},
    ]}/>
</MapView>
```

Доступные props:
```typescript
interface PolygonProps {
  fillColor?: string; // цвет заливки
  strokeColor?: string; // цвет границы
  strokeWidth?: number; // толщина границы
  points: Point[]; // точки полигона
  innerRings: (Point[])[]; // массив полилиний, которые образуют отверстия в полигоне 
  zIndex?: number;
  onPress?: () => void;
}
```

**TODO:** реализовать поддержку полигонов с отверстиями

## Использование апи геокодера

### Инициализация

```typescript jsx
import { Geocoder } from 'react-native-yamap';

Geocoder.init('API_KEY');
```

`API_KEY` для апи геокодера и для карт отличаются. Инициализировать надо оба класса, каждый со своим ключем.

### Прямое геокодирование

```typescript jsx
Geocoder.geocode(geocode: Point, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang);
```

Назначения параметров можно посмотреть в [документации геокодера][yandex-geo-doc]
Описание отета геокодера в [документации][yandex-geo-response]

#### Упрощенный вызов ####
```
Geocoder.geoToAddress(geo: Point);
```
Вернет `null` или объект адреса (строковое значение, почтовый индекс и массив компонентов адреса) первого из предложений геокодера.
```typescript jsx
interface Address {
  country_code: string;
  formatted: string;
  postal_code: string;
  Components: {kind: string, name: string}[];
}
```

### Обратное геокодирование

```typescript jsx
Geocoder.reverseGeocode(geocode: string, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang, rspn?: 0 | 1, ll?: Point, spn?: [number, number], bbox?: [Point, Point]);
```

Назначения параметров можно посмотреть в [документации геокодера][yandex-geo-doc]
Описание отета геокодера в [документации][yandex-geo-response]

#### Упрощенный вызов ####
```
Geocoder.addressToGeo(address: string);
```
Вернет `null` или координаты `{lat: number, lon: number}` первого объекта из предложений геокодера.

[yandex-geo-doc]: https://tech.yandex.ru/maps/geocoder/doc/desc/concepts/input_params-docpage

[yandex-geo-response]: https://tech.yandex.ru/maps/geocoder/doc/desc/reference/response_structure-docpage/
