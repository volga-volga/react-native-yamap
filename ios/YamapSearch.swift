import YandexMapsMobile
import UIKit

@objc(YamapSearch)
class YamapSearch: NSObject {
    enum ArrayError: Error {
        case indexOutOfBounds
    }

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

    private func getGeometry(figure: [String: Any]?) throws -> YMKGeometry {
        if (figure == nil) {
            return YMKGeometry(boundingBox: self.defaultBoundingBox)
        }
        if (figure!["type"] as! String=="POINT") {
            return YMKGeometry.init(point: YMKPoint(latitude: (figure!["value"] as! [String: Any])["lat"] as! Double, longitude: (figure!["value"] as! [String: Any])["lon"] as! Double))
        }
        if (figure!["type"] as! String=="BOUNDINGBOX") {
            var southWest = YMKPoint(latitude: ((figure!["value"] as! [String: Any])["southWest"] as! [String: Any])["lat"] as! Double, longitude: ((figure!["value"] as! [String: Any])["southWest"] as! [String: Any])["lon"] as! Double)
            var northEast = YMKPoint(latitude: ((figure!["value"] as! [String: Any])["northEast"] as! [String: Any])["lat"] as! Double, longitude: ((figure!["value"] as! [String: Any])["northEast"] as! [String: Any])["lon"] as! Double)
            return YMKGeometry.init(boundingBox: YMKBoundingBox(southWest: southWest, northEast: northEast))
        }
        if (figure!["type"] as! String=="POLYLINE") {
            let points = (figure!["value"] as! [String: Any])["points"] as! [[String: Any]];
            var convertedPoints = [YMKPoint]()
            points.forEach{point in
                convertedPoints.append(YMKPoint(latitude: point["lat"] as! Double, longitude: point["lon"] as! Double))
            }
            return YMKGeometry.init(polyline: YMKPolyline(points:convertedPoints))
        }
        if (figure!["type"] as! String=="POLYGON") {
            let linearRingPoints = (figure!["value"] as! [String: Any])["points"] as! [[String: Any]];
            if (linearRingPoints.count != 4) {
                throw ArrayError.indexOutOfBounds
            }
            var convertedlinearRingPoints = [YMKPoint]()
            linearRingPoints.forEach{point in
                convertedlinearRingPoints.append(YMKPoint(latitude: point["lat"] as! Double, longitude: point["lon"] as! Double))
            }
            return YMKGeometry.init(polygon: YMKPolygon(outerRing: YMKLinearRing(points: convertedlinearRingPoints), innerRings: []))
        }
        return YMKGeometry(boundingBox: self.defaultBoundingBox)
    }

    private func convertSearchResponce(search: YMKSearchResponse?) -> [String: Any] {
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

        return searchToPass;
    }

    @objc func searchByAddress(_ searchQuery: String, figure: [String: Any]?, options: [String: Any]?, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.initSearchManager()
        do {
            self.setSearchOptions(options: options)
            let geometryFigure: YMKGeometry = try self.getGeometry(figure: figure)
            runOnMainQueueWithoutDeadlocking {
                self.searchSession = self.searchManager?.submit(withText: searchQuery, geometry: geometryFigure, searchOptions: self.searchOptions, responseHandler: { search, error in
                    if let error = error {
                        rejecter(self.ERR_SEARCH_FAILED, "search request: \(searchQuery)", error)
                        return
                    }

                    resolver(self.convertSearchResponce(search: search))
                })
            }
        } catch {
            rejecter(ERR_NO_REQUEST_ARG, "search request: \(searchQuery)", nil)
        }
    }

    @objc func addressToGeo(_ searchQuery: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        self.initSearchManager()
        do {
            self.setSearchOptions(options: nil)
            runOnMainQueueWithoutDeadlocking {
                self.searchSession = self.searchManager?.submit(withText: searchQuery, geometry: YMKGeometry(boundingBox: self.defaultBoundingBox), searchOptions: self.searchOptions, responseHandler: { search, error in
                    if let error = error {
                        rejecter(self.ERR_SEARCH_FAILED, "search request: \(searchQuery)", error)
                        return
                    }

                    let geoObjects = search?.collection.children.compactMap { $0.obj }

                    let point = (
                        geoObjects?.first?.metadataContainer
                            .getItemOf(YMKSearchToponymObjectMetadata.self) as? YMKSearchToponymObjectMetadata
                    )?.balloonPoint
                    let searchPoint = ["lat": point?.latitude, "lon": point?.longitude];

                    resolver(searchPoint)

                })
            }
        } catch {
            rejecter(ERR_NO_REQUEST_ARG, "search request: \(searchQuery)", nil)
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

                resolver(self.convertSearchResponce(search: search))
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
