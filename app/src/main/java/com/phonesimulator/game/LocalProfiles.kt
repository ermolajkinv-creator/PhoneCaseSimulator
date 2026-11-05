package com.phonesimulator.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class LocalPlayer(val id: String,val name: String)
data class LocalDuel(val id: Long,val a: String,val b: String,val winner: String,val at: Long)
/** Local save slots only. There are no accounts, external messages or network players. */
class LocalProfiles(private val context: Context) {
    private val prefs=context.getSharedPreferences("local_players",Context.MODE_PRIVATE)
    fun players(): List<LocalPlayer> {
        val ids=prefs.getStringSet("ids",setOf("main")).orEmpty().filter { it.matches(Regex("main|p[0-9]+")) }
        return (listOf("main")+ids.filter { it!="main" }.sorted()).map { id ->
            LocalPlayer(id,ProgressStore(context,id).load().nickname)
        }
    }
    fun create(name: String): String? {
        val clean=name.trim().take(24)
        val players=players()
        if(clean.length<2 || players.size>=4) return null
        val next=prefs.getLong("next",1).coerceAtLeast(1)
        if(next==Long.MAX_VALUE) return null
        val id="p$next"
        if(!ProgressStore(context,id).save(Progress(nickname=clean))) return null
        if(!prefs.edit().putStringSet("ids",players.map { it.id }.toSet()+id).putLong("next",next+1).commit()) return null
        return id
    }
    fun select(id: String): Boolean = id in players().map { it.id } && prefs.edit().putString("active",id).commit()
    fun duels(): List<LocalDuel> = try {
        val a=JSONArray(prefs.getString("duels","[]"))
        (0 until minOf(a.length(),200)).map { i -> a.getJSONObject(i).let {
            LocalDuel(it.getLong("id"),it.getString("a"),it.getString("b"),it.getString("winner"),it.getLong("at")) } }
    } catch (_: Exception) { emptyList() }
    fun recordDuel(a: String,b: String,winner: String,at: Long,id: Long): Boolean {
        if(a==b || winner !in listOf(a,b,"draw") || at<0 || id<=0) return false
        if(!players().map { it.id }.containsAll(listOf(a,b))) return false
        val existing=duels()
        if(existing.any { it.id==id }) return true
        val rows=(existing+LocalDuel(id,a,b,winner,at)).takeLast(200)
        return prefs.edit().putString("duels",JSONArray().apply { rows.forEach {
            put(JSONObject().put("id",it.id).put("a",it.a).put("b",it.b).put("winner",it.winner).put("at",it.at))
        } }.toString()).commit()
    }
    companion object {
        fun active(context: Context): String {
            val prefs=context.getSharedPreferences("local_players",Context.MODE_PRIVATE)
            val id=prefs.getString("active","main").orEmpty()
            return if(id=="main" || id in prefs.getStringSet("ids",emptySet()).orEmpty()) id else "main"
        }
    }
}
