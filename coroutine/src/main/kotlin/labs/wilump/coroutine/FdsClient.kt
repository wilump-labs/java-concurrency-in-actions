package labs.wilump.coroutine

class FdsClient {
    fun isSuspendingBankAccount(accountNumber: String): Boolean {
        Thread.sleep(30)
        return accountNumber.startsWith("123")
    }
}