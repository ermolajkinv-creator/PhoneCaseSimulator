package com.phonesimulator.game

/** Stable list positions preserve collections saved by version 1.0. */
enum class Rarity(val title: String, val accent: Int) {
    COMMON("Обычный", 0xFF9CAEC9.toInt()),
    UNCOMMON("Необычный", 0xFF59F3DC.toInt()),
    RARE("Редкий", 0xFF63ADFF.toInt()),
    EPIC("Эпический", 0xFFBA8CFF.toInt()),
    LEGENDARY("Легендарный", 0xFFFFCA71.toInt()),
    ULTRA("Ультралегендарный", 0xFFFF78C6.toInt())
}

data class Phone(val name: String, val price: Int, val rarity: Rarity, val tickets: Int,
                 val chip: String, val display: String, val system: String, val description: String)
object Catalog {
    const val CASE_PRICE = 100
    val phones = listOf(
        Phone("Nokia 3310", 50, Rarity.COMMON, 120, "Кнопочная платформа", "Монохромный LCD", "Nokia OS", "Классика 2000 года: компактный корпус, сменные панели и Snake."),
        Phone("Redmi Note", 150, Rarity.UNCOMMON, 60, "Зависит от поколения", "Серия Redmi Note", "Android / MIUI или HyperOS", "Собирательная игровая модель линейки Redmi Note, без привязки к конкретному поколению."),
        Phone("Google Pixel", 500, Rarity.RARE, 20, "Зависит от поколения", "Серия Pixel", "Android", "Собирательная игровая модель Pixel. Серия известна вычислительной фотографией."),
        Phone("Samsung Galaxy S", 1200, Rarity.EPIC, 10, "Зависит от поколения", "Серия Galaxy S", "Android / One UI", "Собирательная модель флагманской линейки Samsung, а не спецификация отдельного устройства."),
        Phone("iPhone Pro", 2500, Rarity.LEGENDARY, 2, "Apple A-серии", "Серия Pro", "iOS", "Собирательная игровая модель iPhone Pro: премиальная линейка смартфонов Apple."),
        Phone("Samsung Z Fold 8 Ultra", 25000, Rarity.ULTRA, 1, "Snapdragon 8 Elite Gen 5 for Galaxy", "8,0″ внутренний / 6,5″ внешний", "Android 17 / One UI 9 при выпуске", "Складной флагман Samsung 2026 года: основной экран 8 дюймов, камера 200 Мп и батарея 5000 мА·ч. Ультралегендарная редкость — правило игры."),
        Phone("Samsung Galaxy A15", 65, Rarity.COMMON, 100, "Helio G99 (4G)", "6,5″ Super AMOLED", "Android 14 при выпуске", "Доступная модель 2023 года. В справочнике указана версия 4G; у 5G другая платформа."),
        Phone("Redmi 12", 70, Rarity.COMMON, 100, "MediaTek Helio G88", "6,79″ IPS · 90 Гц", "Android 13 при выпуске", "Бюджетный смартфон 2023 года. Здесь описана версия 4G."),
        Phone("Infinix HOT 40", 80, Rarity.COMMON, 100, "MediaTek Helio G88", "6,78″ IPS · 90 Гц", "Android 13 / XOS при выпуске", "Модель 2023 года с большой батареей и экраном Full HD+."),
        Phone("HONOR X9b", 180, Rarity.UNCOMMON, 60, "Snapdragon 6 Gen 1", "6,78″ AMOLED · 120 Гц", "Android 13 / MagicOS при выпуске", "Модель 2023 года с изогнутым экраном. Название может отличаться на отдельных рынках."),
        Phone("POCO X6", 190, Rarity.UNCOMMON, 60, "Snapdragon 7s Gen 2", "6,67″ AMOLED · 120 Гц", "Android 13 / MIUI при выпуске", "Модель 2024 года с экраном высокого разрешения и зарядкой 67 Вт."),
        Phone("Samsung Galaxy A55", 220, Rarity.UNCOMMON, 60, "Samsung Exynos 1480", "6,6″ Super AMOLED · 120 Гц", "Android 14 / One UI при выпуске", "Модель 2024 года с металлической рамкой и защитой IP67."),
        Phone("Google Pixel 4", 550, Rarity.RARE, 25, "Snapdragon 855", "5,7″ OLED · 90 Гц", "Android 10 при выпуске", "Компактный Pixel 2019 года с распознаванием лица и телефотокамерой."),
        Phone("Google Pixel 8", 650, Rarity.RARE, 25, "Google Tensor G3", "6,2″ OLED · 120 Гц", "Android 14 при выпуске", "Модель 2023 года с вычислительной фотографией и компактным корпусом."),
        Phone("Nothing Phone (2)", 700, Rarity.RARE, 25, "Snapdragon 8+ Gen 1", "6,7″ LTPO OLED · 120 Гц", "Android 13 / Nothing OS при выпуске", "Модель 2023 года с полупрозрачной спинкой и световым интерфейсом Glyph."),
        Phone("Xiaomi 13T", 750, Rarity.RARE, 25, "MediaTek Dimensity 8200-Ultra", "6,67″ AMOLED · 144 Гц", "Android 13 / MIUI при выпуске", "Модель 2023 года с камерой Leica и защитой IP68."),
        Phone("OnePlus 12", 1350, Rarity.EPIC, 10, "Snapdragon 8 Gen 3", "6,82″ LTPO AMOLED · 120 Гц", "Android 14 при выпуске", "Флагман конца 2023 года. Оболочка зависит от региона: OxygenOS или ColorOS."),
        Phone("HONOR Magic6 Pro", 1450, Rarity.EPIC, 10, "Snapdragon 8 Gen 3", "6,8″ LTPO OLED · 120 Гц", "Android 14 / MagicOS при выпуске", "Флагман 2024 года с перископической камерой и кремний-углеродной батареей."),
        Phone("Samsung Galaxy S24 Ultra", 1600, Rarity.EPIC, 10, "Snapdragon 8 Gen 3 for Galaxy", "6,8″ Dynamic AMOLED 2X", "Android 14 / One UI при выпуске", "Флагман 2024 года с S Pen и титановой рамкой."),
        Phone("iPhone 15 Pro Max", 1700, Rarity.EPIC, 8, "Apple A17 Pro", "6,7″ OLED · ProMotion", "iOS 17 при выпуске", "Модель 2023 года с USB-C и пятикратным оптическим приближением."),
        Phone("Samsung Galaxy Z Fold6", 3000, Rarity.LEGENDARY, 2, "Snapdragon 8 Gen 3 for Galaxy", "7,6″ внутренний / 6,3″ внешний", "Android 14 / One UI при выпуске", "Складной флагман 2024 года с поддержкой многозадачности на внутреннем экране."),
        Phone("Xiaomi 14 Ultra", 3200, Rarity.LEGENDARY, 2, "Snapdragon 8 Gen 3", "6,73″ LTPO AMOLED · 120 Гц", "Android 14 / HyperOS при выпуске", "Фотофлагман 2024 года с основной камерой дюймового типа и оптикой Leica."),
        Phone("HUAWEI Mate 60 Pro", 3400, Rarity.LEGENDARY, 2, "HiSilicon Kirin 9000S", "6,82″ LTPO OLED · 120 Гц", "HarmonyOS 4 при выпуске", "Флагман 2023 года для китайского рынка. Сервисы и связь зависят от региона."),
        Phone("Sony Xperia 1 VI", 3600, Rarity.LEGENDARY, 1, "Snapdragon 8 Gen 3", "6,5″ OLED · 120 Гц", "Android 14 при выпуске", "Модель 2024 года с разъёмом 3,5 мм и телефотокамерой с переменным оптическим зумом."),
        Phone("OnePlus 15", 4500, Rarity.LEGENDARY, 2, "Snapdragon 8 Elite Gen 5", "6,78″ LTPO AMOLED · до 165 Гц", "Android 16 / OxygenOS 16 при выпуске", "Флагман с батареей 7300 мА·ч. Адаптивные 1–120 Гц, до 165 Гц в поддерживаемых играх. Здесь описана глобальная версия."),
        Phone("OnePlus 13", 1800, Rarity.EPIC, 3, "Snapdragon 8 Elite", "6,82″ LTPO AMOLED · 120 Гц", "Android 15 при выпуске", "Флагман с батареей 6000 мА·ч и камерами Hasselblad. Оболочка зависит от региона: OxygenOS или ColorOS."),
        Phone("Samsung Galaxy Z Fold8", 2200, Rarity.EPIC, 3, "Snapdragon 8 Elite Gen 5 for Galaxy", "7,6″ внутренний / 5,5″ внешний", "Android 17 / One UI 9 при выпуске", "Складная модель Samsung 2026 года с внутренним экраном формата 4:3 и батареей 4800 мА·ч."),
        Phone("Samsung Galaxy Z Flip8", 2000, Rarity.EPIC, 2, "Snapdragon 8 Elite Gen 5 for Galaxy", "6,9″ внутренний / 4,1″ внешний", "Android 17 / One UI 9 при выпуске", "Компактная раскладушка Samsung 2026 года с внешним FlexWindow и батареей 4300 мА·ч."),
        Phone("Samsung Galaxy S23 Ultra", 1500, Rarity.EPIC, 2, "Snapdragon 8 Gen 2 for Galaxy", "6,8″ Dynamic AMOLED 2X · 120 Гц", "Android 13 / One UI при выпуске", "Флагман 2023 года с S Pen и основной камерой 200 Мп."),
        Phone("Google Pixel 7", 600, Rarity.RARE, 10, "Google Tensor G2", "6,3″ OLED · 90 Гц", "Android 13 при выпуске", "Pixel 2022 года с двойной основной камерой и вычислительной фотографией."),
        Phone("iPhone 13", 650, Rarity.RARE, 10, "Apple A15 Bionic", "6,1″ Super Retina XDR OLED", "iOS 15 при выпуске", "Модель Apple 2021 года с двойной камерой и видеорежимом «Киноэффект»."),
        Phone("Xiaomi 12", 620, Rarity.RARE, 10, "Snapdragon 8 Gen 1", "6,28″ AMOLED · 120 Гц", "Android 12 / MIUI при выпуске", "Компактный флагман с оптической стабилизацией основной камеры и беспроводной зарядкой."),
        Phone("OnePlus Nord CE 3 Lite", 200, Rarity.UNCOMMON, 10, "Snapdragon 695", "6,72″ LCD · 120 Гц", "Android 13 / OxygenOS при выпуске", "Модель 2023 года с камерой 108 Мп и проводной зарядкой 67 Вт."),
        Phone("Redmi Note 13 Pro 4G", 210, Rarity.UNCOMMON, 10, "MediaTek Helio G99-Ultra", "6,67″ AMOLED · 120 Гц", "Android 13 / MIUI при выпуске", "Версия 4G с камерой 200 Мп. Не следует путать с Note 13 Pro 5G на другом чипсете."),
        Phone("realme 11 Pro", 230, Rarity.UNCOMMON, 10, "MediaTek Dimensity 7050", "6,7″ AMOLED · 120 Гц", "Android 13 / realme UI при выпуске", "Модель 2023 года с изогнутым экраном и оптической стабилизацией основной камеры."),
        Phone("Samsung Galaxy A34", 190, Rarity.UNCOMMON, 10, "MediaTek Dimensity 1080", "6,6″ Super AMOLED · 120 Гц", "Android 13 / One UI при выпуске", "Модель 2023 года с защитой IP67 и батареей 5000 мА·ч."),
        Phone("Nokia C32", 55, Rarity.COMMON, 20, "Unisoc SC9863A1", "6,5″ IPS LCD", "Android 13 при выпуске", "Доступный смартфон 2023 года со стеклянной задней панелью и камерой 50 Мп."),
        Phone("Samsung Galaxy A05", 60, Rarity.COMMON, 20, "MediaTek Helio G85", "6,7″ PLS LCD", "Android 13 при выпуске", "Доступная модель 2023 года с батареей 5000 мА·ч."),
        Phone("Redmi A3", 45, Rarity.COMMON, 20, "MediaTek Helio G36", "6,71″ LCD · 90 Гц", "Android 14 при выпуске", "Бюджетная модель 2024 года с отдельным слотом для карты памяти."),
        Phone("realme C55", 75, Rarity.COMMON, 20, "MediaTek Helio G88", "6,72″ IPS LCD · 90 Гц", "Android 13 / realme UI при выпуске", "Модель 2023 года с Mini Capsule и проводной зарядкой 33 Вт.")
    )
    // One of exactly 1000 equally likely tickets is Ultra: 0.1%, in every case.
    fun roll(ticket: Int): Int {
        require(ticket in 0..999)
        var edge = 0
        phones.indices.filter { phones[it].rarity != Rarity.ULTRA }.plus(5).forEach { index ->
            val phone = phones[index]
            edge += phone.tickets
            if (ticket < edge) return index
        }
        error("Invalid drop table")
    }
}

data class Progress(val coins: Int = 1000, val owned: List<Int> = emptyList()) {
    fun open(ticket: Int): Pair<Progress, Int>? {
        if (coins < Catalog.CASE_PRICE) return null
        val phone = Catalog.roll(ticket)
        return copy(coins = coins - Catalog.CASE_PRICE, owned = owned + phone) to phone
    }
    fun sell(index: Int): Progress {
        if (index !in Catalog.phones.indices || index !in owned) return this
        val next = owned.toMutableList().apply { remove(index) }
        return copy(coins = credit(Catalog.phones[index].price), owned = next)
    }
    fun grant(index: Int): Progress {
        require(index in Catalog.phones.indices)
        return copy(owned = owned + index)
    }
    fun bonus() = copy(coins = credit(500))
    private fun credit(amount: Int) = (coins.toLong() + amount).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    companion object {
        fun restore(coins: Int, saved: String) = Progress(
            coins.coerceAtLeast(0),
            saved.split(',').mapNotNull { it.toIntOrNull() }.filter { it in Catalog.phones.indices }
        )
    }
}
