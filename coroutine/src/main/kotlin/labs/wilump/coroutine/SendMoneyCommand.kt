package labs.wilump.coroutine

import java.math.BigInteger

data class SendMoneyCommand(
    val txId: Long,
    val sourceBankAccountNumber: String,
    val destinationBankAccountNumber: String,
    val balance: BigInteger,
)