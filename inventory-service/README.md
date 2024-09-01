# 재고시스템 예제를 통한 동시성 제어

## Synchronized
- `synchronized` 키워드를 사용하여 동시성 제어를 할 수 있음
  - cf. `service/SynchronizedInventoryService.java`

### 문제점
#### 1. `@Transactional` 어노테이션을 사용하면 `synchronized` 키워드가 동작하지 않음
- `@Transactional` 어노테이션은 작성한 클래스를 wrapping 하여 동작, 예를 들면 아래와 같은 형태

  ```java
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
  ```

- `synchronized` 키워드가 선언된 함수 자체는 동시 접근을 제한하지만, 실제 DB 반영 전에 다른 스레드에서 호출이 가능함

<br>

#### 2. 서버의 수가 2개 이상일 때는 동시성 제어가 불가능함
- 서버가 2개 이상일 때, 각 서버는 독립적으로 동작하므로 동시성 제어가 불가능함