package labs.wilump.inventory.domain;

import jakarta.persistence.*;

@Entity
public class Stock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    @Version
    private Long version;

    protected Stock() {
    }

    public void decrease(Long quantity) {
        if (this.quantity - quantity < 0) {
            throw new IllegalStateException("차감할 수 있는 재고가 없습니다");
        }

        this.quantity -= quantity;
    }

    public Long getId() { return id; }

    public Long getQuantity() {
        return quantity;
    }

    public static Stock create(Long productId, Long quantity) {
        var newStock = new Stock();
        newStock.productId = productId;
        newStock.quantity = quantity;
        return newStock;
    }
}