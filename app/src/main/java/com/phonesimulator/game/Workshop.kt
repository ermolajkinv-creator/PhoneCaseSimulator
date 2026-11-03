package com.phonesimulator.game

object Workshop {
    fun options(phone: Phone): List<String> = when {
        phone.isApple -> listOf("Заводская", "Джейлбрейк (симуляция)")
        phone.name == "Nokia 3310" || phone.system.startsWith("HarmonyOS") || phone.system == "Игровая платформа" -> listOf("Заводская")
        else -> listOf("Заводская", "LineageOS (симуляция)", "crDroid (симуляция)", "AOSP (симуляция)")
    }
}
