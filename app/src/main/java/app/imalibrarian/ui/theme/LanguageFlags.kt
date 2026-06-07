package app.imalibrarian.ui.theme

object LanguageFlags {
    fun codeToFlagEmoji(code: String): String = when {
        code.equals("en", true) || code.equals("eng", true) -> "\uD83C\uDDEC\uD83C\uDDE7"
        code.equals("es", true) || code.equals("spa", true) -> "\uD83C\uDDEA\uD83C\uDDF8"
        code.equals("de", true) || code.equals("ger", true) || code.equals("deu", true) -> "\uD83C\uDDE9\uD83C\uDDEA"
        else -> ""
    }

    fun isFlagLanguage(code: String): Boolean = codeToFlagEmoji(code).isNotEmpty()

    fun toFlagCode(code: String): String = when {
        code.equals("en", true) || code.equals("eng", true) -> "en"
        code.equals("es", true) || code.equals("spa", true) -> "es"
        code.equals("de", true) || code.equals("ger", true) || code.equals("deu", true) -> "de"
        else -> ""
    }

    fun displayLabel(code: String): String {
        val flag = codeToFlagEmoji(code)
        return if (flag.isNotEmpty()) flag else code
    }
}
