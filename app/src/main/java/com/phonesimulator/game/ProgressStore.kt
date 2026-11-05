package com.phonesimulator.game

import android.content.Context

class ProgressStore(context: Context,profileId: String = LocalProfiles.active(context)) {
    private val suffix=if(profileId=="main") "" else "_"+profileId.also { require(it.matches(Regex("p[0-9]+"))) }
    private val backup = context.getSharedPreferences("game_backup$suffix", Context.MODE_PRIVATE)
    private val prefs = context.getSharedPreferences("game$suffix", Context.MODE_PRIVATE)
    fun load(): Progress {
        val coins = try { prefs.getInt("coins", Economy.START_BALANCE) } catch (_: ClassCastException) { Economy.START_BALANCE }
        val owned = try { prefs.getString("owned", "").orEmpty() } catch (_: ClassCastException) { "" }
        val used = prefs.getString("used", "").orEmpty().split(';').mapNotNull { entry ->
            val parts = entry.split(':')
            val phone = parts.getOrNull(0)?.toIntOrNull()
            val condition = parts.getOrNull(1)?.toIntOrNull()
            if (phone != null && phone in Catalog.phones.indices && condition != null && condition in 0..3)
                UsedPhone(phone, condition,
                    runCatching { Mutation.valueOf(parts.getOrNull(2).orEmpty()) }.getOrDefault(Mutation.NORMAL),
                    parts.getOrNull(3)?.toLongOrNull()?.coerceAtLeast(0) ?: 0L) else null
        }
        val jobParts = prefs.getString("clientJob", "").orEmpty().split(',')
        val job = try {
            val slot = jobParts[0].toLong()
            val phone = jobParts[1].toInt()
            val kind = ServiceKind.valueOf(jobParts[2])
            val cost = jobParts[3].toInt()
            val reward = jobParts[4].toInt()
            val seconds = jobParts[5].toInt()
            val at = jobParts[6].toLong()
            if (slot >= 0 && phone in Catalog.phones.indices && cost >= 0 && reward >= cost && seconds in 10..60 && at >= 0)
                ClientJob(ClientOffer(slot, phone, kind, cost, reward, seconds), at,
                    RepairSession(jobParts.getOrNull(7)?.toIntOrNull() ?: 0, jobParts.getOrNull(8)?.toIntOrNull() ?: 0)) else null
        } catch (_: RuntimeException) { null }
        return Progress.restore(coins, owned).copy(used = used,
            expansion = ExpansionCodec.decode(prefs.getString("expansion", "").orEmpty()),
            world = WorldCodec.decode(prefs.getString("world", "").orEmpty()).let { w ->
                val maxUsedId=used.maxOfOrNull { it.uid } ?: 0L
                w.copy(serial=maxOf(w.serial,if(maxUsedId<Long.MAX_VALUE) maxUsedId+1 else Long.MAX_VALUE))
            },
            workshopLevel = prefs.getInt("workshopLevel", 1).coerceIn(1,5),
            reputation = prefs.getInt("reputation", 0).coerceAtLeast(0),
            servicesCompleted = prefs.getInt("servicesCompleted", 0).coerceAtLeast(0),
            serviceSlot = prefs.getLong("serviceSlot", -1L),
            servicedSlots = prefs.getStringSet("servicedSlots", emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet(),
            clientJob = job,
            favorites = prefs.getStringSet("favorites", emptySet()).orEmpty().mapNotNull { it.toIntOrNull() }.filter { it in Catalog.phones.indices }.toSet(),
            nickname = prefs.getString("nickname", "Коллекционер").orEmpty().take(24).ifBlank { "Коллекционер" },
            clicks = prefs.getInt("clicks", 0).coerceAtLeast(0),
            clickGuard = ClickGuard(
                prefs.getString("clickRecent", "").orEmpty().split(',').mapNotNull { it.toLongOrNull() }
                    .filter { it >= 0L }.takeLast(ClickGuard.MAX_TAPS),
                prefs.getLong("clickBlockedUntil", 0L).coerceAtLeast(0L)),
            casesOpened = prefs.getInt("casesOpened", 0).coerceAtLeast(0),
            questDay = prefs.getLong("questDay", -1L),
            questClicks = prefs.getInt("questClicks", 0).coerceIn(0,100),
            questCases = prefs.getInt("questCases", 0).coerceIn(0,3),
            claimedQuests = prefs.getStringSet("claimedQuests", emptySet()).orEmpty().toSet(),
            lastDailyDay = prefs.getLong("lastDailyDay", -1L), dailyStreak = prefs.getInt("dailyStreak", 0).coerceIn(0,7),
            badges = prefs.getStringSet("badges", emptySet()).orEmpty().toSet(),
            lastGiftAt = prefs.getLong("lastGiftAt", 0L).coerceAtLeast(0L),
            purchased = prefs.getStringSet("purchased", emptySet()).orEmpty().toSet(),
            firmware = prefs.getString("firmware", "").orEmpty().split(';').mapNotNull {
                val parts = it.split(':', limit = 2)
                val model = parts.firstOrNull()?.toIntOrNull()
                val rom = parts.getOrNull(1)
                if (model != null && model in Catalog.phones.indices && rom in Workshop.options(Catalog.phones[model])) model to rom!! else null
            }.toMap())
    }
    // Persist payment and reward together before starting the cosmetic animation.
    fun save(progress: Progress): Boolean = prefs.edit()
        .putInt("coins", progress.coins)
        .putString("expansion", ExpansionCodec.encode(progress.expansion))
        .putString("world", WorldCodec.encode(progress.world))
        .putInt("workshopLevel", progress.workshopLevel)
        .putInt("reputation", progress.reputation)
        .putInt("servicesCompleted", progress.servicesCompleted)
        .putLong("serviceSlot", progress.serviceSlot)
        .putStringSet("servicedSlots", progress.servicedSlots.map { it.toString() }.toSet())
        .putString("clientJob", progress.clientJob?.let { "${it.offer.slot},${it.offer.phone},${it.offer.kind.name},${it.offer.cost},${it.offer.reward},${it.offer.seconds},${it.readyAt},${it.repair.step},${it.repair.mistakes}" }.orEmpty())
        .putLong("questDay", progress.questDay)
        .putInt("questClicks", progress.questClicks)
        .putInt("questCases", progress.questCases)
        .putStringSet("claimedQuests", progress.claimedQuests)
        .putStringSet("favorites", progress.favorites.map { it.toString() }.toSet())
        .putString("nickname", progress.nickname.take(24))
        .putInt("clicks", progress.clicks)
        .putString("clickRecent", progress.clickGuard.recent.joinToString(","))
        .putLong("clickBlockedUntil", progress.clickGuard.blockedUntil)
        .putInt("casesOpened", progress.casesOpened)
        .putLong("lastDailyDay", progress.lastDailyDay)
        .putInt("dailyStreak", progress.dailyStreak)
        .putStringSet("badges", progress.badges)
        .putLong("lastGiftAt", progress.lastGiftAt)
        .putString("firmware", progress.firmware.entries.joinToString(";") { "${it.key}:${it.value}" })
        .putString("used", progress.used.joinToString(";") { "${it.phone}:${it.condition}:${it.mutation.name}:${it.uid}" })
        .putStringSet("purchased", progress.purchased)
        .putString("owned", progress.owned.joinToString(","))
        .commit()
    fun backupTime(): Long = backup.getLong("at",0L)
    fun createBackup(): Boolean {
        if(!save(load())) return false
        val entries=org.json.JSONArray()
        prefs.all.forEach { (key,value) ->
            val type=when(value) { is Int -> "int";is Long -> "long";is Float -> "float";is Boolean -> "boolean";is String -> "string";is Set<*> -> "set";else -> return@forEach }
            entries.put(org.json.JSONObject().put("key",key).put("type",type)
                .put("value",if(value is Set<*>) org.json.JSONArray(value.toList()) else value))
        }
        return backup.edit().putString("data",entries.toString()).putLong("at",System.currentTimeMillis()).commit()
    }
    fun restoreBackup(): Boolean {
        val before=load()
        val data=backup.getString("data",null) ?: return false
        return try {
            val a=org.json.JSONArray(data)
            if(a.length()>256) return false
            val editor=prefs.edit().clear()
            for(i in 0 until a.length()) {
                val entry=a.getJSONObject(i);val key=entry.getString("key")
                when(entry.getString("type")) {
                    "int" -> editor.putInt(key,entry.getInt("value"))
                    "long" -> editor.putLong(key,entry.getLong("value"))
                    "float" -> editor.putFloat(key,entry.getDouble("value").toFloat())
                    "boolean" -> editor.putBoolean(key,entry.getBoolean("value"))
                    "string" -> editor.putString(key,entry.getString("value"))
                    "set" -> { val values=entry.getJSONArray("value");editor.putStringSet(key,(0 until values.length()).map { values.getString(it) }.toSet()) }
                    else -> return false
                }
            }
            // Restoring a local checkpoint must not reset the gift's ten-minute deadline.
            var restoredGift=0L
            for(i in 0 until a.length()) if(a.getJSONObject(i).getString("key")=="lastGiftAt") restoredGift=a.getJSONObject(i).getLong("value")
            editor.putLong("lastGiftAt",maxOf(before.lastGiftAt,restoredGift))
            // A checkpoint must not clear an active clicker pause or its current detection window.
            editor.putLong("clickBlockedUntil", before.clickGuard.blockedUntil)
                .putString("clickRecent", before.clickGuard.recent.joinToString(","))
            val snapshot=(0 until a.length()).associate { a.getJSONObject(it).getString("key") to a.getJSONObject(it) }
            fun number(key: String, default: Long) = snapshot[key]?.optLong("value",default) ?: default
            if(before.lastDailyDay >= number("lastDailyDay",-1)) {
                editor.putLong("lastDailyDay",before.lastDailyDay).putInt("dailyStreak",before.dailyStreak)
            }
            if(before.questDay >= number("questDay",-1)) {
                editor.putLong("questDay",before.questDay).putInt("questClicks",before.questClicks)
                    .putInt("questCases",before.questCases).putStringSet("claimedQuests",before.claimedQuests)
            }
            val snapshotWorld=WorldCodec.decode(snapshot["world"]?.optString("value").orEmpty())
            val newerEconomy=before.world.economyRevision>snapshotWorld.economyRevision
            if(newerEconomy) {
                editor.putInt("coins",before.coins).putString("owned",before.owned.joinToString(","))
                    .putString("used",before.used.joinToString(";"){"${it.phone}:${it.condition}:${it.mutation.name}:${it.uid}"})
            }
            val oldExtra=if(newerEconomy) before.expansion else ExpansionCodec.decode(snapshot["expansion"]?.optString("value").orEmpty())
            editor.putString("world",WorldCodec.encode(WorldCodec.restore(snapshotWorld,before.world)))
            editor.putString("expansion",ExpansionCodec.encode(oldExtra.copy(
                awardsClaimed=oldExtra.awardsClaimed+before.expansion.awardsClaimed,
                deliveryDay=maxOf(oldExtra.deliveryDay,before.expansion.deliveryDay),
                idleAt=maxOf(oldExtra.idleAt,before.expansion.idleAt),
                negotiated=(oldExtra.negotiated+before.expansion.negotiated).toList().takeLast(200).toSet(),
                discounts=(oldExtra.discounts+before.expansion.discounts).entries.toList().takeLast(200).associate { it.toPair() }
            )))
            val oldSlots=snapshot["servicedSlots"]?.optJSONArray("value") ?: org.json.JSONArray()
            val maxSlot=maxOf(before.serviceSlot,number("serviceSlot",-1))
            editor.putLong("serviceSlot",maxSlot).putStringSet("servicedSlots",
                (before.servicedSlots+(0 until oldSlots.length()).mapNotNull { oldSlots.optString(it).toLongOrNull() })
                    .filter { it>=maxSlot-2 }.map { it.toString() }.toSet())
            val oldJobSlot=snapshot["clientJob"]?.optString("value").orEmpty().substringBefore(',').toLongOrNull()
            val currentJob = before.clientJob
            if(oldJobSlot!=null && (oldJobSlot in before.servicedSlots || oldJobSlot<maxSlot-2)) editor.putString("clientJob","")
            else if (oldJobSlot != null && currentJob != null && oldJobSlot == currentJob.offer.slot) {
                val original = snapshot["clientJob"]?.optString("value").orEmpty().split(',').take(7)
                val repair = currentJob.repair
                editor.putString("clientJob", (original + listOf(repair.step.toString(), repair.mistakes.toString())).joinToString(","))
            }
            editor.commit()
        } catch (_: Exception) { false }
    }
}
