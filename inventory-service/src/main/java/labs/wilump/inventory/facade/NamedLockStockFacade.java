package labs.wilump.inventory.facade;

import labs.wilump.inventory.repository.LockRepository;
import labs.wilump.inventory.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            lockRepository.getLock(id.toString());
            stockService.decreaseWithRequiresNew(id, quantity);
        } finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
