package labs.wilump.inventory.service;

import labs.wilump.inventory.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        var stock = stockRepository.findById(id).orElseThrow();

        stock.decrease(quantity);

        stockRepository.save(stock);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseWithRequiresNew(Long id, Long quantity) {
        var stock = stockRepository.findById(id).orElseThrow();

        stock.decrease(quantity);

        stockRepository.save(stock);
    }
}
