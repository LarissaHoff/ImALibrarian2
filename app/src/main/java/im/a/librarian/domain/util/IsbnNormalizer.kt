package im.a.librarian.domain.util

object IsbnNormalizer {

    fun sanitize(raw: String): String =
        raw.filter { it.isDigit() || it == 'X' || it == 'x' }.uppercase()

    fun isValidShape(isbn: String): Boolean {
        val clean = sanitize(isbn)
        return (clean.length == 13 && (clean.startsWith("978") || clean.startsWith("979"))) ||
            (clean.length == 10 &&
                clean.take(9).all { it.isDigit() } &&
                (clean.last().isDigit() || clean.last() == 'X'))
    }

    fun toIsbn13(isbn: String): String {
        val clean = sanitize(isbn)
        return when {
            clean.length == 13 && isValidShape(clean) -> clean
            clean.length == 10 && isValidShape(clean) -> isbn10To13(clean)
            else -> ""
        }
    }

    fun toIsbn10(isbn13: String): String {
        val clean = sanitize(isbn13)
        if (clean.length != 13 || !clean.startsWith("978") || !clean.all { it.isDigit() }) return ""
        var sum = 0
        for (i in 0..8) {
            sum += clean[i + 3].digitToInt() * (10 - i)
        }
        val checkDigit = (11 - (sum % 11)) % 11
        val checkChar = if (checkDigit == 10) "X" else checkDigit.toString()
        return clean.substring(3, 12) + checkChar
    }

    fun hasValidChecksum(isbn: String): Boolean {
        val clean = sanitize(isbn)
        return when {
            clean.length == 13 -> isbn13Checksum(clean) == 0
            clean.length == 10 && isValidShape(clean) -> isbn10Checksum(clean) == 0
            else -> false
        }
    }

    private fun isbn10To13(isbn10: String): String {
        val prefix = "978${isbn10.dropLast(1)}"
        var sum = 0
        for (i in prefix.indices) {
            val digit = prefix[i].digitToInt()
            sum += if (i % 2 == 0) digit else digit * 3
        }
        val checkDigit = (10 - (sum % 10)) % 10
        return "$prefix$checkDigit"
    }

    private fun isbn13Checksum(clean: String): Int {
        return clean.foldIndexed(0) { index, acc, c ->
            acc + (c - '0') * (if (index % 2 == 0) 1 else 3)
        } % 10
    }

    private fun isbn10Checksum(clean: String): Int {
        var sum = 0
        for (i in clean.indices) {
            val value = if (clean[i] == 'X') 10 else clean[i] - '0'
            sum += value * (10 - i)
        }
        return sum % 11
    }
}
