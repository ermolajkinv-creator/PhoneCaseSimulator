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

enum class FormFactor { PHONE, TABLET }

data class Phone(val name: String, val price: Int, val rarity: Rarity, val tickets: Int,
                 val chip: String, val display: String, val system: String, val description: String,
                 val formFactor: FormFactor = FormFactor.PHONE) {
    val isApple: Boolean get() = system.startsWith("iOS") || system.startsWith("iPadOS") || system.startsWith("iPhone OS") ||
        (system != "Игровой Android" && (name.startsWith("iPhone") || name.startsWith("iPad")))
}
object Catalog {
    val CASE_PRICE: Int get() = CaseKind.CLASSIC.cost
    private val entries = listOf(
        Phone("Nokia 3310", 2000, Rarity.COMMON, 120, "Кнопочная платформа", "Монохромный LCD", "Nokia OS", "Классика 2000 года: компактный корпус, сменные панели и Snake."),
        Phone("Redmi Note 10S", 12000, Rarity.UNCOMMON, 60, "MediaTek Helio G95", "6,43″ AMOLED", "Android 11 / MIUI при выпуске", "Redmi Note 10S: модель 2021 года с камерой 64 Мп и зарядкой 33 Вт."),
        Phone("Google Pixel 10 Pro", 85000, Rarity.RARE, 20, "Google Tensor G5", "6,3″ OLED · 120 Гц", "Android 16 при выпуске", "Google Pixel 10 Pro: конкретная флагманская модель Pixel."),
        Phone("Samsung Galaxy S25 Ultra", 95000, Rarity.EPIC, 10, "Snapdragon 8 Elite for Galaxy", "6,9″ AMOLED", "Android 15 / One UI при выпуске", "Samsung Galaxy S25 Ultra с S Pen."),
        Phone("iPhone 17 Pro", 129999, Rarity.LEGENDARY, 2, "Apple A19 Pro", "6,3″ OLED · ProMotion", "iOS 26 при выпуске", "iPhone 17 Pro: флагман Apple с новым блоком камер."),
        Phone("Samsung Z Fold 8 Ultra", 129000, Rarity.ULTRA, 1, "Snapdragon 8 Elite Gen 5 for Galaxy", "8,0″ внутренний / 6,5″ внешний", "Android 17 / One UI 9 при выпуске", "Складной флагман Samsung 2026 года: основной экран 8 дюймов, камера 200 Мп и батарея 5000 мА·ч. Ультралегендарная редкость — правило игры."),
        Phone("Samsung Galaxy A15", 12000, Rarity.COMMON, 100, "Helio G99 (4G)", "6,5″ Super AMOLED", "Android 14 при выпуске", "Доступная модель 2023 года. В справочнике указана версия 4G; у 5G другая платформа."),
        Phone("Redmi 12", 11000, Rarity.COMMON, 100, "MediaTek Helio G88", "6,79″ IPS · 90 Гц", "Android 13 при выпуске", "Бюджетный смартфон 2023 года. Здесь описана версия 4G."),
        Phone("Infinix HOT 40", 12000, Rarity.COMMON, 100, "MediaTek Helio G88", "6,78″ IPS · 90 Гц", "Android 13 / XOS при выпуске", "Модель 2023 года с большой батареей и экраном Full HD+."),
        Phone("HONOR X9b", 23000, Rarity.UNCOMMON, 60, "Snapdragon 6 Gen 1", "6,78″ AMOLED · 120 Гц", "Android 13 / MagicOS при выпуске", "Модель 2023 года с изогнутым экраном. Название может отличаться на отдельных рынках."),
        Phone("POCO X6", 24000, Rarity.UNCOMMON, 60, "Snapdragon 7s Gen 2", "6,67″ AMOLED · 120 Гц", "Android 13 / MIUI при выпуске", "Модель 2024 года с экраном высокого разрешения и зарядкой 67 Вт."),
        Phone("Samsung Galaxy A55", 28000, Rarity.UNCOMMON, 60, "Samsung Exynos 1480", "6,6″ Super AMOLED · 120 Гц", "Android 14 / One UI при выпуске", "Модель 2024 года с металлической рамкой и защитой IP67."),
        Phone("Google Pixel 4", 10000, Rarity.RARE, 25, "Snapdragon 855", "5,7″ OLED · 90 Гц", "Android 10 при выпуске", "Компактный Pixel 2019 года с распознаванием лица и телефотокамерой."),
        Phone("Google Pixel 8", 38000, Rarity.RARE, 25, "Google Tensor G3", "6,2″ OLED · 120 Гц", "Android 14 при выпуске", "Модель 2023 года с вычислительной фотографией и компактным корпусом."),
        Phone("Nothing Phone (2)", 35000, Rarity.RARE, 25, "Snapdragon 8+ Gen 1", "6,7″ LTPO OLED · 120 Гц", "Android 13 / Nothing OS при выпуске", "Модель 2023 года с полупрозрачной спинкой и световым интерфейсом Glyph."),
        Phone("Xiaomi 13T", 29000, Rarity.RARE, 25, "MediaTek Dimensity 8200-Ultra", "6,67″ AMOLED · 144 Гц", "Android 13 / MIUI при выпуске", "Модель 2023 года с камерой Leica и защитой IP68."),
        Phone("OnePlus 12", 55000, Rarity.EPIC, 10, "Snapdragon 8 Gen 3", "6,82″ LTPO AMOLED · 120 Гц", "Android 14 при выпуске", "Флагман конца 2023 года. Оболочка зависит от региона: OxygenOS или ColorOS."),
        Phone("HONOR Magic6 Pro", 65000, Rarity.EPIC, 10, "Snapdragon 8 Gen 3", "6,8″ LTPO OLED · 120 Гц", "Android 14 / MagicOS при выпуске", "Флагман 2024 года с перископической камерой и кремний-углеродной батареей."),
        Phone("Samsung Galaxy S24 Ultra", 75000, Rarity.EPIC, 10, "Snapdragon 8 Gen 3 for Galaxy", "6,8″ Dynamic AMOLED 2X", "Android 14 / One UI при выпуске", "Флагман 2024 года с S Pen и титановой рамкой."),
        Phone("iPhone 15 Pro Max", 85000, Rarity.EPIC, 8, "Apple A17 Pro", "6,7″ OLED · ProMotion", "iOS 17 при выпуске", "Модель 2023 года с USB-C и пятикратным оптическим приближением."),
        Phone("Samsung Galaxy Z Fold6", 95000, Rarity.LEGENDARY, 2, "Snapdragon 8 Gen 3 for Galaxy", "7,6″ внутренний / 6,3″ внешний", "Android 14 / One UI при выпуске", "Складной флагман 2024 года с поддержкой многозадачности на внутреннем экране."),
        Phone("Xiaomi 14 Ultra", 80000, Rarity.LEGENDARY, 2, "Snapdragon 8 Gen 3", "6,73″ LTPO AMOLED · 120 Гц", "Android 14 / HyperOS при выпуске", "Фотофлагман 2024 года с основной камерой дюймового типа и оптикой Leica."),
        Phone("HUAWEI Mate 60 Pro", 70000, Rarity.LEGENDARY, 2, "HiSilicon Kirin 9000S", "6,82″ LTPO OLED · 120 Гц", "HarmonyOS 4 при выпуске", "Флагман 2023 года для китайского рынка. Сервисы и связь зависят от региона."),
        Phone("Sony Xperia 1 VI", 95000, Rarity.LEGENDARY, 1, "Snapdragon 8 Gen 3", "6,5″ OLED · 120 Гц", "Android 14 при выпуске", "Модель 2024 года с разъёмом 3,5 мм и телефотокамерой с переменным оптическим зумом."),
        Phone("OnePlus 15", 85000, Rarity.LEGENDARY, 2, "Snapdragon 8 Elite Gen 5", "6,78″ LTPO AMOLED · до 165 Гц", "Android 16 / OxygenOS 16 при выпуске", "Флагман с батареей 7300 мА·ч. Адаптивные 1–120 Гц, до 165 Гц в поддерживаемых играх. Здесь описана глобальная версия."),
        Phone("OnePlus 13", 65000, Rarity.EPIC, 3, "Snapdragon 8 Elite", "6,82″ LTPO AMOLED · 120 Гц", "Android 15 при выпуске", "Флагман с батареей 6000 мА·ч и камерами Hasselblad. Оболочка зависит от региона: OxygenOS или ColorOS."),
        Phone("Samsung Galaxy Z Fold8", 120000, Rarity.EPIC, 3, "Snapdragon 8 Elite Gen 5 for Galaxy", "7,6″ внутренний / 5,5″ внешний", "Android 17 / One UI 9 при выпуске", "Складная модель Samsung 2026 года с внутренним экраном формата 4:3 и батареей 4800 мА·ч."),
        Phone("Samsung Galaxy Z Flip8", 85000, Rarity.EPIC, 2, "Snapdragon 8 Elite Gen 5 for Galaxy", "6,9″ внутренний / 4,1″ внешний", "Android 17 / One UI 9 при выпуске", "Компактная раскладушка Samsung 2026 года с внешним FlexWindow и батареей 4300 мА·ч."),
        Phone("Samsung Galaxy S23 Ultra", 55000, Rarity.EPIC, 2, "Snapdragon 8 Gen 2 for Galaxy", "6,8″ Dynamic AMOLED 2X · 120 Гц", "Android 13 / One UI при выпуске", "Флагман 2023 года с S Pen и основной камерой 200 Мп."),
        Phone("Google Pixel 7", 24000, Rarity.RARE, 10, "Google Tensor G2", "6,3″ OLED · 90 Гц", "Android 13 при выпуске", "Pixel 2022 года с двойной основной камерой и вычислительной фотографией."),
        Phone("iPhone 13", 32000, Rarity.RARE, 10, "Apple A15 Bionic", "6,1″ Super Retina XDR OLED", "iOS 15 при выпуске", "Модель Apple 2021 года с двойной камерой и видеорежимом «Киноэффект»."),
        Phone("Xiaomi 12", 25000, Rarity.RARE, 10, "Snapdragon 8 Gen 1", "6,28″ AMOLED · 120 Гц", "Android 12 / MIUI при выпуске", "Компактный флагман с оптической стабилизацией основной камеры и беспроводной зарядкой."),
        Phone("OnePlus Nord CE 3 Lite", 15000, Rarity.UNCOMMON, 10, "Snapdragon 695", "6,72″ LCD · 120 Гц", "Android 13 / OxygenOS при выпуске", "Модель 2023 года с камерой 108 Мп и проводной зарядкой 67 Вт."),
        Phone("Redmi Note 13 Pro 4G", 20000, Rarity.UNCOMMON, 10, "MediaTek Helio G99-Ultra", "6,67″ AMOLED · 120 Гц", "Android 13 / MIUI при выпуске", "Версия 4G с камерой 200 Мп. Не следует путать с Note 13 Pro 5G на другом чипсете."),
        Phone("realme 11 Pro", 21000, Rarity.UNCOMMON, 10, "MediaTek Dimensity 7050", "6,7″ AMOLED · 120 Гц", "Android 13 / realme UI при выпуске", "Модель 2023 года с изогнутым экраном и оптической стабилизацией основной камеры."),
        Phone("Samsung Galaxy A34", 21000, Rarity.UNCOMMON, 10, "MediaTek Dimensity 1080", "6,6″ Super AMOLED · 120 Гц", "Android 13 / One UI при выпуске", "Модель 2023 года с защитой IP67 и батареей 5000 мА·ч."),
        Phone("Nokia C32", 7000, Rarity.COMMON, 20, "Unisoc SC9863A1", "6,5″ IPS LCD", "Android 13 при выпуске", "Доступный смартфон 2023 года со стеклянной задней панелью и камерой 50 Мп."),
        Phone("Samsung Galaxy A05", 8000, Rarity.COMMON, 20, "MediaTek Helio G85", "6,7″ PLS LCD", "Android 13 при выпуске", "Доступная модель 2023 года с батареей 5000 мА·ч."),
        Phone("Redmi A3", 6500, Rarity.COMMON, 20, "MediaTek Helio G36", "6,71″ LCD · 90 Гц", "Android 14 при выпуске", "Бюджетная модель 2024 года с отдельным слотом для карты памяти."),
        Phone("realme C55", 10000, Rarity.COMMON, 20, "MediaTek Helio G88", "6,72″ IPS LCD · 90 Гц", "Android 13 / realme UI при выпуске", "Модель 2023 года с Mini Capsule и проводной зарядкой 33 Вт."),
        Phone("HONOR X6c", 11000, Rarity.COMMON, 1, "MediaTek Helio G81 Ultra", "6,61″ LCD · 120 Гц", "Android / MagicOS", "Доступный HONOR с большой батареей. В игре доступен в кейсах и на рынке б/у."),
        Phone("Infinix HOT 60 Pro+", 23000, Rarity.UNCOMMON, 1, "MediaTek Helio G200", "6,78″ AMOLED · 144 Гц", "Android 15 / XOS при выпуске", "Тонкий Infinix с изогнутым AMOLED-дисплеем."),
        Phone("Redmi 13C", 9990, Rarity.RARE, 1, "MediaTek Helio G85 (4G)", "6,74″ LCD · 90 Гц", "Android 13 / MIUI при выпуске", "Версия Redmi 13C 4G. Модель 5G использует другую платформу."),
        Phone("Samsung Galaxy A14", 10000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy A14. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy A16", 13000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy A16. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy A25", 17000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy A25. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy A35", 22000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy A35. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy A54", 22000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy A54. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy A73", 23000, Rarity.RARE, 1, "Snapdragon 778G", "6,7″ Super AMOLED Plus · 120 Гц", "Android 12 / One UI 4.1 при выпуске", "Samsung Galaxy A73 5G (SM-A736), 2022 год: камера 108 Мп с OIS, аккумулятор 5000 мА·ч и защита IP67."),
        Phone("Samsung Galaxy S10", 10000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy S10. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy S20 FE", 15000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy S20 FE. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy S21", 18000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy S21. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy S22", 23000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy S22. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy S24", 45000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy S24. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy Note20 Ultra", 22000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy Note20 Ultra. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy Z Flip5", 42000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy Z Flip5. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Samsung Galaxy Z Fold5", 70000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy Z Fold5. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 5", 14000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 5. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 6", 19000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 6. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 6a", 16000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 6a. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 7a", 21000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 7a. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 8a", 28000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 8a. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 9", 45000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 9. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Google Pixel 9 Pro", 65000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Google Pixel 9 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone SE (2020)", 12000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone SE (2020). Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 11", 19000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 11. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 12", 24000, Rarity.RARE, 1, "Apple A14 Bionic", "6,1″ Super Retina XDR OLED · 60 Гц", "iOS 14 при выпуске", "iPhone 12, 2020 год: две камеры 12 Мп, MagSafe и поддержка 5G. Ёмкость памяти и состояние влияют на реальную цену."),
        Phone("iPhone 12 mini", 22000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 12 mini. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 13 mini", 28000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 13 mini. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 14", 39000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 14. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 14 Pro", 50000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 14 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 15", 50000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 15. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 16", 65000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 16. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("iPhone 16 Pro", 85000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 16 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi Note 8 Pro", 7000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi Note 8 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi Note 9 Pro", 8500, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi Note 9 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi Note 11", 10000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi Note 11. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi Note 12", 13000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi Note 12. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi Note 13", 16000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi Note 13. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi Note 14", 19000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi Note 14. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi 10", 8500, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi 10. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Redmi 12C", 7000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Redmi 12C. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("POCO F3", 17000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель POCO F3. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("POCO F4", 20000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель POCO F4. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("POCO F5", 23000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель POCO F5. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("POCO F6", 29000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель POCO F6. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("POCO X3 Pro", 13000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель POCO X3 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("POCO X5 Pro", 17000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель POCO X5 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("HONOR 90", 25000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель HONOR 90. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("HONOR 200", 32000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель HONOR 200. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("HONOR Magic5 Pro", 42000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель HONOR Magic5 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("HONOR X8b", 16000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель HONOR X8b. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Infinix NOTE 30", 12000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Infinix NOTE 30. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Infinix NOTE 40 Pro", 18000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Infinix NOTE 40 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Infinix GT 20 Pro", 23000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Infinix GT 20 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("TECNO Camon 30", 18000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель TECNO Camon 30. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("TECNO POVA 6 Pro", 20000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель TECNO POVA 6 Pro. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("Motorola Edge 40", 25000, Rarity.COMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Motorola Edge 40. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("ASUS Zenfone 10", 45000, Rarity.UNCOMMON, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель ASUS Zenfone 10. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
        Phone("OPPO Reno10", 25000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель OPPO Reno10. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены.")
    ) + AdditionalPhones.entries + Phone("Палас улыбашкэ", 35000, Rarity.ULTRA, 15,
        "Коллекционный мем", "Улыбка на весь экран", "Игровая платформа",
        "Палас улыбашкэ — особая игровая модель с широкой улыбкой. Второй ультралегендарный телефон: шанс 0,15% в каждом кейсе.") + DeviceExpansion41.entries + Phone("Бурмалдофон", 42000, Rarity.EPIC, 1,
        "Бурмалдо Core", "Экран хорошего настроения", "Игровая платформа",
        "Бурмалдофон — вымышленный мемный телефон с двумя глазами-камерами, нахмуренными бровями и широкой улыбкой. Эпическая модель коллекции Phone Lab; характеристики и цена игровые.")
    const val TOTAL_TICKETS = 10_000
    const val FOLD = 5
    const val PALAS = 500
    const val BURMALDO = 554
    const val FOLD_TICKETS = 10
    const val PALAS_TICKETS = 15
    val ultraIndices = listOf(FOLD, PALAS)
    val phones: List<Phone> = entries.mapIndexed { index, phone ->
        val tickets = when (index) {
            FOLD -> FOLD_TICKETS
            PALAS -> PALAS_TICKETS
            else -> {
                // Preserve other rarity shares; the new 0.15% comes from Legendary (1.1% -> 0.95%).
                val budget = mapOf(Rarity.COMMON to 5000, Rarity.UNCOMMON to 2800, Rarity.RARE to 1500,
                    Rarity.EPIC to 580, Rarity.LEGENDARY to 95).getValue(phone.rarity)
                val peers = entries.filter { it.rarity == phone.rarity }
                require(peers.size <= budget) { "Not enough drop tickets for ${phone.rarity}" }
                val position = peers.indexOf(phone)
                budget / peers.size + if (position < budget % peers.size) 1 else 0
            }
        }
        phone.copy(tickets = tickets)
    }
    fun isUltra(index: Int) = phones[index].rarity == Rarity.ULTRA
    val modelCountLabel: String get() {
        val count = phones.size
        val noun = if (count % 100 in 11..14) "моделей" else when (count % 10) {
            1 -> "модель"; 2, 3, 4 -> "модели"; else -> "моделей"
        }
        return "$count $noun"
    }
    fun percent(tickets: Int) = tickets * 100.0 / TOTAL_TICKETS
    fun ultraDrop(ticket: Int): Int? = when {
        ticket >= TOTAL_TICKETS - FOLD_TICKETS -> FOLD
        ticket >= TOTAL_TICKETS - FOLD_TICKETS - PALAS_TICKETS -> PALAS
        else -> null
    }
    private val dropTable = buildList {
        (phones.indices.filter { !isUltra(it) } + listOf(PALAS, FOLD)).forEach { index ->
            repeat(phones[index].tickets) { add(index) }
        }
    }.also { require(it.size == TOTAL_TICKETS) }
    fun roll(ticket: Int): Int {
        require(ticket in 0 until TOTAL_TICKETS)
        return dropTable[ticket]
    }
}

data class Progress(val coins: Int = Economy.START_BALANCE, val owned: List<Int> = emptyList(),
                    val used: List<UsedPhone> = emptyList(), val purchased: Set<String> = emptySet(), val firmware: Map<Int, String> = emptyMap(), val lastGiftAt: Long = 0L,
                    val favorites: Set<Int> = emptySet(), val nickname: String = "Коллекционер",
                    val clicks: Int = 0, val casesOpened: Int = 0,
                    val lastDailyDay: Long = -1L, val dailyStreak: Int = 0,
                    val badges: Set<String> = emptySet(), val questDay: Long = -1L,
                    val questClicks: Int = 0, val questCases: Int = 0,
                    val claimedQuests: Set<String> = emptySet(),
                    val workshopLevel: Int = 1, val reputation: Int = 0, val servicesCompleted: Int = 0,
                    val serviceSlot: Long = -1L, val servicedSlots: Set<Long> = emptySet(),
                    val clientJob: ClientJob? = null, val expansion: Expansion = Expansion(),
                    val clickGuard: ClickGuard = ClickGuard(), val world: WorldData = WorldData()) {
    fun open(ticket: Int): Pair<Progress, Int>? = openCase(CaseKind.CLASSIC, ticket)
    fun sell(index: Int): Progress {
        if (index !in Catalog.phones.indices || index !in owned) return this
        val position = owned.indexOf(index)
        val price = DeviceSnapshot(index,traitsAt(position)).value
        return removePhoneAt(position).copy(coins = credit(price))
    }
    fun grant(index: Int): Progress {
        require(index in Catalog.phones.indices)
        return appendPhone(index)
    }
    fun buy(listing: Listing): Progress? {
        if (listing.id in purchased || coins < listing.price) return null
        return copy(coins = coins - listing.price, used = used + UsedPhone(listing.phone, listing.condition),
            purchased = purchased + listing.id)
    }
    fun sellUsed(position: Int): Progress {
        if (position !in used.indices) return this
        val item = used[position]
        return copy(coins = credit(DeviceSnapshot(item.phone,PhoneTraits(item.mutation,item.condition,item.uid),true).value),
            used = used.filterIndexed { i, _ -> i != position },world=if(item.uid>0)world.bumpEconomy() else world)
    }
    fun openCase(kind: CaseKind, ticket: Int, day: Long = System.currentTimeMillis()/Progression.DAY_MS,
                 mutationTicket: Int = 0, conditionTicket: Int = 9999,lootTicket: Int = 0): Pair<Progress, Int>? {
        require(mutationTicket in 0..9999 && conditionTicket in 0..9999 && lootTicket in 0..9999)
        if (coins < kind.cost || world.serial == Long.MAX_VALUE) return null
        val phone = kind.roll(ticket)
        val condition = when { conditionTicket < 1000 -> 1; conditionTicket < 3000 -> 2; else -> 3 }
        val next = forQuestDay(day).copy(questCases = (forQuestDay(day).questCases+1).coerceAtMost(3), coins = coins - kind.cost,
            casesOpened = (casesOpened.toLong()+1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
            .appendPhone(phone,PhoneTraits(Mutation.roll(mutationTicket),condition))
        val loot=if(lootTicket>=9500) 1 else 0
        return next.copy(world=next.world.copy(appTokens=(next.world.appTokens.toLong()+loot).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            skins=if(mutationTicket>=9900) next.world.skins+"rainbow" else next.world.skins)) to phone
    }
    fun tune(model: Int, firmwareName: String): Progress {
        require(model in owned || used.any { it.phone == model }) { "Телефона нет в коллекции" }
        require(firmwareName in Workshop.options(Catalog.phones[model]))
        return copy(firmware = firmware + (model to firmwareName))
    }
    fun toggleFavorite(model: Int): Progress {
        require(model in Catalog.phones.indices)
        return copy(favorites = if (model in favorites) favorites - model else favorites + model)
    }
    fun clickCoin(day: Long = System.currentTimeMillis()/Progression.DAY_MS, now: Long = System.currentTimeMillis()): Progress {
        val (guard, allowed) = clickGuard.tap(now)
        if (!allowed) return if (guard == clickGuard) this else copy(clickGuard = guard)
        val ready = forQuestDay(day)
        return ready.copy(coins = credit(Progression.CLICK_REWARD),
            clickGuard = guard,
            clicks = (clicks.toLong()+1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            questClicks = (ready.questClicks+1).coerceAtMost(100))
    }
    fun adminCredit(amount: Long): Progress {
        require(amount in 1L..Int.MAX_VALUE.toLong())
        return copy(coins = credit(amount.toInt()))
    }
    fun forQuestDay(day: Long): Progress {
        require(day >= 0)
        return if (day > questDay) copy(questDay = day, questClicks = 0, questCases = 0, claimedQuests = emptySet()) else this
    }
    fun claimQuest(id: String, day: Long): Progress {
        val ready = forQuestDay(day)
        if (day != ready.questDay || id in ready.claimedQuests) return ready
        val reward = when {
            id == "clicks" && ready.questClicks >= 100 -> 5000
            id == "cases" && ready.questCases >= 3 -> 10000
            else -> return ready
        }
        return ready.copy(coins = ready.credit(reward), claimedQuests = ready.claimedQuests + id)
    }
    fun claimDaily(day: Long): Progress {
        require(day >= 0L)
        if (day <= lastDailyDay) return this
        val streak = if (day == lastDailyDay + 1L) (dailyStreak+1).coerceAtMost(7) else 1
        return copy(coins = credit(Progression.dailyReward(streak)), lastDailyDay = day, dailyStreak = streak)
    }
    fun upgradeChance(source: Int, target: Int): Int {
        require(source in owned && target in Catalog.phones.indices && !Catalog.isUltra(target))
        val value=DeviceSnapshot(source,traitsAt(owned.indexOf(source))).value
        require(Catalog.phones[target].price > value)
        return (value.toLong()*750/Catalog.phones[target].price).toInt().coerceIn(1,750)
    }
    fun upgrade(source: Int, target: Int, ticket: Int): Pair<Progress, Boolean> {
        require(ticket in 0..999)
        val chance = upgradeChance(source, target)
        val won = ticket < chance
        val removed = removePhoneAt(owned.indexOf(source))
        return (if (won) removed.appendPhone(target) else removed) to won
    }
    fun syncClients(now: Long): Progress {
        require(now >= 0)
        val slot = maxOf(serviceSlot, now / Clients.INTERVAL)
        return copy(serviceSlot = slot, servicedSlots = servicedSlots.filter { it >= slot-2 }.toSet())
    }
    fun clientOffers(now: Long): List<ClientOffer> {
        val slot = maxOf(serviceSlot, now / Clients.INTERVAL)
        if (now / Clients.INTERVAL < serviceSlot) return emptyList()
        return ((slot-2).coerceAtLeast(0)..slot).filter { it !in servicedSlots && it != clientJob?.offer?.slot }
            .map { Clients.offer(it, workshopLevel) }
    }
    fun acceptClient(slot: Long, now: Long): Progress {
        val ready = syncClients(now)
        if (ready.clientJob != null) return ready
        val offer = ready.clientOffers(now).firstOrNull { it.slot == slot } ?: return ready
        if (workshopLevel < offer.kind.level || coins < offer.cost) return ready
        return ready.copy(coins = coins-offer.cost, clientJob = ClientJob(offer, now+offer.seconds*1000L))
    }
    fun finishClient(now: Long): Progress {
        val ready = syncClients(now)
        val job = ready.clientJob ?: return ready
        if (now < job.readyAt) return ready
        return ready.copy(coins = ready.credit(job.payout), clientJob = null,
            servicedSlots = ready.servicedSlots + job.offer.slot,
            reputation = (reputation.toLong()+1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            servicesCompleted = (servicesCompleted.toLong()+1).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
    }
    fun improveWorkshop(): Progress {
        if (workshopLevel >= 5) return this
        val cost = Clients.upgradeCosts[workshopLevel-1]
        if (coins < cost || reputation < Clients.reputationRequired[workshopLevel-1]) return this
        return copy(coins = coins-cost, workshopLevel = workshopLevel+1)
    }
    fun giftCooldownRemaining(now: Long = System.currentTimeMillis()): Long =
        if (lastGiftAt == 0L) 0L else (Economy.GIFT_COOLDOWN - (now - lastGiftAt)).coerceIn(0L, Economy.GIFT_COOLDOWN)

    fun canReceiveGift(listings: List<Listing>, now: Long = System.currentTimeMillis()): Boolean =
        giftCooldownRemaining(now) == 0L && coins < CaseKind.entries.minOf { it.cost } &&
            listings.none { it.id !in purchased && askPrice(it) <= coins }

    fun receiveGift(listings: List<Listing>, now: Long = System.currentTimeMillis()): Progress =
        if (canReceiveGift(listings, now)) bonus(now) else this

    // Both free-money entry points share the same persisted cooldown.
    fun bonus(now: Long = System.currentTimeMillis()): Progress =
        if (giftCooldownRemaining(now) == 0L) copy(coins = credit(Economy.BONUS), lastGiftAt = now) else this
    private fun credit(amount: Int) = (coins.toLong() + amount).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    companion object {
        fun restore(coins: Int, saved: String) = Progress(
            coins.coerceAtLeast(0),
            saved.split(',').mapNotNull { it.toIntOrNull() }.filter { it in Catalog.phones.indices }
        )
    }
}
