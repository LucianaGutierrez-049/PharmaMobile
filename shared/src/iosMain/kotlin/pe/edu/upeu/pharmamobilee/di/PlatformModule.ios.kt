package pe.edu.upeu.pharmamobilee.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform
import org.koin.core.qualifier.named
import org.koin.core.module.Module
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguroIos
import pe.edu.upeu.pharmamobilee.platform.CompartidorIos

actual val platformModule: Module = module {
    single<HttpClientEngine> { Darwin.create() }
    single(named("urlBase")) { "http://localhost:8080/api/v1/" }
    single(named("esDepuracion")) { esDepuracionIos() }
    single<AlmacenSeguro> { AlmacenSeguroIos() }
    single<Compartidor> { CompartidorIos() }
}

@OptIn(ExperimentalNativeApi::class)
private fun esDepuracionIos(): Boolean = Platform.isDebugBinary
