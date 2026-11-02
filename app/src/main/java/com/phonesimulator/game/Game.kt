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
        Phone("Nokia 3310", 50, Rarity.COMMON, 200, "Кнопочная платформа", "Монохромный LCD", "Nokia OS", "Классика 2000 года: компактный корпус, сменные панели и Snake."),
        Phone("Redmi Note", 150, Rarity.UNCOMMON, 100, "Зависит от поколения", "Серия Redmi Note", "Android / MIUI или HyperOS", "Собирательная игровая модель линейки Redmi Note, без привязки к конкретному поколению."),
        Phone("Google Pixel", 500, Rarity.RARE, 50, "Зависит от поколения", "Серия Pixel", "Android", "Собирательная игровая модель Pixel. Серия известна вычислительной фотографией."),
        Phone("Samsung Galaxy S", 1200, Rarity.EPIC, 20, "Зависит от поколения", "Серия Galaxy S", "Android / One UI", "Собирательная модель флагманской линейки Samsung, а не спецификация отдельного устройства."),
        Phone("iPhone Pro", 2500, Rarity.LEGENDARY, 4, "Apple A-серии", "Серия Pro", "iOS", "Собирательная игровая модель iPhone Pro: премиальная линейка смартфонов Apple."),
        Phone("Samsung Z Fold 8 Ultra", 25000, Rarity.ULTRA, 1, "Игровая концепция", "Складной экран · концепт", "Не заявлена", "Вымышленная ультралегендарная модель этой игры. Название и характеристики не являются анонсом Samsung."),
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
        Phone("Sony Xperia 1 VI", 3600, Rarity.LEGENDARY, 1, "Snapdragon 8 Gen 3", "6,5″ OLED · 120 Гц", "Android 14 при выпуске", "Модель 2024 года с разъёмом 3,5 мм и телефотокамерой с переменным оптическим зумом.")
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
