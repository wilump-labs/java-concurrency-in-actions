package labs.wilump.inventory.service;

import labs.wilump.inventory.repository.StockRepository;
import org.springframework.stereotype.Service;

@Service
public class SynchronizedStockService {

    private final StockRepository stockRepository;

    public SynchronizedStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public synchronized void decrease(Long id, Long quantity) {
        var stock = stockRepository.findById(id).orElseThrow();

        stock.decrease(quantity);

        stockRepository.save(stock);
    }

}


public class TxSynchronizedStockService {

        private final SynchronizedStockService stockService;

        public TxSynchronizedStockService(SynchronizedStockService stockService) {
            this.stockService = stockService;
        }

        public void decrease(Long id, Long quantity) {
            this.startTransaction();

            // 아래 함수에 대해서는 동시 접근이 제한되지만,
            // 함수 호출 후 커밋이 되어 DB에 반영되기 전에 다른 스레드에서 접근할 수 있음
            stockService.decrease(id, quantity);

            this.endTransaction();
        }
}