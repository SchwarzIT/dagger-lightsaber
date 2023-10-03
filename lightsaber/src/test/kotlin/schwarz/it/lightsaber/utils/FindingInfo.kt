package schwarz.it.lightsaber.utils

internal data class FindingInfo(
    val message: String,
    val line: Int,
    val column: Int,
    val ruleName: String,
    val fileName: String = "test/MyComponent.java",
) {
    override fun toString(): String {
        return "$fileName:$line:$column: $message [$ruleName]"
    }
}
