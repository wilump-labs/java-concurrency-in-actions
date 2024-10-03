package labs.wilump.coroutine.blocking

import labs.wilump.coroutine.FdsClient
import labs.wilump.coroutine.SendMoneyCommand
import labs.wilump.coroutine.SendMoneyUseCase
import java.math.BigInteger

class BlockingSendMoneyService(
    private val fdsClient: FdsClient,
) : SendMoneyUseCase {

    override fun isValidTransaction(command: SendMoneyCommand): Boolean {
        if (fdsClient.isSuspendingBankAccount(command.sourceBankAccountNumber)) {
            return false
        }

        if (fdsClient.isSuspendingBankAccount(command.destinationBankAccountNumber)) {
            return false
        }

        if (this.isDuplicatedTransaction(command.txId)) {
            return false
        }

        if (!this.isSufficientBalance(command.balance)) {
            return false
        }

        return true
    }

    private fun isDuplicatedTransaction(txId: Long): Boolean {
        Thread.sleep(30)
        return txId % 5 == 0L
    }

    private fun isSufficientBalance(balance: BigInteger): Boolean {
        Thread.sleep(30)
        return balance >= BigInteger.valueOf(1000)
    }
}