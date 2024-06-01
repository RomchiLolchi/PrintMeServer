import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.w3c.dom.HTMLInputElement
import org.w3c.fetch.*
import org.w3c.files.File
import org.w3c.files.get
import org.w3c.xhr.FormData
import kotlin.js.Promise

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") {
        PrintMeTheme {
            var printersList by remember { mutableStateOf(listOf("-")) }
            var selectedPrinter by remember { mutableStateOf(printersList[0]) }
            val printAttrs: MutableState<ServiceAttributeSet?> = remember { mutableStateOf(ServiceAttributeSet()) }
            LaunchedEffect(printAttrs.value) {
                println("LaunchedEffect entered composition with following key: ${printAttrs.value ?: "null, but replaced with ${ServiceAttributeSet()}"}")
                val response = window.fetch(
                    "/PrintMe/api/printme/SetAttributesAndGetPrinters",
                    RequestInit(
                        "POST",
                        body = Json.encodeToString(printAttrs.value ?: ServiceAttributeSet()).toJsString(),
                        headers = Headers().apply {
                            append("Content-Type", "application/json")
                            append("Accept", "*/*")
                        },
                        cache = RequestCache.NO_CACHE,
                        credentials = RequestCredentials.OMIT,
                        mode = RequestMode.NO_CORS,
                        redirect = RequestRedirect.FOLLOW,
                        referrerPolicy = "no-referrer".toJsString(),
                        integrity = ""
                    )
                ).await<Response>()
                printersList =
                    if (response.ok) response.text().await<JsString>().toString().removePrefix("[").removeSuffix("]")
                        .split(",").map { it.removeSurrounding("\"") } else listOf("ERROR FETCHING")

                if (printersList.indexOf(selectedPrinter) == -1) {
                    selectedPrinter = printersList[0]
                }
            }
            LaunchedEffect(selectedPrinter) {
                println("LaunchedEffect - selected printer entered composition with key: $selectedPrinter")

                if (selectedPrinter != "-") {
                    window.fetch(
                        "/PrintMe/api/printme/SetChosenPrinterByName",
                        RequestInit(
                            "POST",
                            body = selectedPrinter.toJsString(),
                            headers = Headers().apply {
                                append("Content-Type", "text/plain")
                                append("Accept", "*/*")
                            },
                            cache = RequestCache.NO_CACHE,
                            credentials = RequestCredentials.OMIT,
                            mode = RequestMode.NO_CORS,
                            redirect = RequestRedirect.FOLLOW,
                            referrerPolicy = "no-referrer".toJsString(),
                            integrity = ""
                        )
                    )
                }
            }
            MainScreen(
                printersList = printersList,
                printAttrs = printAttrs,
                selectedPrinter = selectedPrinter,
                onSelectedPrinterChange = { selectedPrinter = it }
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MainScreen(
    printersList: List<String>,
    printAttrs: MutableState<ServiceAttributeSet?>,
    selectedPrinter: String,
    onSelectedPrinterChange: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    var copiesCount by remember { mutableStateOf(1) }
    var isColorPrint by remember { mutableStateOf(false) }
    var sheetOrientation by remember { mutableStateOf(SheetOrientations.Portrait) }
    var chosenFile by remember { mutableStateOf<File?>(null) }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painterResource(DrawableResource("/printer.png")),
            contentDescription = "Принтер",
            modifier = Modifier.padding(top = 20.dp).size(200.dp)
        )
        Text("Сервис PrintMe", fontSize = 38.sp, modifier = Modifier.padding(top = 5.dp))
        Spacer(Modifier.size(40.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Выбранный принтер:", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
            StringSpinner(
                modifier = Modifier.width(400.dp),
                list = printersList,
                preselected = selectedPrinter,
                onSelectionChanged = onSelectedPrinterChange)
        }
        Spacer(Modifier.size(40.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Атрибуты печати:", fontSize = 28.sp)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Количество копий: $copiesCount", fontSize = 20.sp, modifier = Modifier.padding(end = 40.dp))
                Row {
                    Button(
                        onClick = {
                            copiesCount += 1
                            printAttrs.value = printAttrs.value?.copy(copies = copiesCount) ?: ServiceAttributeSet(
                                copies = copiesCount
                            )
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Text("+", fontSize = 15.sp)
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = {
                            if (copiesCount > 1) {
                                copiesCount -= 1
                                printAttrs.value = printAttrs.value?.copy(copies = copiesCount) ?: ServiceAttributeSet(
                                    copies = copiesCount
                                )
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant),
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Text("-", fontSize = 15.sp)
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Цветная печать", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                Checkbox(
                    checked = isColorPrint,
                    onCheckedChange = {
                        isColorPrint = it
                        printAttrs.value =
                            printAttrs.value?.copy(colorPrint = it) ?: ServiceAttributeSet(colorPrint = it)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colors.secondaryVariant,
                        checkmarkColor = Color.Black
                    )
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ориентация листа:", fontSize = 20.sp, modifier = Modifier.padding(end = 10.dp))
                StringSpinner(
                    modifier = Modifier.width(400.dp),
                    list = listOf(SheetOrientations.Portrait.translation, SheetOrientations.Landscape.translation),
                    preselected = sheetOrientation.translation,
                    onSelectionChanged = {
                        sheetOrientation = SheetOrientations.entries.find { item -> item.translation == it }!!
                        printAttrs.value =
                            printAttrs.value?.copy(orientation = sheetOrientation.name.toLowerCase(Locale.current))
                                ?: ServiceAttributeSet(
                                    orientation = sheetOrientation.name.toLowerCase(Locale.current)
                                )
                    }
                )
            }
        }
        Spacer(Modifier.size(40.dp))
        LaunchedEffect(chosenFile) {
            println("LaunchedEffect - chosen file entered the composition with key: $chosenFile")
            chosenFile?.let {
                window.fetch(
                    "/PrintMe/api/printme/PostFile",
                    RequestInit(
                        "POST",
                        body = FormData().apply { append("file", chosenFile!!) },
                        headers = Headers().apply {
                            append("Accept", "*/*")
                        },
                        cache = RequestCache.NO_CACHE,
                        credentials = RequestCredentials.OMIT,
                        mode = RequestMode.NO_CORS,
                        redirect = RequestRedirect.FOLLOW,
                        referrerPolicy = "no-referrer".toJsString(),
                        integrity = ""
                    )
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Выбранный файл: ${chosenFile?.name ?: "Файл не выбран"}",
                fontSize = 20.sp,
                modifier = Modifier.padding(end = 40.dp)
            )
            Button(
                onClick = {
                    coroutineScope.launch {
                        chosenFile = selectFile().await<File>()
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondaryVariant),
            ) {
                Text("Выбрать новый файл", fontSize = 15.sp)
            }
        }
        Spacer(Modifier.size(30.dp))
        Button(
            onClick = {
                window.fetch(
                    "/PrintMe/api/printme/StartPrinting",
                    RequestInit(
                        "GET",
                        headers = Headers().apply {
                            append("Accept", "*/*")
                        },
                        cache = RequestCache.NO_CACHE,
                        credentials = RequestCredentials.OMIT,
                        mode = RequestMode.NO_CORS,
                        redirect = RequestRedirect.FOLLOW,
                        referrerPolicy = "no-referrer".toJsString(),
                        integrity = ""
                    )
                )
            }
        ) {
            Text("Печать!", fontSize = 20.sp)
        }
    }
}

enum class SheetOrientations(val translation: String) {
    Portrait("Книжная"),
    Landscape("Альбомная")
}

fun selectFile(contentType: String = "*/*"): Promise<File> {
    return Promise { resolve: (File) -> Unit, _: (JsAny) -> Unit ->
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.multiple = false
        input.accept = contentType

        input.onchange = {
            resolve(input.files!![0]!!)
        }
        input.click()
    }
}