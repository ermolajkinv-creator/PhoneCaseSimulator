package games.signal.collection

import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

/** Maps album IDs to byte ranges within the uncompressed offline asset. */
internal class OfflinePhotos(index: InputStream, private val openPack: () -> InputStream) {
    private val offsets = DataInputStream(index).use { input ->
        val count = input.readInt()
        if (count !in 1..1_000_000) throw IOException("Invalid photo count")
        LongArray(count + 1) { input.readLong() }.also { table ->
            if (table.first() != 0L) throw IOException("Invalid first offset")
            for (i in 0 until count) {
                if (table[i + 1] - table[i] !in 1L..8_000_000L) throw IOException("Invalid photo length")
            }
        }
    }

    fun open(id: Int): InputStream {
        if (id < 0 || id >= offsets.lastIndex) throw IOException("Unknown photo")
        val bytes = ByteArray((offsets[id + 1] - offsets[id]).toInt())
        DataInputStream(openPack()).use { input ->
            var remaining = offsets[id]
            while (remaining > 0) {
                val skipped = input.skip(remaining)
                if (skipped <= 0) throw IOException("Incomplete photo archive")
                remaining -= skipped
            }
            input.readFully(bytes)
        }
        return bytes.inputStream()
    }
}
