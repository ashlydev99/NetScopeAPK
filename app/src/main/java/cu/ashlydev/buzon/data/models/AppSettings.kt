package cu.ashlydev.buzon.data.models

data class AppSettings(
    val waitTime: Int = 3,
    val messageTime: Int = 60,
    val greetingPath: String = "",
    val farewellPath: String = ""
)