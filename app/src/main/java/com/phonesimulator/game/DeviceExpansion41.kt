package com.phonesimulator.game

/** Append-only: 3 tablets and 50 distinct additional phone models. Prices are game valuations. */
internal object DeviceExpansion41 {
    val entries: List<Phone> = data.trimIndent().lineSequence().filter { it.isNotBlank() }.map { row ->
        val p=row.split('|')
        require(p.size==7)
        val price=p[2].toInt()
        val rarity=when { price<15000->Rarity.COMMON;price<30000->Rarity.UNCOMMON;price<60000->Rarity.RARE
            price<100000->Rarity.EPIC;else->Rarity.LEGENDARY }
        val form=if(p[6]=="tablet")FormFactor.TABLET else FormFactor.PHONE
        Phone(p[0],price,rarity,1,p[3],p[4],"${p[5]} при выпуске",
            "${p[0]} — ${if(form==FormFactor.TABLET) "планшет" else "смартфон"} ${p[1]} года. Данные относятся к указанной версии; регион и обновления могут отличаться. Цена — игровая оценка.",form)
    }.toList().also { require(it.size==53 && it.count { phone->phone.formFactor==FormFactor.TABLET }==3) }

    private val data: String get() = """
        Samsung Galaxy Tab S9 FE+|2023|45000|Samsung Exynos 1380|12,4″ TFT LCD · 90 Гц|Android 13 / One UI|tablet
        iPad Air 11-inch (M3)|2025|65000|Apple M3|11″ Liquid Retina IPS · 60 Гц|iPadOS 18|tablet
        Blackview Tab 12|2022|11000|Unisoc SC9863A|10,1″ IPS · 1920×1200|Android 11 / Doke OS_P|tablet
        Samsung Galaxy A56|2025|33000|Samsung Exynos 1580|6,7″ Super AMOLED · 120 Гц|Android 15 / One UI 7|phone
        Samsung Galaxy A36|2025|27000|Snapdragon 6 Gen 3|6,7″ Super AMOLED · 120 Гц|Android 15 / One UI 7|phone
        Samsung Galaxy A26|2025|21000|Samsung Exynos 1380|6,7″ Super AMOLED · 120 Гц|Android 15 / One UI 7|phone
        Samsung Galaxy S25|2025|65000|Snapdragon 8 Elite for Galaxy|6,2″ Dynamic AMOLED 2X · 120 Гц|Android 15 / One UI 7|phone
        Samsung Galaxy S25+|2025|80000|Snapdragon 8 Elite for Galaxy|6,7″ Dynamic AMOLED 2X · 120 Гц|Android 15 / One UI 7|phone
        Samsung Galaxy Z Fold7|2025|125000|Snapdragon 8 Elite for Galaxy|8,0″ внутренний / 6,5″ внешний AMOLED|Android 16 / One UI 8|phone
        Samsung Galaxy Z Flip7|2025|85000|Samsung Exynos 2500|6,9″ внутренний / 4,1″ внешний AMOLED|Android 16 / One UI 8|phone
        Google Pixel 9 Pro XL|2024|75000|Google Tensor G4|6,8″ LTPO OLED · 120 Гц|Android 14|phone
        Google Pixel 9 Pro Fold|2024|105000|Google Tensor G4|8,0″ внутренний / 6,3″ внешний OLED|Android 14|phone
        Google Pixel 10|2025|65000|Google Tensor G5|6,3″ OLED · 120 Гц|Android 16|phone
        Google Pixel 10 Pro XL|2025|100000|Google Tensor G5|6,8″ LTPO OLED · 120 Гц|Android 16|phone
        Google Pixel 10 Pro Fold|2025|130000|Google Tensor G5|8,0″ внутренний / 6,4″ внешний OLED|Android 16|phone
        Google Pixel 9a|2025|40000|Google Tensor G4|6,3″ OLED · 120 Гц|Android 15|phone
        iPhone 13 Pro Max|2021|48000|Apple A15 Bionic|6,7″ OLED · ProMotion|iOS 15|phone
        iPhone 14 Plus|2022|38000|Apple A15 Bionic|6,7″ Super Retina XDR OLED|iOS 16|phone
        iPhone 14 Pro Max|2022|60000|Apple A16 Bionic|6,7″ OLED · ProMotion|iOS 16|phone
        iPhone 15 Plus|2023|55000|Apple A16 Bionic|6,7″ Super Retina XDR OLED|iOS 17|phone
        iPhone 15 Pro|2023|70000|Apple A17 Pro|6,1″ OLED · ProMotion|iOS 17|phone
        iPhone 16 Plus|2024|75000|Apple A18|6,7″ Super Retina XDR OLED|iOS 18|phone
        iPhone 16 Pro Max|2024|110000|Apple A18 Pro|6,9″ OLED · ProMotion|iOS 18|phone
        iPhone 16e|2025|50000|Apple A18|6,1″ Super Retina XDR OLED|iOS 18|phone
        iPhone 17|2025|85000|Apple A19|6,3″ OLED · ProMotion|iOS 26|phone
        iPhone 17 Pro Max|2025|140000|Apple A19 Pro|6,9″ OLED · ProMotion|iOS 26|phone
        iPhone Air|2025|100000|Apple A19 Pro|6,5″ OLED · ProMotion|iOS 26|phone
        OnePlus 12R|2024|35000|Snapdragon 8 Gen 2|6,78″ LTPO AMOLED · 120 Гц|Android 14 / OxygenOS|phone
        OnePlus 13R|2025|45000|Snapdragon 8 Gen 3|6,78″ LTPO AMOLED · 120 Гц|Android 15 / OxygenOS|phone
        OnePlus Nord 4|2024|30000|Snapdragon 7+ Gen 3|6,74″ AMOLED · 120 Гц|Android 14 / OxygenOS|phone
        OnePlus Nord CE4|2024|23000|Snapdragon 7 Gen 3|6,7″ AMOLED · 120 Гц|Android 14 / OxygenOS|phone
        OnePlus 10T|2022|29000|Snapdragon 8+ Gen 1|6,7″ AMOLED · 120 Гц|Android 12 / OxygenOS|phone
        OnePlus Open|2023|90000|Snapdragon 8 Gen 2|7,82″ внутренний / 6,31″ внешний OLED|Android 13 / OxygenOS|phone
        Xiaomi 15|2024|60000|Snapdragon 8 Elite|6,36″ LTPO OLED · 120 Гц|Android 15 / HyperOS 2|phone
        Xiaomi 15 Ultra|2025|95000|Snapdragon 8 Elite|6,73″ LTPO OLED · 120 Гц|Android 15 / HyperOS 2|phone
        Xiaomi 14 Pro|2023|60000|Snapdragon 8 Gen 3|6,73″ LTPO OLED · 120 Гц|Android 14 / HyperOS|phone
        Xiaomi MIX Flip|2024|70000|Snapdragon 8 Gen 3|6,86″ внутренний / 4,01″ внешний AMOLED|Android 14 / HyperOS|phone
        Redmi Note 14 Pro 5G|2024|26000|Dimensity 7300-Ultra|6,67″ AMOLED · 120 Гц|Android 14 / HyperOS|phone
        Redmi Note 14 Pro+ 5G|2024|33000|Snapdragon 7s Gen 3|6,67″ AMOLED · 120 Гц|Android 14 / HyperOS|phone
        Redmi 14C|2024|10000|Helio G81-Ultra|6,88″ IPS LCD · 120 Гц|Android 14 / HyperOS|phone
        Redmi A5|2025|8000|Unisoc T7250|6,88″ IPS LCD · 120 Гц|Android 15 Go|phone
        POCO X7|2025|24000|Dimensity 7300-Ultra|6,67″ AMOLED · 120 Гц|Android 14 / HyperOS|phone
        POCO X7 Pro|2025|35000|Dimensity 8400-Ultra|6,67″ AMOLED · 120 Гц|Android 15 / HyperOS 2|phone
        POCO M6 Pro 4G|2024|16000|Helio G99-Ultra|6,67″ AMOLED · 120 Гц|Android 13 / MIUI|phone
        HONOR X8c|2025|20000|Snapdragon 685|6,7″ AMOLED · 120 Гц|Android 15 / MagicOS 9|phone
        HONOR X9c|2024|27000|Snapdragon 6 Gen 1|6,78″ AMOLED · 120 Гц|Android 14 / MagicOS 8|phone
        HONOR 200 Lite|2024|17000|Dimensity 6080|6,7″ AMOLED · 90 Гц|Android 14 / MagicOS 8|phone
        HONOR Magic7 Pro|2024|75000|Snapdragon 8 Elite|6,8″ LTPO OLED · 120 Гц|Android 15 / MagicOS 9|phone
        Infinix NOTE 50 Pro|2025|22000|Helio G100 Ultimate|6,78″ AMOLED · 144 Гц|Android 15 / XOS 15|phone
        Infinix HOT 50 Pro+|2024|17000|Helio G100|6,78″ AMOLED · 120 Гц|Android 14 / XOS|phone
        TECNO Camon 40 Pro 5G|2025|25000|Dimensity 7300|6,78″ AMOLED · 144 Гц|Android 15 / HiOS|phone
        realme GT 7 Pro|2024|60000|Snapdragon 8 Elite|6,78″ LTPO AMOLED · 120 Гц|Android 15 / realme UI|phone
        Nothing Phone (3a)|2025|30000|Snapdragon 7s Gen 3|6,77″ AMOLED · 120 Гц|Android 15 / Nothing OS|phone
    """
}
