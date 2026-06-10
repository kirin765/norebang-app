package com.happylife2080.norebang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.happylife2080.norebang.data.Song

private val Purple = Color(0xFF5B2EFF)
private val TjColor = Color(0xFF2E7DFF)
private val KyColor = Color(0xFFE5484D)

private val LightColors: ColorScheme = lightColorScheme(
    primary = Purple,
    surface = Color(0xFFFCFBFF),
    background = Color(0xFFFCFBFF),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = LightColors) {
                App()
            }
        }
    }
}

@Composable
fun App(vm: SearchViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0, onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "검색") },
                    label = { Text("검색") },
                )
                NavigationBarItem(
                    selected = tab == 1, onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "즐겨찾기") },
                    label = { Text("즐겨찾기") },
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (tab == 0) SearchScreen(state, vm) else FavoritesScreen(state, vm)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(state: UiState, vm: SearchViewModel) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = vm::onQueryChange,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            placeholder = { Text("곡명 또는 가수명 검색") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
        )
        Row(
            Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(selected = state.brandFilter == null, onClick = { vm.onBrandFilter(null) }, label = { Text("전체") })
            FilterChip(selected = state.brandFilter == "TJ", onClick = { vm.onBrandFilter("TJ") }, label = { Text("TJ") })
            FilterChip(selected = state.brandFilter == "KY", onClick = { vm.onBrandFilter("KY") }, label = { Text("금영") })
        }
        if (state.query.isBlank()) {
            Column(
                Modifier.fillMaxSize().padding(bottom = 80.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🎤", fontSize = 44.sp)
                Spacer(Modifier.padding(4.dp))
                Text("부르고 싶은 곡을 검색하세요", color = Color.Gray)
                Spacer(Modifier.padding(2.dp))
                Text(
                    "곡 DB ${state.dbVersion} · ${"%,d".format(state.songCount)}곡",
                    color = Color.LightGray, fontSize = 12.sp,
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(state.results, key = { "${it.brand}:${it.number}" }) { song ->
                    SongRow(song, "${song.brand}:${song.number}" in state.favoriteKeys, vm::toggleFavorite)
                    HorizontalDivider(color = Color(0xFFF0EEF5))
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(state: UiState, vm: SearchViewModel) {
    if (state.favorites.isEmpty()) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("♡", fontSize = 44.sp, color = Color.LightGray)
            Text("검색 결과에서 하트를 눌러\n자주 부르는 곡을 저장하세요", color = Color.Gray)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            items(state.favorites, key = { "${it.brand}:${it.number}" }) { song ->
                SongRow(song, isFavorite = true, onToggle = vm::toggleFavorite)
                HorizontalDivider(color = Color(0xFFF0EEF5))
            }
        }
    }
}

@Composable
fun SongRow(song: Song, isFavorite: Boolean, onToggle: (Song) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val badgeColor = if (song.brand == "TJ") TjColor else KyColor
        Box(
            Modifier
                .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                "${if (song.brand == "TJ") "TJ" else "금영"} ${song.number}",
                color = badgeColor, fontWeight = FontWeight.Bold, fontSize = 13.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = Color.Gray, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = { onToggle(song) }) {
            Icon(
                if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "즐겨찾기",
                tint = if (isFavorite) Color(0xFFE5484D) else Color.LightGray,
            )
        }
    }
}
