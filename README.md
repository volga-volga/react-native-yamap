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

class Map extends React.Component {
  handleOnRouteFound(event) {
    console.log(event.nativeEvent.routes);
  }

  render() {
    return (
      <YaMap
        vehicles={["bus", "walk"]} // bus, railway, trolleybus, tramway, suburban, underground, walk
        userLocationIcon={{ uri: 'https://www.clipartmax.com/png/middle/180-1801760_pin-png.png' }}
        onRouteFound={this.handleOnRouteFound}
        routeColors={{bus: '#fff', walk: '#f00'}}
        route={route}
        style={{ flex: 1 }}
      />
    );
  }
}
```
#### Основные типы
```typescript
interface Point {
 lat: Number; 
 lon: Number;
}  
interface Route {
  start: Point
  end: Point
}
export type Vehiles =  'bus' | 'railway' | 'tramway' | 'suburban' | 'trolleybus' | 'underground' | 'walk';
```

#### Доступные `props` для компонента **MapView**
```typescript
interface Props extends ViewProps {
  userLocationIcon: ImageSource; // иконка позиции пользователя. Доступны те же значения что и у компонента Image из react native
  route?: Route; // запрашиваемый маршурут
  vehicles?: Array<Vehiles>; // доступные виды транспорта
  routeColors?: { [key in Vehiles]: string }; // цвета отображения маршрута для каждого транспорта
  onRouteFound?: (event: Event) => void; // вызовется, если найден запрошеный маршрут
  children: Marker | Polygon | Polyline; // см раздел "Отображение примитивов"
}
```

#### Методы
- `fitAllMarkers` - подобрать положение камеры, чтобы вместить все маркеры
(если возможно)
 
- `setCenter(center: { lat, lon, zoom })` - устанавливает камеру в позицию, указанную в аргументе метода, с заданным zoom

#### Замечание
- Компонент карт стилизуется, как и View из react-native. Если карта не отображается, после инициализации с валидным ключем АПИ, вероятно необходимо прописать стиль, который опишет размеры компонента (height+width или flex)

- При использовании изображений из js (через require('./img.png')) в дебаге и релизе на андроиде могут быть разные размеры маркера. В текущей версии рекомендуется проверять рендер в релизной сборке. Будет исправлено в следующих релизах

## Отображение примитивов

### Marker

```
import { Polyline } from 'react-native-yamap';

...
<MapView>
    <Marker point={{lat: 50, lon: 50}}/>
</MapView>
```

#### Доступные props:

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

#### Доступные props:

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

#### Доступные props:

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
