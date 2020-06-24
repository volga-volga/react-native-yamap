## Установка

```
yarn add react-native-yamap
```

### Линковка

Если вы планируете использовать только апи геокодера, то линковка библиотеки необязательна. В таком случае, можете отключить автолинкинг библиотеки для `react-native@^0.60.0`.

Для использования Yandex MapKit необходима линковка (библиотека поддерживает автолинкинг).

#### Линковка react-native версии меньше 60

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

#### для ios

Рекомендуется инициализировать MapKit в функции `didFinishLaunchingWithOptions` в AppDelegate.m

```
#import <YandexMapKit/YMKMapKitFactory.h>

...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    ...
    [YMKMapKit setApiKey: @"API_KEY"];
    return YES;
}
```

### Изменение языка карт


```
// js
import YaMap from 'react-native-yamap';

const currentLocale = await YaMap.getLocale(); 
YaMap.setLocale('en'); // 'ru'...
YaMap.resetLocale();

```
- **getLocale(): Promise\<string\>** - возвращает используемый язык карт
- **setLocale(locale: string): Promise\<void\>** - установить язык
- **resetLocale(): Promise\<void\>** - использовать для карт язык системы

Каждый метод возвращает Promise, который выполняется, при ответе нативного sdk. Promise может отклониться, если sdk вернет ошибку.

Замечание: Изменение языка карт вступит в силу только после перезапуска приложения. См метод setLocale в [документации нативного sdk](https://tech.yandex.com/maps/mapkit/doc/3.x/concepts/android/runtime/ref/com/yandex/runtime/i18n/I18nManagerFactory-docpage/#method_detail__method_setLocale___NonNullString___NonNullString___NonNullLocaleUpdateListener).

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
        userLocationIcon={{ uri: 'https://www.clipartmax.com/png/middle/180-1801760_pin-png.png' }}
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

export type MasstransitVehicles = 'bus' | 'trolleybus' | 'tramway' | 'minibus' | 'suburban' | 'underground' | 'ferry' | 'cable' | 'funicular';

export type Vehicles = MasstransitVehicles | 'walk' | 'car';


export interface DrivingInfo {
  time: string;
  timeWithTraffic: string;
  distance: number;
}

export interface MasstransitInfo {
  time: string;
  transferCount: number;
  walkingDistance: number;
}

export interface RouteInfo<T extends (DrivingInfo | MasstransitInfo)> {
  id: string;
  sections: {
    points: Point[];
    sectionInfo: T;
    routeInfo: T;
    routeIndex: number;
    stops: any[];
    type: string;
    transports?: any;
    sectionColor?: string;
  }
}


export interface RoutesFoundEvent<T extends (DrivingInfo | MasstransitInfo)> {
  nativeEvent: {
    status: 'success' | 'error';
    id: string;
    routes: RouteInfo<T>[];
  };
}
```

#### Доступные `props` для компонента **MapView**
```typescript
interface Props extends ViewProps {
  showUserPosition?: boolean; // если false, то не будут отслеживаться и отображаться геоданные пользователя. Значение по умолчанию true
  userLocationIcon: ImageSource; // иконка позиции пользователя. Доступны те же значения что и у компонента Image из react native
  children: Marker | Polygon | Polyline; // см раздел "Отображение примитивов"
}
```

#### Методы
- `fitAllMarkers` - подобрать положение камеры, чтобы вместить все маркеры
(если возможно)
 
- `setCenter(center: { lat, lon, zoom })` - устанавливает камеру в позицию, указанную в аргументе метода, с заданным zoom

- `findRoutes(points: Point[], vehicles: Vehicles[], callback: (event: RoutesFoundEvent) => void)` - запрос маршрутов через точки `points` с использованием транспорта `vehicles`. При получении маршрутов будет вызван `callback` с информацией обо всех маршрутах (подробнее в разделе **"Запрос маршрутов"**)
- `findMasstransitRoutes(points: Point[], callback: (event: RoutesFoundEvent<MasstransitInfo>) => void): void` - запрос маршрутов на любом общественном транспорте
- `findPedestrianRoutes(points: Point[], callback: (event: RoutesFoundEvent<MasstransitInfo>) => void): void` - запрос пешеходного маршрута
- `findDrivingRoutes(points: Point[], callback: (event: RoutesFoundEvent<DrivingInfo>) => void): void` - запрос маршрута для автомобиля

#### Замечание
- Компонент карт стилизуется, как и View из react-native. Если карта не отображается, после инициализации с валидным ключем АПИ, вероятно необходимо прописать стиль, который опишет размеры компонента (height+width или flex)

- При использовании изображений из js (через require('./img.png')) в дебаге и релизе на андроиде могут быть разные размеры маркера. В текущей версии рекомендуется проверять рендер в релизной сборке. Будет исправлено в следующих релизах

## Отображение примитивов

### Marker

```
import { Marker } from 'react-native-yamap';

...
<YaMap>
    <Marker point={{lat: 50, lon: 50}}/>
</YaMap>
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
<YaMap>
    <Polyline points={[
      {lat: 50, lon: 50},
      {lat: 50, lon: 20},
      {lat: 20, lon: 20},
    ]}/>
</YaMap>
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
<YaMap>
    <Polygon points={[
      {lat: 50, lon: 50},
      {lat: 50, lon: 20},
      {lat: 20, lon: 20},
    ]}/>
</YaMap>
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

## Запрос маршрутов

Маршруты можно запросить используя метод `findRoutes` компонента `YaMap` (через ref).

`findRoutes(points: Point[], vehicles: Vehicles[], callback: (event: RoutesFoundEvent) => void)` - запрос маршрутов через точки `points` с использованием транспорта `vehicles`. При получении маршрутов будет вызван `callback` с информацией обо всех маршрутах.

В настоящее время добавлены следующие роутеры из Yandex MapKit:
- **masstransit** - для маршрутов на общественном транспорте
- **pedestrian** - для пешеходных маршрутов
- **driving** - для маршрутов на автомобиле 

Тип роутера зависит от переданного в функцию массива `vehicles`:
- если передан пустой массив (`this.map.current.findRoutes(points, [], () => null);`), то будет использован `PedestrianRouter`
- если передан массив с одним элементом `'car'` (`this.map.current.findRoutes(points, ['car'], () => null);`), то будет использован `DrivingRouter`
- во всех остальных случаях используется `MasstransitRouter`

Также можно использовать нужный роутер, вызвав соответствующую функцию
```
findMasstransitRoutes(points: Point[], callback: (event: RoutesFoundEvent) => void): void;

findPedestrianRoutes(points: Point[], callback: (event: RoutesFoundEvent) => void): void;

findDrivingRoutes(points: Point[], callback: (event: RoutesFoundEvent) => void): void;
```

#### Замечание
В зависимости от типа роутера информация о маршутах может незначительно отличаться.

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
