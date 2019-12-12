## Установка

```
yarn add https://github.com/volga-volga/react-native-yamap.git
```
Для react-native версии меньше 60
```
react-native link react-native-yamap
``` 

## Добавление ресурсов (необязательно)

- **ios** - добавить в `Images.xcassets` изображения: иконка позиции пользователя, стрелка иконки позиции (для компаса), обычный маркер, выделенный маркер
```
// AppDelegate.m
#import <YandexMapKit/YMKMapKitFactory.h>
...
yamap *map = [[yamap alloc] init];
[yamap setPinIcon:@"ICON_ASSET_NAME"];
[yamap setArrowIcon:@"ICON_ASSET_NAME"];
[yamap setMarkerIcon:@"ICON_ASSET_NAME"];
[yamap setSelectedMarkerIcon:@"ICON_ASSET_NAME"];
```

- **android** - для использования необходимо отключить автолинкинг (для react-native 60+) 
  при создании `RNYamapPackage` необходимо передать экземпляры `ImageProvider` для всех изображений (иконка позиции пользователя, стрелка иконки позиции (для компаса), обычный маркер, выделенный маркер). Ниже пример кода, когда изображения помещены в `android/app/src/main/res/mipmap` с именами `selected`, `normal`, `base_location`, `location_arrow`:
```
    import com.yandex.runtime.image.ImageProvider;
    ...
    ImageProvider userLocationPinIcon = ImageProvider.fromResource(getApplication(), R.mipmap.location_pin);
    ImageProvider userLocationArrowIcon = ImageProvider.fromResource(getApplication(), R.mipmap.location_arrow);
    ImageProvider selectedMarkerIcon = ImageProvider.fromResource(getApplicationContext(), R.mipmap.selected);
    ImageProvider markerIcon = ImageProvider.fromResource(getApplicationContext(), R.mipmap.normal);
    ...
    new RNYamapPackage(userLocationPinIcon, userLocationArrowIcon, selectedMarkerIcon, markerIcon)
```

## Использование

### Инициализировать карты

```
// js
import YaMap from 'react-native-yamap';

YaMap.init('API_KEY');
```

### Использование компонента
```typescript
import YaMap from 'react-native-yamap';

// ...

handleOnRouteFound(event) {
  this.setState({ routes: event.nativeEvent.routes });
}

handleOnMarkerPress(id: number) {
  console.log(`Marker with id ${id} pressed!`);
}
// ...

<YaMap
  vehicles={["bus", "walk"]} // bus, railway, trolleybus, tramway, suburban, underground, walk
  onRouteFound={this.handleOnRouteFound}
  routeColors={{bus: '#fff', walk: '#f00'}}
  center={{lat: double, lon: double, zoom: double}}
  markers={markers}
  route={Route}
  onMarkerPress={this.handleOnMarkerPress}
  style={styles.container}/>
```

```typescript
export interface Marker {
  id: number
  lon: number
  lat: number
  selected: boolean
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
