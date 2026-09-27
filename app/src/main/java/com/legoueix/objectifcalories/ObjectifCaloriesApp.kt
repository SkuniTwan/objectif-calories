package com.legoueix.objectifcalories

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.legoueix.objectifcalories.activite.ActiviteScreen
import com.legoueix.objectifcalories.activite.CaloriesParSportScreen
import com.legoueix.objectifcalories.activite.ActiviteViewModel
import com.legoueix.objectifcalories.activite.ActiviteViewModelFactory
import com.legoueix.objectifcalories.activite.data.ActiviteRepository
import com.legoueix.objectifcalories.poids.data.PoidsRepository
import com.legoueix.objectifcalories.poids.PoidsScreen
import com.legoueix.objectifcalories.poids.rememberDernierPoidsKg
import com.legoueix.objectifcalories.poids.rememberHistoriquePoids
import com.legoueix.objectifcalories.analysis.MealAnalyzer
import com.legoueix.objectifcalories.bilan.BilanBottomNav
import com.legoueix.objectifcalories.bilan.BilanPeriode
import com.legoueix.objectifcalories.bilan.journalier.BilanJournalierScreen
import com.legoueix.objectifcalories.bilan.journalier.BilanJournalierViewModel
import com.legoueix.objectifcalories.bilan.journalier.BilanJournalierViewModelFactory
import com.legoueix.objectifcalories.bilan.hebdo.BilanHebdoScreen
import com.legoueix.objectifcalories.bilan.hebdo.BilanHebdoViewModel
import com.legoueix.objectifcalories.bilan.hebdo.BilanHebdoViewModelFactory
import com.legoueix.objectifcalories.bilan.mensuel.BilanMensuelScreen
import com.legoueix.objectifcalories.bilan.mensuel.BilanMensuelViewModel
import com.legoueix.objectifcalories.bilan.mensuel.BilanMensuelViewModelFactory
import com.legoueix.objectifcalories.capture.CameraScreen
import com.legoueix.objectifcalories.ciqual.AlimentRepository
import com.legoueix.objectifcalories.correction.CaptureUiState
import com.legoueix.objectifcalories.correction.CorrectionScreen
import com.legoueix.objectifcalories.correction.MealCaptureViewModel
import com.legoueix.objectifcalories.correction.MealCaptureViewModelFactory
import com.legoueix.objectifcalories.favoris.FavoriCreationScreen
import com.legoueix.objectifcalories.favoris.FavorisScreen
import com.legoueix.objectifcalories.favoris.FavorisViewModel
import com.legoueix.objectifcalories.favoris.FavorisViewModelFactory
import com.legoueix.objectifcalories.favoris.data.RepasFavoriRepository
import com.legoueix.objectifcalories.historique.data.RepasRepository
import com.legoueix.objectifcalories.hydratation.HydratationScreen
import com.legoueix.objectifcalories.hydratation.HydratationViewModel
import com.legoueix.objectifcalories.hydratation.HydratationViewModelFactory
import com.legoueix.objectifcalories.hydratation.data.HydratationRepository
import com.legoueix.objectifcalories.gallery.GalleryPickerScreen
import com.legoueix.objectifcalories.home.HomeScreen
import com.legoueix.objectifcalories.openfoodfacts.OpenFoodFactsRepository
import com.legoueix.objectifcalories.textentry.TextEntryScreen
import com.legoueix.objectifcalories.textentry.TextEntryViewModel
import com.legoueix.objectifcalories.textentry.TextEntryViewModelFactory
import com.legoueix.objectifcalories.ui.AppDrawerContent
import com.legoueix.objectifcalories.ui.AppDrawerViewModel
import com.legoueix.objectifcalories.ui.AppDrawerViewModelFactory
import com.legoueix.objectifcalories.ui.AppTopBar
import com.legoueix.objectifcalories.ui.LegalScreen
import com.legoueix.objectifcalories.voice.VoiceRecordingScreen
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val VOICE = "voice"
    const val TEXT = "text"
    const val GALLERY = "gallery"
    const val FAVORIS = "favoris"
    const val FAVORI_CREATION = "favori_creation"
    const val EAU = "eau"
    const val BILAN_JOURNALIER = "bilan_journalier"
    const val BILAN_HEBDO = "bilan_hebdo"
    const val BILAN_MENSUEL = "bilan_mensuel"
    const val CALORIES_SPORT = "calories_sport"
    const val POIDS = "poids"
    const val ACTIVITES_SPORTIVES = "activites_sportives"
    const val MENTIONS_LEGALES = "mentions_legales"
}

@Composable
fun ObjectifCaloriesApp(analyzer: MealAnalyzer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = LocalContext.current
    val openFoodFactsRepository = remember { OpenFoodFactsRepository() }
    val repasRepository = remember { RepasRepository(context.applicationContext) }
    val alimentRepository = remember { AlimentRepository(context.applicationContext) }
    val viewModel: MealCaptureViewModel = viewModel(
        factory = MealCaptureViewModelFactory(analyzer, alimentRepository, openFoodFactsRepository, repasRepository),
    )
    val captureUiState by viewModel.uiState.collectAsState()

    val textEntryViewModel: TextEntryViewModel = viewModel(factory = TextEntryViewModelFactory(alimentRepository))
    val repasFavoriRepository = remember { RepasFavoriRepository(context.applicationContext) }
    val hydratationRepository = remember { HydratationRepository(context.applicationContext) }
    val activiteRepository = remember { ActiviteRepository(context.applicationContext) }
    val poidsRepository = remember { PoidsRepository(context.applicationContext) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val validatedMessage = stringResource(R.string.correction_validated_confirmation)
    val eauValidatedMessage = stringResource(R.string.eau_validated_confirmation)
    val activiteValidatedMessage = stringResource(R.string.activite_validated_confirmation)
    val poidsValidatedMessage = stringResource(R.string.poids_validated_confirmation)

    // En mode caméra, le bandeau ne s'affiche que sur l'écran de correction (capture immersive sinon).
    val showTopBar = currentRoute != Routes.CAMERA || captureUiState is CaptureUiState.Result

    val routesBilan = setOf(Routes.BILAN_JOURNALIER, Routes.BILAN_HEBDO, Routes.BILAN_MENSUEL)

    fun navigateFromDrawer(route: String) {
        navController.navigate(route) { launchSingleTop = true }
        coroutineScope.launch { drawerState.close() }
    }

    fun navigateBilan(route: String) {
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(Routes.BILAN_JOURNALIER) { saveState = true }
        }
    }

    val appDrawerViewModel: AppDrawerViewModel = viewModel(
        factory = AppDrawerViewModelFactory(repasRepository, hydratationRepository, activiteRepository, poidsRepository),
    )
    val drawerUiState by appDrawerViewModel.uiState.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                eauClAujourdhui = drawerUiState.eauClAujourdhui,
                kcalSeanceAujourdhui = drawerUiState.kcalActiviteAujourdhui,
                dernierPoidsKg = drawerUiState.dernierPoidsKg,
                deltaPoidsKg = drawerUiState.deltaPoidsKg,
                kcalConsommesSemaine = drawerUiState.kcalConsommesSemaine,
                onOpenEnregistrerRepas = { navigateFromDrawer(Routes.HOME) },
                onOpenEau = { navigateFromDrawer(Routes.EAU) },
                onOpenBilan = { navigateFromDrawer(Routes.BILAN_JOURNALIER) },
                onOpenCaloriesSport = { navigateFromDrawer(Routes.CALORIES_SPORT) },
                onOpenPoids = { navigateFromDrawer(Routes.POIDS) },
                onOpenActivitesSportives = { navigateFromDrawer(Routes.ACTIVITES_SPORTIVES) },
                onOpenMentionsLegales = { navigateFromDrawer(Routes.MENTIONS_LEGALES) },
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showTopBar) {
                    AppTopBar(
                        onOpenMenu = { coroutineScope.launch { drawerState.open() } },
                    )
                }
            },
            bottomBar = {
                if (currentRoute in routesBilan) {
                    BilanBottomNav(
                        selection = when (currentRoute) {
                            Routes.BILAN_HEBDO -> BilanPeriode.SEMAINE
                            Routes.BILAN_MENSUEL -> BilanPeriode.MOIS
                            else -> BilanPeriode.JOUR
                        },
                        onSelectionChange = { periode ->
                            navigateBilan(
                                when (periode) {
                                    BilanPeriode.JOUR -> Routes.BILAN_JOURNALIER
                                    BilanPeriode.SEMAINE -> Routes.BILAN_HEBDO
                                    BilanPeriode.MOIS -> Routes.BILAN_MENSUEL
                                },
                            )
                        },
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            val screenModifier = Modifier.padding(paddingValues)
            NavHost(navController = navController, startDestination = Routes.HOME) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onOpenPhotoMode = { navController.navigate(Routes.CAMERA) },
                        onOpenVoiceMode = { navController.navigate(Routes.VOICE) },
                        onOpenTextMode = { navController.navigate(Routes.TEXT) },
                        onOpenGalleryMode = { navController.navigate(Routes.GALLERY) },
                        onOpenFavorisMode = { navController.navigate(Routes.FAVORIS) },
                        modifier = screenModifier,
                    )
                }
                composable(Routes.CAMERA) {
                    // Repart toujours d'un état propre en quittant ce mode, quelle que soit la façon
                    // dont on en sort (bouton fermer, retour système, ou validation du repas).
                    DisposableEffect(Unit) {
                        onDispose { viewModel.onRetakePhoto() }
                    }
                    when (val state = captureUiState) {
                        is CaptureUiState.Result -> CorrectionScreen(
                            result = state,
                            onGrammesCorriges = viewModel::onGrammesCorriges,
                            onValidate = { nom, dateHeure ->
                                viewModel.onValidate(nom, dateHeure)
                                coroutineScope.launch { snackbarHostState.showSnackbar(validatedMessage) }
                                navController.popBackStack()
                            },
                            onRetakePhoto = viewModel::onRetakePhoto,
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                        else -> CameraScreen(
                            uiState = state,
                            onPhotoCaptured = { photo -> viewModel.onPhotoCaptured(photo) },
                            onCodeBarresDetecte = viewModel::onCodeBarresDetecte,
                            onClose = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                    }
                }
                composable(Routes.VOICE) {
                    // Même nettoyage qu'en mode caméra : repartir d'un état propre en quittant.
                    DisposableEffect(Unit) {
                        onDispose {
                            viewModel.onRetakePhoto()
                            textEntryViewModel.reinitialiser()
                        }
                    }
                    when (val state = captureUiState) {
                        is CaptureUiState.Result -> CorrectionScreen(
                            result = state,
                            onGrammesCorriges = viewModel::onGrammesCorriges,
                            onValidate = { nom, dateHeure ->
                                viewModel.onValidate(nom, dateHeure)
                                coroutineScope.launch { snackbarHostState.showSnackbar(validatedMessage) }
                                navController.popBackStack()
                            },
                            onRetakePhoto = viewModel::onRetakePhoto,
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                        else -> {
                            var dicteeTerminee by rememberSaveable { mutableStateOf(false) }
                            if (dicteeTerminee) {
                                TextEntryScreen(
                                    viewModel = textEntryViewModel,
                                    onValidated = { items -> viewModel.onManualEntryValidated(items) },
                                    onAbandon = { navController.popBackStack() },
                                    modifier = screenModifier,
                                )
                            } else {
                                VoiceRecordingScreen(
                                    onDicteeTerminee = { paires ->
                                        textEntryViewModel.definirDepuisDictee(paires)
                                        dicteeTerminee = true
                                    },
                                    onEcrireManuel = { dicteeTerminee = true },
                                    onAbandon = { navController.popBackStack() },
                                    modifier = screenModifier,
                                )
                            }
                        }
                    }
                }
                composable(Routes.TEXT) {
                    // Même nettoyage qu'en mode caméra : repartir d'un état propre en quittant.
                    DisposableEffect(Unit) {
                        onDispose {
                            viewModel.onRetakePhoto()
                            textEntryViewModel.reinitialiser()
                        }
                    }
                    when (val state = captureUiState) {
                        is CaptureUiState.Result -> CorrectionScreen(
                            result = state,
                            onGrammesCorriges = viewModel::onGrammesCorriges,
                            onValidate = { nom, dateHeure ->
                                viewModel.onValidate(nom, dateHeure)
                                coroutineScope.launch { snackbarHostState.showSnackbar(validatedMessage) }
                                navController.popBackStack()
                            },
                            onRetakePhoto = viewModel::onRetakePhoto,
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                        else -> TextEntryScreen(
                            viewModel = textEntryViewModel,
                            onValidated = { items -> viewModel.onManualEntryValidated(items) },
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                    }
                }
                composable(Routes.GALLERY) {
                    // Même nettoyage qu'en mode caméra : repartir d'un état propre en quittant.
                    DisposableEffect(Unit) {
                        onDispose { viewModel.onRetakePhoto() }
                    }
                    when (val state = captureUiState) {
                        is CaptureUiState.Result -> CorrectionScreen(
                            result = state,
                            onGrammesCorriges = viewModel::onGrammesCorriges,
                            onValidate = { nom, dateHeure ->
                                viewModel.onValidate(nom, dateHeure)
                                coroutineScope.launch { snackbarHostState.showSnackbar(validatedMessage) }
                                navController.popBackStack()
                            },
                            onRetakePhoto = viewModel::onRetakePhoto,
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                        else -> GalleryPickerScreen(
                            uiState = state,
                            onImport = { photo, nom -> viewModel.onPhotoCaptured(photo, nom) },
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                    }
                }
                composable(Routes.FAVORIS) {
                    // Même nettoyage qu'en mode caméra : repartir d'un état propre en quittant.
                    DisposableEffect(Unit) {
                        onDispose { viewModel.onRetakePhoto() }
                    }
                    when (val state = captureUiState) {
                        is CaptureUiState.Result -> CorrectionScreen(
                            result = state,
                            onGrammesCorriges = viewModel::onGrammesCorriges,
                            onValidate = { nom, dateHeure ->
                                viewModel.onValidate(nom, dateHeure)
                                coroutineScope.launch { snackbarHostState.showSnackbar(validatedMessage) }
                                navController.popBackStack()
                            },
                            onRetakePhoto = viewModel::onRetakePhoto,
                            onAbandon = { navController.popBackStack() },
                            modifier = screenModifier,
                        )
                        else -> {
                            val favorisViewModel: FavorisViewModel =
                                viewModel(factory = FavorisViewModelFactory(repasFavoriRepository, alimentRepository))
                            val favoris by favorisViewModel.favoris.collectAsState()
                            FavorisScreen(
                                favoris = favoris,
                                onRepasChoisi = { repas ->
                                    coroutineScope.launch {
                                        // Retrouve la fiche Ciqual exacte de chaque aliment du favori
                                        // via son code — jamais via son nom (voir MealCaptureViewModel).
                                        val items = repas.items.mapNotNull { item ->
                                            alimentRepository.parCode(item.codeCiqual)?.let { it to item.grammes }
                                        }
                                        viewModel.onManualEntryValidated(items, repas.photo, repas.nom)
                                    }
                                },
                                onSupprimer = { repas -> favorisViewModel.onSupprimer(repas.id) },
                                onAjouterRepas = { navController.navigate(Routes.FAVORI_CREATION) },
                                onAbandon = { navController.popBackStack() },
                                modifier = screenModifier,
                            )
                        }
                    }
                }
                composable(Routes.FAVORI_CREATION) {
                    val creationLigneViewModel: TextEntryViewModel =
                        viewModel(factory = TextEntryViewModelFactory(alimentRepository))
                    FavoriCreationScreen(
                        ligneViewModel = creationLigneViewModel,
                        onEnregistrer = { nom, photo, items ->
                            coroutineScope.launch {
                                repasFavoriRepository.enregistrer(nom = nom, photo = photo, items = items)
                                navController.popBackStack()
                            }
                        },
                        onAbandon = { navController.popBackStack() },
                        modifier = screenModifier,
                    )
                }
                composable(Routes.EAU) {
                    val hydratationViewModel: HydratationViewModel = viewModel(
                        factory = HydratationViewModelFactory(hydratationRepository),
                    )
                    HydratationScreen(
                        viewModel = hydratationViewModel,
                        onEnregistrer = { centilitres, dateHeure ->
                            coroutineScope.launch { hydratationRepository.enregistrer(dateHeure, centilitres) }
                            coroutineScope.launch { snackbarHostState.showSnackbar(eauValidatedMessage) }
                        },
                        onAbandon = { navController.popBackStack() },
                        modifier = screenModifier,
                    )
                }
                composable(Routes.BILAN_JOURNALIER) {
                    val bilanJournalierViewModel: BilanJournalierViewModel = viewModel(
                        factory = BilanJournalierViewModelFactory(repasRepository, hydratationRepository, activiteRepository, poidsRepository),
                    )
                    BilanJournalierScreen(
                        viewModel = bilanJournalierViewModel,
                        modifier = screenModifier,
                    )
                }
                composable(Routes.BILAN_HEBDO) {
                    val bilanHebdoViewModel: BilanHebdoViewModel = viewModel(
                        factory = BilanHebdoViewModelFactory(repasRepository, hydratationRepository, activiteRepository),
                    )
                    BilanHebdoScreen(
                        viewModel = bilanHebdoViewModel,
                        modifier = screenModifier,
                    )
                }
                composable(Routes.BILAN_MENSUEL) {
                    val bilanMensuelViewModel: BilanMensuelViewModel = viewModel(
                        factory = BilanMensuelViewModelFactory(repasRepository, hydratationRepository, activiteRepository),
                    )
                    BilanMensuelScreen(
                        viewModel = bilanMensuelViewModel,
                        modifier = screenModifier,
                    )
                }
                composable(Routes.CALORIES_SPORT) {
                    val dernierPoidsKg by rememberDernierPoidsKg(poidsRepository)
                    CaloriesParSportScreen(
                        dernierPoidsKg = dernierPoidsKg,
                        onBack = { navController.popBackStack() },
                        modifier = screenModifier,
                    )
                }
                composable(Routes.POIDS) {
                    val historiquePoids by rememberHistoriquePoids(poidsRepository)
                    PoidsScreen(
                        dernierPoidsKg = historiquePoids.firstOrNull()?.poidsKg,
                        historique = historiquePoids,
                        onEnregistrer = { poidsKg, dateHeure ->
                            coroutineScope.launch { poidsRepository.enregistrer(poidsKg, dateHeure) }
                            coroutineScope.launch { snackbarHostState.showSnackbar(poidsValidatedMessage) }
                        },
                        onSupprimer = { id ->
                            coroutineScope.launch { poidsRepository.supprimer(id) }
                        },
                        onAbandon = { navController.popBackStack() },
                        modifier = screenModifier,
                    )
                }
                composable(Routes.ACTIVITES_SPORTIVES) {
                    val activiteViewModel: ActiviteViewModel = viewModel(
                        factory = ActiviteViewModelFactory(activiteRepository, poidsRepository),
                    )
                    val dernierPoidsKg by activiteViewModel.dernierPoidsKg.collectAsState()
                    val activitesAujourdhui by activiteViewModel.activitesAujourdhui.collectAsState()
                    ActiviteScreen(
                        dernierPoidsKg = dernierPoidsKg,
                        activitesAujourdhui = activitesAujourdhui,
                        onEnregistrerActivite = { activite, dureeMinutes, dateHeure ->
                            activiteViewModel.onEnregistrerActivite(activite, dureeMinutes, dateHeure)
                            coroutineScope.launch { snackbarHostState.showSnackbar(activiteValidatedMessage) }
                        },
                        onSupprimerActivite = { id -> activiteViewModel.onSupprimerActivite(id) },
                        onAbandon = { navController.popBackStack() },
                        modifier = screenModifier,
                    )
                }
                composable(Routes.MENTIONS_LEGALES) {
                    LegalScreen(modifier = screenModifier)
                }
            }
        }
    }
}
