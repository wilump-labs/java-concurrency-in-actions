package labs.wilump.inventory.repository;

import labs.wilump.inventory.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
