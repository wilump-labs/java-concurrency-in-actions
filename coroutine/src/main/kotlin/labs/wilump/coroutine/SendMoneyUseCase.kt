package labs.wilump.coroutine

interface SendMoneyUseCase {
    fun isValidTransaction(command: SendMoneyCommand): Boolean
}