package labs.wilump.inventory.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.isNull;

/**
 * Lock 사용을 할 경우 datasource 분리해서 사용
 * - connection pool 점유하는 것을 방지
 */
@Component
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
