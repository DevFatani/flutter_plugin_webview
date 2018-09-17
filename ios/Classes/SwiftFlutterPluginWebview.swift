import Flutter
import WebKit

public class SwiftFlutterPluginWebview: NSObject, FlutterPlugin, WKNavigationDelegate  {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_plugin_webview", binaryMessenger: registrar.messenger())
        let viewController = registrar.messenger() as! UIViewController
        let instance = SwiftFlutterPluginWebview(viewController, channel)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    private final var viewController: UIViewController
    private final var channel: FlutterMethodChannel
    private var swipeRefresh: UIRefreshControl?
    private var webView: WKWebView?
    
    init(_ viewController: UIViewController,_ channel: FlutterMethodChannel){
        self.viewController = viewController
        self.channel = channel
        
        super.init()
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) -> Void {
        switch call.method {
        case "launch": launch(call, result)
        case "reload": launch(call, result, false)
        case "openUrl": openUrl(call, result)
        case "back": back(result)
        case "hasBack": result(hasBack())
        case "forward": forward(result)
        case "hasForward": result(hasForward())
        case "refresh": refresh(result)
        case "close": close(result)
        case "clearCookies": clearCookies(result)
        case "clearCache": clearCache(result)
        case "eval": eval(call, result)
        case "resize": resize(call, result)
        case "stopLoading": stopLoading(result)
            
        default:
            result(FlutterMethodNotImplemented)
        }
    }
    
    func launch(_ call: FlutterMethodCall,_ result: @escaping FlutterResult,_ initIfClosed: Bool = true){
        let arguments: [String: Any?] = call.arguments as! [String: Any?]
        let url: URL = URL(string: arguments["url"] as! String)!
        let userAgent: String? = arguments["userAgent"] as? String
        let enableJavascript: Bool = arguments["enableJavaScript"] as! Bool
        let clearCache: Bool = arguments["clearCache"] as! Bool
        let clearCookies: Bool = arguments["clearCookies"] as! Bool
        let enableLocalStorage: Bool = arguments["enableLocalStorage"] as! Bool
        let headers: [String: String]? = arguments["headers"] as? [String: String]
        let enableScroll: Bool = arguments["enableScroll"] as! Bool
        let enableSwipeToRefresh: Bool = arguments["enableSwipeToRefresh"] as! Bool
        
        if initIfClosed || webView != nil {
            let preferences = WKPreferences()
            let configuration = WKWebViewConfiguration()
            
            preferences.javaScriptEnabled = enableJavascript
            if #available(iOS 9.0, *), enableLocalStorage {
                configuration.websiteDataStore = WKWebsiteDataStore.default()
            }
            
            configuration.preferences = preferences
            
            initWebView(
                buildRect(call),
                configuration,
                enableSwipeToRefresh
            )
            
            webView?.allowsBackForwardNavigationGestures = true
            webView?.scrollView.isScrollEnabled = enableScroll
            
            if #available(iOS 9.0, *),userAgent != nil {
                webView?.customUserAgent = userAgent
            }
            
            if clearCache {
                self.clearCache()
            }
            
            if clearCookies {
                self.clearCookies()
            }
            
            var request: URLRequest = URLRequest(url: url)
            
            request.allHTTPHeaderFields = headers
            
            webView?.load(request)
        }
    }
    
    private func initWebView(_ rect: CGRect,_ configuration: WKWebViewConfiguration,_ enableSwipeToRefresh: Bool){
        if webView == nil {
            webView = WKWebView(frame: rect,configuration: configuration)
            webView!.navigationDelegate = self
            if enableSwipeToRefresh {
                webView?.scrollView.bounces = true
                swipeRefresh = UIRefreshControl()
                swipeRefresh?.addTarget(self, action: #selector(swipeRefreshAction), for: .valueChanged)
                webView?.scrollView.addSubview(swipeRefresh!)
            }
            viewController.view?.addSubview(webView!)
        }
    }
    
    @objc private func swipeRefreshAction() {
        refresh()
    }
    
    private func buildRect(_ call: FlutterMethodCall) -> CGRect {
        let arguments: [String: Any?] = call.arguments as! [String: Any?]
        if let rect: [String: NSNumber] = arguments["rect"] as? [String: NSNumber] {
            return CGRect(
                x: CGFloat(rect["left"]!.doubleValue),
                y: CGFloat(rect["top"]!.doubleValue),
                width: CGFloat(rect["width"]!.doubleValue),
                height: CGFloat(rect["height"]!.doubleValue)
            )
        } else {
            return viewController.view.bounds
        }
    }
    
    private func openUrl(_ call: FlutterMethodCall,_ result: @escaping FlutterResult){
        let arguments: [String: Any?] = call.arguments as! [String: Any?]
        let url: URL = URL(string: arguments["url"] as! String)!
        let headers: [String: String]? = arguments["headers"] as? [String: String]
        
        var request = URLRequest(url: url)
        
        request.allHTTPHeaderFields = headers
        
        webView?.load(request)
        
        result(webView != nil)
    }
    
    private func hasBack()-> Bool {
        return webView?.canGoBack ?? false
    }
    
    private func back(_ result: @escaping FlutterResult) {
        let hasBack = self.hasBack()
        if hasBack {
            webView?.goBack()
        }
        
        result(hasBack)
    }
    
    private func hasForward()-> Bool {
        return webView?.canGoForward ?? false
    }
    
    private func forward(_ result: @escaping FlutterResult) {
        let hasForward = self.hasForward()
        if hasForward {
            webView?.goForward()
        }
        
        result(hasForward)
    }
    
    private func refresh(_ result: @escaping FlutterResult = {(value: Any?) -> Void in}){
        webView?.reload()
        
        result(webView != nil)
    }
    
    private func close(_ result: @escaping FlutterResult){
        if webView != nil {
            webView?.stopLoading()
            webView?.removeFromSuperview()
            webView?.navigationDelegate = nil
            swipeRefresh?.endRefreshing()
            swipeRefresh = nil
            webView = nil
            
            WebviewState.onStateChange(channel ,["event": "closed"])
        }
        
        result(true)
    }
    
    private func stopLoading(_ result: @escaping FlutterResult){
        webView?.stopLoading()
        
        result(webView != nil)
    }
    
    private func clearCache(_ result: @escaping FlutterResult = {(value: Any?) -> Void in}) {
        if #available(iOS 9.0, *) {
            let websiteDataTypes = NSSet(array:
                [
                    WKWebsiteDataTypeDiskCache,
                    WKWebsiteDataTypeOfflineWebApplicationCache,
                    WKWebsiteDataTypeMemoryCache
                ]
            )
            let date = NSDate(timeIntervalSince1970: 0)
            
            WKWebsiteDataStore.default().removeData(ofTypes: websiteDataTypes as! Set<String>, modifiedSince: date as Date, completionHandler:{
                result(true)
            })
        }
        else {
            URLCache.shared.removeAllCachedResponses()
            result(true)
        }
    }
    
    private func clearCookies(_ result: @escaping FlutterResult = {(value: Any?) -> Void in}){
        if #available(iOS 9.0, *) {
            let websiteDataTypes = NSSet(array:
                [
                    WKWebsiteDataTypeCookies
                ]
            )
            let date = NSDate(timeIntervalSince1970: 0)
            
            WKWebsiteDataStore.default().removeData(ofTypes: websiteDataTypes as! Set<String>, modifiedSince: date as Date, completionHandler:{
                result(true)
            })
        }
        else {
            var libraryPath = NSSearchPathForDirectoriesInDomains(
                FileManager.SearchPathDirectory.libraryDirectory,
                FileManager.SearchPathDomainMask.userDomainMask,
                false
                ).first!
            libraryPath += "/Cookies"
            
            do {
                try FileManager.default.removeItem(atPath: libraryPath)
            } catch {
                result(false)
                return
            }
            
            result(true)
        }
    }
    
    private func eval(_ call: FlutterMethodCall,_ result: @escaping FlutterResult) {
        let arguments: [String: Any?] = call.arguments as! [String: Any?]
        if let script = arguments["code"] as? String {
            webView?.evaluateJavaScript(script, completionHandler: { (value: Any?, error: Error?) in
                if error != nil {
                    result(error?.localizedDescription ?? "Unknown error")
                } else {
                    result(value ?? "")
                }
            })
        } else {
            result("code is null")
        }
    }
    
    private func resize(_ call: FlutterMethodCall,_ result: @escaping FlutterResult) {
        let rect = buildRect(call)
        webView?.frame = rect
        result(webView != nil)
    }
    
    public func webView(_ webView: WKWebView, didCommit navigation: WKNavigation!) {
        WebviewState.onStateChange(channel ,["event": "loadStarted", "url": webView.url?.absoluteString ?? ""])
    }
    
    public func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {        
        swipeRefresh?.endRefreshing()
        
        WebviewState.onStateChange(channel ,["event": "loadFinished", "url": webView.url?.absoluteString ?? ""])
    }
    
    public func webView(_ webView: WKWebView, decidePolicyFor navigationResponse: WKNavigationResponse, decisionHandler: @escaping (WKNavigationResponsePolicy) -> Void) {
        if navigationResponse.response is HTTPURLResponse {
            let response = navigationResponse.response as! HTTPURLResponse
            if response.statusCode != 200 {
                WebviewState.onStateChange(channel ,["event": "error", "statusCode": response.statusCode, "url": webView.url?.absoluteString ?? ""])
            }
            
            decisionHandler(.allow)
        } else {
            decisionHandler(.allow)
        }
    }
    
    public func webView(_ webView: WKWebView, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void){
        WebviewState.onStateChange(channel ,["event": "error", "statusCode": 401, "url": webView.url?.absoluteString ?? ""])
        
        completionHandler(.useCredential, nil)
    }
}
