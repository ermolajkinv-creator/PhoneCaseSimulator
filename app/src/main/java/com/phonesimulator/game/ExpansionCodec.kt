package com.phonesimulator.game

import org.json.JSONArray
import org.json.JSONObject

object ExpansionCodec {
    fun encode(e: Expansion): String = JSONObject().apply {
        put("vault",e.vault);put("income",e.income);put("expenses",e.expenses)
        put("negotiated",JSONArray(e.negotiated.toList()));put("awards",JSONArray(e.awardsClaimed.toList()))
        put("discounts",JSONObject(e.discounts));put("deliveryDay",e.deliveryDay);put("idleAt",e.idleAt)
        put("ledger",JSONArray().apply { e.ledger.takeLast(50).forEach { row ->
            put(JSONObject().put("at",row.at).put("amount",row.amount).put("title",row.title))
        } })
    }.toString()
    fun decode(raw: String): Expansion = try {
        val j=JSONObject(raw)
        fun strings(key: String): Set<String> {
            val a=j.optJSONArray(key) ?: JSONArray()
            return (0 until minOf(a.length(),200)).map { a.optString(it) }.toSet()
        }
        val d=j.optJSONObject("discounts") ?: JSONObject()
        val discounts=d.keys().asSequence().take(200).mapNotNull { k ->
            val price=d.optInt(k,0);if(price>0) k to price else null
        }.toMap()
        val a=j.optJSONArray("ledger") ?: JSONArray()
        val ledger=(0 until minOf(a.length(),50)).mapNotNull { index ->
            val row=a.optJSONObject(index) ?: return@mapNotNull null
            val amount=row.optLong("amount",0)
            if(amount in -4294967294L..4294967294L) CashEvent(row.optLong("at",0).coerceAtLeast(0),amount,row.optString("title").take(60)) else null
        }
        Expansion(j.optInt("vault",0).coerceAtLeast(0),j.optLong("income",0).coerceAtLeast(0),
            j.optLong("expenses",0).coerceAtLeast(0),ledger,strings("negotiated"),discounts,
            strings("awards"),j.optLong("deliveryDay",-1),j.optLong("idleAt",0).coerceAtLeast(0))
    } catch (_: Exception) { Expansion() }
}
