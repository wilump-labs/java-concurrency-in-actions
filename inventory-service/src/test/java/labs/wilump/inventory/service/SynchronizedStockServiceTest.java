package labs.wilump.inventory.service;


import labs.wilump.inventory.domain.Stock;
import labs.wilump.inventory.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SynchronizedStockServiceTest {

    @Autowired
    private SynchronizedStockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void 동시에_100명_주문() throws InterruptedException {
        // given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);

        var savedId = stockRepository.save(Stock.create(1L, 100L)).getId();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(savedId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Stock stock = stockRepository.findById(savedId).orElseThrow();
        assertEquals(0, stock.getQuantity());
    }

    @AfterEach
    public void tearDown() {
        stockRepository.deleteAll();
    }
}
