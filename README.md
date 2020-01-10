## Установка

```
yarn add https://github.com/volga-volga/react-native-yamap.git
```
Для react-native версии меньше 60
```
react-native link react-native-yamap
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
  userLocationIcon={IMAGE}
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

- Для отображения маркера поверх других, у объекта marker используется параметр zIndex.

- Для обработки нажатия на маркер, используется onMarkerPress. В метод приходит id выбранного маркера. Если для маркеров не передается id, то вместо него будет использоваться индекс в массиве маркеров. Не рекомендуется передавать id только для части маркеров - необходимо либо передавать во все маркеры, либо не передавать нигде.

- Для центрированиия карты можно использовать методы по ref: fitAllMarkers() и setCenter(center), где center { lat, lon, zoom }. fitAllMarkers подбирает положение камеры, чтобы вместить все маркеры (если возможно)


- Для кастомизации изображения маркера, маркеру можно передать параметр source, как и у Image из react-native.

### Замечание
При использовании изображений из js (через require('./img.png')) в дебаге и релизе на андроиде могут быть разные размеры маркера. В текущей версии рекомендуется проверять рендер в релизной сборке. Будет исправлено в следующих релизах
