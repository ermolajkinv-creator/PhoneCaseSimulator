package com.phonesimulator.game

import org.json.JSONArray
import org.json.JSONObject

object WorldCodec {
    private fun strings(values: Collection<String>)=JSONArray(values.toList())
    private fun trait(t: PhoneTraits)=JSONObject().put("mutation",t.mutation.name).put("condition",t.condition).put("uid",t.uid)
    private fun companyPhone(p: CompanyPhone)=JSONObject().put("id",p.id).put("name",p.name).put("level",p.level).put("color",p.color)
    private fun device(d: DeviceSnapshot)=JSONObject().put("model",d.model).put("traits",trait(d.traits)).put("used",d.used)
        .apply { d.company?.let { put("company",companyPhone(it)) } }
    fun encode(w: WorldData): String {
        if(w==WorldData()) return ""
        return JSONObject().apply {
            put("traits",JSONArray().apply { w.traits.forEach { put(trait(it)) } });put("serial",w.serial)
            put("profiles",JSONObject().apply { w.profiles.forEach { (key,p) ->
                put(key,JSONObject().put("apps",strings(p.apps)).put("color",p.color).put("wallpaper",p.wallpaper)
                    .put("theme",p.theme).put("animation",p.animation).put("bestSnake",p.bestSnake))
            } })
            put("tokens",w.appTokens);put("albums",strings(w.albums));put("skins",strings(w.skins))
            put("codes",strings(w.codes));put("seasonalSkins",strings(w.seasonalSkins))
            put("settled",JSONArray(w.settledLots.toList()))
            put("auctions",JSONArray().apply { w.auctions.forEach { a -> put(JSONObject().put("id",a.id).put("device",device(a.device))
                .put("reserve",a.reserve).put("start",a.startedAt).put("end",a.endsAt).put("ceiling",a.ceiling)) } })
            val c=w.company
            put("company",JSONObject().put("name",c.name).put("level",c.level).put("readyAt",c.readyAt).put("produced",c.produced)
                .put("productionLevel",c.productionLevel).put("series",c.series)
                .put("phones",JSONArray().apply { c.phones.forEach { put(companyPhone(it)) } }))
            put("miner",JSONObject().put("key",w.miner.key).put("activeMs",w.miner.activeMs))
            put("battleAt",w.lastBattleAt);put("wins",w.npcWins);put("losses",w.npcLosses);put("revision",w.economyRevision)
        }.toString()
    }
    private fun readStrings(j: JSONObject,key: String,limit: Int=1000): Set<String> {
        val a=j.optJSONArray(key) ?: return emptySet()
        return (0 until minOf(a.length(),limit)).map { a.optString(it).take(80) }.filter { it.isNotBlank() }.toSet()
    }
    private fun readTrait(j: JSONObject?): PhoneTraits {
        if(j==null) return PhoneTraits()
        val mutation=runCatching { Mutation.valueOf(j.optString("mutation")) }.getOrDefault(Mutation.NORMAL)
        return PhoneTraits(mutation,j.optInt("condition",3).coerceIn(0,3),j.optLong("uid",0).coerceAtLeast(0))
    }
    private fun readCompanyPhone(j: JSONObject?): CompanyPhone? {
        if(j==null || j.optLong("id",0)<=0) return null
        return CompanyPhone(j.getLong("id"),j.optString("name","Мой смартфон").take(60),j.optInt("level",1).coerceIn(1,5),j.optInt("color",VirtualApps.colors[0]))
    }
    private fun readDevice(j: JSONObject?): DeviceSnapshot? {
        if(j==null) return null
        val model=j.optInt("model",-1)
        val company=readCompanyPhone(j.optJSONObject("company"))
        if(model !in Catalog.phones.indices && company==null) return null
        val traits=readTrait(j.optJSONObject("traits"))
        if(traits.uid<=0) return null
        return DeviceSnapshot(model,traits,j.optBoolean("used",false),company)
    }
    fun decode(raw: String): WorldData = try {
        val j=JSONObject(raw)
        val array=j.optJSONArray("traits") ?: JSONArray()
        val traits=(0 until minOf(array.length(),50000)).map { readTrait(array.optJSONObject(it)) }
        val p=j.optJSONObject("profiles") ?: JSONObject()
        val profiles=p.keys().asSequence().take(5000).mapNotNull { key ->
            if(!key.matches(Regex("(phone|company):[0-9]+"))) return@mapNotNull null
            val v=p.optJSONObject(key) ?: return@mapNotNull null
            key to VirtualProfile(readStrings(v,"apps",5).filter { it in VirtualApps.prices }.toSet(),v.optInt("color",0),
                v.optInt("wallpaper",0).coerceIn(0,5),v.optString("theme","default").take(80),
                v.optString("animation","none").take(20),v.optInt("bestSnake",0).coerceIn(0,10000))
        }.toMap()
        val c=j.optJSONObject("company") ?: JSONObject()
        val companyArray=c.optJSONArray("phones") ?: JSONArray()
        val products=(0 until minOf(companyArray.length(),20)).mapNotNull { readCompanyPhone(companyArray.optJSONObject(it)) }.distinctBy { it.id }
        val company=Company(c.optString("name","").take(24),c.optInt("level",0).coerceIn(0,5),c.optLong("readyAt",0).coerceAtLeast(0),
            c.optInt("produced",0).coerceAtLeast(0),products,c.optInt("productionLevel",0).coerceIn(0,5),c.optString("series","One").take(20))
        val auctionArray=j.optJSONArray("auctions") ?: JSONArray()
        val auctions=(0 until minOf(auctionArray.length(),5)).mapNotNull { index ->
            val a=auctionArray.optJSONObject(index) ?: return@mapNotNull null
            val item=readDevice(a.optJSONObject("device")) ?: return@mapNotNull null
            val id=a.optLong("id",0);val start=a.optLong("start",0);val end=a.optLong("end",0)
            val reserve=a.optInt("reserve",0);val ceiling=a.optInt("ceiling",0)
            if(id<=0 || start<0 || end<=start || reserve<=0 || ceiling<0) null else AuctionLot(id,item,reserve,start,end,ceiling)
        }.distinctBy { it.id }
        val settledArray=j.optJSONArray("settled") ?: JSONArray()
        val settled=(0 until minOf(settledArray.length(),200)).map { settledArray.optLong(it,0) }.filter { it>0 }.toSet()
        val maxId=(traits.map { it.uid }+products.map { it.id }+auctions.map { it.id }+auctions.map { it.device.traits.uid }).maxOrNull() ?: 0L
        val serial=maxOf(j.optLong("serial",1).coerceAtLeast(1),if(maxId<Long.MAX_VALUE) maxId+1 else Long.MAX_VALUE)
        val miner=j.optJSONObject("miner") ?: JSONObject()
        WorldData(traits,serial,profiles,j.optInt("tokens",0).coerceAtLeast(0),readStrings(j,"albums",20),readStrings(j,"skins"),
            auctions.filter { it.id !in settled },settled,company,Miner(miner.optString("key","").take(80),miner.optLong("activeMs",0).coerceIn(0,1800000)),
            j.optLong("battleAt",0).coerceAtLeast(0),j.optInt("wins",0).coerceAtLeast(0),j.optInt("losses",0).coerceAtLeast(0),
            readStrings(j,"codes",20),readStrings(j,"seasonalSkins",200),j.optLong("revision",0).coerceAtLeast(0))
    } catch (_: Exception) { WorldData() }

    fun restore(snapshot: WorldData,current: WorldData): WorldData {
        val live=if(current.economyRevision>=snapshot.economyRevision)current else snapshot
        val base=if(current.economyRevision>snapshot.economyRevision)current else snapshot
        return base.copy(
        serial=maxOf(snapshot.serial,current.serial),albums=snapshot.albums+current.albums,skins=snapshot.skins+current.skins,
        codes=snapshot.codes+current.codes,seasonalSkins=snapshot.seasonalSkins+current.seasonalSkins,
        auctions=live.auctions,settledLots=(snapshot.settledLots+current.settledLots).sorted().takeLast(200).toSet(),
        company=live.company,miner=live.miner,lastBattleAt=maxOf(snapshot.lastBattleAt,current.lastBattleAt),
        npcWins=maxOf(snapshot.npcWins,current.npcWins),npcLosses=maxOf(snapshot.npcLosses,current.npcLosses))
    }
}
