package app.sadhan.update

data class Version(val major: Int, val minor: Int, val patch: Int, val prerelease: List<String>) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch }).let { if (it != 0) return it }
        if (prerelease.isEmpty() || other.prerelease.isEmpty()) return when {
            prerelease.isEmpty() && other.prerelease.isEmpty() -> 0
            prerelease.isEmpty() -> 1
            else -> -1
        }
        for (index in 0 until minOf(prerelease.size, other.prerelease.size)) {
            val left = prerelease[index]
            val right = other.prerelease[index]
            val a = left.toBigIntegerOrNull()
            val b = right.toBigIntegerOrNull()
            val order = when {
                a != null && b != null -> a.compareTo(b)
                a != null -> -1
                b != null -> 1
                else -> left.compareTo(right)
            }
            if (order != 0) return order
        }
        return prerelease.size.compareTo(other.prerelease.size)
    }

    companion object {
        fun parse(input: String): Version {
            val match = Regex("^(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$")
                .matchEntire(input.removePrefix("v")) ?: throw IllegalArgumentException("Invalid semantic version: $input")
            val prerelease = match.groupValues[4].takeIf { it.isNotEmpty() }?.split('.') ?: emptyList()
            require(prerelease.none { it.length > 1 && it.all(Char::isDigit) && it.startsWith('0') }) { "Invalid prerelease version" }
            return Version(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt(), prerelease)
        }
    }
}
