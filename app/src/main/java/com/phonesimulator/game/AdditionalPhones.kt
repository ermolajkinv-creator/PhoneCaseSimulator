package com.phonesimulator.game

/** Append-only expansion: positions 100..499, separate hardware models, not colour/storage variants.
 * Prices are offline game valuations in rubles, not current retail quotes.
 * System describes the launch platform; regional hardware variants are noted in chip names.
 */
internal object AdditionalPhones {
    val entries: List<Phone> = rows.trimIndent().lineSequence().filter { it.isNotBlank() }.map { row ->
        val fields = row.split('|')
        require(fields.size == 6) { "Invalid phone row: $row" }
        val (name, year, value, chip, display) = fields
        val price = value.toInt()
        val rarity = when {
            price < 15000 -> Rarity.COMMON
            price < 30000 -> Rarity.UNCOMMON
            price < 60000 -> Rarity.RARE
            else -> Rarity.EPIC
        }
        Phone(name, price, rarity, 1, chip, display, "${fields[5]} при выпуске",
            "$name — модель $year года. Характеристики описывают указанную версию; регион, обновления и состояние могут отличаться. Цена — игровая оценка, иллюстрация стилизована.")
    }.toList().also { require(it.size == 400) }

    private val rows: String
        get() = """
            Samsung Galaxy S|2010|2000|Exynos 3110|4,0″ Super AMOLED|Android 2.1
            Samsung Galaxy S II|2011|2500|Exynos 4210|4,3″ Super AMOLED Plus|Android 2.3
            Samsung Galaxy S III|2012|3000|Exynos 4412|4,8″ Super AMOLED|Android 4.0
            Samsung Galaxy S4|2013|3500|Зависит от региона|5,0″ Super AMOLED|Android 4.2
            Samsung Galaxy S5|2014|4000|Snapdragon 801 (основная версия)|5,1″ Super AMOLED|Android 4.4
            Samsung Galaxy S6|2015|5000|Exynos 7420|5,1″ Super AMOLED|Android 5.0
            Samsung Galaxy S6 edge|2015|5500|Exynos 7420|5,1″ Super AMOLED|Android 5.0
            Samsung Galaxy S6 edge+|2015|6000|Exynos 7420|5,7″ Super AMOLED|Android 5.1
            Samsung Galaxy S7|2016|6500|Exynos 8890 / Snapdragon 820|5,1″ Super AMOLED|Android 6.0
            Samsung Galaxy S7 edge|2016|7500|Exynos 8890 / Snapdragon 820|5,5″ Super AMOLED|Android 6.0
            Samsung Galaxy S8|2017|8000|Exynos 8895 / Snapdragon 835|5,8″ Super AMOLED|Android 7.0
            Samsung Galaxy S8+|2017|9000|Exynos 8895 / Snapdragon 835|6,2″ Super AMOLED|Android 7.0
            Samsung Galaxy S9|2018|9500|Exynos 9810 / Snapdragon 845|5,8″ Super AMOLED|Android 8.0
            Samsung Galaxy S9+|2018|11000|Exynos 9810 / Snapdragon 845|6,2″ Super AMOLED|Android 8.0
            Samsung Galaxy S10e|2019|10000|Exynos 9820 / Snapdragon 855|5,8″ Dynamic AMOLED|Android 9
            Samsung Galaxy S10+|2019|14000|Exynos 9820 / Snapdragon 855|6,4″ Dynamic AMOLED|Android 9
            Samsung Galaxy S10 5G|2019|16000|Exynos 9820 / Snapdragon 855|6,7″ Dynamic AMOLED|Android 9
            Samsung Galaxy S10 Lite|2020|13000|Snapdragon 855|6,7″ Super AMOLED Plus|Android 10
            Samsung Galaxy S20|2020|15000|Exynos 990 / Snapdragon 865|6,2″ Dynamic AMOLED 2X|Android 10
            Samsung Galaxy S20+|2020|18000|Exynos 990 / Snapdragon 865|6,7″ Dynamic AMOLED 2X|Android 10
            Samsung Galaxy S20 Ultra|2020|23000|Exynos 990 / Snapdragon 865|6,9″ Dynamic AMOLED 2X|Android 10
            Samsung Galaxy S21+|2021|22000|Exynos 2100 / Snapdragon 888|6,7″ Dynamic AMOLED 2X|Android 11
            Samsung Galaxy S21 Ultra|2021|30000|Exynos 2100 / Snapdragon 888|6,8″ Dynamic AMOLED 2X|Android 11
            Samsung Galaxy S21 FE|2022|20000|Snapdragon 888 / Exynos 2100|6,4″ Dynamic AMOLED 2X|Android 12
            Samsung Galaxy S22+|2022|28000|Exynos 2200 / Snapdragon 8 Gen 1|6,6″ Dynamic AMOLED 2X|Android 12
            Samsung Galaxy S22 Ultra|2022|38000|Exynos 2200 / Snapdragon 8 Gen 1|6,8″ Dynamic AMOLED 2X|Android 12
            Samsung Galaxy S23|2023|32000|Snapdragon 8 Gen 2 for Galaxy|6,1″ Dynamic AMOLED 2X|Android 13
            Samsung Galaxy S23+|2023|40000|Snapdragon 8 Gen 2 for Galaxy|6,6″ Dynamic AMOLED 2X|Android 13
            Samsung Galaxy S23 FE|2023|27000|Exynos 2200 / Snapdragon 8 Gen 1|6,4″ Dynamic AMOLED 2X|Android 13
            Samsung Galaxy S24+|2024|55000|Exynos 2400 / Snapdragon 8 Gen 3|6,7″ Dynamic AMOLED 2X|Android 14
            Samsung Galaxy S24 FE|2024|40000|Exynos 2400e|6,7″ Dynamic AMOLED 2X|Android 14
            Samsung Galaxy Note|2011|3000|Exynos 4210|5,3″ Super AMOLED|Android 2.3
            Samsung Galaxy Note II|2012|3500|Exynos 4412|5,5″ Super AMOLED|Android 4.1
            Samsung Galaxy Note 3|2013|4500|Snapdragon 800 / Exynos 5420|5,7″ Super AMOLED|Android 4.3
            Samsung Galaxy Note 4|2014|5500|Snapdragon 805 / Exynos 5433|5,7″ Super AMOLED|Android 4.4
            Samsung Galaxy Note5|2015|6500|Exynos 7420|5,7″ Super AMOLED|Android 5.1
            Samsung Galaxy Note8|2017|9000|Exynos 8895 / Snapdragon 835|6,3″ Super AMOLED|Android 7.1
            Samsung Galaxy Note9|2018|13000|Exynos 9810 / Snapdragon 845|6,4″ Super AMOLED|Android 8.1
            Samsung Galaxy Note10|2019|16000|Exynos 9825|6,3″ Dynamic AMOLED|Android 9
            Samsung Galaxy Note10+|2019|20000|Exynos 9825|6,8″ Dynamic AMOLED|Android 9
            Samsung Galaxy Note10 Lite|2020|14000|Exynos 9810|6,7″ Super AMOLED|Android 10
            Samsung Galaxy Note20|2020|24000|Exynos 990 / Snapdragon 865+|6,7″ Super AMOLED Plus|Android 10
            Samsung Galaxy Fold|2019|35000|Snapdragon 855|7,3″ складной AMOLED|Android 9
            Samsung Galaxy Z Fold2|2020|42000|Snapdragon 865+|7,6″ складной AMOLED|Android 10
            Samsung Galaxy Z Fold3|2021|50000|Snapdragon 888|7,6″ складной AMOLED|Android 11
            Samsung Galaxy Z Fold4|2022|65000|Snapdragon 8+ Gen 1|7,6″ складной AMOLED|Android 12L
            Samsung Galaxy Z Flip|2020|24000|Snapdragon 855+|6,7″ складной AMOLED|Android 10
            Samsung Galaxy Z Flip 5G|2020|27000|Snapdragon 865+|6,7″ складной AMOLED|Android 10
            Samsung Galaxy Z Flip3|2021|30000|Snapdragon 888|6,7″ складной AMOLED|Android 11
            Samsung Galaxy Z Flip4|2022|37000|Snapdragon 8+ Gen 1|6,7″ складной AMOLED|Android 12
            Samsung Galaxy Z Flip6|2024|65000|Snapdragon 8 Gen 3 for Galaxy|6,7″ складной AMOLED|Android 14
            Samsung Galaxy A01|2019|4000|Snapdragon 439|5,7″ PLS LCD|Android 10
            Samsung Galaxy A02|2021|4500|MediaTek MT6739W|6,5″ PLS LCD|Android 10
            Samsung Galaxy A02s|2020|5000|Snapdragon 450|6,5″ PLS LCD|Android 10
            Samsung Galaxy A03|2021|5500|Unisoc T606|6,5″ PLS LCD|Android 11
            Samsung Galaxy A03s|2021|5500|Helio P35|6,5″ PLS LCD|Android 11
            Samsung Galaxy A04|2022|6000|Helio P35|6,5″ PLS LCD|Android 12
            Samsung Galaxy A04s|2022|6500|Exynos 850|6,5″ PLS LCD|Android 12
            Samsung Galaxy A05s|2023|8500|Snapdragon 680|6,7″ PLS LCD|Android 13
            Samsung Galaxy A06|2024|9000|Helio G85|6,7″ PLS LCD|Android 14
            Samsung Galaxy A10|2019|4500|Exynos 7884|6,2″ IPS LCD|Android 9
            Samsung Galaxy A10s|2019|5000|Helio P22|6,2″ PLS LCD|Android 9
            Samsung Galaxy A11|2020|5500|Snapdragon 450|6,4″ PLS LCD|Android 10
            Samsung Galaxy A12|2020|6500|Helio P35|6,5″ PLS LCD|Android 10
            Samsung Galaxy A13|2022|8000|Exynos 850 (4G)|6,6″ PLS LCD|Android 12
            Samsung Galaxy A20|2019|5500|Exynos 7884|6,4″ Super AMOLED|Android 9
            Samsung Galaxy A20s|2019|6000|Snapdragon 450|6,5″ IPS LCD|Android 9
            Samsung Galaxy A21s|2020|7000|Exynos 850|6,5″ PLS LCD|Android 10
            Samsung Galaxy A22|2021|8500|Helio G80 (4G)|6,4″ Super AMOLED|Android 11
            Samsung Galaxy A23|2022|10000|Snapdragon 680 (4G)|6,6″ PLS LCD|Android 12
            Samsung Galaxy A24|2023|12000|Helio G99|6,5″ Super AMOLED|Android 13
            Samsung Galaxy A30|2019|6500|Exynos 7904|6,4″ Super AMOLED|Android 9
            Samsung Galaxy A30s|2019|7000|Exynos 7904|6,4″ Super AMOLED|Android 9
            Samsung Galaxy A31|2020|8500|Helio P65|6,4″ Super AMOLED|Android 10
            Samsung Galaxy A32|2021|10000|Helio G80 (4G)|6,4″ Super AMOLED|Android 11
            Samsung Galaxy A33 5G|2022|13000|Exynos 1280|6,4″ Super AMOLED|Android 12
            Samsung Galaxy A50|2019|8000|Exynos 9610|6,4″ Super AMOLED|Android 9
            Samsung Galaxy A51|2019|9500|Exynos 9611|6,5″ Super AMOLED|Android 10
            Samsung Galaxy A52|2021|14000|Snapdragon 720G (4G)|6,5″ Super AMOLED|Android 11
            Samsung Galaxy A53 5G|2022|17000|Exynos 1280|6,5″ Super AMOLED|Android 12
            Xiaomi Mi 3|2013|2500|Snapdragon 800 (WCDMA)|5,0″ IPS|Android 4.3 / MIUI
            Xiaomi Mi 4|2014|3000|Snapdragon 801|5,0″ IPS|Android 4.4 / MIUI
            Xiaomi Mi 4c|2015|3500|Snapdragon 808|5,0″ IPS|Android 5.1 / MIUI
            Xiaomi Mi 5|2016|4000|Snapdragon 820|5,15″ IPS|Android 6 / MIUI
            Xiaomi Mi 5s|2016|4500|Snapdragon 821|5,15″ IPS|Android 6 / MIUI
            Xiaomi Mi 5s Plus|2016|5000|Snapdragon 821|5,7″ IPS|Android 6 / MIUI
            Xiaomi Mi 6|2017|6000|Snapdragon 835|5,15″ IPS|Android 7.1 / MIUI
            Xiaomi Mi 8|2018|7500|Snapdragon 845|6,21″ AMOLED|Android 8.1 / MIUI
            Xiaomi Mi 8 Lite|2018|5500|Snapdragon 660|6,26″ IPS|Android 8.1 / MIUI
            Xiaomi Mi 9|2019|9000|Snapdragon 855|6,39″ AMOLED|Android 9 / MIUI
            Xiaomi Mi 9 SE|2019|8500|Snapdragon 712|5,97″ AMOLED|Android 9 / MIUI
            Xiaomi Mi 9T|2019|9500|Snapdragon 730|6,39″ AMOLED|Android 9 / MIUI
            Xiaomi Mi 9T Pro|2019|12000|Snapdragon 855|6,39″ AMOLED|Android 9 / MIUI
            Xiaomi Mi 10|2020|16000|Snapdragon 865|6,67″ AMOLED|Android 10 / MIUI
            Xiaomi Mi 10 Pro|2020|21000|Snapdragon 865|6,67″ AMOLED|Android 10 / MIUI
            Xiaomi Mi 10T|2020|14000|Snapdragon 865|6,67″ IPS|Android 10 / MIUI
            Xiaomi Mi 10T Pro|2020|16000|Snapdragon 865|6,67″ IPS|Android 10 / MIUI
            Xiaomi Mi 10 Lite 5G|2020|12000|Snapdragon 765G|6,57″ AMOLED|Android 10 / MIUI
            Xiaomi Mi 11|2021|23000|Snapdragon 888|6,81″ AMOLED|Android 11 / MIUI
            Xiaomi Mi 11 Ultra|2021|35000|Snapdragon 888|6,81″ AMOLED|Android 11 / MIUI
            Xiaomi Mi 11 Lite|2021|12000|Snapdragon 732G (4G)|6,55″ AMOLED|Android 11 / MIUI
            Xiaomi 11 Lite 5G NE|2021|15000|Snapdragon 778G|6,55″ AMOLED|Android 11 / MIUI
            Xiaomi 11T|2021|17000|Dimensity 1200-Ultra|6,67″ AMOLED|Android 11 / MIUI
            Xiaomi 11T Pro|2021|21000|Snapdragon 888|6,67″ AMOLED|Android 11 / MIUI
            Xiaomi 12X|2021|23000|Snapdragon 870|6,28″ AMOLED|Android 11 / MIUI
            Xiaomi 12 Pro|2021|32000|Snapdragon 8 Gen 1|6,73″ AMOLED|Android 12 / MIUI
            Xiaomi 12 Lite|2022|19000|Snapdragon 778G|6,55″ AMOLED|Android 12 / MIUI
            Xiaomi 12T|2022|25000|Dimensity 8100-Ultra|6,67″ AMOLED|Android 12 / MIUI
            Xiaomi 12T Pro|2022|31000|Snapdragon 8+ Gen 1|6,67″ AMOLED|Android 12 / MIUI
            Xiaomi 13|2022|38000|Snapdragon 8 Gen 2|6,36″ AMOLED|Android 13 / MIUI
            Xiaomi 13 Pro|2022|48000|Snapdragon 8 Gen 2|6,73″ AMOLED|Android 13 / MIUI
            Xiaomi 13 Ultra|2023|58000|Snapdragon 8 Gen 2|6,73″ AMOLED|Android 13 / MIUI
            Xiaomi 13 Lite|2023|22000|Snapdragon 7 Gen 1|6,55″ AMOLED|Android 12 / MIUI
            Xiaomi 13T Pro|2023|39000|Dimensity 9200+|6,67″ AMOLED|Android 13 / MIUI
            Xiaomi 14|2023|50000|Snapdragon 8 Gen 3|6,36″ OLED|Android 14 / HyperOS
            Xiaomi 14T|2024|38000|Dimensity 8300-Ultra|6,67″ AMOLED|Android 14 / HyperOS
            Xiaomi 14T Pro|2024|52000|Dimensity 9300+|6,67″ AMOLED|Android 14 / HyperOS
            Xiaomi Mi MIX|2016|6500|Snapdragon 821|6,4″ IPS|Android 6 / MIUI
            Xiaomi Mi MIX 2|2017|7500|Snapdragon 835|5,99″ IPS|Android 7.1 / MIUI
            Xiaomi Mi MIX 2S|2018|9000|Snapdragon 845|5,99″ IPS|Android 8 / MIUI
            Xiaomi Mi MIX 3|2018|10000|Snapdragon 845|6,39″ AMOLED|Android 9 / MIUI
            Xiaomi MIX 4|2021|30000|Snapdragon 888+|6,67″ AMOLED|Android 11 / MIUI
            Xiaomi MIX Fold 2|2022|65000|Snapdragon 8+ Gen 1|8,02″ складной OLED|Android 12 / MIUI
            Xiaomi MIX Fold 3|2023|85000|Snapdragon 8 Gen 2|8,03″ складной OLED|Android 13 / MIUI
            Redmi Note 4|2016|3000|Helio X20|5,5″ IPS|Android 6 / MIUI
            Redmi Note 4X|2017|3500|Snapdragon 625 (основная версия)|5,5″ IPS|Android 6 / MIUI
            Redmi Note 5|2018|4500|Snapdragon 636 (глобальная версия)|5,99″ IPS|Android 8 / MIUI
            Redmi Note 6 Pro|2018|5000|Snapdragon 636|6,26″ IPS|Android 8.1 / MIUI
            Redmi Note 7|2019|5500|Snapdragon 660|6,3″ IPS|Android 9 / MIUI
            Redmi Note 8|2019|6000|Snapdragon 665|6,3″ IPS|Android 9 / MIUI
            Redmi Note 8T|2019|6500|Snapdragon 665|6,3″ IPS|Android 9 / MIUI
            Redmi Note 9|2020|7000|Helio G85|6,53″ IPS|Android 10 / MIUI
            Redmi Note 9S|2020|8000|Snapdragon 720G|6,67″ IPS|Android 10 / MIUI
            Redmi Note 10|2021|9000|Snapdragon 678 (4G)|6,43″ AMOLED|Android 11 / MIUI
            Redmi Note 10 Pro|2021|13000|Snapdragon 732G (глобальная версия)|6,67″ AMOLED|Android 11 / MIUI
            Redmi Note 11S|2022|11000|Helio G96 (4G)|6,43″ AMOLED|Android 11 / MIUI
            Redmi Note 11 Pro 5G|2022|15000|Snapdragon 695|6,67″ AMOLED|Android 11 / MIUI
            Redmi Note 12 Pro 5G|2022|19000|Dimensity 1080|6,67″ OLED|Android 12 / MIUI
            Redmi Note 12 Pro+ 5G|2022|24000|Dimensity 1080|6,67″ OLED|Android 12 / MIUI
            Redmi Note 13 Pro 5G|2023|25000|Snapdragon 7s Gen 2|6,67″ AMOLED|Android 13 / MIUI
            Redmi Note 13 Pro+ 5G|2023|30000|Dimensity 7200-Ultra|6,67″ AMOLED|Android 13 / MIUI
            Redmi 7|2019|4000|Snapdragon 632|6,26″ IPS|Android 9 / MIUI
            Redmi 8|2019|4500|Snapdragon 439|6,22″ IPS|Android 9 / MIUI
            Redmi 9|2020|5500|Helio G80|6,53″ IPS|Android 10 / MIUI
            POCO F1|2018|9000|Snapdragon 845|6,18″ IPS|Android 8.1 / MIUI
            POCO F2 Pro|2020|18000|Snapdragon 865|6,67″ AMOLED|Android 10 / MIUI
            POCO F4 GT|2022|25000|Snapdragon 8 Gen 1|6,67″ AMOLED|Android 12 / MIUI
            POCO F5 Pro|2023|31000|Snapdragon 8+ Gen 1|6,67″ AMOLED|Android 13 / MIUI
            POCO F6 Pro|2024|40000|Snapdragon 8 Gen 2|6,67″ AMOLED|Android 14 / HyperOS
            POCO X6 Pro|2024|29000|Dimensity 8300-Ultra|6,67″ AMOLED|Android 14 / HyperOS
            iPhone 3G|2008|1500|Samsung S5L8900|3,5″ LCD|iPhone OS 2
            iPhone 3GS|2009|2000|Samsung S5PC100|3,5″ LCD|iPhone OS 3
            iPhone 4|2010|2500|Apple A4|3,5″ IPS|iOS 4
            iPhone 4s|2011|3000|Apple A5|3,5″ IPS|iOS 5
            iPhone 5|2012|3500|Apple A6|4,0″ IPS|iOS 6
            iPhone 5c|2013|3500|Apple A6|4,0″ IPS|iOS 7
            iPhone 5s|2013|4000|Apple A7|4,0″ IPS|iOS 7
            iPhone 6|2014|4500|Apple A8|4,7″ IPS|iOS 8
            iPhone 6 Plus|2014|5000|Apple A8|5,5″ IPS|iOS 8
            iPhone 6s|2015|5500|Apple A9|4,7″ IPS|iOS 9
            iPhone 6s Plus|2015|6500|Apple A9|5,5″ IPS|iOS 9
            iPhone SE (2016)|2016|5000|Apple A9|4,0″ IPS|iOS 9
            iPhone 7|2016|7000|Apple A10 Fusion|4,7″ IPS|iOS 10
            iPhone 7 Plus|2016|9000|Apple A10 Fusion|5,5″ IPS|iOS 10
            iPhone 8|2017|10000|Apple A11 Bionic|4,7″ IPS|iOS 11
            iPhone 8 Plus|2017|13000|Apple A11 Bionic|5,5″ IPS|iOS 11
            iPhone X|2017|15000|Apple A11 Bionic|5,8″ OLED|iOS 11
            iPhone XR|2018|16000|Apple A12 Bionic|6,1″ IPS|iOS 12
            iPhone XS|2018|17000|Apple A12 Bionic|5,8″ OLED|iOS 12
            iPhone XS Max|2018|21000|Apple A12 Bionic|6,5″ OLED|iOS 12
            iPhone 11 Pro|2019|23000|Apple A13 Bionic|5,8″ OLED|iOS 13
            iPhone 11 Pro Max|2019|27000|Apple A13 Bionic|6,5″ OLED|iOS 13
            iPhone 12 Pro|2020|30000|Apple A14 Bionic|6,1″ OLED|iOS 14
            iPhone 12 Pro Max|2020|35000|Apple A14 Bionic|6,7″ OLED|iOS 14
            iPhone 13 Pro|2021|42000|Apple A15 Bionic|6,1″ OLED ProMotion|iOS 15
            Google Nexus 4|2012|2500|Snapdragon S4 Pro|4,7″ IPS|Android 4.2
            Google Nexus 5|2013|3500|Snapdragon 800|4,95″ IPS|Android 4.4
            Google Nexus 5X|2015|4500|Snapdragon 808|5,2″ IPS|Android 6
            Google Nexus 6P|2015|5500|Snapdragon 810|5,7″ AMOLED|Android 6
            Google Pixel|2016|6000|Snapdragon 821|5,0″ AMOLED|Android 7.1
            Google Pixel XL|2016|7000|Snapdragon 821|5,5″ AMOLED|Android 7.1
            Google Pixel 2|2017|7500|Snapdragon 835|5,0″ AMOLED|Android 8
            Google Pixel 2 XL|2017|8500|Snapdragon 835|6,0″ P-OLED|Android 8
            Google Pixel 3|2018|9000|Snapdragon 845|5,5″ P-OLED|Android 9
            Google Pixel 3 XL|2018|10000|Snapdragon 845|6,3″ P-OLED|Android 9
            Google Pixel 3a|2019|8000|Snapdragon 670|5,6″ OLED|Android 9
            Google Pixel 3a XL|2019|9000|Snapdragon 670|6,0″ OLED|Android 9
            Google Pixel 4 XL|2019|12000|Snapdragon 855|6,3″ OLED|Android 10
            Google Pixel 4a|2020|11000|Snapdragon 730G|5,81″ OLED|Android 10
            Google Pixel 4a 5G|2020|14000|Snapdragon 765G|6,2″ OLED|Android 11
            OnePlus One|2014|4000|Snapdragon 801|5,5″ IPS|Android 4.4 / Cyanogen OS
            OnePlus 2|2015|4500|Snapdragon 810|5,5″ IPS|Android 5.1 / OxygenOS
            OnePlus X|2015|4000|Snapdragon 801|5,0″ AMOLED|Android 5.1 / OxygenOS
            OnePlus 3|2016|5500|Snapdragon 820|5,5″ Optic AMOLED|Android 6 / OxygenOS
            OnePlus 3T|2016|6500|Snapdragon 821|5,5″ Optic AMOLED|Android 6 / OxygenOS
            OnePlus 5|2017|7500|Snapdragon 835|5,5″ Optic AMOLED|Android 7.1 / OxygenOS
            OnePlus 5T|2017|8500|Snapdragon 835|6,01″ Optic AMOLED|Android 7.1 / OxygenOS
            OnePlus 6|2018|9500|Snapdragon 845|6,28″ Optic AMOLED|Android 8.1 / OxygenOS
            OnePlus 6T|2018|11000|Snapdragon 845|6,41″ Optic AMOLED|Android 9 / OxygenOS
            OnePlus 7|2019|13000|Snapdragon 855|6,41″ Optic AMOLED|Android 9 / OxygenOS
            OnePlus 7 Pro|2019|16000|Snapdragon 855|6,67″ Fluid AMOLED|Android 9 / OxygenOS
            OnePlus 7T|2019|15000|Snapdragon 855+|6,55″ Fluid AMOLED|Android 10 / OxygenOS
            OnePlus 7T Pro|2019|18000|Snapdragon 855+|6,67″ Fluid AMOLED|Android 10 / OxygenOS
            OnePlus 8|2020|18000|Snapdragon 865|6,55″ Fluid AMOLED|Android 10 / OxygenOS
            OnePlus 8 Pro|2020|23000|Snapdragon 865|6,78″ Fluid AMOLED|Android 10 / OxygenOS
            OnePlus 8T|2020|21000|Snapdragon 865|6,55″ Fluid AMOLED|Android 11 / OxygenOS
            OnePlus 9|2021|24000|Snapdragon 888|6,55″ Fluid AMOLED|Android 11 / OxygenOS
            OnePlus 9 Pro|2021|29000|Snapdragon 888|6,7″ LTPO AMOLED|Android 11 / OxygenOS
            OnePlus 10 Pro|2022|34000|Snapdragon 8 Gen 1|6,7″ LTPO AMOLED|Android 12 / OxygenOS
            OnePlus 11|2023|43000|Snapdragon 8 Gen 2|6,7″ LTPO AMOLED|Android 13 / OxygenOS
            HONOR 6|2014|3000|Kirin 920|5,0″ IPS|Android 4.4 / EMUI
            HONOR 7|2015|3500|Kirin 935|5,2″ IPS|Android 5 / EMUI
            HONOR 8|2016|4500|Kirin 950|5,2″ IPS|Android 6 / EMUI
            HONOR 9|2017|5500|Kirin 960|5,15″ IPS|Android 7 / EMUI
            HONOR 10|2018|7000|Kirin 970|5,84″ IPS|Android 8.1 / EMUI
            HONOR 10 Lite|2018|5000|Kirin 710|6,21″ IPS|Android 9 / EMUI
            HONOR 20|2019|9500|Kirin 980|6,26″ IPS|Android 9 / Magic UI
            HONOR 20 Pro|2019|12000|Kirin 980|6,26″ IPS|Android 9 / Magic UI
            HONOR View 10|2017|6500|Kirin 970|5,99″ IPS|Android 8 / EMUI
            HONOR View 20|2018|10000|Kirin 980|6,4″ IPS|Android 9 / Magic UI
            HONOR 50|2021|16000|Snapdragon 778G|6,57″ OLED|Android 11 / Magic UI
            HONOR 50 Lite|2021|9500|Snapdragon 662|6,67″ IPS|Android 11 / Magic UI
            HONOR 70|2022|22000|Snapdragon 778G+|6,67″ OLED|Android 12 / Magic UI
            HONOR 90 Lite|2023|15000|Dimensity 6020|6,7″ IPS|Android 13 / MagicOS
            HONOR 200 Pro|2024|45000|Snapdragon 8s Gen 3|6,78″ OLED|Android 14 / MagicOS
            HONOR Magic4 Pro|2022|35000|Snapdragon 8 Gen 1|6,81″ LTPO OLED|Android 12 / Magic UI
            HONOR Magic5 Lite|2023|17000|Snapdragon 695|6,67″ AMOLED|Android 12 / Magic UI
            HONOR X7|2022|9000|Snapdragon 680|6,74″ IPS|Android 11 / Magic UI
            HONOR X7a|2023|10000|Helio G37|6,75″ IPS|Android 12 / Magic UI
            HONOR X9a|2023|18000|Snapdragon 695|6,67″ AMOLED|Android 12 / Magic UI
            HUAWEI P8|2015|3500|Kirin 930|5,2″ IPS|Android 5 / EMUI
            HUAWEI P9|2016|4500|Kirin 955|5,2″ IPS|Android 6 / EMUI
            HUAWEI P9 Lite|2016|3500|Kirin 650|5,2″ IPS|Android 6 / EMUI
            HUAWEI P10|2017|5500|Kirin 960|5,1″ IPS|Android 7 / EMUI
            HUAWEI P10 Plus|2017|6500|Kirin 960|5,5″ IPS|Android 7 / EMUI
            HUAWEI P20|2018|7500|Kirin 970|5,8″ IPS|Android 8.1 / EMUI
            HUAWEI P20 Pro|2018|10000|Kirin 970|6,1″ OLED|Android 8.1 / EMUI
            HUAWEI P20 Lite|2018|5000|Kirin 659|5,84″ IPS|Android 8 / EMUI
            HUAWEI P30|2019|12000|Kirin 980|6,1″ OLED|Android 9 / EMUI
            HUAWEI P30 Pro|2019|18000|Kirin 980|6,47″ OLED|Android 9 / EMUI
            HUAWEI P30 Lite|2019|7000|Kirin 710|6,15″ IPS|Android 9 / EMUI
            HUAWEI P40|2020|18000|Kirin 990 5G|6,1″ OLED|Android 10 / EMUI без GMS
            HUAWEI P40 Pro|2020|25000|Kirin 990 5G|6,58″ OLED|Android 10 / EMUI без GMS
            HUAWEI Mate 8|2015|4500|Kirin 950|6,0″ IPS|Android 6 / EMUI
            HUAWEI Mate 9|2016|6000|Kirin 960|5,9″ IPS|Android 7 / EMUI
            HUAWEI Mate 10 Pro|2017|8000|Kirin 970|6,0″ OLED|Android 8 / EMUI
            HUAWEI Mate 20|2018|10000|Kirin 980|6,53″ IPS|Android 9 / EMUI
            HUAWEI Mate 20 Pro|2018|14000|Kirin 980|6,39″ OLED|Android 9 / EMUI
            HUAWEI Mate 30 Pro|2019|18000|Kirin 990|6,53″ OLED|Android 10 / EMUI без GMS
            HUAWEI Mate 40 Pro|2020|29000|Kirin 9000|6,76″ OLED|Android 10 / EMUI без GMS
            realme 1|2018|4000|Helio P60|6,0″ IPS|Android 8.1 / ColorOS
            realme 2|2018|4000|Snapdragon 450|6,2″ IPS|Android 8.1 / ColorOS
            realme 2 Pro|2018|5000|Snapdragon 660|6,3″ IPS|Android 8.1 / ColorOS
            realme 3|2019|4500|Helio P60 / P70|6,22″ IPS|Android 9 / ColorOS
            realme 3 Pro|2019|6000|Snapdragon 710|6,3″ IPS|Android 9 / ColorOS
            realme 5|2019|5500|Snapdragon 665|6,5″ IPS|Android 9 / ColorOS
            realme 5 Pro|2019|6500|Snapdragon 712|6,3″ IPS|Android 9 / ColorOS
            realme 6|2020|7500|Helio G90T|6,5″ IPS|Android 10 / realme UI
            realme 6 Pro|2020|9500|Snapdragon 720G|6,6″ IPS|Android 10 / realme UI
            realme 7|2020|8500|Helio G95 (глобальная версия)|6,5″ IPS|Android 10 / realme UI
            realme 7 Pro|2020|11000|Snapdragon 720G|6,4″ Super AMOLED|Android 10 / realme UI
            realme 8|2021|10000|Helio G95 (4G)|6,4″ Super AMOLED|Android 11 / realme UI
            realme 8 Pro|2021|13000|Snapdragon 720G|6,4″ Super AMOLED|Android 11 / realme UI
            realme 9|2022|12000|Snapdragon 680 (4G)|6,4″ Super AMOLED|Android 12 / realme UI
            realme 9 Pro|2022|14000|Snapdragon 695|6,6″ IPS|Android 12 / realme UI
            realme 9 Pro+|2022|19000|Dimensity 920|6,4″ Super AMOLED|Android 12 / realme UI
            realme 10|2022|14000|Helio G99 (4G)|6,4″ Super AMOLED|Android 12 / realme UI
            realme 10 Pro|2022|16000|Snapdragon 695|6,72″ IPS|Android 13 / realme UI
            realme 10 Pro+|2022|22000|Dimensity 1080 (основная версия)|6,7″ AMOLED|Android 13 / realme UI
            realme 11 Pro+|2023|27000|Dimensity 7050|6,7″ AMOLED|Android 13 / realme UI
            realme 12 Pro|2024|25000|Snapdragon 6 Gen 1|6,7″ AMOLED|Android 14 / realme UI
            realme 12 Pro+|2024|32000|Snapdragon 7s Gen 2|6,7″ AMOLED|Android 14 / realme UI
            realme GT|2021|20000|Snapdragon 888|6,43″ Super AMOLED|Android 11 / realme UI
            realme GT Neo 2|2021|22000|Snapdragon 870|6,62″ AMOLED|Android 11 / realme UI
            realme GT Neo 3|2022|27000|Dimensity 8100|6,7″ AMOLED|Android 12 / realme UI
            realme GT 2|2022|25000|Snapdragon 888|6,62″ AMOLED|Android 12 / realme UI
            realme GT 2 Pro|2022|33000|Snapdragon 8 Gen 1|6,7″ LTPO AMOLED|Android 12 / realme UI
            realme GT 6|2024|45000|Snapdragon 8s Gen 3 (глобальная версия)|6,78″ LTPO AMOLED|Android 14 / realme UI
            realme C21|2021|5500|Helio G35|6,5″ IPS|Android 10 / realme UI
            realme C35|2022|8000|Unisoc T616|6,6″ IPS|Android 11 / realme UI
            OPPO Find X|2018|14000|Snapdragon 845|6,42″ AMOLED|Android 8.1 / ColorOS
            OPPO Find X2|2020|23000|Snapdragon 865|6,7″ AMOLED|Android 10 / ColorOS
            OPPO Find X2 Pro|2020|28000|Snapdragon 865|6,7″ AMOLED|Android 10 / ColorOS
            OPPO Find X3 Pro|2021|35000|Snapdragon 888|6,7″ LTPO AMOLED|Android 11 / ColorOS
            OPPO Find X5|2022|37000|Snapdragon 888|6,55″ AMOLED|Android 12 / ColorOS
            OPPO Find X5 Pro|2022|45000|Snapdragon 8 Gen 1 (глобальная версия)|6,7″ LTPO AMOLED|Android 12 / ColorOS
            OPPO Find X6 Pro|2023|65000|Snapdragon 8 Gen 2|6,82″ LTPO AMOLED|Android 13 / ColorOS
            OPPO Find X7 Ultra|2024|80000|Snapdragon 8 Gen 3|6,82″ LTPO AMOLED|Android 14 / ColorOS
            OPPO Find N|2021|45000|Snapdragon 888|7,1″ складной AMOLED|Android 11 / ColorOS
            OPPO Find N2|2022|60000|Snapdragon 8+ Gen 1|7,1″ складной AMOLED|Android 13 / ColorOS
            OPPO Find N2 Flip|2022|40000|Dimensity 9000+|6,8″ складной AMOLED|Android 13 / ColorOS
            OPPO Find N3|2023|90000|Snapdragon 8 Gen 2|7,82″ складной LTPO OLED|Android 13 / ColorOS
            OPPO Reno2|2019|10000|Snapdragon 730G|6,5″ AMOLED|Android 9 / ColorOS
            OPPO Reno3 Pro 5G|2019|16000|Snapdragon 765G|6,5″ AMOLED|Android 10 / ColorOS
            OPPO Reno4 Pro 5G|2020|20000|Snapdragon 765G|6,55″ AMOLED|Android 10 / ColorOS
            OPPO Reno5 5G|2020|18000|Snapdragon 765G|6,43″ OLED|Android 11 / ColorOS
            OPPO Reno6 5G|2021|21000|Dimensity 900|6,43″ AMOLED|Android 11 / ColorOS
            OPPO Reno7 5G|2022|23000|Dimensity 900 (глобальная версия)|6,43″ AMOLED|Android 11 / ColorOS
            OPPO Reno8 5G|2022|26000|Dimensity 1300|6,4″ AMOLED|Android 12 / ColorOS
            OPPO Reno10 Pro+|2023|42000|Snapdragon 8+ Gen 1|6,74″ AMOLED|Android 13 / ColorOS
            vivo X21|2018|8000|Snapdragon 660|6,28″ Super AMOLED|Android 8.1 / Funtouch OS
            vivo X23|2018|10000|Snapdragon 670|6,41″ Super AMOLED|Android 8.1 / Funtouch OS
            vivo X27|2019|12000|Snapdragon 710 (основная версия)|6,39″ Super AMOLED|Android 9 / Funtouch OS
            vivo X30 Pro|2019|16000|Exynos 980|6,44″ Super AMOLED|Android 9 / Funtouch OS
            vivo X50|2020|17000|Snapdragon 730 (глобальная версия)|6,56″ AMOLED|Android 10 / Funtouch OS
            vivo X50 Pro|2020|23000|Snapdragon 765G|6,56″ AMOLED|Android 10 / Funtouch OS
            vivo X60|2021|25000|Snapdragon 870 (глобальная версия)|6,56″ AMOLED|Android 11 / Funtouch OS
            vivo X60 Pro|2021|30000|Snapdragon 870 (глобальная версия)|6,56″ AMOLED|Android 11 / Funtouch OS
            vivo X70 Pro|2021|34000|Dimensity 1200 (глобальная версия)|6,56″ AMOLED|Android 11 / Funtouch OS
            vivo X70 Pro+|2021|42000|Snapdragon 888+|6,78″ LTPO AMOLED|Android 11 / Funtouch OS
            vivo X80|2022|37000|Dimensity 9000|6,78″ AMOLED|Android 12 / Funtouch OS
            vivo X80 Pro|2022|48000|Snapdragon 8 Gen 1 (глобальная версия)|6,78″ LTPO AMOLED|Android 12 / Funtouch OS
            vivo X90|2022|43000|Dimensity 9200|6,78″ AMOLED|Android 13 / Funtouch OS
            vivo X90 Pro|2022|55000|Dimensity 9200|6,78″ AMOLED|Android 13 / Funtouch OS
            vivo X100|2023|52000|Dimensity 9300|6,78″ LTPO AMOLED|Android 14 / Funtouch OS
            vivo X100 Pro|2023|68000|Dimensity 9300|6,78″ LTPO AMOLED|Android 14 / Funtouch OS
            vivo V21 5G|2021|15000|Dimensity 800U|6,44″ AMOLED|Android 11 / Funtouch OS
            vivo V23 5G|2022|20000|Dimensity 920|6,44″ AMOLED|Android 12 / Funtouch OS
            vivo V25|2022|22000|Dimensity 900|6,44″ AMOLED|Android 12 / Funtouch OS
            vivo V27|2023|28000|Dimensity 7200|6,78″ AMOLED|Android 13 / Funtouch OS
            Motorola Moto G|2013|2500|Snapdragon 400|4,5″ IPS|Android 4.3
            Motorola Moto G (2nd gen)|2014|3000|Snapdragon 400|5,0″ IPS|Android 4.4
            Motorola Moto G (3rd gen)|2015|3500|Snapdragon 410|5,0″ IPS|Android 5.1
            Motorola Moto G4|2016|4000|Snapdragon 617|5,5″ IPS|Android 6
            Motorola Moto G4 Plus|2016|4500|Snapdragon 617|5,5″ IPS|Android 6
            Motorola Moto G5|2017|4500|Snapdragon 430|5,0″ IPS|Android 7
            Motorola Moto G5 Plus|2017|5500|Snapdragon 625|5,2″ IPS|Android 7
            Motorola Moto G6|2018|5500|Snapdragon 450|5,7″ IPS|Android 8
            Motorola Moto G7|2019|6500|Snapdragon 632|6,2″ IPS|Android 9
            Motorola Moto G8 Power|2020|8000|Snapdragon 665|6,4″ IPS|Android 10
            Motorola Moto G9 Plus|2020|10000|Snapdragon 730G|6,81″ IPS|Android 10
            Motorola Moto G30|2021|8500|Snapdragon 662|6,5″ IPS|Android 11
            Motorola Moto G60|2021|12000|Snapdragon 732G|6,8″ IPS|Android 11
            Motorola Moto G84|2023|18000|Snapdragon 695|6,5″ P-OLED|Android 13
            Motorola Edge 20|2021|19000|Snapdragon 778G|6,7″ OLED|Android 11
            Motorola Edge 20 Pro|2021|25000|Snapdragon 870|6,7″ OLED|Android 11
            Motorola Edge 30|2022|23000|Snapdragon 778G+|6,5″ AMOLED|Android 12
            Motorola Edge 30 Ultra|2022|37000|Snapdragon 8+ Gen 1|6,67″ P-OLED|Android 12
            Motorola Edge 40 Pro|2023|45000|Snapdragon 8 Gen 2|6,67″ OLED|Android 13
            Motorola Razr 40 Ultra|2023|55000|Snapdragon 8+ Gen 1|6,9″ складной LTPO AMOLED|Android 13
            Sony Xperia Z|2013|3000|Snapdragon S4 Pro|5,0″ TFT LCD|Android 4.1
            Sony Xperia Z1|2013|3500|Snapdragon 800|5,0″ TFT LCD|Android 4.2
            Sony Xperia Z2|2014|4000|Snapdragon 801|5,2″ IPS|Android 4.4
            Sony Xperia Z3|2014|4500|Snapdragon 801|5,2″ IPS|Android 4.4
            Sony Xperia Z3 Compact|2014|4500|Snapdragon 801|4,6″ IPS|Android 4.4
            Sony Xperia Z5|2015|5500|Snapdragon 810|5,2″ IPS|Android 5.1
            Sony Xperia Z5 Compact|2015|5000|Snapdragon 810|4,6″ IPS|Android 5.1
            Sony Xperia Z5 Premium|2015|6500|Snapdragon 810|5,5″ IPS|Android 5.1
            Sony Xperia XZ|2016|6500|Snapdragon 820|5,2″ IPS|Android 6
            Sony Xperia XZ Premium|2017|8500|Snapdragon 835|5,46″ IPS|Android 7.1
            Sony Xperia XZ1|2017|8000|Snapdragon 835|5,2″ IPS|Android 8
            Sony Xperia XZ2|2018|10000|Snapdragon 845|5,7″ IPS|Android 8
            Sony Xperia 1|2019|18000|Snapdragon 855|6,5″ OLED|Android 9
            Sony Xperia 1 II|2020|28000|Snapdragon 865|6,5″ OLED|Android 10
            Sony Xperia 5 II|2020|24000|Snapdragon 865|6,1″ OLED|Android 10
            ASUS Zenfone 2 ZE551ML|2015|3500|Intel Atom Z3580 / Z3560|5,5″ IPS|Android 5 / ZenUI
            ASUS Zenfone 3 ZE552KL|2016|5000|Snapdragon 625|5,5″ IPS|Android 6 / ZenUI
            ASUS Zenfone 4 ZE554KL|2017|6500|Snapdragon 630 / 660|5,5″ IPS|Android 7 / ZenUI
            ASUS Zenfone 5 ZE620KL|2018|8000|Snapdragon 636|6,2″ IPS|Android 8 / ZenUI
            ASUS Zenfone 5Z|2018|10000|Snapdragon 845|6,2″ IPS|Android 8 / ZenUI
            ASUS Zenfone 6|2019|15000|Snapdragon 855|6,4″ IPS|Android 9 / ZenUI
            ASUS Zenfone 7 Pro|2020|24000|Snapdragon 865+|6,67″ AMOLED|Android 10 / ZenUI
            ASUS Zenfone 8|2021|27000|Snapdragon 888|5,9″ AMOLED|Android 11 / ZenUI
            ASUS Zenfone 9|2022|35000|Snapdragon 8+ Gen 1|5,9″ AMOLED|Android 12 / ZenUI
            ASUS ROG Phone 6|2022|45000|Snapdragon 8+ Gen 1|6,78″ AMOLED|Android 12 / ROG UI
            Nokia 3|2017|3000|MediaTek MT6737|5,0″ IPS|Android 7
            Nokia 5|2017|3500|Snapdragon 430|5,2″ IPS|Android 7
            Nokia 6|2017|4000|Snapdragon 430|5,5″ IPS|Android 7
            Nokia 8|2017|6500|Snapdragon 835|5,3″ IPS|Android 7.1
            Nokia 7 Plus|2018|7000|Snapdragon 660|6,0″ IPS|Android 8.1
            Nokia 6.1|2018|5000|Snapdragon 630|5,5″ IPS|Android 8
            Nokia 6.1 Plus|2018|5500|Snapdragon 636|5,8″ IPS|Android 8.1
            Nokia 7.2|2019|7000|Snapdragon 660|6,3″ IPS|Android 9
            Nokia 8.3 5G|2020|14000|Snapdragon 765G|6,81″ IPS|Android 10
            Nokia X20|2021|16000|Snapdragon 480|6,67″ IPS|Android 11
            Infinix HOT 10|2020|5000|Helio G70|6,78″ IPS|Android 10 / XOS
            Infinix HOT 10S|2021|6000|Helio G85|6,82″ IPS|Android 11 / XOS
            Infinix HOT 11S|2021|7000|Helio G88|6,78″ IPS|Android 11 / XOS
            Infinix HOT 12|2022|7000|Helio G85|6,82″ IPS|Android 12 / XOS
            Infinix HOT 20|2022|8000|Helio G85 (4G)|6,82″ IPS|Android 12 / XOS
            Infinix HOT 30|2023|9000|Helio G88 (4G)|6,78″ IPS|Android 13 / XOS
            Infinix HOT 40 Pro|2023|13000|Helio G99|6,78″ IPS|Android 13 / XOS
            Infinix NOTE 12 G96|2022|10000|Helio G96|6,7″ AMOLED|Android 12 / XOS
            Infinix ZERO 30 5G|2023|22000|Dimensity 8020|6,78″ AMOLED|Android 13 / XOS
            Infinix GT 10 Pro|2023|18000|Dimensity 8050|6,67″ AMOLED|Android 13 / XOS
            TECNO Camon 17|2021|7000|Helio G85|6,6″ IPS|Android 11 / HiOS
            TECNO Camon 18|2021|8000|Helio G88|6,8″ IPS|Android 11 / HiOS
            TECNO Camon 19|2022|9000|Helio G85|6,8″ IPS|Android 12 / HiOS
            TECNO Camon 20|2023|11000|Helio G85|6,67″ AMOLED|Android 13 / HiOS
            TECNO Camon 20 Pro 5G|2023|17000|Dimensity 8050|6,67″ AMOLED|Android 13 / HiOS
            TECNO POVA 3|2022|9500|Helio G88|6,9″ IPS|Android 11 / HiOS
            TECNO POVA 4|2022|11000|Helio G99|6,82″ IPS|Android 12 / HiOS
            TECNO POVA 5|2023|13000|Helio G99|6,78″ IPS|Android 13 / HiOS
            TECNO Phantom X2|2022|27000|Dimensity 9000|6,8″ AMOLED|Android 12 / HiOS
            TECNO Phantom V Fold|2023|65000|Dimensity 9000+|7,85″ складной LTPO AMOLED|Android 13 / HiOS
            ZTE Axon 7|2016|6000|Snapdragon 820|5,5″ AMOLED|Android 6
            ZTE Axon 10 Pro|2019|14000|Snapdragon 855|6,47″ AMOLED|Android 9
            ZTE Axon 20 5G|2020|18000|Snapdragon 765G|6,92″ OLED|Android 10
            ZTE Axon 30 5G|2021|22000|Snapdragon 870|6,92″ AMOLED|Android 11
            ZTE Axon 40 Ultra|2022|38000|Snapdragon 8 Gen 1|6,8″ AMOLED|Android 12
        """
}
