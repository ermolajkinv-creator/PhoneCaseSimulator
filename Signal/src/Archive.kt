package signal

// The packager provides the catalogue before Kotlin starts; rows are in album ID order.
external val signalCatalog: String

internal fun archivePhones(data: String): List<Phone> = data.trimEnd().lineSequence().map { row ->
    val fields = row.split('\t', limit = 6)
    val (name, year, tier, price, credit) = fields
    Phone(name, tier.toInt(), fields[5], year.toInt(), credit, price.toInt())
}.toList()
