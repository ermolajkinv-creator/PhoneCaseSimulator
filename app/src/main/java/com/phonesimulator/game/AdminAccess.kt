package com.phonesimulator.game

import java.security.MessageDigest

/** Offline convenience lock for this local game, not server-side authorization. */
object AdminAccess {
    private const val PASSWORD_HASH = "6764477a82c8a644d44f3fca129cdaa059e8ddf9216c868931de4a748715ddc7"
    fun accepts(password: String): Boolean {
        val actual = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        val expected = PASSWORD_HASH.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        return MessageDigest.isEqual(actual, expected)
    }
}
