package pe.edu.upeu.pharmamobilee.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.local.CreadorDeDriver
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositorioEnMemoria
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositorioOffline
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion
import pe.edu.upeu.pharmamobilee.presentation.sesion.SesionViewModel

val dataModule = module {
    single {
        crearHttpClient(
            engine = get(),
            urlBase = get(named("urlBase")),
            almacenSeguro = get<AlmacenSeguro>(),
            esDepuracion = get(named("esDepuracion"))
        )
    }
    single { ProductoApi(get()) }
    single { ProductoRepositorioEnMemoria() }
    single<ProductoRepository>(named("repositorioRemoto")) {
        ProductoRepositoryImpl(
            api = get(),
            categoriaPorDefecto = 1L
        )
    }
    single { ProductoLocal(get<CreadorDeDriver>().crear()) }
    single<ProductoRepository> {
        ProductoRepositorioOffline(
            local = get(),
            remoto = get(named("repositorioRemoto"))
        )
    }
    single<AuthRepository> { AuthRepositorioRest(get(), get()) }
}

val domainModule = module {
    factory {
        RegistrarProductoUseCase(get())
    }
    factory {
        ActualizarProductoUseCase(get())
    }
    factory {
        ListarProductosUseCase(get())
    }
    factory {
        EliminarProductoUseCase(get())
    }
}

val presentationModule = module {
    single { GestorSesion() }
    viewModel { SesionViewModel(get(), get()) }
    viewModel {
        ProductoViewModel(
            listarProductosUseCase = get(),
            registrarProductoUseCase = get(),
            actualizarProductoUseCase = get(),
            eliminarProductoUseCase = get(),
            compartidor = get(),
            authRepository = get(),
            gestorSesion = get(),
            productoRepository = get()
        )
    }
}

expect val platformModule: Module

fun initKoin(
    config: KoinApplication.() -> Unit = {}
) {
    startKoin {
        config()
        modules(
            dataModule,
            domainModule,
            presentationModule,
            platformModule
        )
    }
}
