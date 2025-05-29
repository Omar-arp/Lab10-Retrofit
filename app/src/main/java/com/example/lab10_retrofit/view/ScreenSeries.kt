package com.example.lab10_retrofit.view

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lab10_retrofit.data.SerieApiService
import com.example.lab10_retrofit.data.SerieModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ScreenSeries {
}

@Composable
fun ContenidoSeriesListado(navController: NavHostController, servicio: SerieApiService) {
    var listaSeries: SnapshotStateList<SerieModel> = remember { mutableStateListOf() }
    LaunchedEffect(Unit) {
        try {
            val listado = servicio.selectSeries()
            Log.d("API", "Series recibidas: ${listado.size}")
            listado.forEach {
                Log.d("API", "Serie: ${it.name}")
                listaSeries.add(it)
            }
        } catch (e: Exception) {
            Log.e("API", "Error al obtener series", e)
        }
    }

    LazyColumn (

    ){
        item {
            Row (
                modifier = Modifier.fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.1f)
                )
                Text(
                    text = "SERIE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "Accion",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.2f)
                ) //, fontWeight = FontWeight.Bold)
            }
        }

        items(listaSeries) { item ->
            Row(
                modifier = Modifier.padding(start=8.dp).fillParentMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${item.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier=Modifier.weight(0.1f))
                Text(text = item.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier=Modifier.weight(0.6f))
                IconButton(
                    onClick = {
                        navController.navigate("serieVer/${item.id}")
                        Log.e("SERIE-VER","ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Ver", modifier=Modifier.align(Alignment.CenterVertically))
                }
                IconButton(
                    onClick = {
                        navController.navigate("serieDel/${item.id}")
                        Log.e("SERIE-DEL","ID = ${item.id}")
                    },
                    Modifier.weight(0.1f)
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Ver", modifier=Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}

@Composable
fun ContenidoSerieEditar(navController: NavHostController, servicio: SerieApiService, pid: Int = 0 ) {
    var id by remember { mutableStateOf<Int>(pid) }
    var name by remember { mutableStateOf<String?>("") }
    var release_date by remember { mutableStateOf<String?>("") }
    var rating by remember { mutableStateOf<String?>("") }
    var category by remember { mutableStateOf<String?>("") }
    var grabar by remember { mutableStateOf(false) }

    if (id != 0) {
        LaunchedEffect(Unit) {
            val objSerie = servicio.selectSerie(id.toString())
            delay(100)
            name = objSerie.body()?.name
            release_date = objSerie.body()?.release_date
            rating = objSerie.body()?.rating.toString()
            category = objSerie.body()?.category
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        // Spacer(Modifier.height(50.dp))
        TextField(
            value = id.toString(),
            onValueChange = { },
            label = { Text("ID (solo lectura)") },
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = name!!,
            onValueChange = { name = it },
            label = { Text("Name: ") },
            singleLine = true
        )
        TextField(
            value = release_date!!,
            onValueChange = { release_date = it },
            label = { Text("Release Date:") },
            singleLine = true
        )
        TextField(
            value = rating!!,
            onValueChange = { rating = it },
            label = { Text("Rating:") },
            singleLine = true
        )
        TextField(
            value = category!!,
            onValueChange = { category = it },
            label = { Text("Category:") },
            singleLine = true
        )
        Button(
            onClick = {
                grabar = true
            }
        ) {
            Text("Grabar", fontSize=16.sp)
        }
    }

    if (grabar) {
        val objSerie = SerieModel(id,name!!, release_date!!, rating!!.toInt(), category!!)
        LaunchedEffect(Unit) {
            if (id == 0)
                servicio.insertSerie(objSerie)
            else
                servicio.updateSerie(id.toString(), objSerie)
        }
        grabar = false
        navController.navigate("series")
    }
}

@Composable
fun ContenidoSerieEliminar(navController: NavHostController, servicio: SerieApiService, id: Int) {
    val showDialog = remember { mutableStateOf(true) }
    val contexto = LocalContext.current

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
                navController.navigate("series")
            },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Está seguro de eliminar la Serie?") },
            confirmButton = {
                Button(onClick = {
                    showDialog.value = false
                    // ✅ Ejecutar eliminación solo cuando se acepta
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            servicio.deleteSerie(id.toString())
                        } catch (e: Exception) {
                            Log.e("DELETE", "Error eliminando", e)
                        }
                        withContext(Dispatchers.Main) {
                            navController.navigate("series")
                        }
                    }
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog.value = false
                    navController.navigate("series")
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}


@Composable
fun ScreenInicio() {
    Text(text = "Bienvenido a SeriesApp")
}

