package labs.wilump.inventory.facade;

import labs.wilump.inventory.domain.Stock;
import labs.wilump.inventory.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LettuceLockStockFacadeTest {

    @Autowired
    private LettuceLockStockFacade lettuceLockStockFacade;

    @Autowired
    private StockRepository stockRepository;


    @Test
    public void 동시에_100개_요청_with_Lettuce_Lock() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // given
        var savedId = stockRepository.save(Stock.create(1L, 100L)).getId();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lettuceLockStockFacade.decrease(savedId, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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
