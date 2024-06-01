import kotlinx.serialization.Serializable

@Serializable
data class ServiceAttributeSet(
    val copies: Int = 1,
    val orientation: String = "portrait",
    val colorPrint: Boolean = false,
)