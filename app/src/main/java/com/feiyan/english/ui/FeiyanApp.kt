package com.feiyan.english.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import com.feiyan.english.ui.beidanci.BeidanciCourseScreen
import com.feiyan.english.ui.beidanci.BeidanciStudyScreen
import com.feiyan.english.ui.by.ByBookScreen
import com.feiyan.english.ui.by.ByReaderScreen
import com.feiyan.english.ui.channel.ChannelScreen
import com.feiyan.english.ui.components.FeiyanBottomBar
import com.feiyan.english.ui.components.LocalBottomOverlayPadding
import com.feiyan.english.ui.components.MiniPlayer
import com.feiyan.english.ui.echo.EchoDocumentScreen
import com.feiyan.english.ui.echo.EchoYearScreen
import com.feiyan.english.ui.eg.EgArticleScreen
import com.feiyan.english.ui.em.EmEpisodeScreen
import com.feiyan.english.ui.iciba.IcibaCourseScreen
import com.feiyan.english.ui.iciba.IcibaGroupScreen
import com.feiyan.english.ui.iciba.IcibaLessonScreen
import com.feiyan.english.ui.ipa.IpaDetailScreen
import com.feiyan.english.ui.ipa.IpaScreen
import com.feiyan.english.ui.em.EmListScreen
import com.feiyan.english.ui.episode.EpisodeScreen
import com.feiyan.english.ui.game.GamePlayScreen
import com.feiyan.english.data.LibraryStore
import com.feiyan.english.ui.library.AboutAppScreen
import com.feiyan.english.ui.library.DisclaimerScreen
import com.feiyan.english.ui.library.FavoritesScreen
import com.feiyan.english.ui.library.HistoryScreen
import com.feiyan.english.ui.library.LibraryScreen
import com.feiyan.english.ui.search.SearchScreen
import com.feiyan.english.ui.settings.SettingsScreen
import com.feiyan.english.ui.study.StudyScreen
import com.feiyan.english.ui.tts.TtsScreen
import com.feiyan.english.ui.update.UpdateDialog
import com.feiyan.english.ui.update.UpdateViewModel
import com.feiyan.english.ui.wordforest.WordArticleScreen
import com.feiyan.english.ui.wordforest.WordBookScreen
import com.feiyan.english.ui.wordforest.WordDetailScreen
import com.feiyan.english.ui.xy.XyEpisodeScreen
import com.feiyan.english.ui.xy.XyPodcastScreen
import com.feiyan.english.ui.yy.YyBookScreen
import com.feiyan.english.ui.yy.YyChapterScreen
import com.feiyan.english.ui.theme.Paper
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

object Routes {
    const val Study = "study"
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
    const val EchoYear = "echo/{exam}/{year}"
    const val EchoDocument = "echo/doc?path={path}"
    const val WfBook = "wordforest/book/{id}"
    const val WfWord = "wordforest/word/{word}"
    const val WfArticle = "wordforest/article/{id}"
    const val IcibaGroup = "iciba/group/{classId}"
    const val IcibaCourse = "iciba/course/{classId}"
    const val IcibaLesson = "iciba/lesson/{classId}/{courseId}"
    const val BdCourse = "beidanci/course/{key}"
    const val BdStudy = "beidanci/study/{key}/{unitId}"
    const val Tts = "tts"
    const val Ipa = "ipa"
    const val IpaDetail = "ipa/detail/{id}"
    const val GamePlay = "game/{gameId}"

    val topLevel = setOf(Study, Library)

    fun channel(isbn: String) = "channel/$isbn"
    fun episode(key: String) = "episode/$key"
    fun emEpisode(slug: String) = "em/$slug"
    fun xyEpisode(eid: String) = "xy/$eid"
    fun egArticle(id: String) = "eg/$id"
    fun yyBook(bookId: String) = "yy/$bookId"
    fun yyChapter(bookId: String, index: Int) = "yy/$bookId/$index"
    fun byBook(bookId: String) = "by/$bookId"
    fun byReader(bookId: String, chapterId: Long) = "by/$bookId/$chapterId"
    fun echoYear(exam: String, year: Int) = "echo/$exam/$year"
    fun echoDocument(path: String) = "echo/doc?path=${Uri.encode(path)}"
    fun wfBook(id: String) = "wordforest/book/$id"
    fun wfWord(word: String) = "wordforest/word/${Uri.encode(word)}"
    fun wfArticle(id: String) = "wordforest/article/$id"
    fun icibaGroup(classId: String) = "iciba/group/$classId"
    fun icibaCourse(classId: String) = "iciba/course/$classId"
    fun icibaLesson(classId: String, courseId: Int) = "iciba/lesson/$classId/$courseId"
    fun bdCourse(key: String) = "beidanci/course/${Uri.encode(key)}"
    fun bdStudy(key: String, unitId: Int) = "beidanci/study/${Uri.encode(key)}/$unitId"
    fun ipaDetail(id: String) = "ipa/detail/${Uri.encode(id)}"
    fun gamePlay(gameId: String) = "game/$gameId"
}

@Composable
fun FeiyanApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val visibleEntries by navController.visibleEntries.collectAsStateWithLifecycle()
    val route = backStackEntry?.destination?.route ?: Routes.Study
    val transitioning = visibleEntries.size > 1
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
    val showTabs = !transitioning && route in Routes.topLevel
    val showMini = !transitioning &&
        player.hasMedia &&
        route != Routes.Episode &&
        route != Routes.EmEpisode &&
        route != Routes.XyEpisode &&
        !route.startsWith("game/")
    val overlayPadding = (if (showTabs) 74.dp else 0.dp) +
        (if (showMini) 56.dp else 0.dp) +
        navBarInset

    Box(Modifier.fillMaxSize().background(Paper)) {
        CompositionLocalProvider(LocalBottomOverlayPadding provides overlayPadding) {
            NavHost(
                navController = navController,
                startDestination = Routes.Study,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .hazeSource(state = hazeState),
                // 默认淡入淡出会让新旧页面在同一帧叠层，透明页面还会透出
                // 上一页内容；路由切换统一即时完成，避免任何残留画面。
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
            ) {
                composable(Routes.Study) {
                    OpaqueDestination {
                        StudyScreen(
                            onSearchClick = { navController.navigate(Routes.Search) },
                            onChannelClick = { navController.navigate(Routes.channel(it)) },
                            onEpisodeClick = { navController.navigate(Routes.episode(it)) },
                            onEmAllClick = { navController.navigate(Routes.EmList) },
                            onEmEpisodeClick = { navController.navigate(Routes.emEpisode(it)) },
                            onXyAllClick = { navController.navigate(Routes.XyPodcast) },
                            onXyEpisodeClick = { navController.navigate(Routes.xyEpisode(it)) },
                            onYyBookClick = { navController.navigate(Routes.yyBook(it)) },
                            onByBookClick = { navController.navigate(Routes.byBook(it)) },
                            onEgArticleClick = { navController.navigate(Routes.egArticle(it)) },
                            onByArticleClick = { bookId, chapterId ->
                                navController.navigate(Routes.byReader(bookId, chapterId))
                            },
                            onGamePlayClick = { gameId ->
                                navController.navigate(Routes.gamePlay(gameId))
                            },
                            onEchoYearClick = { exam, year ->
                                navController.navigate(Routes.echoYear(exam.key, year))
                            },
                            onEchoDocumentClick = { path ->
                                navController.navigate(Routes.echoDocument(path))
                            },
                            onWfBookClick = { navController.navigate(Routes.wfBook(it)) },
                            onWfWordClick = { navController.navigate(Routes.wfWord(it)) },
                            onWfArticleClick = { navController.navigate(Routes.wfArticle(it)) },
                            onIcibaGroupClick = { navController.navigate(Routes.icibaGroup(it)) },
                            onBdCourseClick = { navController.navigate(Routes.bdCourse(it)) },
                            onBdStudyClick = { key, unitId ->
                                navController.navigate(Routes.bdStudy(key, unitId))
                            },
                            onTtsClick = { navController.navigate(Routes.Tts) },
                            onIpaClick = { navController.navigate(Routes.Ipa) },
                        )
                    }
                }
                composable(
                    route = Routes.Channel,
                    arguments = listOf(navArgument("isbn") { type = NavType.StringType }),
                ) {
                    OpaqueDestination {
                        ChannelScreen(
                            onBack = { navController.popBackStack() },
                            onEpisodeClick = { navController.navigate(Routes.episode(it)) },
                        )
                    }
                }
                composable(
                    route = Routes.Episode,
                    arguments = listOf(navArgument("key") { type = NavType.StringType }),
                ) {
                    OpaqueDestination {
                        EpisodeScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.Search) {
                    OpaqueDestination {
                        SearchScreen(
                            onBack = { navController.popBackStack() },
                            onChannelClick = { navController.navigate(Routes.channel(it)) },
                        )
                    }
                }
                composable(
                    route = Routes.ByBook,
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                ) { entry ->
                    val bookId = entry.arguments?.getString("bookId").orEmpty()
                    OpaqueDestination {
                        ByBookScreen(
                            onBack = { navController.popBackStack() },
                            onChapterClick = { chapterId ->
                                navController.navigate(Routes.byReader(bookId, chapterId))
                            },
                        )
                    }
                }
                composable(
                    route = Routes.ByReader,
                    arguments = listOf(
                        navArgument("bookId") { type = NavType.StringType },
                        navArgument("chapterId") { type = NavType.StringType },
                    ),
                ) {
                    OpaqueDestination {
                        ByReaderScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(
                    route = Routes.YyBook,
                    arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
                ) { entry ->
                    val bookId = entry.arguments?.getString("bookId").orEmpty()
                    OpaqueDestination {
                        YyBookScreen(
                            onBack = { navController.popBackStack() },
                            onChapterClick = { index ->
                                navController.navigate(Routes.yyChapter(bookId, index))
                            },
                        )
                    }
                }
                composable(
                    route = Routes.YyChapter,
                    arguments = listOf(
                        navArgument("bookId") { type = NavType.StringType },
                        navArgument("index") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val bookId = entry.arguments?.getString("bookId").orEmpty()
                    OpaqueDestination {
                        YyChapterScreen(
                            onBack = { navController.popBackStack() },
                            onOpenChapter = { index ->
                                navController.navigate(Routes.yyChapter(bookId, index)) {
                                    popUpTo(Routes.YyChapter) { inclusive = true }
                                }
                            },
                        )
                    }
                }
                composable(
                    route = Routes.EgArticle,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) {
                    OpaqueDestination {
                        EgArticleScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.Library) {
                    OpaqueDestination {
                        LibraryScreen(
                            onSettingsClick = { navController.navigate(Routes.Settings) },
                            onHistoryClick = { navController.navigate(Routes.History) },
                            onFavoritesClick = { navController.navigate(Routes.Favorites) },
                            onAboutClick = { navController.navigate(Routes.About) },
                            onDisclaimerClick = { navController.navigate(Routes.Disclaimer) },
                        )
                    }
                }
                composable(
                    route = Routes.EchoYear,
                    arguments = listOf(
                        navArgument("exam") { type = NavType.StringType },
                        navArgument("year") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val exam = entry.arguments?.getString("exam").orEmpty()
                    val year = entry.arguments?.getString("year").orEmpty().toIntOrNull() ?: 0
                    OpaqueDestination {
                        EchoYearScreen(
                            examKey = exam,
                            yearValue = year,
                            onBack = { navController.popBackStack() },
                            onDocumentClick = { path ->
                                navController.navigate(Routes.echoDocument(path))
                            },
                        )
                    }
                }
                composable(
                    route = Routes.EchoDocument,
                    arguments = listOf(navArgument("path") { type = NavType.StringType }),
                ) { entry ->
                    val path = Uri.decode(entry.arguments?.getString("path").orEmpty())
                    OpaqueDestination {
                        EchoDocumentScreen(
                            path = path,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(
                    route = Routes.WfBook,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    OpaqueDestination {
                        WordBookScreen(
                            bookId = id,
                            onBack = { navController.popBackStack() },
                            onWordClick = { navController.navigate(Routes.wfWord(it)) },
                        )
                    }
                }
                composable(
                    route = Routes.WfWord,
                    arguments = listOf(navArgument("word") { type = NavType.StringType }),
                ) { entry ->
                    val word = Uri.decode(entry.arguments?.getString("word").orEmpty())
                    OpaqueDestination {
                        WordDetailScreen(
                            word = word,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(
                    route = Routes.WfArticle,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    OpaqueDestination {
                        WordArticleScreen(
                            articleId = id,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(
                    route = Routes.IcibaGroup,
                    arguments = listOf(navArgument("classId") { type = NavType.StringType }),
                ) { entry ->
                    val classId = entry.arguments?.getString("classId").orEmpty()
                    OpaqueDestination {
                        IcibaGroupScreen(
                            classId = classId,
                            onBack = { navController.popBackStack() },
                            onCourseClick = { courseClassId ->
                                navController.navigate(Routes.icibaCourse(courseClassId))
                            },
                            onLessonClick = { courseId ->
                                navController.navigate(Routes.icibaLesson(classId, courseId))
                            },
                        )
                    }
                }
                composable(
                    route = Routes.IcibaCourse,
                    arguments = listOf(navArgument("classId") { type = NavType.StringType }),
                ) { entry ->
                    val classId = entry.arguments?.getString("classId").orEmpty()
                    OpaqueDestination {
                        IcibaCourseScreen(
                            classId = classId,
                            onBack = { navController.popBackStack() },
                            onLessonClick = { courseId ->
                                navController.navigate(Routes.icibaLesson(classId, courseId))
                            },
                        )
                    }
                }
                composable(
                    route = Routes.IcibaLesson,
                    arguments = listOf(
                        navArgument("classId") { type = NavType.StringType },
                        navArgument("courseId") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val classId = entry.arguments?.getString("classId").orEmpty()
                    val courseId = entry.arguments?.getString("courseId").orEmpty().toIntOrNull() ?: 0
                    OpaqueDestination {
                        IcibaLessonScreen(
                            classId = classId,
                            courseId = courseId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(
                    route = Routes.BdCourse,
                    arguments = listOf(navArgument("key") { type = NavType.StringType }),
                ) { entry ->
                    val key = Uri.decode(entry.arguments?.getString("key").orEmpty())
                    OpaqueDestination {
                        BeidanciCourseScreen(
                            courseKey = key,
                            onBack = { navController.popBackStack() },
                            onStudyClick = { courseKey, unitId ->
                                navController.navigate(Routes.bdStudy(courseKey, unitId))
                            },
                        )
                    }
                }
                composable(
                    route = Routes.BdStudy,
                    arguments = listOf(
                        navArgument("key") { type = NavType.StringType },
                        navArgument("unitId") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val key = Uri.decode(entry.arguments?.getString("key").orEmpty())
                    val unitId = entry.arguments?.getString("unitId").orEmpty().toIntOrNull() ?: 0
                    OpaqueDestination {
                        BeidanciStudyScreen(
                            courseKey = key,
                            unitId = unitId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(
                    route = Routes.GamePlay,
                    arguments = listOf(navArgument("gameId") { type = NavType.StringType }),
                ) { entry ->
                    val gameId = entry.arguments?.getString("gameId").orEmpty()
                    OpaqueDestination {
                        GamePlayScreen(
                            gameId = gameId,
                            onExit = { navController.popBackStack() },
                        )
                    }
                }
                composable(Routes.Tts) {
                    OpaqueDestination {
                        TtsScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.Ipa) {
                    OpaqueDestination {
                        IpaScreen(
                            onBack = { navController.popBackStack() },
                            onPhonemeClick = { navController.navigate(Routes.ipaDetail(it)) },
                        )
                    }
                }
                composable(
                    route = Routes.IpaDetail,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { entry ->
                    val id = Uri.decode(entry.arguments?.getString("id").orEmpty())
                    OpaqueDestination {
                        IpaDetailScreen(
                            phonemeId = id,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
                composable(Routes.Disclaimer) {
                    OpaqueDestination {
                        DisclaimerScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.History) {
                    OpaqueDestination {
                        HistoryScreen(
                            onBack = { navController.popBackStack() },
                            onOpen = { route -> navController.navigate(route) },
                        )
                    }
                }
                composable(Routes.Favorites) {
                    OpaqueDestination {
                        FavoritesScreen(
                            onBack = { navController.popBackStack() },
                            onOpen = { route -> navController.navigate(route) },
                        )
                    }
                }
                composable(Routes.About) {
                    OpaqueDestination {
                        AboutAppScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.Settings) {
                    OpaqueDestination {
                        SettingsScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.XyPodcast) {
                    OpaqueDestination {
                        XyPodcastScreen(
                            onBack = { navController.popBackStack() },
                            onEpisodeClick = { navController.navigate(Routes.xyEpisode(it)) },
                        )
                    }
                }
                composable(
                    route = Routes.XyEpisode,
                    arguments = listOf(navArgument("eid") { type = NavType.StringType }),
                ) {
                    OpaqueDestination {
                        XyEpisodeScreen(onBack = { navController.popBackStack() })
                    }
                }
                composable(Routes.EmList) {
                    OpaqueDestination {
                        EmListScreen(
                            onBack = { navController.popBackStack() },
                            onEpisodeClick = { navController.navigate(Routes.emEpisode(it)) },
                        )
                    }
                }
                composable(
                    route = Routes.EmEpisode,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType }),
                ) {
                    OpaqueDestination {
                        EmEpisodeScreen(onBack = { navController.popBackStack() })
                    }
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

/** 每个导航目的地都铺不透明底色，避免切换时透出上一页内容。 */
@Composable
private fun OpaqueDestination(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Paper),
    ) {
        content()
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
