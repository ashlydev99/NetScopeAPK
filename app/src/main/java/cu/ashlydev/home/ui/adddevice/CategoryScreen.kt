package cu.ashlydev.home.ui.adddevice

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cu.ashlydev.home.R
import cu.ashlydev.home.ui.theme.*

data class DeviceCategory(
    val name: String,
    val types: List<String>
)

@Composable
fun CategoryScreen(
    onBackClick: () -> Unit,
    onCategorySelected: (String, String) -> Unit
) {
    val categories = listOf(
        DeviceCategory(
            name = "RED",
            types = listOf(
                "WiFi de casa",
                "Mobile Router",
                "Home Router",
                "5G Router",
                "Repetidor WiFi",
                "Access Point",
                "Other"
            )
        ),
        DeviceCategory(
            name = "ENTRETENIMIENTO",
            types = listOf(
                "Earphones",
                "Parlante",
                "TV",
                "Chromecast",
                "Consola",
                "Other"
            )
        ),
        DeviceCategory(
            name = "OTROS",
            types = listOf(
                "Añadir dispositivo"
            )
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // Top bar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Atrás",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Añadir nuevo dispositivo:",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkOnBackground
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                categories.forEach { category ->
                    item {
                        Text(
                            text = category.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = BattleNetBlue,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(category.types) { type ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategorySelected(category.name, type) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DarkSurface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = type,
                                    fontSize = 16.sp,
                                    color = DarkOnBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = ">",
                                    fontSize = 18.sp,
                                    color = BattleNetBlue
                                )
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}