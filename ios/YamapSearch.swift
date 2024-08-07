import YandexMapsMobile
import UIKit

@objc(YamapSearch)
class YamapSearch: NSObject {
    var searchManager: YMKSearchManager?
    let defaultBoundingBox: YMKBoundingBox
    var searchSession: YMKSearchSession?
    var searchOptions: YMKSearchOptions
    
    let ERR_NO_REQUEST_ARG = "YANDEX_SEARCH_ERR_NO_REQUEST_ARG"
    let ERR_SEARCH_FAILED = "YANDEX_SEARCH_ERR_SEARCH_FAILED"
    let YandexSuggestErrorDomain = "YandexSuggestErrorDomain"

    override init() {
        let southWestPoint = YMKPoint(latitude: -90.0, longitude: -180.0)
        let northEastPoint = YMKPoint(latitude: -85.0, longitude: -175.0)
        self.defaultBoundingBox = YMKBoundingBox(southWest: southWestPoint, northEast: northEastPoint)
        self.searchOptions = YMKSearchOptions()
        super.init()
    }
    
    private func setSearchOptions(options: [String: Any]?) -> Void {
        self.searchOptions = YMKSearchOptions();
        if ((options?.keys) != nil) {
            for (key, value) in options! {
                self.searchOptions.setValue(value, forKey: key)
            }
        }
    }
    
    func runOnMainQueueWithoutDeadlocking(_ block: @escaping () -> Void) {
        if Thread.isMainThread {
            block()
        } else {
            DispatchQueue.main.sync(execute: block)
        }
    }
    
    func initSearchManager() -> Void {
        if searchManager == nil {
            runOnMainQueueWithoutDeadlocking {
                self.searchManager = YMKSearchFactory.instance().createSearchManager(with: .online)
            }
        }
    }
    
    private func getGeometry(figure: UIView?) -> YMKGeometry {
        if ((figure?.isKind(of: YamapMarkerView.self)) != nil) {
            let marker = figure as? YamapMarkerView;
            return YMKGeometry.init(point: marker!.getPoint())
        }
        if ((figure?.isKind(of: YamapCircleView.self)) != nil) {
            let circle = figure as? YamapCircleView;
            return YMKGeometry.init(circle: circle!.getCircle())
        }
        if ((figure?.isKind(of: YamapPolygonView.self)) != nil) {
            let polygon = figure as? YamapPolygonView;
            return YMKGeometry.init(polygon: polygon!.getPolygon())
        }
        if ((figure?.isKind(of: YamapPolylineView.self)) != nil) {
            let polyline = figure as? YamapPolylineView;
            return YMKGeometry.init(polyline: polyline!.getPolyline())
        }
        return YMKGeometry(boundingBox: self.defaultBoundingBox)
    }
    
    private func convertSearchResponce(search: YMKSearchResponse?) -> [[String: Any]] {
        var searchesToPass = [[String: Any]]()
        let geoObjects = search?.collection.children.compactMap { $0.obj }
        
        geoObjects?.forEach { geoItem in
            var searchToPass = [String: Any]()
            searchToPass["title"] = geoItem.name
            searchToPass["address"] = (
                geoItem.metadataContainer
                    .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
            )?.address.formattedAddress
            let point = (
                geoItem.metadataContainer
                    .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
            )?.balloonPoint
            searchToPass["point"] = ["lat": point?.latitude, "lon": point?.longitude];
            searchToPass["uri"] = (
                geoItem.metadataContainer.getItemOf(YMKUriObjectMetadata.self) as? YMKUriObjectMetadata
            )?.uris.first?.value
            searchesToPass.append(searchToPass)
        }
        return searchesToPass;
    }
    
    @objc func searchByAddress(_ searchQuery: String, figure: UIView?, options: [String: Any]?, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.initSearchManager()
        self.setSearchOptions(options: options)
        runOnMainQueueWithoutDeadlocking {
            self.searchSession = self.searchManager?.submit(withText: searchQuery, geometry: self.getGeometry(figure: figure), searchOptions: self.searchOptions, responseHandler: { search, error in
                if let error = error {
                    rejecter(self.ERR_SEARCH_FAILED, "search request: \(searchQuery)", error)
                    return
                }
                
                resolver(self.convertSearchResponce(search: search))
            })
        }
    }
    
    @objc func addressToGeo(_ searchQuery: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.initSearchManager()
        self.setSearchOptions(options: nil)
        runOnMainQueueWithoutDeadlocking {
            self.searchSession = self.searchManager?.submit(withText: searchQuery, geometry: self.getGeometry(figure: nil), searchOptions: self.searchOptions, responseHandler: { search, error in
                if let error = error {
                    rejecter(self.ERR_SEARCH_FAILED, "search request: \(searchQuery)", error)
                    return
                }
                
                let geoObjects = search?.collection.children.compactMap { $0.obj }
                
                let point = (
                    geoObjects?.first?.metadataContainer
                        .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
                )?.balloonPoint
                var searchPoint = ["lat": point?.latitude, "lon": point?.longitude];
              
                resolver(searchPoint)
              
            })
        }
    }
    
    @objc func searchByPoint(_ point: [String: Any], zoom: NSNumber, options: [String: Any]?, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        let searchPoint = YMKPoint(latitude: point["lat"] as! Double, longitude: point["lat"] as! Double)
        self.initSearchManager()
        self.setSearchOptions(options: options)
        runOnMainQueueWithoutDeadlocking {
            self.searchSession = self.searchManager?.submit(with: searchPoint, zoom: zoom, searchOptions: self.searchOptions, responseHandler: { search, error in
                if let error = error {
                    rejecter(self.ERR_SEARCH_FAILED, "search request: \(point)", error)
                    return
                }
              
                resolver(self.convertSearchResponce(search: search))
            })
        }
    }
    
    @objc func geoToAddress(_ point: [String: Any], resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        let searchPoint = YMKPoint(latitude: point["lat"] as! Double, longitude: point["lat"] as! Double)
        self.initSearchManager()
        self.setSearchOptions(options: nil)
        runOnMainQueueWithoutDeadlocking {
            self.searchSession = self.searchManager?.submit(with: searchPoint, zoom: 10, searchOptions: self.searchOptions, responseHandler: { search, error in
                if let error = error {
                    rejecter(self.ERR_SEARCH_FAILED, "search request: \(point)", error)
                    return
                }
                
                var searchToPass = [String: Any]()
                let geoObjects = search?.collection.children.compactMap { $0.obj }
                
                searchToPass["formatted"] = (
                    geoObjects?.first?.metadataContainer
                        .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
                )?.address.formattedAddress
                
                searchToPass["country_code"] = (
                    geoObjects?.first?.metadataContainer
                        .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
                )?.address.countryCode
                
                var components = [[String: Any]]()
                
                geoObjects?.forEach { geoItem in
                    var component = [String: Any]()
                    component["name"] = geoItem.name
                    component["kind"] = (
                        geoItem.metadataContainer
                            .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
                    )?.address.components.last?.kinds.first?.stringValue
                    components.append(component)
                }

                searchToPass["Components"] = components
                searchToPass["uri"] = (
                    geoObjects?.first?.metadataContainer.getItemOf(YMKUriObjectMetadata.self) as? YMKUriObjectMetadata
                )?.uris.first?.value
                
                resolver(searchToPass)
            })
        }
    }
    
    @objc func searchByURI(_ searchUri: NSString, options: [String: Any]?, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.initSearchManager()
        self.setSearchOptions(options: options)
        runOnMainQueueWithoutDeadlocking {
            self.searchSession = self.searchManager?.searchByURI(withUri: searchUri as String, searchOptions: self.searchOptions, responseHandler: { search, error in
                if let error = error {
                    rejecter(self.ERR_SEARCH_FAILED, "search request: \(searchUri)", error)
                    return
                }
              
                resolver(self.convertSearchResponce(search: search))
            })
        }
    }
    
    @objc func resolveURI(_ searchUri: NSString, options: [String: Any]?, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.initSearchManager()
        self.setSearchOptions(options: options)
        runOnMainQueueWithoutDeadlocking {
            self.searchSession = self.searchManager?.resolveURI(withUri: searchUri as String, searchOptions: self.searchOptions, responseHandler: { search, error in
                if let error = error {
                    rejecter(self.ERR_SEARCH_FAILED, "search request: \(searchUri)", error)
                    return
                }
              
                resolver(self.convertSearchResponce(search: search))
            })
        }
    }

    @objc static func moduleName() -> String {
        return "YamapSearch"
    }
}
