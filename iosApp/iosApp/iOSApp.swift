import SwiftUI
import Shared
#if canImport(FirebaseCore)
import FirebaseCore
#endif

@main
struct iOSApp: App {
    init() {
        #if canImport(FirebaseCore)
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        }
        #endif
        KoinIosKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
