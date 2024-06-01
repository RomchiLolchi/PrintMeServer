import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PrintMeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        lightColors(
            primary = Color(0xFF5DADEC),
            primaryVariant = Color(0xFF62686E),
            secondary = Color(0xFFEDC55C),
            secondaryVariant = Color(0xFFED9B5C),
            onPrimary = Color.Black,
            onSecondary = Color.Black
        ),
        content = content
    )
}

@Composable
fun StringSpinner(
    modifier: Modifier = Modifier,
    list: List<String>,
    preselected: String,
    onSelectionChanged: (data: String) -> Unit,
) {
    var selected = preselected
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable {
            expanded = !expanded
        }
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = selected,
                modifier = Modifier.weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Icon(Icons.Outlined.ArrowDropDown, null, modifier = Modifier.padding(8.dp))

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.wrapContentWidth()
            ) {
                list.forEach { listEntry ->

                    DropdownMenuItem(
                        onClick = {
                            selected = listEntry
                            expanded = false
                            onSelectionChanged(selected)
                        },
                        content = {
                            Text(
                                text = listEntry,
                                modifier = Modifier
                                    .wrapContentWidth()
                            )
                        },
                    )
                }
            }

        }
    }
}