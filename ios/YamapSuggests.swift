import YandexMapsMobile

@objc(YamapSuggests)
class YamapSuggests: NSObject {
    var searchManager: YMKSearchManager?
    var suggestClient: YMKSearchSuggestSession?
    let defaultBoundingBox: YMKBoundingBox
    let suggestOptions: YMKSuggestOptions

    override init() {
        let southWestPoint = YMKPoint(latitude: -90.0, longitude: -180.0)
        let northEastPoint = YMKPoint(latitude: 90.0, longitude: 180.0)
        self.defaultBoundingBox = YMKBoundingBox(southWest: southWestPoint, northEast: northEastPoint)
        self.suggestOptions = YMKSuggestOptions(suggestTypes: [], userPosition: nil, suggestWords: false, strictBounds: false)
        super.init()
    }

    @objc static func requiresMainQueueSetup() -> Bool {
        return true
    }

    func runOnMainQueueWithoutDeadlocking(_ block: @escaping () -> Void) {
        if Thread.isMainThread {
            block()
        } else {
            DispatchQueue.main.sync(execute: block)
        }
    }

    func runAsyncOnMainQueueWithoutDeadlocking(_ block: @escaping () -> Void) {
        if Thread.isMainThread {
            block()
        } else {
            DispatchQueue.main.async(execute: block)
        }
    }

    let ERR_NO_REQUEST_ARG = "YANDEX_SUGGEST_ERR_NO_REQUEST_ARG"
    let ERR_SUGGEST_FAILED = "YANDEX_SUGGEST_ERR_SUGGEST_FAILED"
    let YandexSuggestErrorDomain = "YandexSuggestErrorDomain"

    func getSuggestClient() -> YMKSearchSuggestSession {
        if let client = suggestClient {
            return client
        }

        if searchManager == nil {
            runOnMainQueueWithoutDeadlocking {
                self.searchManager = YMKSearchFactory.instance().createSearchManager(with: .online)
            }
        }

        runOnMainQueueWithoutDeadlocking {
            self.suggestClient = self.searchManager?.createSuggestSession()
        }

        return suggestClient!
    }

    @objc func suggestHandler(_ searchQuery: String, options: YMKSuggestOptions, boundingBox: YMKBoundingBox, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        do {
            let session = getSuggestClient()

            runAsyncOnMainQueueWithoutDeadlocking {
                session.suggest(withText: searchQuery, window: boundingBox, suggestOptions: options) { suggest, error in
                    if let error = error {
                        rejecter(self.ERR_SUGGEST_FAILED, "search request: \(searchQuery)", error)
                        return
                    }

                    var suggestsToPass = [[String: Any]]()

                    suggest?.items.forEach { suggestItem in
                        var suggestToPass = [String: Any]()
                        suggestToPass["title"] = suggestItem.title.text
                        suggestToPass["subtitle"] = suggestItem.subtitle?.text
                        suggestToPass["uri"] = suggestItem.uri
                        suggestsToPass.append(suggestToPass)
                    }

                    resolver(suggestsToPass)
                }
            }
        } catch {
            rejecter(ERR_NO_REQUEST_ARG, "search request: \(searchQuery)", nil)
        }
    }

    func makeError(withText descriptionText: String) -> NSError {
        let errorDictionary = [NSLocalizedDescriptionKey: descriptionText]
        return NSError(domain: YandexSuggestErrorDomain, code: 0, userInfo: errorDictionary)
    }

    func mapPoint(fromDictionary: [String: Any], withKey pointKey: String) throws -> YMKPoint? {
        guard let pointDictionary = fromDictionary[pointKey] as? [String: Any],
              let lat = pointDictionary["lat"] as? NSNumber,
              let lon = pointDictionary["lon"] as? NSNumber else {
            throw makeError(withText: "search request: \(pointKey) is not an Object")
        }

        return YMKPoint(latitude: lat.doubleValue, longitude: lon.doubleValue)
    }

    @objc func suggest(_ searchQuery: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        suggestHandler(searchQuery, options: self.suggestOptions, boundingBox: self.defaultBoundingBox, resolver: resolver, rejecter: rejecter)
    }

    @objc func suggestWithOptions(_ searchQuery: String, options: [String: Any], resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        var suggestType: YMKSuggestType = .geo
        var boundingBox = self.defaultBoundingBox
        let opt = YMKSuggestOptions()

        if let suggestWords = options["suggestWords"] as? Bool {
            opt.suggestWords = suggestWords
        } else if options["suggestWords"] != nil {
            rejecter(ERR_NO_REQUEST_ARG, "search request: suggestWords must be a Boolean", nil)
            return
        }

        if let suggestTypes = options["suggestTypes"] as? [NSNumber] {
            suggestType = []
            for value in suggestTypes {
                suggestType.insert(YMKSuggestType(rawValue: value.uintValue))
            }
        } else if options["suggestTypes"] != nil {
            rejecter(ERR_NO_REQUEST_ARG, "search request: suggestTypes is not an Array", nil)
            return
        }

        opt.suggestTypes = suggestType

        if let userPosition = options["userPosition"] as? [String: Any] {
            do {
                if let userPoint = try mapPoint(fromDictionary: userPosition, withKey: "userPosition") {
                    opt.userPosition = userPoint
                }
            } catch {
                rejecter(ERR_NO_REQUEST_ARG, error.localizedDescription, nil)
                return
            }
        }

        if let boxDictionary = options["boundingBox"] as? [String: Any] {
            do {
                if let southWest = try mapPoint(fromDictionary: boxDictionary, withKey: "southWest"),
                   let northEast = try mapPoint(fromDictionary: boxDictionary, withKey: "northEast") {
                    boundingBox = YMKBoundingBox(southWest: southWest, northEast: northEast)
                }
            } catch {
                rejecter(ERR_NO_REQUEST_ARG, error.localizedDescription, nil)
                return
            }
        } else if options["boundingBox"] != nil {
            rejecter(ERR_NO_REQUEST_ARG, "search request: boundingBox is not an Object", nil)
            return
        }

        suggestHandler(searchQuery, options: opt, boundingBox: boundingBox, resolver: resolver, rejecter: rejecter)
    }

    @objc func reset(_ resolve: @escaping RCTPromiseResolveBlock, rejecter reject: @escaping RCTPromiseRejectBlock) {
        do {
            if let client = suggestClient {
                DispatchQueue.main.async {
                    client.reset()
                }
            }
            resolve([])
        } catch {
            reject("ERROR", "Error during reset suggestions", nil)
        }
    }

    @objc static func moduleName() -> String {
        return "YamapSuggests"
    }
}
