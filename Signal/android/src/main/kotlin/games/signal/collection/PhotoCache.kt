package games.signal.collection

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Full-size pictures for the optional small download. Album IDs and hashes come from the APK. */
internal class PhotoCache(private val directory: File, private val release: URL, private val hashes: ByteArray) {
    private val locks = Array(32) { Any() }

    fun open(id: Int): InputStream {
        if (id !in 0 until hashes.size / 32) throw IOException("Unknown picture")
        synchronized(locks[id % locks.size]) {
            val expected = hashes.copyOfRange(id * 32, id * 32 + 32)
            val name = expected.joinToString("") { "%02x".format(it.toInt() and 255) }
            val file = File(directory, name)
            if (file.isFile) return file.inputStream()

            val connection = URL(release, "phone$id.webp").openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 8000
            connection.instanceFollowRedirects = false
            val bytes = try {
                if (connection.responseCode != 200) throw IOException("Photo unavailable")
                connection.inputStream.use { stream ->
                    val out = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = stream.read(buffer)
                        if (read < 0) break
                        if (out.size() + read > 2_000_000) throw IOException("Oversized picture")
                        out.write(buffer, 0, read)
                    }
                    out.toByteArray()
                }
            } finally {
                connection.disconnect()
            }
            if (!MessageDigest.isEqual(expected, MessageDigest.getInstance("SHA-256").digest(bytes))) {
                throw IOException("Incomplete or incorrect picture")
            }
            // Display the verified bytes even when Android has no space left for its cache.
            try {
                directory.mkdirs()
                val temporary = File.createTempFile("photo-", ".part", directory)
                try {
                    temporary.writeBytes(bytes)
                    temporary.renameTo(file)
                } finally {
                    temporary.delete()
                }
            } catch (_: IOException) {
                // A later visit can retry caching.
            }
            return bytes.inputStream()
        }
    }
}
