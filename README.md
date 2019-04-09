## Установка

```
yarn add https://github.com/volga-volga/react-native-yamap.git
react-native link react-native-yamap
``` 

## Добавление ресурсов

- **ios** - добавить в `Images.xcassets` изображения с именем `selected`, `normal`, `base_location`
- **android** - при создании `RNYamapPackage` необходимо передать экземпляры `ImageProvider` для всех изображений. Ниже пример кода, когда изображения помещены в `android/app/src/main/res/mipmap` с именами `selected`, `normal`, `base_location`:
```
    import com.yandex.runtime.image.ImageProvider;
    ...
    ImageProvider location = ImageProvider.fromResource(getApplication(), R.mipmap.base_location);
    ImageProvider selected = ImageProvider.fromResource(getApplicationContext(), R.mipmap.selected);
    ImageProvider marker = ImageProvider.fromResource(getApplicationContext(), R.mipmap.normal);
    ...
    new RNYamapPackage(location, selected, marker),
```

## Использование

### Инициализировать карты

- андроид (для ios используется заглушка, поэтому можно использовать без Platform)
```
// js
import YaMap from 'react-native-yamap';

YaMap.init('API_KEY');
```
- ios
```
// AppDelegate.m
#import <YandexMapKit/YMKMapKitFactory.h>
...
[YMKMapKit setApiKey: @"API_LEY"];
```

### Использование компонента
```jsx harmony
<YaMap
  center={{ lat: selectedShop.lat, lon: selectedShop.lon }}
  markers={markers}
  onMarkerPress={this.handleMarkerPress}
  style={styles.container}
/>
```

Тип маркер:
```typescript
export interface Marker {
  id: number,
  lon: number,
  lat: number,
  selected: boolean,
}
```
