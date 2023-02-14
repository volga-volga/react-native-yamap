
## React Native Yandex Maps (Яндекс Карты)

Библиотека для интеграции Yandex Maps (Яндекс Карт) в React Native.

## Пример

[Пример использования библиотеки](https://github.com/ownikss/rn-yamaps-example)

## Установка

```
yarn add react-native-yamap
```
или
```
npm i react-native-yamap --save
```

### Линковка

Если вы планируете использовать только API геокодера, то линковка библиотеки необязательна. В таком случае, можете отключить автолинкинг библиотеки для React Native **>0.60**.

Для использования Yandex MapKit необходима линковка (библиотека поддерживает автолинкинг).

#### Линковка в React Native <0.60

```
react-native link react-native-yamap
```

## Использование карт

### Инициализировать карты

Для этого лучше всего зайти в корневой файл приложения, например `App.js`, и добавить инициализацию:

```js
import YaMap from 'react-native-yamap';

YaMap.init('API_KEY');
```

#### iOS

  

**Обязательно** инициализировать MapKit в функции `didFinishLaunchingWithOptions` в AppDelegate.m/AppDelegate.mm:

```C
#import <YandexMapsMobile/YMKMapKitFactory.h>

...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  ...

  [YMKMapKit setLocale:@"ru_RU"];
  [YMKMapKit setApiKey:@"API_KEY"];

  return YES;
}
```

### Изменение языка карт

```js
import YaMap from 'react-native-yamap';

const currentLocale = await YaMap.getLocale();
YaMap.setLocale('en_US');  // 'ru_RU' или другие
YaMap.resetLocale();
```

-  **getLocale(): Promise\<string\>** - возвращает используемый язык карт;

-  **setLocale(locale: string): Promise\<void\>** - установить язык карт;

-  **resetLocale(): Promise\<void\>** - использовать для карт язык системы.

Каждый метод возвращает Promise, который выполняется при ответе нативного SDK. Promise может отклониться, если SDK вернет ошибку.

**ВАЖНО!**

1. Для **Android** изменение языка карт вступит в силу только после **перезапуска** приложения.
2. Для **iOS** методы изменения языка можно вызывать только до первого рендера карты. Также нельзя повторно вызывать метод, если язык уже изменялся (можно только после перезапуска приложения), иначе изменения приняты не будут, а в терминал будет выведено сообщение с пердупреждением. В коде при этом не будет информации об ошибке.

### Использование компонента

```jsx
import React from 'react';
import YaMap from 'react-native-yamap';

const Map = () => {
  return (
    <YaMap
      userLocationIcon={{ uri: 'https://www.clipartmax.com/png/middle/180-1801760_pin-png.png' }}
      initialRegion={{
        lat: 50,
        lon: 50,
        zoom: 10,
        azimuth: 80,
        tilt: 100
      }}
      style={{ flex: 1 }}
    />
  );
};
```

#### Основные типы

```typescript
interface Point {
  lat: Number;
  lon: Number;
}

interface ScreenPoint {
  x: number;
  y: number;
}

interface MapLoaded {
  renderObjectCount: number;
  curZoomModelsLoaded: number;
  curZoomPlacemarksLoaded: number;
  curZoomLabelsLoaded: number;
  curZoomGeometryLoaded: number;
  tileMemoryUsage: number;
  delayedGeometryLoaded: number;
  fullyAppeared: number;
  fullyLoaded: number;
}

interface InitialRegion {
  lat: number;
  lon: number;
  zoom?: number;
  azimuth?: number;
  tilt?: number;
}


type MasstransitVehicles = 'bus' | 'trolleybus' | 'tramway' | 'minibus' | 'suburban' | 'underground' | 'ferry' | 'cable' | 'funicular';

type Vehicles = MasstransitVehicles | 'walk' | 'car';

type MapType = 'none' | 'raster' | 'vector';


interface DrivingInfo {
  time: string;
  timeWithTraffic: string;
  distance: number;
}

interface MasstransitInfo {
  time:  string;
  transferCount:  number;
  walkingDistance:  number;
}

interface RouteInfo<T extends(DrivingInfo | MasstransitInfo)> {
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

interface RoutesFoundEvent<T extends(DrivingInfo | MasstransitInfo)> {
  nativeEvent:  {
    status: 'success' | 'error';
    id: string;
    routes: RouteInfo<T>[];
  };
}

interface CameraPosition {
  zoom: number;
  tilt: number;
  azimuth: number;
  point: Point;
}

type VisibleRegion = {
  bottomLeft: Point;
  bottomRight: Point;
  topLeft: Point;
  topRight: Point;
}


type YamapSuggest = {
  title: string;
  subtitle?: string;
  uri?: string;
}

type YamapCoords = {
  lon: number;
  lat: number;
}

type YamapSuggestWithCoords = {
  lon: number;
  lat: number;
  title: string;
  subtitle?: string;
  uri?: string;
}

type YandexLogoPosition = {
  horizontal: 'left' | 'center' | 'right';
  vertical: 'top' | 'bottom';
}

type YandexLogoPadding = {
  horizontal?: number;
  vertical?: number;
}
```

#### Доступные `props` для компонента **MapView**:

| Название | Тип | Стандартное значение | Описание |
|--|--|--|--|
| showUserPosition | boolean | true | Отслеживание геоданных и отображение позиции пользователя |
| followUser | boolean | true | слежение камеры за пользователем |
| userLocationIcon | ImageSource | false | Иконка для позиции пользователя. Доступны те же значения что и у компонента Image из React Native |
| userLocationIconScale | number | 1 | Масштабирование иконки пользователя |
| initialRegion | InitialRegion | | Изначальное местоположение карты при загрузке |
| interactive | boolean | true | Интерактивная ли карта (перемещение по карте, отслеживание нажатий) |
| nightMode | boolean | false | Использование ночного режима |
| onMapLoaded | function | | Колбек на загрузку карты |
| onCameraPositionChange | function | | Колбек на изменение положения камеры |
| onCameraPositionChangeEnd | function | | Колбек при завершении изменения положения камеры |
| onMapPress | function | | Событие нажития на карту. Возвращает координаты точки на которую нажали |
| onMapLongPress | function | | Событие долгого нажития на карту. Возвращает координаты точки на которую нажали |
| userLocationAccuracyFillColor | string |  | Цвет фона зоны точности определения позиции пользователя |
| userLocationAccuracyStrokeColor | string |  | Цвет границы зоны точности определения позиции пользователя |
| userLocationAccuracyStrokeWidth | number | | Толщина зоны точности определения позиции пользователя |
| scrollGesturesEnabled | boolean | true | Включены ли жесты скролла |
| zoomGesturesEnabled | boolean | true | Включены ли жесты зума |
| tiltGesturesEnabled | boolean | true | Включены ли жесты наклона камеры двумя пальцами |
| rotateGesturesEnabled | boolean | true | Включены ли жесты поворота камеры |
| fastTapEnabled | boolean | true | Убрана ли задержка в 300мс при клике/тапе |
| clusterColor | string | 'red' | Цвет фона метки-кластера |
| maxFps | number | 60 | Максимальная частота обновления карты |
| logoPosition | YandexLogoPosition | {} | Позиция логотипа Яндекса на карте |
| logoPadding | YandexLogoPadding | {} | Отступ логотипа Яндекса на карте |
| mapType | string | 'vector' | Тип карты |
| mapStyle | string | {} | Стили карты согласно [документации](https://yandex.ru/dev/maps/mapkit/doc/dg/concepts/style.html) |

#### Доступные методы для компонента **MapView**:

-  `fitMarkers(points: Point[]): void` - подобрать положение камеры, чтобы вместить указанные маркеры (если возможно);
-  `fitAllMarkers(): void` - подобрать положение камеры, чтобы вместить все маркеры (если возможно);
-  `setCenter(center: { lon: number, lat: number }, zoom: number = 10, azimuth: number = 0, tilt: number = 0, duration: number = 0, animation: Animation = Animation.SMOOTH)` - устанавливает камеру в точку с заданным zoom, поворотом по азимуту и наклоном карты (`tilt`). Можно параметризовать анимацию: длительность и тип. Если длительность установить 0, то переход будет без анимации. Возможные типы анимаций `Animation.SMOOTH` и `Animation.LINEAR`;
-  `setZoom(zoom: number, duration: number, animation: Animation)` - изменить текущий zoom карты. Параметры `duration` и `animation` работают по аналогии с `setCenter`;
-  `getCameraPosition(callback: (position: CameraPosition) => void)` - запрашивает положение камеры и вызывает переданный колбек с текущим значением;
-  `getVisibleRegion(callback: (region: VisibleRegion) => void)` - запрашивает видимый регион и вызывает переданный колбек с текущим значением;
-  `findRoutes(points: Point[], vehicles: Vehicles[], callback: (event: RoutesFoundEvent) => void)` - запрос маршрутов через точки `points` с использованием транспорта `vehicles`. При получении маршрутов будет вызван `callback` с информацией обо всех маршрутах (подробнее в разделе **"Запрос маршрутов"**);
-  `findMasstransitRoutes(points: Point[], callback: (event: RoutesFoundEvent<MasstransitInfo>) => void): void` - запрос маршрутов на любом общественном транспорте;
-  `findPedestrianRoutes(points: Point[], callback: (event: RoutesFoundEvent<MasstransitInfo>) => void): void` - запрос пешеходного маршрута;
-  `findDrivingRoutes(points: Point[], callback: (event: RoutesFoundEvent<DrivingInfo>) => void): void` - запрос маршрута для автомобиля;
-  `setTrafficVisible(isVisible: boolean): void` - включить/отключить отображение слоя с пробками на картах;
-  `getScreenPoints(point: Point[], callback: (screenPoints: ScreenPoint[]) => void)` - получить кооординаты на экране (x и y) по координатам маркеров;
-  `getWorldPoints(screenPoint: ScreenPoint[], callback: (worldPoints: Point[]) => void)` - получить координаты точек (lat и lon) по координатам на экране.

**ВАЖНО**

- Компонент карт стилизуется, как и `View` из React Native. Если карта не отображается, после инициализации с валидным ключем API, вероятно необходимо прописать стиль, который опишет размеры компонента (`height + width` или `flex`);
- При использовании изображений из JS (через `require('./img.png')`) в дебаге и релизе на Android могут быть разные размеры маркера. Рекомендуется проверять рендер в релизной сборке.

## Отображение примитивов

### Marker

```jsx
import { Marker } from 'react-native-yamap';

<YaMap>
  <Marker point={{ lat: 50, lon: 50 }}/>
</YaMap>
```

#### Доступные `props` для примитива **Marker**:

| Название | Тип | Описание |
|--|--|--|
| point | Point | Координаты точки для отображения маркера |
| scale | number | Масштабирование иконки маркера. Не работает если использовать children у маркера |
| source | ImageSource | Данные для изображения маркера |
| children | ReactElement | Рендер маркера как компонента |
| onPress | function | Действие при нажатии/клике |
| anchor | {  x:  number,  y:  number  } | Якорь иконки маркера. Координаты принимают значения от 0 до 1 |
| zIndex | number | Отображение элемента по оси Z |
| visible | boolean | Отображение маркера на карте |

#### Доступные методы для примитива **Marker**:

-  `animatedMoveTo(point: Point, duration: number)` - плавное изменение позиции маркера;
-  `animatedRotateTo(angle: number, duration: number)` - плавное вращение маркера.

### Circle

```jsx
import { Circle } from 'react-native-yamap';

<YaMap>
  <Circle center={{ lat: 50, lon: 50 }} radius={300} />
</YaMap>
```

#### Доступные `props` для примитива **Circle**:

| Название | Тип | Описание |
|--|--|--|
| center | Point | Координаты центра круга |
| radius | number | Радиус круга в метрах |
| fillColor | string | Цвет заливки |
| strokeColor | string | Цвет границы |
| strokeWidth | number | Толщина границы |
| onPress | function | Действие при нажатии/клике |
| zIndex | number | Отображение элемента по оси Z |

### Polyline

```jsx
import { Polyline } from 'react-native-yamap';

<YaMap>
  <Polyline
    points={[
      { lat: 50, lon: 50 },
      { lat: 50, lon: 20 },
      { lat: 20, lon: 20 },
    ]}
  />
</YaMap>
```

#### Доступные `props` для примитива **Polyline**:

| Название | Тип | Описание |
|--|--|--|
| points | Point[] | Массив точек линии |
| strokeColor | string | Цвет линии |
| strokeWidth | number | Толщина линии |
| outlineColor | string | Цвет обводки |
| outlineWidth | number | Толщина обводки |
| dashLength | number | Длина штриха |
| dashOffset | number | Отступ первого штриха от начала полилинии |
| gapLength | number | Длина разрыва между штрихами |
| onPress | function | Действие при нажатии/клике |
| zIndex | number | Отображение элемента по оси Z |

### Polygon

```jsx
import { Polygon } from 'react-native-yamap';

<YaMap>
  <Polygon
    points={[
      { lat: 50, lon: 50 },
      { lat: 50, lon: 20 },
      { lat: 20, lon: 20 },
    ]}
  />
</YaMap>
```

#### Доступные `props` для примитива **Polygon**:

| Название | Тип | Описание |
|--|--|--|
| points | Point[] | Массив точек линии |
| fillColor | string | Цвет заливки |
| strokeColor | string | Цвет границы |
| strokeWidth | number | Толщина границы |
| innerRings | (Point[])[] | Массив полилиний, которые образуют отверстия в полигоне |
| onPress | function | Действие при нажатии/клике |
| zIndex | number | Отображение элемента по оси Z |

## Запрос маршрутов

Маршруты можно запросить используя метод `findRoutes` компонента `YaMap` (через ref).

`findRoutes(points: Point[], vehicles: Vehicles[], callback: (event: RoutesFoundEvent) => void)` - запрос маршрутов через точки `points` с использованием транспорта `vehicles`. При получении маршрутов будет вызван `callback` с информацией обо всех маршрутах.

Доступны следующие роутеры из Yandex MapKit:

-  **masstransit** - для маршрутов на общественном транспорте;
-  **pedestrian** - для пешеходных маршрутов;
-  **driving** - для маршрутов на автомобиле.

Тип роутера зависит от переданного в функцию массива `vehicles`:

- Если передан пустой массив (`this.map.current.findRoutes(points, [], () => null);`), то будет использован `PedestrianRouter`;
- Если передан массив с одним элементом `'car'` (`this.map.current.findRoutes(points, ['car'], () => null);`), то будет использован `DrivingRouter`;
- Во всех остальных случаях используется `MasstransitRouter`.

Также можно использовать нужный роутер, вызвав соответствующую функцию

```typescript
findMasstransitRoutes(points: Point[], callback: (event: RoutesFoundEvent) => void): void;
findPedestrianRoutes(points: Point[], callback: (event: RoutesFoundEvent) => void): void;
findDrivingRoutes(points: Point[], callback: (event: RoutesFoundEvent) => void): void;
```

#### Замечание

В зависимости от типа роутера информация о маршутах может незначительно отличаться.

## Использование API геокодера

### Инициализация

```typescript
import { Geocoder } from 'react-native-yamap';

Geocoder.init('API_KEY');
```

`API_KEY` для API геокодера и для карт **отличаются**. Инициализировать надо оба класса и каждый со своим ключем.

### Прямое геокодирование

```typescript
Geocoder.geocode(geocode: Point, kind?: ObjectKind, results?: number, skip?: number, lang?: Lang);
```

[Документация по запросу к геокодеру](https://yandex.ru/dev/maps/geocoder/doc/desc/concepts/input_params.html)
[Документация по ответу геокодера]( https://yandex.ru/dev/maps/geocoder/doc/desc/reference/response_structure.html)

#### Упрощенный вызов ####

```typescript
Geocoder.geoToAddress(geo: Point);
```

Вернет `null` или объект адреса (строковое значение, почтовый индекс и массив компонентов адреса) первого из предложений геокодера.

```typescript
interface Address {
  country_code: string;
  formatted: string;
  postal_code: string;
  Components: {
    kind: string,
    name: string
  }[];
}
```


### Обратное геокодирование

```typescript
Geocoder.reverseGeocode(geocode: string, kind?: ObjectKind, results?: number,  skip?: number, lang?: Lang, rspn?: 0 | 1, ll?: Point, spn?: [number, number],  bbox?: [Point, Point]);
```

[Документация по запросу к геокодеру](https://yandex.ru/dev/maps/geocoder/doc/desc/concepts/input_params.html)
[Документация по ответу геокодера]( https://yandex.ru/dev/maps/geocoder/doc/desc/reference/response_structure.html)

#### Упрощенный вызов ####

```typescript
Geocoder.addressToGeo(address: string);
```

Вернет `null` или координаты `{ lat: number, lon: number }` первого объекта из предложений геокодера.

## Поиск по гео с подсказсками (GeoSuggestions)

Для поиска с геоподсказками нужно воспользоваться модулем Suggest:

```typescript

import { Suggest } from 'react-native-yamap';

const find = async (query: string, options?: SuggestOptions) => {
  const suggestions = await Suggest.suggest(query, options);

  // suggestion = [{
  //   subtitle: "Москва, Россия"
  //   title: "улица Льва Толстого, 16"
  //   uri: "ymapsbm1://geo?ll=37.587093%2C55.733974&spn=0.001000%2C0.001000&text=%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F%2C%20%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C%20%D1%83%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9B%D1%8C%D0%B2%D0%B0%20%D0%A2%D0%BE%D0%BB%D1%81%D1%82%D0%BE%D0%B3%D0%BE%2C%2016"
  // }, ...]

  const suggestionsWithCoards = await Suggest.suggestWithCoords(query, options);

  // suggestionsWithCoards = [{
  //   subtitle: "Москва, Россия"
  //   title: "улица Льва Толстого, 16"
  //   lat: 55.733974
  //   lon: 37.587093
  //   uri: "ymapsbm1://geo?ll=37.587093%2C55.733974&spn=0.001000%2C0.001000&text=%D0%A0%D0%BE%D1%81%D1%81%D0%B8%D1%8F%2C%20%D0%9C%D0%BE%D1%81%D0%BA%D0%B2%D0%B0%2C%20%D1%83%D0%BB%D0%B8%D1%86%D0%B0%20%D0%9B%D1%8C%D0%B2%D0%B0%20%D0%A2%D0%BE%D0%BB%D1%81%D1%82%D0%BE%D0%B3%D0%BE%2C%2016"
  // }, ...]

  // After searh session is finished
  Suggest.reset();
}
```


### Использование компонента ClusteredYamap

```jsx
import React from 'react';
import { ClusteredYamap } from '../../react-native-yamap/src';

const Map = () => {
  return (
    <ClusteredYamap
      clusterColor="red"
      clusteredMarkers={[
        {
          point: {
            lat: 56.754215,
            lon: 38.622504,
          },
          data: {},
        },
        {
          point: {
            lat: 56.754215,
            lon: 38.222504,
          },
          data: {},
        },
      ]}
      renderMarker={(info, index) => (
        <Marker
          key={index}
          point={info.point}
        />
      )}
      style={{flex: 1}}
    />
  );
};
```

# Использование с Expo
Для подключения нативного модуля в приложение с expo используйте expo prebuild.
Он выполнит eject и сгенерирует привычные папки android и ios с нативным кодом. Это позволит использовать любую библиотеку так же, как и приложение с react native cli.

Для корректной работы на iOS react-native-yamap требует обновить AppDelegate.mm и инициализировать YMKMapKit при запуске приложения. prebuild не гарантирует сохранности папок android и ios, их нет смысла включать в Git. Чтобы напрямую менять нативный код есть config plugins.

Обновите app.json на app.config.ts и используйте этот пример модификации AppDelegate:
```jsx
import { type ExpoConfig } from "@expo/config-types";
import { withAppDelegate, type ConfigPlugin } from "expo/config-plugins";

const config: ExpoConfig = {
  name: "Example",
  slug: "example-app",
  version: "1.0.0",
  extra: {
    mapKitApiKey: "bla-bla-bla",
  },
};

const withYandexMaps: ConfigPlugin = (config) => {
  return withAppDelegate(config, async (config) => {
    const appDelegate = config.modResults;

    // Add import
    if (!appDelegate.contents.includes("#import <YandexMapsMobile/YMKMapKitFactory.h>")) {
      // Replace the first line with the intercom import
      appDelegate.contents = appDelegate.contents.replace(
        /#import "AppDelegate.h"/g,
        `#import "AppDelegate.h"\n#import <YandexMapsMobile/YMKMapKitFactory.h>`
      );
    }

    const mapKitMethodInvocations = [
      `[YMKMapKit setApiKey:@"${config.extra?.mapKitApiKey}"];`,
      `[YMKMapKit setLocale:@"ru_RU"];`,
      `[YMKMapKit mapKit];`,
    ]
      .map((line) => `\t${line}`)
      .join("\n");

    // Add invocation
    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
    if (!appDelegate.contents.includes(mapKitMethodInvocations)) {
      appDelegate.contents = appDelegate.contents.replace(
        /\s+return YES;/g,
        `\n\n${mapKitMethodInvocations}\n\n\treturn YES;`
      );
    }

    return config;
  });
};

export default withYandexMaps(config);
```
