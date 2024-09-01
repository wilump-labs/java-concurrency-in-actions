package labs.wilump.inventory;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DirtiesContext
@Testcontainers
abstract class AbstractContainerBaseTest {

    private final static String REDIS_IMAGE = "redis:7-alpine";
    private final static int REDIS_PORT = 6379;

    @Container
    private static GenericContainer REDIS_CONTAINER = new GenericContainer(REDIS_IMAGE)
            .withExposedPorts(6379)
            .withReuse(true);

    @BeforeAll
    public static void startContainer() {
        REDIS_CONTAINER.start();
    }

    @DynamicPropertySource
    private static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(REDIS_PORT)::toString);
    }
}
