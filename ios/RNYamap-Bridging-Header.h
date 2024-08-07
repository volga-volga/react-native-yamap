//
//  newYamap-Bridging-Header.h
//  newYamap
//
//  Created by Tim on 06.08.24.
//

#import "React/RCTBridgeModule.h"
#import "React/RCTEventEmitter.h"
#import <React/RCTViewManager.h>
#import <React/RCTComponent.h>
#import <YamapMarkerView.h>
#import <YamapPolygonView.h>
#import <YamapPolylineView.h>
#import <YamapCircleView.h>
#import <React/RCTConvert.h>
#import <UIKit/UIKit.h>

@interface RCTConvert (UIView)

+ (UIView *)UIView:(id)json;

@end
