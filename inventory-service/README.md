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

<br>
<br>

## MySQL을 활용한 동시성 제어
### Pessimistic Lock(비관적 락)
- 실제로 데이터에 lock을 걸어서 정합성을 맞추는 방법
- `exclusive lock`(배타 락)을 걸게 되며 다른 트랜잭션에서는 lock이 해제되기 전에 데이터를 가져갈 수 없음
- `deadlock`이 걸릴 수 있기 때문에 주의 필요

#### 사용 방법
```java
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id=:id")
    Stock findByIdWithPessimisticLock(@Param("id") Long id);
    
}
```

<br>

### Optimistic Lock(낙관적 락)
- 실제 lock을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법
- 데이터를 읽은 후에 update를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트를 진행
- 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은후에 작업을 수행

#### 사용 방법
```java
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    @Lock(value = LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithOptimisticLock(@Param("id") Long id);
    
}
```

version 미일칠로 인한 변경 실패 시 작업을 핸들링할 수 있음
```java
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity);
                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }
}
```

<br>

### Named Lock(네임드 락)
- 이름을 가진 metadata locking 방법
- 이름을 가진 lock을 획득한 후 해제할 때까지 다른 세션은 이 lock을 획득할 수 없도록 함
- 주의할 점은 transaction이 종료될 때 lock이 자동으로 해제되지 않음
- 별도의 명령어로 해제를 수행해주거나 선점 시간이 끝나야 해제

#### 사용 방법
별도의 LockRepository를 사용을 할 경우 datasource 분리해서 사용
- connection pool 점유하는 것을 방지
- `decrease` 요청이 한 번 있을 때 Lock transaction과 Decrease transaction 작업을 실제 2개의 물리적인 connection을 사용하여 처리

```java
public class LockRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public LockRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    private static final String KEY_PARAM = "key";
    private static final String TIMEOUT_PARAM = "timeout";
    private static final String GET_LOCK = "SELECT GET_LOCK(:key, :timeout)";
    private static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(:key)";
    private static final int DEFAULT_TIMEOUT_SECONDS = 3000;

    public <T> T executeWithLock(String key, int timeoutSeconds, Supplier<T> supplier) {
        try {
            getLock(key, timeoutSeconds);
            return supplier.get();
        } finally {
            releaseLock(key);
        }
    }

    public <T> T executeWithLock(String key, Supplier<T> supplier) {
        return executeWithLock(key, DEFAULT_TIMEOUT_SECONDS, supplier);
    }

    public void getLock(String key, int timeout) {
        Map<String, Object> params = Map.of(
                KEY_PARAM, key,
                TIMEOUT_PARAM, timeout
        );
        Integer result = namedParameterJdbcTemplate.queryForObject(GET_LOCK, params, Integer.class);
        checkResult(result);
    }

    public void getLock(String key) {
        getLock(key, DEFAULT_TIMEOUT_SECONDS);
    }

    public void releaseLock(String key) {
        Map<String, Object> params = Map.of(KEY_PARAM, key);

        Integer result = namedParameterJdbcTemplate.queryForObject(RELEASE_LOCK, params, Integer.class);
        checkResult(result);
    }

    private void checkResult(Integer result) {
        if (isNull(result) || result != 1) {
            throw new IllegalStateException("Lock failed");
        }
    }

}
```

이후 LockRepository를 사용하여 GET_LOCK, RELEASE_LOCK을 수행
- 이 때 비즈니스 로직이 담긴 함수(`decreaseWithRequiresNew`)는 `@Transactional(propagation = Propagation.REQUIRES_NEW)`로 선언하여 새로운 트랜잭션을 생성
  - `Transaction` 동작의 경우 부모의 트랜잭션을 그대로 사용하게 되기 때문에 자식 트랜잭션에서 이어받음 (= 부모 작업까지 끝나야 커밋을 한다는 의미)
- 새롭게 트랜잭션을 만들어서 커밋을 하지 않으면 `releaseLock` 호출 이후 커밋을 하기 때문에 대기 중인 다른 스레드(트랜잭션)에서 커밋되지 않은 데이터를 읽을 수도 있음


```java
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
```

<br>

#### 참고
- [MySQL - Locking Functions](https://dev.mysql.com/doc/refman/8.0/en/locking-functions.html)
- [MySQL - Metadata Locking](https://dev.mysql.com/doc/refman/8.0/en/metadata-locking.html)