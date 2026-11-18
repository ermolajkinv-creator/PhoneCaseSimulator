package signal

val Phone.yearLabel: String get() = if (year > 0) year.toString() else "Год не указан"

private val tablets = Regex("""\b(?:tab|pad|ipad|tablet)\b|matepad|zenpad|mediapad|viewpad|медиапад""", RegexOption.IGNORE_CASE)
private val performanceChip = Regex(
    """Snapdragon\s+(?:8s\s+Gen\s+[34]|7\+?\s+Gen\s+[234]|7\+\s*Gen\s*2)|Dimensity\s+(?:8[12345]\d\d|8500)\b|Exynos\s+(?:1480|1580|1680)\b""",
    RegexOption.IGNORE_CASE
)
private val performanceSeries = Regex(
    """\b(?:POCO (?:X[45678] Pro|F[45678](?: Pro)?(?! Ultra))|Galaxy (?:S2[23456]\s?FE|A(?:55|56|57))|OnePlus (?:1[12345]R|Nord [3456])|Realme GT (?:Neo|[67]T)|Redmi (?:Turbo [345]|K[678]0E)|iQOO Neo\s?\d+)\b""",
    RegexOption.IGNORE_CASE
)
private val flagshipChip = Regex(
    """Snapdragon\s+8\s*(?:Elite|\+?\s*Gen\s*[2345])|Dimensity\s+9[23456]\d\d|Tensor\s+G[3456]|A(?:1[6789]|20)\s+Pro|Kirin\s+90[23]0|Exynos\s+(?:2400|2500|2600)""",
    RegexOption.IGNORE_CASE
)

// Some flagship families used mid-range silicon (e.g. Pixel 5). Match the marketed
// family as well as the platform; Lite, SE and Pixel A-series have different positions.
private val formerFlagshipFamily = Regex(
    """^(?:(?:Samsung )?Galaxy (?:S2[0-3](?!\d)|Note\s?20)|(?:Apple )?iPhone (?:1[2345])(?:\b|$)|Google Pixel (?:5|6|7|8)(?: Pro| XL)?$|(?:Xiaomi )?(?:Mi )?(?:10|11|12|13)(?:T|S)?(?:$| (?:Pro|Ultra|T Pro)\b)|Huawei (?:P(?:40|50|60)|Mate (?:40|50|60))|(?:Sony )?Xperia (?:1|5) (?:II|III|IV|V)\b|OnePlus (?:8|9|10|11)(?:$|[TR]\b| Pro)|(?:Oppo )?Find X[2356](?:$| Pro)|(?:Vivo )?X(?:50|60|70|80|90|100)(?:$| Pro)|Honor Magic\s?[345](?:$| Pro| Ultimate)|Asus (?:ROG Phone|Zenfone (?:7|8|9|10))|Meizu (?:17|18|20)(?:$| Pro)|(?:Motorola )?Edge (?:Plus|\+|30 Ultra|40 Pro))""",
    RegexOption.IGNORE_CASE
)
private val trimmedFamily = Regex("""\b(?:lite|neo|play|SE|Pixel [5678]a)\b""", RegexOption.IGNORE_CASE)
private val formerFlagshipChip = Regex(
    """Snapdragon\s+(?:865|870|888|8\s*\+?\s*Gen\s*[123])|Dimensity\s+(?:9000|9200|9300)|Exynos\s+(?:990|1080|2100|2200)|Kirin\s+(?:990|9000)|Tensor(?:\s+G[23])?\b""",
    RegexOption.IGNORE_CASE
)

internal fun classifyPhone(phone: Phone): Phone {
    val rank = when {
        phone.tier >= catalogFoldableTier -> phone.tier + 1
        phone.tier == oldTier -> oldTier
        tablets.containsMatchIn(phone.name) -> if (phone.tier == 5) flagshipTier else phone.tier
        phone.year in 2020..2023 && !trimmedFamily.containsMatchIn(phone.name) &&
            (phone.tier == 5 || formerFlagshipChip.containsMatchIn(phone.note) || formerFlagshipFamily.containsMatchIn(phone.name)) -> subflagshipTier
        phone.year >= 2020 && performanceChip.containsMatchIn(phone.note) -> subflagshipTier
        phone.year >= 2022 && performanceSeries.containsMatchIn(phone.name) -> subflagshipTier
        phone.year >= 2024 && flagshipChip.containsMatchIn(phone.note) -> flagshipTier
        phone.tier == 5 -> if (phone.year in 2020..2023) subflagshipTier else flagshipTier
        else -> phone.tier
    }
    return if (rank == phone.tier) phone else phone.copy(tier = rank)
}
