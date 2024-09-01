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