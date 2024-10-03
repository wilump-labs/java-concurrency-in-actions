package labs.wilump.coroutine.executor

data class InvalidEvent(
    val txId: Long,
    val cause: String? = null
)