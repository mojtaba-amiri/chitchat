import android.app.Application
import com.chitchat.common.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App: Application() {

    val androidModule = module {
        // Android specific dependencies
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            // androidModule: refers to Android-specific dependencies
            // appModule: refers to Shared module dependencies
            modules(appModule() + androidModule)
        }
    }
}