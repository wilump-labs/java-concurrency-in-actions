package labs.wilump.coroutine

import labs.wilump.coroutine.blocking.BlockingSendMoneyService
import labs.wilump.coroutine.executor.ExecutorSendMoneyService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch
import java.math.BigInteger
import java.util.concurrent.atomic.AtomicInteger


class SendMoneyUseCaseBenchmark {

    private val client = FdsClient()
    private val blockingUseCase = BlockingSendMoneyService(client)
    private val executorUseCase = ExecutorSendMoneyService(client)

    private val requests = `샘플데이터 생성`(100)

    private val stopWatch = StopWatch()


    @BeforeEach
    fun setUp() {
        stopWatch.start()
    }

    @Test
    fun blockingSendMoney() {
        val success = AtomicInteger()
        requests.forEach {
            if (blockingUseCase.isValidTransaction(it)) {
                success.incrementAndGet()
            }
        }
        println("Success: ${success.get()}")
    }

    @Test
    fun executorSendMoney() {
        val success = AtomicInteger()
        requests.forEach {
            if (executorUseCase.isValidTransaction(it)) {
                success.incrementAndGet()
            }
        }
        println("Success: ${success.get()}")
    }

    @AfterEach
    fun tearDown() {
        stopWatch.stop()
        println(stopWatch.prettyPrint())
    }


    // txId % 5 == 0L: invalid
    // number startWith 123: invalid
    // balance < 1000: invalid

    private fun `샘플데이터 생성`(amount: Int) = (1..amount).map {
        when (it % 5) {
            // invalid txId
            0 -> SendMoneyCommand(
                it.toLong(),
                "1234567890",
                "1234567890",
                BigInteger.valueOf(1000)
            )
            // invalid source number
            1 -> SendMoneyCommand(
                it.toLong(),
                "1234567890",
                "4561237890",
                BigInteger.valueOf(1000)
            )
            // invalid destination number
            2 -> SendMoneyCommand(
                it.toLong(),
                "4561237890",
                "1234567890",
                BigInteger.valueOf(1000)
            )
            // invalid balance
            3 -> SendMoneyCommand(
                it.toLong(),
                "4561237890",
                "4561237890",
                BigInteger.valueOf(999)
            )
            else -> SendMoneyCommand(
                it.toLong(),
                "4561237890",
                "4561237890",
                BigInteger.valueOf(1000)
            )
        }
    }
}