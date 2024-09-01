package labs.wilump.inventory.service;

import labs.wilump.inventory.domain.Stock;
import labs.wilump.inventory.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Test
    public void 단일_요청에_의한_재고_차감() {
        // given
        var savedId = stockRepository.save(Stock.create(1L, 100L)).getId();

        // when
        stockService.decrease(savedId, 1L);

        // then
        Stock stock = stockRepository.findById(savedId).orElseThrow();

        assertEquals(99, stock.getQuantity());
    }

    @AfterEach
    public void tearDown() {
        stockRepository.deleteAll();
    }
}
