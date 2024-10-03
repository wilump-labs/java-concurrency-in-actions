package labs.wilump.coroutine.executor

import labs.wilump.coroutine.FdsClient
import labs.wilump.coroutine.SendMoneyCommand
import labs.wilump.coroutine.SendMoneyUseCase
import java.math.BigInteger
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class ExecutorSendMoneyService(
    private val fdsClient: FdsClient,
) : SendMoneyUseCase {

    private val executor = Executors.newFixedThreadPool(20)

    override fun isValidTransaction(command: SendMoneyCommand): Boolean {
        val invalidEvents = CopyOnWriteArrayList<InvalidEvent>()
        validateTransactionAndAddInvalidEvents(command, invalidEvents)
        return invalidEvents.isEmpty()
    }

    private fun validateTransactionAndAddInvalidEvents(
        command: SendMoneyCommand,
        invalidEvents: CopyOnWriteArrayList<InvalidEvent>
    ) {
        val latch = CountDownLatch(4)
        executor.submit {
            if (fdsClient.isSuspendingBankAccount(command.sourceBankAccountNumber)) {
                invalidEvents.add(
                    InvalidEvent(
                        command.txId,
                        "Invalid source bank account"
                    )
                )
            }
            latch.countDown()
        }

        executor.submit {
            if (fdsClient.isSuspendingBankAccount(command.destinationBankAccountNumber)) {
                invalidEvents.add(
                    InvalidEvent(
                        command.txId,
                        "Invalid destination bank account"
                    )
                )
            }
            latch.countDown()
        }

        executor.submit {
            if (this.isDuplicatedTransaction(command.txId)) {
                invalidEvents.add(
                    InvalidEvent(
                        command.txId,
                        "Duplicated transaction"
                    )
                )
            }
            latch.countDown()
        }

        executor.submit {
            if (!this.isSufficientBalance(command.balance)) {
                invalidEvents.add(
                    InvalidEvent(
                        command.txId,
                        "Insufficient balance"
                    )
                )
            }
            latch.countDown()
        }
        latch.await()
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