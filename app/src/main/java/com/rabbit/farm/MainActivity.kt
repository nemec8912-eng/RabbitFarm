package com.rabbit.farm

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

enum class AppTab {
    CURRENT,
    ADULTS,
    YOUNG,
    SOLD,
    MEAT,
    DEATH,
    MATINGS,
    ALL_TIME
}

enum class AdultFilter {
    ALL,
    FEMALE,
    MALE
}

enum class AdultSort {
    NORMAL,
    YOUNGEST,
    OLDEST
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RabbitFarmApp()
        }
    }
}

@Composable
fun RabbitFarmApp() {

    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences(
            "rabbit_prefs",
            Context.MODE_PRIVATE
        )
    }

    var selectedTab by remember {
        mutableStateOf(AppTab.CURRENT)
    }

    var adultFilter by remember {
        mutableStateOf(AdultFilter.ALL)
    }

    var adultSort by remember {
        mutableStateOf(AdultSort.NORMAL)
    }

    var adultSearch by remember {
        mutableStateOf("")
    }

    // =====================================================
    // ОКНА
    // =====================================================

    var showAddRabbit by remember {
        mutableStateOf(false)
    }

    var showEditRabbit by remember {
        mutableStateOf(false)
    }

    var showMatingDialog by remember {
        mutableStateOf(false)
    }

    var showYoungDialog by remember {
        mutableStateOf(false)
    }

    var showYoungMeatDialog by remember {
        mutableStateOf(false)
    }

    var showYoungDeathDialog by remember {
        mutableStateOf(false)
    }

    var showAdultSaleDialog by remember {
        mutableStateOf(false)
    }

    var showYoungSaleDialog by remember {
        mutableStateOf(false)
    }

    // =====================================================
    // НОВЫЙ КРОЛИК
    // =====================================================

    var rabbitName by remember {
        mutableStateOf("")
    }

    var rabbitGender by remember {
        mutableStateOf("Самка")
    }

    var rabbitBirthDate by remember {
        mutableStateOf("")
    }

    var rabbitPhotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var rabbitNotes by remember {
        mutableStateOf("")
    }

    // =====================================================
    // РЕДАКТИРОВАНИЕ
    // =====================================================

    var editingRabbitOriginal by remember {
        mutableStateOf("")
    }

    var editRabbitName by remember {
        mutableStateOf("")
    }

    var editRabbitGender by remember {
        mutableStateOf("Самка")
    }

    var editRabbitBirthDate by remember {
        mutableStateOf("")
    }

    var editRabbitPhotoUri by remember {
        mutableStateOf<Uri?>(null)
    }

    var editRabbitNotes by remember {
        mutableStateOf("")
    }

    // =====================================================
    // ФОТО
    // =====================================================

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {

            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            rabbitPhotoUri = uri
        }
    }

    val editPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        if (uri != null) {

            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }

            editRabbitPhotoUri = uri
        }
    }

    // =====================================================
    // ВЯЗКА
    // =====================================================

    var matingFemale by remember {
        mutableStateOf("")
    }

    var matingMale by remember {
        mutableStateOf("")
    }

    var matingDate by remember {
        mutableStateOf("")
    }

    // =====================================================
    // МОЛОДНЯК
    // =====================================================

    var youngMother by remember {
        mutableStateOf("")
    }

    // Родительская пара берётся из выбранной вязки.
    var youngFather by remember { mutableStateOf("") }
    var selectedMatingId by remember { mutableStateOf("") }

    var youngBirthDate by remember {
        mutableStateOf("")
    }

    var youngCountText by remember {
        mutableStateOf("")
    }

    var selectedYoungItem by remember {
        mutableStateOf("")
    }

    var youngMeatCountText by remember {
        mutableStateOf("")
    }

    var youngDeathCountText by remember {
        mutableStateOf("")
    }

    // =====================================================
    // ПРОДАЖА ВЗРОСЛОГО
    // =====================================================

    var selectedAdultSaleRabbit by remember {
        mutableStateOf("")
    }

    var adultSaleBuyer by remember {
        mutableStateOf("")
    }

    var adultSaleDate by remember {
        mutableStateOf("")
    }

    var adultSalePrice by remember {
        mutableStateOf("")
    }

    // =====================================================
    // ПРОДАЖА МОЛОДНЯКА
    // =====================================================

    var youngSaleCount by remember {
        mutableStateOf("")
    }

    var youngSaleBuyer by remember {
        mutableStateOf("")
    }

    var youngSaleDate by remember {
        mutableStateOf("")
    }

    var youngSalePrice by remember {
        mutableStateOf("")
    }

    // =====================================================
    // ДАННЫЕ
    // =====================================================

    var rabbitList by remember {
        mutableStateOf(
            prefs.getStringSet(
                "rabbit_list",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    var adultMeatList by remember {
        mutableStateOf(
            prefs.getStringSet(
                "meat_list",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    var adultDeathList by remember {
        mutableStateOf(
            prefs.getStringSet(
                "adult_death_list",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    var matingList by remember {
        mutableStateOf(
            prefs.getStringSet(
                "mating_list",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    // id | matingId | mother | father | birthDate | bornCount
    // История окролов остаётся даже после выбытия молодняка.
    var kindlingHistory by remember {
        mutableStateOf(
            prefs.getStringSet("kindling_history", emptySet())?.toList() ?: emptyList()
        )
    }

    var youngList by remember {
        mutableStateOf(
            prefs.getStringSet(
                "young_list",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    var youngMeatTotal by remember {
        mutableStateOf(
            prefs.getInt(
                "young_meat_total",
                0
            )
        )
    }

    var youngDeathTotal by remember {
        mutableStateOf(
            prefs.getInt(
                "young_death_total",
                0
            )
        )
    }

    var youngDeathHistory by remember {
        mutableStateOf(
            prefs.getStringSet(
                "young_death_history",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    var adultSales by remember {
        mutableStateOf(
            prefs.getStringSet(
                "adult_sales",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    var youngSales by remember {
        mutableStateOf(
            prefs.getStringSet(
                "young_sales",
                emptySet()
            )?.toList() ?: emptyList()
        )
    }

    // =====================================================
    // САМКИ И САМЦЫ
    // =====================================================

    val femaleNames = rabbitList.mapNotNull { rabbit ->

        val parts = rabbit.split("|")
        val name = parts.getOrElse(0) { "" }
        val gender = parts.getOrElse(1) { "" }

        if (
            gender == "Самка" &&
            name.isNotBlank()
        ) {
            name
        } else {
            null
        }
    }

    val maleNames = rabbitList.mapNotNull { rabbit ->

        val parts = rabbit.split("|")
        val name = parts.getOrElse(0) { "" }
        val gender = parts.getOrElse(1) { "" }

        if (
            gender == "Самец" &&
            name.isNotBlank()
        ) {
            name
        } else {
            null
        }
    }

    // =====================================================
    // ПОИСК И СОРТИРОВКА
    // =====================================================

    val filteredAdultList =
        rabbitList
            .filter { rabbit ->

                val parts =
                    rabbit.split("|")

                val name =
                    parts.getOrElse(0) { "" }

                val gender =
                    parts.getOrElse(1) { "" }

                val genderOk =
                    when (adultFilter) {

                        AdultFilter.ALL ->
                            true

                        AdultFilter.FEMALE ->
                            gender == "Самка"

                        AdultFilter.MALE ->
                            gender == "Самец"
                    }

                val searchOk =
                    name.contains(
                        adultSearch.trim(),
                        ignoreCase = true
                    )

                genderOk &&
                        searchOk
            }
            .let { list ->

                when (adultSort) {

                    AdultSort.NORMAL ->
                        list

                    AdultSort.YOUNGEST ->
                        list.sortedByDescending { rabbit ->

                            parseRabbitDate(
                                rabbit
                                    .split("|")
                                    .getOrElse(2) { "" }
                            ) ?: LocalDate.MIN
                        }

                    AdultSort.OLDEST ->
                        list.sortedBy { rabbit ->

                            parseRabbitDate(
                                rabbit
                                    .split("|")
                                    .getOrElse(2) { "" }
                            ) ?: LocalDate.MAX
                        }
                }
            }

    // =====================================================
    // СТАТИСТИКА
    // =====================================================

    val youngTotal =
        youngList.sumOf { item ->

            item
                .split("|")
                .getOrNull(3)
                ?.toIntOrNull()
                ?: 0
        }

    val adultNow =
        rabbitList.size

    val adultMeatTotal =
        adultMeatList.size

    val adultDeathTotal =
        adultDeathList.size

    val adultSoldTotal =
        adultSales.size

    val youngSoldTotal =
        youngSales.sumOf { item ->

            item
                .split("|")
                .getOrNull(3)
                ?.toIntOrNull()
                ?: 0
        }

    val soldTotal =
        adultSoldTotal +
                youngSoldTotal

    val meatTotal =
        adultMeatTotal +
                youngMeatTotal

    val deathTotal =
        adultDeathTotal +
                youngDeathTotal

    val totalNow =
        adultNow +
                youngTotal

    val adultIncome =
        adultSales.sumOf { item ->

            item
                .split("|")
                .getOrNull(6)
                ?.toDoubleOrNull()
                ?: 0.0
        }

    val youngIncome =
        youngSales.sumOf { item ->

            item
                .split("|")
                .getOrNull(6)
                ?.toDoubleOrNull()
                ?: 0.0
        }

    val totalIncome =
        adultIncome +
                youngIncome

    val allTime =
        totalNow +
                soldTotal +
                meatTotal +
                deathTotal

    // =====================================================
    // ОТСАДКА ПОСЛЕ 45 ДНЕЙ
    // =====================================================

    val weaningDueGroups =
        youngList.filter { item ->

            val parts =
                item.split("|")

            val date =
                parts.getOrElse(2) { "" }

            val separated =
                parts.getOrElse(
                    4
                ) { "0" } == "1"

            val days =
                calculateAgeDays(
                    date
                )

            days != null &&
                    days >= 45 &&
                    !separated
        }

    // =====================================================
    // ЦВЕТА
    // =====================================================

    val currentColor =
        Color(0xFFA7D8FF)

    val currentBorder =
        Color(0xFF1565C0)

    val adultColor =
        Color(0xFFA9E6A4)

    val adultBorder =
        Color(0xFF2E7D32)

    val youngColor =
        Color(0xFFFFC970)

    val youngBorder =
        Color(0xFFEF6C00)

    val soldColor =
        Color(0xFF9FE3CF)

    val soldBorder =
        Color(0xFF00856A)

    val meatColor =
        Color(0xFFFF9C9C)

    val meatBorder =
        Color(0xFFC62828)

    val deathColor =
        Color(0xFFD895A1)

    val deathBorder =
        Color(0xFF7B1F2B)

    val matingColor =
        Color(0xFFD1A5F3)

    val matingBorder =
        Color(0xFF6A1B9A)

    val historyColor =
        Color(0xFF82D5E0)

    val historyBorder =
        Color(0xFF007C91)

    // =====================================================
    // РЕДАКТИРОВАНИЕ
    // =====================================================

    fun openRabbitEditor(
        rabbit: String
    ) {

        val parts =
            rabbit.split("|")

        editingRabbitOriginal =
            rabbit

        editRabbitName =
            parts.getOrElse(
                0
            ) { "" }

        editRabbitGender =
            parts.getOrElse(
                1
            ) { "Самка" }

        editRabbitBirthDate =
            parts.getOrElse(
                2
            ) { "" }

        val photo =
            parts.getOrElse(
                4
            ) { "" }

        editRabbitNotes =
            parts.getOrElse(
                5
            ) { "" }

        editRabbitPhotoUri =
            if (
                photo.isNotBlank()
            ) {
                Uri.parse(photo)
            } else {
                null
            }

        showEditRabbit =
            true
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF236B4D),
            secondary = Color(0xFF497C5A),
            background = Color(0xFFF8FAF6),
            surface = Color(0xFFF8FAF6)
        )
    ) {

        Surface(
            modifier =
                Modifier.fillMaxSize()
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(14.dp)
            ) {

                Text(
                    text =
                        "Ушастый учёт",
                    style =
                        MaterialTheme
                            .typography
                            .headlineMedium,
                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F3E9)),
                    border = BorderStroke(1.dp, Color(0xFFA8D0B3))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Племенной учёт", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Кролики  →  Вязка  →  Окрол  →  Молодняк")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Вязок: ${matingList.size} · Записано окролов: ${kindlingHistory.size}")
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { selectedTab = AppTab.MATINGS },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Открыть историю разведения") }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                // =================================================
                // НАПОМИНАНИЕ ОБ ОТСАДКЕ
                // =================================================

                if (
                    weaningDueGroups.isNotEmpty()
                ) {

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(8.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFFFFB74D)
                            ),

                        border =
                            BorderStroke(
                                2.dp,
                                Color(0xFFE65100)
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(14.dp)
                        ) {

                            Text(
                                text =
                                    "⚠ Пора отсаживать молодняк",
                                style =
                                    MaterialTheme
                                        .typography
                                        .titleMedium,
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(8.dp)
                            )

                            weaningDueGroups.forEach { young ->

                                val parts =
                                    young.split("|")

                                val mother =
                                    parts.getOrElse(
                                        1
                                    ) { "" }

                                val date =
                                    parts.getOrElse(
                                        2
                                    ) { "" }

                                val count =
                                    parts
                                        .getOrElse(
                                            3
                                        ) { "0" }
                                        .toIntOrNull()
                                        ?: 0

                                val days =
                                    calculateAgeDays(
                                        date
                                    ) ?: 0

                                Card(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(
                                                bottom = 8.dp
                                            ),

                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor =
                                                Color(
                                                    0xFFFFD699
                                                )
                                        ),

                                    shape =
                                        RoundedCornerShape(
                                            6.dp
                                        ),

                                    border =
                                        BorderStroke(
                                            1.dp,
                                            Color(
                                                0xFFE65100
                                            )
                                        )
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.padding(
                                                10.dp
                                            )
                                    ) {

                                        Text(
                                            text =
                                                "Мать: $mother",
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            "Дата окрола: $date"
                                        )

                                        Text(
                                            text =
                                                "Количество: $count",
                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        Text(
                                            "Возраст: $days дней"
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    selectedTab =
                                        AppTab.YOUNG
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                shape =
                                    RoundedCornerShape(
                                        8.dp
                                    )
                            ) {

                                Text(
                                    "Открыть молодняк"
                                )
                            }
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )
                }

                CategoryCard(
                    "Сейчас в хозяйстве",
                    totalNow.toString(),
                    selectedTab == AppTab.CURRENT,
                    currentColor,
                    currentBorder
                ) {
                    selectedTab =
                        AppTab.CURRENT
                }

                CategoryCard(
                    "Взрослые",
                    adultNow.toString(),
                    selectedTab == AppTab.ADULTS,
                    adultColor,
                    adultBorder
                ) {
                    selectedTab =
                        AppTab.ADULTS
                }

                CategoryCard(
                    "Молодняк",
                    youngTotal.toString(),
                    selectedTab == AppTab.YOUNG,
                    youngColor,
                    youngBorder
                ) {
                    selectedTab =
                        AppTab.YOUNG
                }

                CategoryCard(
                    "Продано",
                    soldTotal.toString(),
                    selectedTab == AppTab.SOLD,
                    soldColor,
                    soldBorder
                ) {
                    selectedTab =
                        AppTab.SOLD
                }

                CategoryCard(
                    "Ушло на мясо",
                    meatTotal.toString(),
                    selectedTab == AppTab.MEAT,
                    meatColor,
                    meatBorder
                ) {
                    selectedTab =
                        AppTab.MEAT
                }

                CategoryCard(
                    "Падёж",
                    deathTotal.toString(),
                    selectedTab == AppTab.DEATH,
                    deathColor,
                    deathBorder
                ) {
                    selectedTab =
                        AppTab.DEATH
                }

                CategoryCard(
                    "Вязки",
                    matingList.size.toString(),
                    selectedTab == AppTab.MATINGS,
                    matingColor,
                    matingBorder
                ) {
                    selectedTab =
                        AppTab.MATINGS
                }

                CategoryCard(
                    "Всего за всё время",
                    allTime.toString(),
                    selectedTab == AppTab.ALL_TIME,
                    historyColor,
                    historyBorder
                ) {
                    selectedTab =
                        AppTab.ALL_TIME
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                // =================================================
                // СЕЙЧАС В ХОЗЯЙСТВЕ
                // =================================================

                if (
                    selectedTab ==
                    AppTab.CURRENT
                ) {

                    SectionTitle(
                        "Взрослые"
                    )

                    rabbitList.forEach { rabbit ->

                        RabbitCard(
                            rabbit = rabbit,

                            onEdit = {
                                openRabbitEditor(
                                    rabbit
                                )
                            },

                            onSale = {

                                selectedAdultSaleRabbit =
                                    rabbit

                                adultSaleBuyer =
                                    ""

                                adultSaleDate =
                                    ""

                                adultSalePrice =
                                    ""

                                showAdultSaleDialog =
                                    true
                            },

                            onMeat = {

                                val rabbits =
                                    rabbitList.filter {
                                        it != rabbit
                                    }

                                val meat =
                                    adultMeatList +
                                            rabbit

                                rabbitList =
                                    rabbits

                                adultMeatList =
                                    meat

                                prefs.edit()
                                    .putStringSet(
                                        "rabbit_list",
                                        rabbits.toSet()
                                    )
                                    .putStringSet(
                                        "meat_list",
                                        meat.toSet()
                                    )
                                    .apply()
                            },

                            onDeath = {

                                val rabbits =
                                    rabbitList.filter {
                                        it != rabbit
                                    }

                                val death =
                                    adultDeathList +
                                            "$rabbit|${System.currentTimeMillis()}"

                                rabbitList =
                                    rabbits

                                adultDeathList =
                                    death

                                prefs.edit()
                                    .putStringSet(
                                        "rabbit_list",
                                        rabbits.toSet()
                                    )
                                    .putStringSet(
                                        "adult_death_list",
                                        death.toSet()
                                    )
                                    .apply()
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    SectionTitle(
                        "Молодняк"
                    )

                    youngList.forEach { young ->

                        YoungCard(
                            young = young,

                            onSale = {

                                selectedYoungItem =
                                    young

                                youngSaleCount =
                                    ""

                                youngSaleBuyer =
                                    ""

                                youngSaleDate =
                                    ""

                                youngSalePrice =
                                    ""

                                showYoungSaleDialog =
                                    true
                            },

                            onMeat = {

                                selectedYoungItem =
                                    young

                                youngMeatCountText =
                                    ""

                                showYoungMeatDialog =
                                    true
                            },

                            onDeath = {

                                selectedYoungItem =
                                    young

                                youngDeathCountText =
                                    ""

                                showYoungDeathDialog =
                                    true
                            },

                            onSeparated = {

                                val updated =
                                    updateYoungSeparated(
                                        youngList,
                                        young
                                    )

                                youngList =
                                    updated

                                prefs.edit()
                                    .putStringSet(
                                        "young_list",
                                        updated.toSet()
                                    )
                                    .apply()
                            }
                        )
                    }
                }

                // =================================================
                // ВЗРОСЛЫЕ
                // =================================================

                if (
                    selectedTab ==
                    AppTab.ADULTS
                ) {

                    SectionTitle(
                        "Взрослые кролики"
                    )

                    OutlinedTextField(
                        value =
                            adultSearch,
                        onValueChange = {
                            adultSearch =
                                it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Поиск по кличке"
                            )
                        },
                        singleLine =
                            true
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        "Пол",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Row {

                        FilterChip(
                            selected =
                                adultFilter ==
                                        AdultFilter.ALL,
                            onClick = {
                                adultFilter =
                                    AdultFilter.ALL
                            },
                            label = {
                                Text("Все")
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        FilterChip(
                            selected =
                                adultFilter ==
                                        AdultFilter.FEMALE,
                            onClick = {
                                adultFilter =
                                    AdultFilter.FEMALE
                            },
                            label = {
                                Text("Самки")
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        FilterChip(
                            selected =
                                adultFilter ==
                                        AdultFilter.MALE,
                            onClick = {
                                adultFilter =
                                    AdultFilter.MALE
                            },
                            label = {
                                Text("Самцы")
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        "Возраст",
                        fontWeight =
                            FontWeight.Bold
                    )

                    FilterChip(
                        selected =
                            adultSort ==
                                    AdultSort.NORMAL,
                        onClick = {
                            adultSort =
                                AdultSort.NORMAL
                        },
                        label = {
                            Text(
                                "Обычный порядок"
                            )
                        }
                    )

                    Row {

                        FilterChip(
                            selected =
                                adultSort ==
                                        AdultSort.YOUNGEST,
                            onClick = {
                                adultSort =
                                    AdultSort.YOUNGEST
                            },
                            label = {
                                Text(
                                    "Младшие"
                                )
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        FilterChip(
                            selected =
                                adultSort ==
                                        AdultSort.OLDEST,
                            onClick = {
                                adultSort =
                                    AdultSort.OLDEST
                            },
                            label = {
                                Text(
                                    "Старшие"
                                )
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text =
                            "Найдено: ${filteredAdultList.size}",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    filteredAdultList.forEach { rabbit ->

                        RabbitCard(
                            rabbit = rabbit,

                            onEdit = {
                                openRabbitEditor(
                                    rabbit
                                )
                            },

                            onSale = {

                                selectedAdultSaleRabbit =
                                    rabbit

                                adultSaleBuyer =
                                    ""

                                adultSaleDate =
                                    ""

                                adultSalePrice =
                                    ""

                                showAdultSaleDialog =
                                    true
                            },

                            onMeat = {

                                val rabbits =
                                    rabbitList.filter {
                                        it != rabbit
                                    }

                                val meat =
                                    adultMeatList +
                                            rabbit

                                rabbitList =
                                    rabbits

                                adultMeatList =
                                    meat

                                prefs.edit()
                                    .putStringSet(
                                        "rabbit_list",
                                        rabbits.toSet()
                                    )
                                    .putStringSet(
                                        "meat_list",
                                        meat.toSet()
                                    )
                                    .apply()
                            },

                            onDeath = {

                                val rabbits =
                                    rabbitList.filter {
                                        it != rabbit
                                    }

                                val death =
                                    adultDeathList +
                                            "$rabbit|${System.currentTimeMillis()}"

                                rabbitList =
                                    rabbits

                                adultDeathList =
                                    death

                                prefs.edit()
                                    .putStringSet(
                                        "rabbit_list",
                                        rabbits.toSet()
                                    )
                                    .putStringSet(
                                        "adult_death_list",
                                        death.toSet()
                                    )
                                    .apply()
                            }
                        )
                    }
                }

                // =================================================
                // МОЛОДНЯК
                // =================================================

                if (
                    selectedTab ==
                    AppTab.YOUNG
                ) {

                    SectionTitle(
                        "Молодняк"
                    )

                    Text(
                        text =
                            "Всего сейчас: $youngTotal",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    youngList.forEach { young ->

                        YoungCard(
                            young = young,

                            onSale = {

                                selectedYoungItem =
                                    young

                                youngSaleCount =
                                    ""

                                youngSaleBuyer =
                                    ""

                                youngSaleDate =
                                    ""

                                youngSalePrice =
                                    ""

                                showYoungSaleDialog =
                                    true
                            },

                            onMeat = {

                                selectedYoungItem =
                                    young

                                youngMeatCountText =
                                    ""

                                showYoungMeatDialog =
                                    true
                            },

                            onDeath = {

                                selectedYoungItem =
                                    young

                                youngDeathCountText =
                                    ""

                                showYoungDeathDialog =
                                    true
                            },

                            onSeparated = {

                                val updated =
                                    updateYoungSeparated(
                                        youngList,
                                        young
                                    )

                                youngList =
                                    updated

                                prefs.edit()
                                    .putStringSet(
                                        "young_list",
                                        updated.toSet()
                                    )
                                    .apply()
                            }
                        )
                    }
                }

                // =================================================
                // ПРОДАНО
                // =================================================

                if (
                    selectedTab ==
                    AppTab.SOLD
                ) {

                    SectionTitle(
                        "Продано"
                    )

                    SmallStatCard(
                        "Продано взрослых",
                        adultSoldTotal.toString(),
                        soldColor,
                        soldBorder
                    )

                    SmallStatCard(
                        "Продано молодняка",
                        youngSoldTotal.toString(),
                        youngColor,
                        youngBorder
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(8.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFF80E0BE)
                            ),

                        border =
                            BorderStroke(
                                2.dp,
                                Color(0xFF007A5E)
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                "Доход от продаж",
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                "${formatMoney(totalIncome)} ₽",
                                style =
                                    MaterialTheme
                                        .typography
                                        .headlineMedium,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Text(
                        "Проданные взрослые",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    adultSales.forEach {
                        AdultSaleCard(
                            it
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Text(
                        "Проданный молодняк",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    youngSales.forEach {
                        YoungSaleCard(
                            it
                        )
                    }
                }

                // =================================================
                // МЯСО
                // =================================================

                if (
                    selectedTab ==
                    AppTab.MEAT
                ) {

                    SectionTitle(
                        "Ушло на мясо"
                    )

                    SmallStatCard(
                        "Взрослые",
                        adultMeatTotal.toString(),
                        meatColor,
                        meatBorder
                    )

                    SmallStatCard(
                        "Молодняк",
                        youngMeatTotal.toString(),
                        youngColor,
                        youngBorder
                    )

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    adultMeatList.forEach {
                        MeatRabbitCard(
                            it
                        )
                    }
                }

                // =================================================
                // ПАДЁЖ
                // =================================================

                if (
                    selectedTab ==
                    AppTab.DEATH
                ) {

                    SectionTitle(
                        "Падёж"
                    )

                    SmallStatCard(
                        "Взрослые",
                        adultDeathTotal.toString(),
                        deathColor,
                        deathBorder
                    )

                    SmallStatCard(
                        "Молодняк",
                        youngDeathTotal.toString(),
                        youngColor,
                        youngBorder
                    )

                    Spacer(
                        modifier =
                            Modifier.height(14.dp)
                    )

                    Text(
                        "Падёж взрослых",
                        fontWeight =
                            FontWeight.Bold
                    )

                    adultDeathList.forEach {
                        DeathRabbitCard(
                            it
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(18.dp)
                    )

                    Text(
                        "Падёж молодняка",
                        fontWeight =
                            FontWeight.Bold
                    )

                    youngDeathHistory.forEach {
                        YoungDeathHistoryCard(
                            it
                        )
                    }
                }

                // =================================================
                // ВЯЗКИ
                // =================================================

                if (
                    selectedTab ==
                    AppTab.MATINGS
                ) {

                    SectionTitle(
                        "Вязки"
                    )

                    Text("Шаг 1: записать вязку. Шаг 2: открыть её карточку и внести результат окрола.")
                    Spacer(modifier = Modifier.height(10.dp))
                    val completedIds = kindlingHistory.mapNotNull {
                        it.split("|").getOrNull(1)
                    }.toSet()
                    val pendingCount = matingList.count { record ->
                        val id = record.split("|").getOrElse(0) { "" }
                        id !in completedIds
                    }
                    Text("Ожидают окрола: $pendingCount · Завершено: ${matingList.size - pendingCount}",
                        fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(10.dp))

                    matingList.sortedByDescending {
                        parseRabbitDate(it.split("|").getOrElse(3) { "" }) ?: LocalDate.MIN
                    }.forEach { mating ->
                        val matingId = mating.split("|").getOrElse(0) { "" }
                        val result = kindlingHistory.firstOrNull {
                            it.split("|").getOrNull(1) == matingId
                        }
                        MatingCard(mating, result) { chosenMating ->
                            val details = chosenMating.split("|")
                            selectedMatingId = details.getOrElse(0) { "" }
                            youngMother = details.getOrElse(1) { "" }
                            youngFather = details.getOrElse(2) { "" }
                            youngBirthDate = ""
                            youngCountText = ""
                            showYoungDialog = true
                        }
                    }
                }

                // =================================================
                // ОБЩАЯ СТАТИСТИКА
                // =================================================

                if (
                    selectedTab ==
                    AppTab.ALL_TIME
                ) {

                    SectionTitle(
                        "Статистика за всё время"
                    )

                    SmallStatCard(
                        "Всего учтено",
                        allTime.toString(),
                        historyColor,
                        historyBorder
                    )

                    SmallStatCard(
                        "Сейчас в хозяйстве",
                        totalNow.toString(),
                        currentColor,
                        currentBorder
                    )

                    SmallStatCard(
                        "Продано",
                        soldTotal.toString(),
                        soldColor,
                        soldBorder
                    )

                    SmallStatCard(
                        "На мясо",
                        meatTotal.toString(),
                        meatColor,
                        meatBorder
                    )

                    SmallStatCard(
                        "Падёж",
                        deathTotal.toString(),
                        deathColor,
                        deathBorder
                    )

                    SmallStatCard(
                        "Вязки",
                        matingList.size.toString(),
                        matingColor,
                        matingBorder
                    )

                    Card(
                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(8.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFF80E0BE)
                            ),

                        border =
                            BorderStroke(
                                2.dp,
                                Color(0xFF007A5E)
                            )
                    ) {

                        Column(
                            modifier =
                                Modifier.padding(16.dp)
                        ) {

                            Text(
                                "Общий доход от продаж",
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Text(
                                "${formatMoney(totalIncome)} ₽",
                                style =
                                    MaterialTheme
                                        .typography
                                        .headlineMedium,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Button(
                    onClick = {

                        rabbitName =
                            ""

                        rabbitGender =
                            "Самка"

                        rabbitBirthDate =
                            ""

                        rabbitPhotoUri =
                            null

                        rabbitNotes =
                            ""

                        showAddRabbit =
                            true
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(8.dp)
                ) {

                    Text(
                        "Добавить кролика"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Button(
                    onClick = {

                        matingFemale =
                            ""

                        matingMale =
                            ""

                        matingDate =
                            ""

                        showMatingDialog =
                            true
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(8.dp)
                ) {

                    Text(
                        "Добавить вязку"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Button(
                    onClick = { selectedTab = AppTab.MATINGS },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Записать окрол по вязке")
                }

                Spacer(
                    modifier =
                        Modifier.height(30.dp)
                )
            }
        }
    }

    // =====================================================
    // ДОБАВИТЬ КРОЛИКА
    // =====================================================

    if (
        showAddRabbit
    ) {

        AlertDialog(
            onDismissRequest = {
                showAddRabbit =
                    false
            },

            title = {
                Text(
                    "Добавить кролика"
                )
            },

            text = {

                Column(
                    modifier =
                        Modifier.verticalScroll(
                            rememberScrollState()
                        )
                ) {

                    OutlinedTextField(
                        value =
                            rabbitName,
                        onValueChange = {
                            rabbitName =
                                it
                        },
                        label = {
                            Text(
                                "Кличка"
                            )
                        },
                        singleLine =
                            true
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        "Пол",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Row {

                        FilterChip(
                            selected =
                                rabbitGender ==
                                        "Самка",
                            onClick = {
                                rabbitGender =
                                    "Самка"
                            },
                            label = {
                                Text("Самка")
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        FilterChip(
                            selected =
                                rabbitGender ==
                                        "Самец",
                            onClick = {
                                rabbitGender =
                                    "Самец"
                            },
                            label = {
                                Text("Самец")
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    DateField(
                        label =
                            "Дата рождения",
                        value =
                            rabbitBirthDate
                    ) {
                        rabbitBirthDate =
                            it
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    OutlinedTextField(
                        value =
                            rabbitNotes,
                        onValueChange = {
                            rabbitNotes =
                                it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Заметки"
                            )
                        },
                        placeholder = {
                            Text(
                                "Характер, здоровье, особенности..."
                            )
                        },
                        minLines =
                            3
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Button(
                        onClick = {

                            photoPicker.launch(
                                arrayOf(
                                    "image/*"
                                )
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(8.dp)
                    ) {

                        Text(
                            if (
                                rabbitPhotoUri ==
                                null
                            ) {
                                "Выбрать фото"
                            } else {
                                "Фото выбрано ✓"
                            }
                        )
                    }

                    if (
                        rabbitPhotoUri !=
                        null
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        AsyncImage(
                            model =
                                rabbitPhotoUri,
                            contentDescription =
                                "Фото",
                            modifier = Modifier
                                .size(100.dp)
                                .align(
                                    Alignment
                                        .CenterHorizontally
                                )
                                .clip(
                                    CircleShape
                                ),
                            contentScale =
                                ContentScale.Crop
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val name =
                            cleanField(
                                rabbitName
                            )

                        val date =
                            cleanField(
                                rabbitBirthDate
                            )

                        val notes =
                            cleanField(
                                rabbitNotes
                            )

                        val photo =
                            rabbitPhotoUri
                                ?.toString()
                                ?: ""

                        val rabbit =
                            "$name|" +
                                    "$rabbitGender|" +
                                    "$date|" +
                                    "${calculateAge(date)}|" +
                                    "$photo|" +
                                    notes

                        val updated =
                            rabbitList +
                                    rabbit

                        rabbitList =
                            updated

                        prefs.edit()
                            .putStringSet(
                                "rabbit_list",
                                updated.toSet()
                            )
                            .apply()

                        showAddRabbit =
                            false
                    },
                    enabled =
                        rabbitName.isNotBlank() &&
                                rabbitBirthDate.isNotBlank(),
                    shape =
                        RoundedCornerShape(8.dp)
                ) {

                    Text(
                        "Сохранить"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showAddRabbit =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }

    // =====================================================
    // РЕДАКТИРОВАТЬ
    // =====================================================

    if (
        showEditRabbit
    ) {

        AlertDialog(
            onDismissRequest = {
                showEditRabbit =
                    false
            },

            title = {
                Text(
                    "Редактировать профиль"
                )
            },

            text = {

                Column(
                    modifier =
                        Modifier.verticalScroll(
                            rememberScrollState()
                        )
                ) {

                    OutlinedTextField(
                        value =
                            editRabbitName,
                        onValueChange = {
                            editRabbitName =
                                it
                        },
                        label = {
                            Text(
                                "Кличка"
                            )
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Row {

                        FilterChip(
                            selected =
                                editRabbitGender ==
                                        "Самка",
                            onClick = {
                                editRabbitGender =
                                    "Самка"
                            },
                            label = {
                                Text("Самка")
                            }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        FilterChip(
                            selected =
                                editRabbitGender ==
                                        "Самец",
                            onClick = {
                                editRabbitGender =
                                    "Самец"
                            },
                            label = {
                                Text("Самец")
                            }
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    DateField(
                        "Дата рождения",
                        editRabbitBirthDate
                    ) {

                        editRabbitBirthDate =
                            it
                    }

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    OutlinedTextField(
                        value =
                            editRabbitNotes,
                        onValueChange = {
                            editRabbitNotes =
                                it
                        },
                        modifier =
                            Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                "Заметки"
                            )
                        },
                        minLines =
                            3
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Button(
                        onClick = {

                            editPhotoPicker.launch(
                                arrayOf(
                                    "image/*"
                                )
                            )
                        },
                        modifier =
                            Modifier.fillMaxWidth()
                    ) {

                        Text(
                            "Изменить фото"
                        )
                    }

                    if (
                        editRabbitPhotoUri !=
                        null
                    ) {

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        AsyncImage(
                            model =
                                editRabbitPhotoUri,
                            contentDescription =
                                "Фото",
                            modifier = Modifier
                                .size(100.dp)
                                .align(
                                    Alignment
                                        .CenterHorizontally
                                )
                                .clip(
                                    CircleShape
                                ),
                            contentScale =
                                ContentScale.Crop
                        )
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val name =
                            cleanField(
                                editRabbitName
                            )

                        val date =
                            cleanField(
                                editRabbitBirthDate
                            )

                        val notes =
                            cleanField(
                                editRabbitNotes
                            )

                        val photo =
                            editRabbitPhotoUri
                                ?.toString()
                                ?: ""

                        val updatedRabbit =
                            "$name|" +
                                    "$editRabbitGender|" +
                                    "$date|" +
                                    "${calculateAge(date)}|" +
                                    "$photo|" +
                                    notes

                        val updated =
                            rabbitList.map {

                                if (
                                    it ==
                                    editingRabbitOriginal
                                ) {
                                    updatedRabbit
                                } else {
                                    it
                                }
                            }

                        rabbitList =
                            updated

                        prefs.edit()
                            .putStringSet(
                                "rabbit_list",
                                updated.toSet()
                            )
                            .apply()

                        showEditRabbit =
                            false
                    },
                    enabled =
                        editRabbitName.isNotBlank(),
                    shape =
                        RoundedCornerShape(8.dp)
                ) {

                    Text(
                        "Сохранить"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showEditRabbit =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }

    // =====================================================
    // ПРОДАЖА ВЗРОСЛОГО
    // =====================================================

    if (
        showAdultSaleDialog &&
        selectedAdultSaleRabbit.isNotBlank()
    ) {

        val parts =
            selectedAdultSaleRabbit
                .split("|")

        val name =
            parts.getOrElse(
                0
            ) { "" }

        AlertDialog(
            onDismissRequest = {
                showAdultSaleDialog =
                    false
            },

            title = {
                Text(
                    "Продать кролика"
                )
            },

            text = {

                Column {

                    Text(
                        "Кролик: $name",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    OutlinedTextField(
                        value =
                            adultSaleBuyer,
                        onValueChange = {
                            adultSaleBuyer =
                                it
                        },
                        label = {
                            Text(
                                "Кому продан"
                            )
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    DateField(
                        "Дата продажи",
                        adultSaleDate
                    ) {

                        adultSaleDate =
                            it
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value =
                            adultSalePrice,
                        onValueChange = {
                            adultSalePrice =
                                filterMoneyInput(
                                    it
                                )
                        },
                        label = {
                            Text(
                                "Цена"
                            )
                        },
                        suffix = {
                            Text("₽")
                        }
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val price =
                            parseMoney(
                                adultSalePrice
                            )

                        val rabbitParts =
                            selectedAdultSaleRabbit
                                .split("|")

                        val sale =
                            "${System.currentTimeMillis()}|" +
                                    "${cleanField(rabbitParts.getOrElse(0) { "" })}|" +
                                    "${cleanField(rabbitParts.getOrElse(1) { "" })}|" +
                                    "${cleanField(rabbitParts.getOrElse(2) { "" })}|" +
                                    "${cleanField(adultSaleBuyer)}|" +
                                    "${cleanField(adultSaleDate)}|" +
                                    price

                        val rabbits =
                            rabbitList.filter {
                                it !=
                                        selectedAdultSaleRabbit
                            }

                        val sales =
                            adultSales +
                                    sale

                        rabbitList =
                            rabbits

                        adultSales =
                            sales

                        prefs.edit()
                            .putStringSet(
                                "rabbit_list",
                                rabbits.toSet()
                            )
                            .putStringSet(
                                "adult_sales",
                                sales.toSet()
                            )
                            .apply()

                        selectedAdultSaleRabbit =
                            ""

                        showAdultSaleDialog =
                            false

                        selectedTab =
                            AppTab.SOLD
                    },

                    enabled =
                        adultSaleBuyer.isNotBlank() &&
                                adultSaleDate.isNotBlank() &&
                                parseMoney(
                                    adultSalePrice
                                ) > 0.0
                ) {

                    Text(
                        "Продать"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showAdultSaleDialog =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }

    // =====================================================
    // ПРОДАЖА МОЛОДНЯКА
    // =====================================================

    if (
        showYoungSaleDialog &&
        selectedYoungItem.isNotBlank()
    ) {

        val parts =
            selectedYoungItem
                .split("|")

        val mother =
            parts.getOrElse(
                1
            ) { "" }

        val count =
            parts
                .getOrElse(
                    3
                ) { "0" }
                .toIntOrNull()
                ?: 0

        AlertDialog(
            onDismissRequest = {
                showYoungSaleDialog =
                    false
            },

            title = {
                Text(
                    "Продать молодняк"
                )
            },

            text = {

                Column(
                    modifier =
                        Modifier.verticalScroll(
                            rememberScrollState()
                        )
                ) {

                    Text(
                        "Мать: $mother"
                    )

                    Text(
                        "Сейчас: $count",
                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    OutlinedTextField(
                        value =
                            youngSaleCount,
                        onValueChange = {

                            youngSaleCount =
                                it.filter {
                                        char ->
                                    char.isDigit()
                                }
                        },
                        label = {
                            Text(
                                "Количество"
                            )
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value =
                            youngSaleBuyer,
                        onValueChange = {
                            youngSaleBuyer =
                                it
                        },
                        label = {
                            Text(
                                "Кому проданы"
                            )
                        }
                    )

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    DateField(
                        "Дата продажи",
                        youngSaleDate
                    ) {

                        youngSaleDate =
                            it
                    }

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    OutlinedTextField(
                        value =
                            youngSalePrice,
                        onValueChange = {
                            youngSalePrice =
                                filterMoneyInput(
                                    it
                                )
                        },
                        label = {
                            Text(
                                "Общая сумма"
                            )
                        },
                        suffix = {
                            Text("₽")
                        }
                    )
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val saleCount =
                            youngSaleCount
                                .toIntOrNull()
                                ?: 0

                        val price =
                            parseMoney(
                                youngSalePrice
                            )

                        val selectedParts =
                            selectedYoungItem
                                .split("|")

                        val kindling =
                            selectedParts.getOrElse(
                                2
                            ) { "" }

                        val updatedYoung =
                            subtractYoungCount(
                                youngList,
                                selectedYoungItem,
                                saleCount
                            )

                        val sale =
                            "${System.currentTimeMillis()}|" +
                                    "${cleanField(mother)}|" +
                                    "${cleanField(kindling)}|" +
                                    "$saleCount|" +
                                    "${cleanField(youngSaleBuyer)}|" +
                                    "${cleanField(youngSaleDate)}|" +
                                    price

                        val sales =
                            youngSales +
                                    sale

                        youngList =
                            updatedYoung

                        youngSales =
                            sales

                        prefs.edit()
                            .putStringSet(
                                "young_list",
                                updatedYoung.toSet()
                            )
                            .putStringSet(
                                "young_sales",
                                sales.toSet()
                            )
                            .apply()

                        selectedYoungItem =
                            ""

                        showYoungSaleDialog =
                            false

                        selectedTab =
                            AppTab.SOLD
                    },

                    enabled = run {

                        val amount =
                            youngSaleCount
                                .toIntOrNull()
                                ?: 0

                        amount > 0 &&
                                amount <= count &&
                                youngSaleBuyer.isNotBlank() &&
                                youngSaleDate.isNotBlank() &&
                                parseMoney(
                                    youngSalePrice
                                ) > 0.0
                    }
                ) {

                    Text(
                        "Продать"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showYoungSaleDialog =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }

    // =====================================================
    // ВЯЗКА
    // =====================================================

    if (
        showMatingDialog
    ) {

        AlertDialog(
            onDismissRequest = {
                showMatingDialog =
                    false
            },

            title = {
                Text(
                    "Добавить вязку"
                )
            },

            text = {

                Column(
                    modifier =
                        Modifier.verticalScroll(
                            rememberScrollState()
                        )
                ) {

                    Text(
                        "Самка",
                        fontWeight =
                            FontWeight.Bold
                    )

                    femaleNames.forEach { name ->

                        FilterChip(
                            selected =
                                matingFemale ==
                                        name,
                            onClick = {
                                matingFemale =
                                    name
                            },
                            label = {
                                Text(
                                    name
                                )
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    Text(
                        "Самец",
                        fontWeight =
                            FontWeight.Bold
                    )

                    maleNames.forEach { name ->

                        FilterChip(
                            selected =
                                matingMale ==
                                        name,
                            onClick = {
                                matingMale =
                                    name
                            },
                            label = {
                                Text(
                                    name
                                )
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.height(12.dp)
                    )

                    DateField(
                        "Дата вязки",
                        matingDate
                    ) {

                        matingDate =
                            it
                    }
                }
            },

            confirmButton = {

                Button(
                    onClick = {

                        val item =
                            "${System.currentTimeMillis()}|" +
                                    "${cleanField(matingFemale)}|" +
                                    "${cleanField(matingMale)}|" +
                                    cleanField(matingDate)

                        val updated =
                            matingList +
                                    item

                        matingList =
                            updated

                        prefs.edit()
                            .putStringSet(
                                "mating_list",
                                updated.toSet()
                            )
                            .apply()

                        showMatingDialog =
                            false

                        selectedTab =
                            AppTab.MATINGS
                    },

                    enabled =
                        matingFemale.isNotBlank() &&
                                matingMale.isNotBlank() &&
                                (parseRabbitDate(matingDate)?.let { !it.isAfter(LocalDate.now()) } == true)
                ) {

                    Text(
                        "Сохранить"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showMatingDialog =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }

    // =====================================================
    // ОКРОЛ: строго по выбранной вязке
    // =====================================================

    if (showYoungDialog) {
        val chosenMating = matingList.firstOrNull {
            it.split("|").getOrElse(0) { "" } == selectedMatingId
        }
        val sourceMatingDate = chosenMating?.split("|")?.getOrElse(3) { "" } ?: ""
        val validBirthDate = isValidKindlingDate(youngBirthDate, sourceMatingDate)
        val recorded = kindlingHistory.any {
            it.split("|").getOrNull(1) == selectedMatingId
        }
        val count = youngCountText.toIntOrNull()

        AlertDialog(
            onDismissRequest = { showYoungDialog = false },
            title = { Text("Записать окрол") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Родительская пара", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Самка: $youngMother")
                    Text("Самец: $youngFather")
                    Text("Вязка: $sourceMatingDate")
                    Spacer(modifier = Modifier.height(14.dp))
                    DateField("Дата рождения крольчат", youngBirthDate) { youngBirthDate = it }
                    if (youngBirthDate.isNotBlank() && !validBirthDate) {
                        Text(
                            "Дата рождения должна быть не раньше вязки и не позже сегодняшнего дня.",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = youngCountText,
                        onValueChange = { youngCountText = it.filter(Char::isDigit) },
                        label = { Text("Сколько крольчат родилось") },
                        supportingText = { Text("Можно указать 0, если приплода не было") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Родители, дата и количество сохранятся в истории этой вязки.",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chosenMating != null && validBirthDate && !recorded && count != null && count >= 0) {
                            val id = System.currentTimeMillis().toString()
                            val historyRecord = listOf(
                                id, cleanField(selectedMatingId), cleanField(youngMother),
                                cleanField(youngFather), cleanField(youngBirthDate), count.toString()
                            ).joinToString("|")
                            val historyUpdated = kindlingHistory + historyRecord
                            kindlingHistory = historyUpdated

                            val editor = prefs.edit().putStringSet(
                                "kindling_history", historyUpdated.toSet()
                            )
                            if (count > 0) {
                                // Первые 5 полей — старый формат. Новые: отец и ID вязки.
                                val youngRecord = listOf(
                                    id, cleanField(youngMother), cleanField(youngBirthDate),
                                    count.toString(), "0", cleanField(youngFather), cleanField(selectedMatingId)
                                ).joinToString("|")
                                val youngUpdated = youngList + youngRecord
                                youngList = youngUpdated
                                editor.putStringSet("young_list", youngUpdated.toSet())
                            }
                            editor.apply()
                            showYoungDialog = false
                            selectedTab = AppTab.MATINGS
                        }
                    },
                    enabled = chosenMating != null && !recorded && validBirthDate &&
                        count != null && count >= 0
                ) { Text("Сохранить результат") }
            },
            dismissButton = {
                TextButton(onClick = { showYoungDialog = false }) { Text("Отмена") }
            }
        )
    }

    // =====================================================
    // МОЛОДНЯК НА МЯСО
    // =====================================================

    if (
        showYoungMeatDialog &&
        selectedYoungItem.isNotBlank()
    ) {

        val parts =
            selectedYoungItem.split("|")

        val count =
            parts
                .getOrElse(
                    3
                ) { "0" }
                .toIntOrNull()
                ?: 0

        AlertDialog(
            onDismissRequest = {
                showYoungMeatDialog =
                    false
            },

            title = {
                Text(
                    "Молодняк на мясо"
                )
            },

            text = {

                OutlinedTextField(
                    value =
                        youngMeatCountText,
                    onValueChange = {

                        youngMeatCountText =
                            it.filter {
                                    char ->
                                char.isDigit()
                            }
                    },
                    label = {
                        Text(
                            "Количество"
                        )
                    }
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        val amount =
                            youngMeatCountText
                                .toIntOrNull()
                                ?: 0

                        val updated =
                            subtractYoungCount(
                                youngList,
                                selectedYoungItem,
                                amount
                            )

                        youngList =
                            updated

                        youngMeatTotal +=
                            amount

                        prefs.edit()
                            .putStringSet(
                                "young_list",
                                updated.toSet()
                            )
                            .putInt(
                                "young_meat_total",
                                youngMeatTotal
                            )
                            .apply()

                        selectedYoungItem =
                            ""

                        showYoungMeatDialog =
                            false
                    },

                    enabled = run {

                        val value =
                            youngMeatCountText
                                .toIntOrNull()
                                ?: 0

                        value > 0 &&
                                value <= count
                    }
                ) {

                    Text(
                        "Списать"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showYoungMeatDialog =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }

    // =====================================================
    // ПАДЁЖ МОЛОДНЯКА
    // =====================================================

    if (
        showYoungDeathDialog &&
        selectedYoungItem.isNotBlank()
    ) {

        val parts =
            selectedYoungItem
                .split("|")

        val mother =
            parts.getOrElse(
                1
            ) { "" }

        val date =
            parts.getOrElse(
                2
            ) { "" }

        val count =
            parts
                .getOrElse(
                    3
                ) { "0" }
                .toIntOrNull()
                ?: 0

        AlertDialog(
            onDismissRequest = {
                showYoungDeathDialog =
                    false
            },

            title = {
                Text(
                    "Падёж молодняка"
                )
            },

            text = {

                OutlinedTextField(
                    value =
                        youngDeathCountText,
                    onValueChange = {

                        youngDeathCountText =
                            it.filter {
                                    char ->
                                char.isDigit()
                            }
                    },
                    label = {
                        Text(
                            "Количество"
                        )
                    }
                )
            },

            confirmButton = {

                Button(
                    onClick = {

                        val amount =
                            youngDeathCountText
                                .toIntOrNull()
                                ?: 0

                        val updated =
                            subtractYoungCount(
                                youngList,
                                selectedYoungItem,
                                amount
                            )

                        val historyItem =
                            "${System.currentTimeMillis()}|" +
                                    "${cleanField(mother)}|" +
                                    "${cleanField(date)}|" +
                                    amount.toString()

                        val newHistory: List<String> =
                            youngDeathHistory + historyItem

                        youngList = updated
                        youngDeathHistory = newHistory
                        youngDeathTotal += amount

                        prefs.edit()
                            .putStringSet(
                                "young_list",
                                updated.toSet()
                            )
                            .putStringSet(
                                "young_death_history",
                                newHistory.toSet()
                            )
                            .putInt(
                                "young_death_total",
                                youngDeathTotal
                            )
                            .apply()

                        selectedYoungItem = ""
                        youngDeathCountText = ""
                        showYoungDeathDialog = false
                    },

                    enabled = run {

                        val value =
                            youngDeathCountText
                                .toIntOrNull()
                                ?: 0

                        value > 0 &&
                                value <= count
                    }
                ) {

                    Text(
                        "Записать"
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showYoungDeathDialog =
                            false
                    }
                ) {

                    Text(
                        "Отмена"
                    )
                }
            }
        )
    }
}

// =========================================================
// КАЛЕНДАРЬ
// =========================================================

@Composable
fun DateField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit
) {

    val context =
        LocalContext.current

    OutlinedButton(
        onClick = {

            showDatePicker(
                context,
                value,
                onDateSelected
            )
        },
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(8.dp)
    ) {

        Column(
            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                label,
                style =
                    MaterialTheme
                        .typography
                        .bodySmall
            )

            Text(
                if (
                    value.isBlank()
                ) {
                    "Выбрать дату"
                } else {
                    value
                },
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

fun showDatePicker(
    context: Context,
    currentValue: String,
    onDateSelected: (String) -> Unit
) {

    val oldDate =
        parseRabbitDate(
            currentValue
        )

    val calendar =
        Calendar.getInstance()

    if (
        oldDate != null
    ) {

        calendar.set(
            oldDate.year,
            oldDate.monthValue - 1,
            oldDate.dayOfMonth
        )
    }

    DatePickerDialog(
        context,
        { _, year, month, day ->

            val date =
                LocalDate.of(
                    year,
                    month + 1,
                    day
                )

            onDateSelected(
                date.format(
                    DateTimeFormatter.ofPattern(
                        "dd.MM.yyyy"
                    )
                )
            )
        },
        calendar.get(
            Calendar.YEAR
        ),
        calendar.get(
            Calendar.MONTH
        ),
        calendar.get(
            Calendar.DAY_OF_MONTH
        )
    ).show()
}

// =========================================================
// КАРТОЧКИ
// =========================================================

@Composable
fun CategoryCard(
    title: String,
    value: String,
    selected: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                onClick()
            },

        shape =
            RoundedCornerShape(8.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    backgroundColor
            ),

        border =
            BorderStroke(
                if (
                    selected
                ) {
                    2.dp
                } else {
                    1.dp
                },
                borderColor
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                title,
                fontWeight =
                    if (
                        selected
                    ) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Medium
                    }
            )

            Text(
                value,
                fontWeight =
                    FontWeight.Bold,
                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )
        }
    }
}

@Composable
fun SmallStatCard(
    title: String,
    value: String,
    color: Color,
    borderColor: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),

        shape =
            RoundedCornerShape(8.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    color
            ),

        border =
            BorderStroke(
                1.dp,
                borderColor
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(title)

            Text(
                value,
                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionTitle(
    text: String
) {

    Text(
        text,
        style =
            MaterialTheme
                .typography
                .titleLarge,
        fontWeight =
            FontWeight.Bold
    )

    Spacer(
        modifier =
            Modifier.height(10.dp)
    )
}

@Composable
fun RabbitCard(
    rabbit: String,
    onEdit: () -> Unit,
    onSale: () -> Unit,
    onMeat: () -> Unit,
    onDeath: () -> Unit
) {

    val parts =
        rabbit.split("|")

    val name =
        parts.getOrElse(
            0
        ) { "" }

    val gender =
        parts.getOrElse(
            1
        ) { "" }

    val birth =
        parts.getOrElse(
            2
        ) { "" }

    val photo =
        parts.getOrElse(
            4
        ) { "" }

    val notes =
        parts.getOrElse(
            5
        ) { "" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable {
                onEdit()
            },

        shape =
            RoundedCornerShape(8.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFFD9F3D5)
            ),

        border =
            BorderStroke(
                1.dp,
                Color(0xFF2E7D32)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(12.dp)
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                if (
                    photo.isNotBlank()
                ) {

                    AsyncImage(
                        model =
                            Uri.parse(photo),
                        contentDescription =
                            name,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape),
                        contentScale =
                            ContentScale.Crop
                    )

                } else {

                    Surface(
                        modifier =
                            Modifier.size(68.dp),
                        shape =
                            CircleShape
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                "🐇",
                                style =
                                    MaterialTheme
                                        .typography
                                        .headlineMedium
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(12.dp)
                )

                Column {

                    Text(
                        name,
                        fontWeight =
                            FontWeight.Bold,
                        style =
                            MaterialTheme
                                .typography
                                .titleMedium
                    )

                    Text(gender)

                    Text(
                        "Дата: $birth"
                    )

                    Text(
                        "Возраст: ${calculateAge(birth)}"
                    )
                }
            }

            if (
                notes.isNotBlank()
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    "Заметки: $notes"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Button(
                onClick =
                    onSale,
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF00866A)
                    ),
                shape =
                    RoundedCornerShape(8.dp)
            ) {

                Text(
                    "Продать"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Row {

                Button(
                    onClick =
                        onMeat,
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        "На мясо"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Button(
                    onClick =
                        onDeath,
                    modifier =
                        Modifier.weight(1f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF8B3442)
                        )
                ) {

                    Text(
                        "Падёж"
                    )
                }
            }
        }
    }
}

@Composable
fun YoungCard(
    young: String,
    onSale: () -> Unit,
    onMeat: () -> Unit,
    onDeath: () -> Unit,
    onSeparated: () -> Unit
) {

    val parts =
        young.split("|")

    val mother =
        parts.getOrElse(
            1
        ) { "" }

    val father = parts.getOrElse(5) { "" }

    val date =
        parts.getOrElse(
            2
        ) { "" }

    val count =
        parts
            .getOrElse(
                3
            ) { "0" }
            .toIntOrNull()
            ?: 0

    val separated =
        parts.getOrElse(
            4
        ) { "0" } == "1"

    val days =
        calculateAgeDays(
            date
        )

    val needsWeaning =
        days != null &&
                days >= 45 &&
                !separated

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),

        shape =
            RoundedCornerShape(8.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (
                        needsWeaning
                    ) {
                        Color(0xFFFFBF66)
                    } else {
                        Color(0xFFFFD89A)
                    }
            ),

        border =
            BorderStroke(
                if (
                    needsWeaning
                ) {
                    2.dp
                } else {
                    1.dp
                },
                Color(0xFFEF6C00)
            )
    ) {

        Column(
            modifier =
                Modifier.padding(14.dp)
        ) {

            Text(
                "Мать: $mother",
                fontWeight =
                    FontWeight.Bold
            )

            Text(if (father.isBlank()) "Отец: не указан (старая запись)" else "Отец: $father")

            Text(
                "Окрол: $date"
            )

            Text(
                "Возраст: ${calculateYoungAge(date)}"
            )

            Text(
                "Количество: $count",
                fontWeight =
                    FontWeight.Bold
            )

            if (
                separated
            ) {

                Text(
                    "✓ Отсажены от матери",
                    color =
                        Color(0xFF1B5E20),
                    fontWeight =
                        FontWeight.Bold
                )
            }

            if (
                needsWeaning
            ) {

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    "⚠ Пора отсаживать от матери",
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        Color(0xFF9A2F00)
                )

                Button(
                    onClick =
                        onSeparated,
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Отсажены"
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(8.dp)
            )

            Button(
                onClick =
                    onSale,
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF00866A)
                    )
            ) {

                Text(
                    "Продать"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Button(
                onClick =
                    onMeat,
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text(
                    "На мясо"
                )
            }

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Button(
                onClick =
                    onDeath,
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF8B3442)
                    )
            ) {

                Text(
                    "Падёж"
                )
            }
        }
    }
}

@Composable
fun AdultSaleCard(
    sale: String
) {

    val p =
        sale.split("|")

    val price =
        p.getOrElse(
            6
        ) { "0" }
            .toDoubleOrNull()
            ?: 0.0

    HistoryCard(
        title =
            p.getOrElse(
                1
            ) { "" },

        lines =
            listOf(
                p.getOrElse(
                    2
                ) { "" },
                "Покупатель: ${
                    p.getOrElse(
                        4
                    ) { "" }
                }",
                "Дата: ${
                    p.getOrElse(
                        5
                    ) { "" }
                }",
                "Доход: ${formatMoney(price)} ₽"
            ),

        background =
            Color(0xFFC6F0E3),

        border =
            Color(0xFF00856A)
    )
}

@Composable
fun YoungSaleCard(
    sale: String
) {

    val p =
        sale.split("|")

    val price =
        p.getOrElse(
            6
        ) { "0" }
            .toDoubleOrNull()
            ?: 0.0

    HistoryCard(
        title =
            "Молодняк от ${
                p.getOrElse(
                    1
                ) { "" }
            }",

        lines =
            listOf(
                "Окрол: ${
                    p.getOrElse(
                        2
                    ) { "" }
                }",
                "Продано: ${
                    p.getOrElse(
                        3
                    ) { "0" }
                }",
                "Покупатель: ${
                    p.getOrElse(
                        4
                    ) { "" }
                }",
                "Дата: ${
                    p.getOrElse(
                        5
                    ) { "" }
                }",
                "Доход: ${formatMoney(price)} ₽"
            ),

        background =
            Color(0xFFC6F0E3),

        border =
            Color(0xFF00856A)
    )
}

@Composable
fun MeatRabbitCard(
    rabbit: String
) {

    val p =
        rabbit.split("|")

    HistoryCard(
        title =
            p.getOrElse(
                0
            ) { "" },

        lines =
            listOf(
                p.getOrElse(
                    1
                ) { "" },
                "Статус: на мясо"
            ),

        background =
            Color(0xFFFFC7C7),

        border =
            Color(0xFFC62828)
    )
}

@Composable
fun DeathRabbitCard(
    rabbit: String
) {

    val p =
        rabbit.split("|")

    HistoryCard(
        title =
            p.getOrElse(
                0
            ) { "" },

        lines =
            listOf(
                p.getOrElse(
                    1
                ) { "" },
                "Статус: падёж"
            ),

        background =
            Color(0xFFE6B4BC),

        border =
            Color(0xFF7B1F2B)
    )
}

@Composable
fun YoungDeathHistoryCard(
    item: String
) {

    val p =
        item.split("|")

    HistoryCard(
        title =
            "Мать: ${
                p.getOrElse(
                    1
                ) { "" }
            }",

        lines =
            listOf(
                "Окрол: ${
                    p.getOrElse(
                        2
                    ) { "" }
                }",
                "Падёж: ${
                    p.getOrElse(
                        3
                    ) { "0" }
                }"
            ),

        background =
            Color(0xFFE6B4BC),

        border =
            Color(0xFF7B1F2B)
    )
}

@Composable
fun HistoryCard(
    title: String,
    lines: List<String>,
    background: Color,
    border: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    background
            ),

        border =
            BorderStroke(
                1.dp,
                border
            ),

        shape =
            RoundedCornerShape(8.dp)
    ) {

        Column(
            modifier =
                Modifier.padding(14.dp)
        ) {

            Text(
                title,
                fontWeight =
                    FontWeight.Bold
            )

            lines.forEach {
                Text(it)
            }
        }
    }
}

@Composable
fun MatingCard(
    mating: String,
    kindling: String?,
    onKindling: (String) -> Unit
) {
    val p = mating.split("|")
    val female = p.getOrElse(1) { "" }
    val male = p.getOrElse(2) { "" }
    val date = p.getOrElse(3) { "" }
    val born = kindling?.split("|")
    val birthDate = born?.getOrElse(4) { "" } ?: ""
    val bornCount = born?.getOrElse(5) { "" } ?: ""
    val completed = born != null

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completed) Color(0xFFE6F3E9) else Color(0xFFFFF2DA)
        ),
        border = BorderStroke(1.dp, if (completed) Color(0xFF7EB595) else Color(0xFFE4B66F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("$female  ×  $male", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Дата вязки: $date")
            Spacer(modifier = Modifier.height(6.dp))
            if (completed) {
                Text("✓ Окрол записан", color = Color(0xFF236B4D), fontWeight = FontWeight.Bold)
                Text("Дата рождения: $birthDate")
                Text("Родилось крольчат: $bornCount", fontWeight = FontWeight.SemiBold)
            } else {
                Text("Ожидает записи окрола", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onKindling(mating) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Записать окрол")
                }
            }
        }
    }
}

// =========================================================
// ФУНКЦИИ
// =========================================================

fun cleanField(
    value: String
): String {

    return value
        .trim()
        .replace(
            "|",
            "/"
        )
}

fun parseRabbitDate(
    value: String
): LocalDate? {

    return try {

        LocalDate.parse(
            value,
            DateTimeFormatter.ofPattern(
                "dd.MM.yyyy"
            )
        )

    } catch (_: Exception) {

        null
    }
}

fun isValidKindlingDate(birthValue: String, matingValue: String): Boolean {
    val birth = parseRabbitDate(birthValue) ?: return false
    val mating = parseRabbitDate(matingValue) ?: return false
    return !birth.isBefore(mating) && !birth.isAfter(LocalDate.now())
}

fun calculateAge(
    birthDate: String
): String {

    val birth =
        parseRabbitDate(
            birthDate
        ) ?: return "неизвестен"

    val age =
        Period.between(
            birth,
            LocalDate.now()
        )

    if (
        age.isNegative
    ) {
        return "дата в будущем"
    }

    return when {

        age.years > 0 ->
            "${age.years} г. ${age.months} мес."

        age.months > 0 ->
            "${age.months} мес. ${age.days} дн."

        else ->
            "${age.days} дн."
    }
}

fun calculateYoungAge(
    birthDate: String
): String {

    val days =
        calculateAgeDays(
            birthDate
        ) ?: return "неизвестен"

    if (
        days < 0
    ) {
        return "дата в будущем"
    }

    return if (
        days < 30
    ) {

        "$days дней"

    } else {

        calculateAge(
            birthDate
        )
    }
}

fun calculateAgeDays(
    birthDate: String
): Long? {

    val birth =
        parseRabbitDate(
            birthDate
        ) ?: return null

    return ChronoUnit.DAYS.between(
        birth,
        LocalDate.now()
    )
}

fun updateYoungSeparated(
    youngList: List<String>,
    selectedYoung: String
): List<String> = youngList.map { young ->
    if (young != selectedYoung) young else {
        val fields = young.split("|").toMutableList()
        while (fields.size < 5) fields.add("")
        fields[4] = "1"
        // Поля 5 и 6 (отец и вязка) не теряются при отсадке.
        fields.joinToString("|")
    }
}

fun subtractYoungCount(
    youngList: List<String>,
    selectedYoung: String,
    amount: Int
): List<String> {
    val fields = selectedYoung.split("|").toMutableList()
    val oldCount = fields.getOrElse(3) { "0" }.toIntOrNull() ?: 0
    val result = youngList.toMutableList()
    result.remove(selectedYoung)
    val remaining = oldCount - amount
    if (remaining > 0) {
        while (fields.size < 5) fields.add("")
        fields[3] = remaining.toString()
        // При продаже и списании сохраняем всю родословную.
        result.add(fields.joinToString("|"))
    }
    return result
}

fun filterMoneyInput(
    value: String
): String {

    var dotUsed =
        false

    val result =
        StringBuilder()

    value.forEach { char ->

        when {

            char.isDigit() ->
                result.append(
                    char
                )

            (
                    char == '.' ||
                            char == ','
                    ) &&
                    !dotUsed -> {

                result.append('.')

                dotUsed =
                    true
            }
        }
    }

    return result.toString()
}

fun parseMoney(
    value: String
): Double {

    return value
        .replace(
            ",",
            "."
        )
        .toDoubleOrNull()
        ?: 0.0
}

fun formatMoney(
    value: Double
): String {

    return if (
        value % 1.0 ==
        0.0
    ) {

        String.format(
            Locale.getDefault(),
            "%.0f",
            value
        )

    } else {

        String.format(
            Locale.getDefault(),
            "%.2f",
            value
        )
    }
}