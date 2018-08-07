#import "FlutterPluginWebview.h"
#import <flutter_plugin_webview/flutter_plugin_webview-Swift.h>

@implementation FlutterPluginWebview
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    [SwiftFlutterPluginWebview registerWithRegistrar:registrar];
}
@end
