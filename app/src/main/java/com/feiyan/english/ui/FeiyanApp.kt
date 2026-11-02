package com.feiyan.english.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.feiyan.english.player.PlayerController
import com.feiyan.english.ui.by.ByBookScreen
import com.feiyan.english.ui.by.ByReaderScreen
import com.feiyan.english.ui.channel.ChannelScreen
import com.feiyan.english.ui.components.FeiyanBottomBar
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.MiniPlayer
import com.feiyan.english.ui.eg.EgArticleScreen
import com.feiyan.english.ui.eg.EgListScreen
import com.feiyan.english.ui.em.EmEpisodeScreen
import com.feiyan.english.ui.em.EmListScreen
import com.feiyan.english.ui.episode.EpisodeScreen
import com.feiyan.english.ui.home.HomeScreen
import com.feiyan.english.data.LibraryStore
import com.feiyan.english.ui.library.AboutAppScreen
import com.feiyan.english.ui.library.DisclaimerScreen
import com.feiyan.english.ui.library.FavoritesScreen
import com.feiyan.english.ui.library.HistoryScreen
import com.feiyan.english.ui.library.LibraryScreen
import com.feiyan.english.ui.search.SearchScreen
import com.feiyan.english.ui.settings.SettingsScreen
import com.feiyan.english.ui.update.UpdateDialog
import com.feiyan.english.ui.update.UpdateViewModel
import com.feiyan.english.ui.xy.XyEpisodeScreen
import com.feiyan.english.ui.xy.XyPodcastScreen
import com.feiyan.english.ui.yy.YyBookScreen
import com.feiyan.english.ui.yy.YyChapterScreen
import com.feiyan.english.ui.yy.YyNovelsScreen
import com.feiyan.english.ui.theme.Paper
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

object Routes {
    const val Home = "home"
    const val Novels = "novels"
    const val Articles = "articles"
    const val Library = "library"
    const val Search = "search"
    const val Settings = "settings"
    const val Channel = "channel/{isbn}"
    const val Episode = "episode/{key}"
    const val EmList = "em"
    const val EmEpisode = "em/{slug}"
    const val XyPodcast = "xy"
    const val XyEpisode = "xy/{eid}"
    const val EgArticle = "eg/{id}"
    const val YyBook = "yy/{bookId}"
    const val YyChapter = "yy/{bookId}/{index}"
    const val ByBook = "by/{bookId}"
    const val ByReader = "by/{bookId}/{chapterId}"
    const val History = "history"
    const val Favorites = "favorites"
    const val About = "about"
    const val Disclaimer = "disclaimer"

    val topLevel = setOf(Home, Novels, Articles, Library)

    fun channel(isbn: String) = "channel/$isbn"
    fun episode(key: String) = "episode/$key"
    fun emEpisode(slug: String) = "em/$slug"
    fun xyEpisode(eid: String) = "xy/$eid"
    fun egArticle(id: String) = "eg/$id"
    fun yyBook(bookId: String) = "yy/$bookId"
    fun yyChapter(bookId: String, index: Int) = "yy/$bookId/$index"
    fun byBook(bookId: String) = "by/$bookId"
    fun byReader(bookId: String, chapterId: Long) = "by/$bookId/$chapterId"
}

@Composable
fun FeiyanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.Home
    val player by PlayerController.state.collectAsStateWithLifecycle()
    val hazeState = remember { HazeState() }

    // 启动自动检查更新：开启「自动更新」时直接下载安装，否则只提示
    val updateViewModel: UpdateViewModel = viewModel()
    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        updateViewModel.check(silent = true, autoInstall = true)
    }

    NotificationPermissionEffect(LocalContext.current)

    val navBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val showTabs = route in Routes.topLevel
    val showMini = player.hasMedia &&
        route != Routes.Episode &&
        route != Routes.EmEpisode &&
        route != Routes.XyEpisode
    val overlayPadding = (if (showTabs) 74.dp else 0.dp) +
        (if (showMini) 56.dp else 0.dp) +
        navBarInset

    Box(Modifier.fillMaxSize().background(Paper)) {
        CompositionLocalProvider(LocalBottomOverlayPadding provides overlayPadding) {
            NavHost(
                navController = navController,
                startDestination = Routes.Home,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .hazeSource(state = hazeState),
            ) {
                composable(Routes.Home) {
                    HomeScreen(
                        onChannelClick = { navController.navigate(Routes.channel(it)) },
                        onSearchClick = { navController.navigate(Routes.Search) },
                        onEpisodeClick = { navController.navigate(Routes.episode(it)) },
                        onEmAllClick = { navController.navigate(Routes.EmList) },
                        onEmEpisodeClick = { navController.navigate(Routes.emEpisode(it)) },
                        onXyAllClick = { navController.navigate(Routes.XyPodcast) },
                        onXyEpisodeClick = { navController.navigate(Routes.xyEpisode(it)) },
                    )
                }
                composable(
                    route = Routes.Channel,
                    arguments = listOf(navArgument("isbn") { type = NavType.StringType }),
                ) {
                    ChannelScreen(
                        onBack = { navController.popBackStack() },
                        onEpisodeClick = { navController.navigate(Routes.episode(it)) },
                    )
                }
                composable(
                    route = Routes.Episode,
                    arguments = listOf(navArgument("key") { type = NavType.StringType }),
                ) {
                    EpisodeScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.Search) {
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        onChannelClick = { navController.navigate(Routes.channel(it)) },
                    )
                }
                composable(Routes.Novels) {
                    YyNovelsScreen(
                        onBookClick = { navController.navigate(Routes.yyBook(it)) },
                        onByBookClick = { navController.navigate(Routes.byBook(it)) },
                    )
                }
                composable(
                    route = Routes.ByBook,
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                ) { entry ->
                    val bookId = entry.arguments?.getString("bookId").orEmpty()
                    ByBookScreen(
                        onBack = { navController.popBackStack() },
                        onChapterClick = { chapterId ->
                            navController.navigate(Routes.byReader(bookId, chapterId))
                        },
                    )
                }
                composable(
                    route = Routes.ByReader,
                    arguments = listOf(
                        navArgument("bookId") { type = NavType.StringType },
                        navArgument("chapterId") { type = NavType.StringType },
                    ),
                ) {
                    ByReaderScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Routes.YyBook,
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                ) { entry ->
                    val bookId = entry.arguments?.getString("bookId").orEmpty()
                    YyBookScreen(
                        onBack = { navController.popBackStack() },
                        onChapterClick = { index ->
                            navController.navigate(Routes.yyChapter(bookId, index))
                        },
                    )
                }
                composable(
                    route = Routes.YyChapter,
                    arguments = listOf(
                        navArgument("bookId") { type = NavType.StringType },
                        navArgument("index") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val bookId = entry.arguments?.getString("bookId").orEmpty()
                    YyChapterScreen(
                        onBack = { navController.popBackStack() },
                        onOpenChapter = { index ->
                            navController.navigate(Routes.yyChapter(bookId, index)) {
                                popUpTo(Routes.YyChapter) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Routes.Articles) {
                    EgListScreen(
                        onArticleClick = { navController.navigate(Routes.egArticle(it)) },
                        onByArticleClick = { bookId, chapterId ->
                            navController.navigate(Routes.byReader(bookId, chapterId))
                        },
                    )
                }
                composable(
                    route = Routes.EgArticle,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) {
                    EgArticleScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.Library) {
                    LibraryScreen(
                        onSettingsClick = { navController.navigate(Routes.Settings) },
                        onHistoryClick = { navController.navigate(Routes.History) },
                        onFavoritesClick = { navController.navigate(Routes.Favorites) },
                        onAboutClick = { navController.navigate(Routes.About) },
                        onDisclaimerClick = { navController.navigate(Routes.Disclaimer) },
                    )
                }
                composable(Routes.Disclaimer) {
                    DisclaimerScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.History) {
                    HistoryScreen(
                        onBack = { navController.popBackStack() },
                        onOpen = { route -> navController.navigate(route) },
                    )
                }
                composable(Routes.Favorites) {
                    FavoritesScreen(
                        onBack = { navController.popBackStack() },
                        onOpen = { route -> navController.navigate(route) },
                    )
                }
                composable(Routes.About) {
                    AboutAppScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.Settings) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.XyPodcast) {
                    XyPodcastScreen(
                        onBack = { navController.popBackStack() },
                        onEpisodeClick = { navController.navigate(Routes.xyEpisode(it)) },
                    )
                }
                composable(
                    route = Routes.XyEpisode,
                    arguments = listOf(navArgument("eid") { type = NavType.StringType }),
                ) {
                    XyEpisodeScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.EmList) {
                    EmListScreen(
                        onBack = { navController.popBackStack() },
                        onEpisodeClick = { navController.navigate(Routes.emEpisode(it)) },
                    )
                }
                composable(
                    route = Routes.EmEpisode,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType }),
                ) {
                    EmEpisodeScreen(onBack = { navController.popBackStack() })
                }
            }
        }

        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            if (showMini) {
                MiniPlayer(
                    state = player,
                    onClick = {
                        val route = LibraryStore.history.value.firstOrNull { it.id == player.mediaId }?.route
                        if (route != null) navController.navigate(route)
                    },
                    onToggle = PlayerController::toggle,
                    onClose = PlayerController::stop,
                )
            }
            if (showTabs) {
                FeiyanBottomBar(
                    currentRoute = route,
                    onSelect = { navController.switchTab(it) },
                    hazeState = hazeState,
                    modifier = Modifier.navigationBarsPadding(),
                )
            } else {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }

        UpdateDialog(
            state = updateState,
            onUpdate = updateViewModel::startDownload,
            onDismiss = updateViewModel::dismiss,
        )
    }
}

private fun NavHostController.switchTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun NotificationPermissionEffect(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
