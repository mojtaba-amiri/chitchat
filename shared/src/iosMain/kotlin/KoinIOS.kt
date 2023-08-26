import org.koin.core.context.startKoin

fun initKoin() {
    // start Koin
    startKoin {
        modules(appModule())
    }
}