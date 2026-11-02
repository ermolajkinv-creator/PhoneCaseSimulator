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
        Phone("Samsung Galaxy A73", 23000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель Samsung Galaxy A73. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
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
        Phone("iPhone 12", 24000, Rarity.RARE, 1, "Данные уточняются", "Данные уточняются", "Версия зависит от обновлений", "Коллекционная модель iPhone 12. Комплектация и характеристики могут зависеть от рынка. Подробные технические данные пока не внесены."),
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
    )
    val phones: List<Phone> = entries.map { phone ->
        val budget = mapOf(Rarity.COMMON to 500, Rarity.UNCOMMON to 280, Rarity.RARE to 150,
            Rarity.EPIC to 58, Rarity.LEGENDARY to 11, Rarity.ULTRA to 1).getValue(phone.rarity)
        val peers = entries.filter { it.rarity == phone.rarity }
        val position = peers.indexOf(phone)
        phone.copy(tickets = budget / peers.size + if (position < budget % peers.size) 1 else 0)
    }
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

data class Progress(val coins: Int = Economy.START_BALANCE, val owned: List<Int> = emptyList(),
                    val used: List<UsedPhone> = emptyList(), val purchased: Set<String> = emptySet(), val firmware: Map<Int, String> = emptyMap()) {
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
    fun buy(listing: Listing): Progress? {
        if (listing.id in purchased || coins < listing.price) return null
        return copy(coins = coins - listing.price, used = used + UsedPhone(listing.phone, listing.condition),
            purchased = purchased + listing.id)
    }
    fun sellUsed(position: Int): Progress {
        if (position !in used.indices) return this
        val item = used[position]
        return copy(coins = credit(Market.price(item.phone, item.condition)),
            used = used.filterIndexed { i, _ -> i != position })
    }
    fun openCase(kind: CaseKind, ticket: Int): Pair<Progress, Int>? {
        if (coins < kind.cost) return null
        val phone = kind.roll(ticket)
        return copy(coins = coins - kind.cost, owned = owned + phone) to phone
    }
    fun tune(model: Int, firmwareName: String): Progress {
        require(model in owned || used.any { it.phone == model }) { "Телефона нет в коллекции" }
        require(firmwareName in Workshop.options(Catalog.phones[model]))
        return copy(firmware = firmware + (model to firmwareName))
    }
    fun canReceiveGift(listings: List<Listing>): Boolean =
        coins < CaseKind.entries.minOf { it.cost } &&
            listings.none { it.id !in purchased && it.price <= coins }

    fun receiveGift(listings: List<Listing>): Progress =
        if (canReceiveGift(listings)) copy(coins = credit(Economy.BONUS)) else this

    fun bonus() = copy(coins = credit(Economy.BONUS))
    private fun credit(amount: Int) = (coins.toLong() + amount).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    companion object {
        fun restore(coins: Int, saved: String) = Progress(
            coins.coerceAtLeast(0),
            saved.split(',').mapNotNull { it.toIntOrNull() }.filter { it in Catalog.phones.indices }
        )
    }
}
