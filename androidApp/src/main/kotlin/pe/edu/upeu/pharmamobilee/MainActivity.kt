package pe.edu.upeu.pharmamobilee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.koin.android.ext.android.inject
import pe.edu.upeu.pharmamobilee.data.permiso.GestorPermisosAndroid

class MainActivity : ComponentActivity() {
    private val permisos by inject<GestorPermisosAndroid>()

    override fun onDestroy() {
        permisos.desconectar(this)
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        permisos.conectar(this)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
