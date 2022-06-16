#import <React/RCTComponent.h>
#import <React/UIView+React.h>

#import <MapKit/MapKit.h>
@import YandexMapsMobile;

#ifndef MAX
#import <NSObjCRuntime.h>
#endif

#import "RNCYMView.h"
#import <YamapMarkerView.h>

#define ANDROID_COLOR(c) [UIColor colorWithRed:((c>>16)&0xFF)/255.0 green:((c>>8)&0xFF)/255.0 blue:((c)&0xFF)/255.0  alpha:((c>>24)&0xFF)/255.0]

#define UIColorFromRGB(rgbValue) [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 green:((float)((rgbValue & 0xFF00) >> 8))/255.0 blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0]

@implementation RNCYMView {
    YMKMasstransitSession *masstransitSession;
    YMKMasstransitSession *walkSession;
    YMKMasstransitRouter *masstransitRouter;
    YMKDrivingRouter* drivingRouter;
    YMKDrivingSession* drivingSession;
    YMKPedestrianRouter *pedestrianRouter;
    YMKMasstransitOptions *masstransitOptions;
    YMKMasstransitSessionRouteHandler routeHandler;
    NSMutableArray<UIView*>* _reactSubviews;
    NSMutableArray *routes;
    NSMutableArray *currentRouteInfo;
    NSMutableArray<YMKRequestPoint *>* lastKnownRoutePoints;
    YMKUserLocationView* userLocationView;
    NSMutableDictionary *vehicleColors;
    UIImage* userLocationImage;
    NSArray *acceptVehicleTypes;
    YMKUserLocationLayer *userLayer;
    UIColor* userLocationAccuracyFillColor;
    UIColor* userLocationAccuracyStrokeColor;
    float userLocationAccuracyStrokeWidth;
    YMKClusterizedPlacemarkCollection *clusterCollection;
    UIColor* clusterColor;
    NSMutableArray<YMKPlacemarkMapObject *>* placemarks;
    BOOL userClusters;
}

- (instancetype)init {
    self = [super init];
    _reactSubviews = [[NSMutableArray alloc] init];
    masstransitRouter = [[YMKTransport sharedInstance] createMasstransitRouter];
    drivingRouter = [[YMKDirections sharedInstance] createDrivingRouter];
    pedestrianRouter = [[YMKTransport sharedInstance] createPedestrianRouter];
    masstransitOptions = [YMKMasstransitOptions masstransitOptionsWithAvoidTypes:[[NSArray alloc] init] acceptTypes:[[NSArray alloc] init] timeOptions:[[YMKTimeOptions alloc] init]];
    acceptVehicleTypes = [[NSMutableArray<NSString *> alloc] init];
    routes = [[NSMutableArray alloc] init];
    currentRouteInfo = [[NSMutableArray alloc] init];
    lastKnownRoutePoints = [[NSMutableArray alloc] init];
    vehicleColors = [[NSMutableDictionary alloc] init];
    placemarks = [[NSMutableArray alloc] init];
    [vehicleColors setObject:@"#59ACFF" forKey:@"bus"];
    [vehicleColors setObject:@"#7D60BD" forKey:@"minibus"];
    [vehicleColors setObject:@"#F8634F" forKey:@"railway"];
    [vehicleColors setObject:@"#C86DD7" forKey:@"tramway"];
    [vehicleColors setObject:@"#3023AE" forKey:@"suburban"];
    [vehicleColors setObject:@"#BDCCDC" forKey:@"underground"];
    [vehicleColors setObject:@"#55CfDC" forKey:@"trolleybus"];
    [vehicleColors setObject:@"#2d9da8" forKey:@"walk"];
    userLocationAccuracyFillColor = nil;
    userLocationAccuracyStrokeColor = nil;
    clusterColor=nil;
    userClusters=NO;
    userLocationAccuracyStrokeWidth = 0.f;
    [self.mapWindow.map addCameraListenerWithCameraListener:self];
    [self.mapWindow.map addInputListenerWithInputListener:(id<YMKMapInputListener>) self];
    clusterCollection = [self.mapWindow.map.mapObjects addClusterizedPlacemarkCollectionWithClusterListener:self];
    return self;
}

- (void)setClusteredMarkers:(NSArray*) markers {
    [placemarks removeAllObjects];
    [clusterCollection clear];
    NSMutableArray<YMKPoint*> *newMarkers = [NSMutableArray new];
    for (NSDictionary *mark in markers) {
        [newMarkers addObject:[YMKPoint pointWithLatitude:[[mark objectForKey:@"lat"] doubleValue] longitude:[[mark objectForKey:@"lon"] doubleValue]]];
    }
    NSArray<YMKPlacemarkMapObject *>* newPlacemarks = [clusterCollection addPlacemarksWithPoints:newMarkers image:[self clusterImage:[NSNumber numberWithFloat:[newMarkers count]]] style:[YMKIconStyle new]];
    [placemarks addObjectsFromArray:newPlacemarks];
    for (int i=0; i<[placemarks count]; i++) {
        if (i<[_reactSubviews count]) {
            UIView *subview = [_reactSubviews objectAtIndex:i];
            if ([subview isKindOfClass:[YamapMarkerView class]]) {
                YamapMarkerView* marker = (YamapMarkerView*) subview;
                [marker setMapObject:[placemarks objectAtIndex:i]];
            }
        }
    }
    [clusterCollection clusterPlacemarksWithClusterRadius:50 minZoom:12];
}

- (void)fitMarkers:(NSArray<YMKPlacemarkMapObject*>*) _points {
    NSMutableArray<YMKPoint*>* lastKnownMarkers = [[NSMutableArray alloc] init];
    for (int i = 0; i < [_points count]; ++i) {
            [lastKnownMarkers addObject:[[_points objectAtIndex:i] geometry]];
    }
    if ([lastKnownMarkers count] == 0) {
        return;
    }
    if ([lastKnownMarkers count] == 1) {
        YMKPoint *center = [lastKnownMarkers objectAtIndex:0];
        [self.mapWindow.map moveWithCameraPosition:[YMKCameraPosition cameraPositionWithTarget:center zoom:15 azimuth:0 tilt:0]];
        return;
    }
    double minLon = [lastKnownMarkers[0] longitude], maxLon = [lastKnownMarkers[0] longitude];
    double minLat = [lastKnownMarkers[0] latitude], maxLat = [lastKnownMarkers[0] latitude];
    for (int i = 0; i < [lastKnownMarkers count]; i++) {
        if ([lastKnownMarkers[i] longitude] > maxLon) maxLon = [lastKnownMarkers[i] longitude];
        if ([lastKnownMarkers[i] longitude] < minLon) minLon = [lastKnownMarkers[i] longitude];
        if ([lastKnownMarkers[i] latitude] > maxLat) maxLat = [lastKnownMarkers[i] latitude];
        if ([lastKnownMarkers[i] latitude] < minLat) minLat = [lastKnownMarkers[i] latitude];
    }
    YMKPoint *southWest = [YMKPoint pointWithLatitude:minLat longitude:minLon];
    YMKPoint *northEast = [YMKPoint pointWithLatitude:maxLat longitude:maxLon];
    YMKPoint *rectCenter = [YMKPoint pointWithLatitude:(minLat + maxLat) / 2 longitude:(minLon + maxLon) / 2];
    CLLocation *centerP = [[CLLocation alloc] initWithLatitude:northEast.latitude longitude:northEast.longitude];
    CLLocation *edgeP = [[CLLocation alloc] initWithLatitude:rectCenter.latitude longitude:rectCenter.longitude];
    CLLocationDistance distance = [centerP distanceFromLocation:edgeP];
    double scale = (distance/2)/140;
    int zoom = (int) (16 - log(scale) / log(2));
    YMKBoundingBox *boundingBox = [YMKBoundingBox boundingBoxWithSouthWest:southWest northEast:northEast];
    YMKCameraPosition *cameraPosition = [self.mapWindow.map cameraPositionWithBoundingBox:boundingBox];
    cameraPosition = [YMKCameraPosition cameraPositionWithTarget:cameraPosition.target zoom:zoom azimuth:cameraPosition.azimuth tilt:cameraPosition.tilt];
    [self.mapWindow.map moveWithCameraPosition:cameraPosition animationType:[YMKAnimation animationWithType:YMKAnimationTypeSmooth duration:1.0] cameraCallback:^(BOOL completed){}];
}

- (void)setClusterColor: (UIColor*) color {
    clusterColor = color;
}

- (void)onObjectRemovedWithView:(nonnull YMKUserLocationView *) view {
}

- (void)onMapTapWithMap:(nonnull YMKMap *) map
                  point:(nonnull YMKPoint *) point {
    if (self.onMapPress) {
        NSDictionary* data = @{
            @"lat": [NSNumber numberWithDouble:point.latitude],
            @"lon": [NSNumber numberWithDouble:point.longitude],
        };
        self.onMapPress(data);
    }
}

- (void)onMapLongTapWithMap:(nonnull YMKMap *) map
                      point:(nonnull YMKPoint *) point {
    if (self.onMapLongPress) {
        NSDictionary* data = @{
            @"lat": [NSNumber numberWithDouble:point.latitude],
            @"lon": [NSNumber numberWithDouble:point.longitude],
        };
        self.onMapLongPress(data);
    }
}

// utils
+ (UIColor*)colorFromHexString:(NSString*) hexString {
    unsigned rgbValue = 0;
    NSScanner *scanner = [NSScanner scannerWithString:hexString];
    [scanner setScanLocation:1];
    [scanner scanHexInt:&rgbValue];
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16)/255.0 green:((rgbValue & 0xFF00) >> 8)/255.0 blue:(rgbValue & 0xFF)/255.0 alpha:1.0];
}

+ (NSString*)hexStringFromColor:(UIColor *) color {
    const CGFloat *components = CGColorGetComponents(color.CGColor);
    CGFloat r = components[0];
    CGFloat g = components[1];
    CGFloat b = components[2];
    return [NSString stringWithFormat:@"#%02lX%02lX%02lX", lroundf(r * 255), lroundf(g * 255), lroundf(b * 255)];
}

// children
- (void)addSubview:(UIView *) view {
    [super addSubview:view];
}

- (void)insertReactSubview:(UIView<RCTComponent>*) subview atIndex:(NSInteger) atIndex {
     if ([subview isKindOfClass:[YamapMarkerView class]]) {
        YamapMarkerView* marker = (YamapMarkerView*) subview;
         if (atIndex<[placemarks count]) {
             [marker setMapObject:[placemarks objectAtIndex:atIndex]];
         }
    }
    [_reactSubviews insertObject:subview atIndex:atIndex];
    [super insertMarkerReactSubview:subview atIndex:atIndex];
}

- (void)removeReactSubview:(UIView<RCTComponent>*) subview {
     if ([subview isKindOfClass:[YamapMarkerView class]]) {
        YamapMarkerView* marker = (YamapMarkerView*) subview;
        [clusterCollection removeWithPlacemark:[marker getMapObject]];
    } else {
        NSArray<id<RCTComponent>> *childSubviews = [subview reactSubviews];
        for (int i = 0; i < childSubviews.count; i++) {
            [self removeReactSubview:(UIView *)childSubviews[i]];
        }
    }
    [_reactSubviews removeObject:subview];
    [super removeMarkerReactSubview:subview];
}

-(UIImage*)clusterImage:(NSNumber*) clusterSize {
    float FONT_SIZE = 45;
    float MARGIN_SIZE = 9;
    float STROKE_SIZE = 9;
    NSString *text = [clusterSize stringValue];
    UIFont *font = [UIFont systemFontOfSize:FONT_SIZE];
    CGSize size = [text sizeWithFont:font];
    float textRadius = sqrt(size.height * size.height + size.width * size.width) / 2;
    float internalRadius = textRadius + MARGIN_SIZE;
    float externalRadius = internalRadius + STROKE_SIZE;
    UIImage *someImageView = [UIImage alloc];
    // This function returns a newImage, based on image, that has been:
       // - scaled to fit in (CGRect) rect
       // - and cropped within a circle of radius: rectWidth/2

       //Create the bitmap graphics context
       UIGraphicsBeginImageContextWithOptions(CGSizeMake(externalRadius*2, externalRadius*2), NO, 1.0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetFillColorWithColor(context, [clusterColor CGColor]);
    CGContextFillEllipseInRect(context, CGRectMake(0, 0, externalRadius*2, externalRadius*2));
    CGContextSetFillColorWithColor(context, [UIColor.whiteColor CGColor]);
    CGContextFillEllipseInRect(context, CGRectMake(STROKE_SIZE, STROKE_SIZE, internalRadius*2, internalRadius*2));
    [text drawInRect:CGRectMake(externalRadius - size.width/2, externalRadius - size.height/2, externalRadius, externalRadius) withAttributes:@{NSFontAttributeName: font, NSForegroundColorAttributeName: UIColor.blackColor }];
       UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
       UIGraphicsEndImageContext();

       return newImage;
}

- (void)onClusterAddedWithCluster:(nonnull YMKCluster *)cluster {
    NSNumber *myNum = @([cluster size]);
    [[cluster appearance] setIconWithImage:[self clusterImage:myNum]];
    [cluster addClusterTapListenerWithClusterTapListener:self];
}

- (BOOL)onClusterTapWithCluster:(nonnull YMKCluster *)cluster {
    [self fitMarkers:[cluster placemarks]];
    return YES;
}


@synthesize reactTag;

@end
