package labs.wilump.thread;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * SynchronizedList VS CopyOnWriteArrayList
 * - read 작업량 < write 작업량 인 경우: SynchronizedList 효율적
 * - read 작업량 > write 작업량 인 경우: CopyOnWriteArrayList 효율적
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3, time = 500, timeUnit = MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = MILLISECONDS)
@OutputTimeUnit(MILLISECONDS)
@Fork(1)
public class ConcurrentListTest {

    List<Integer> synchronizedList = Collections.synchronizedList(new ArrayList<>());
    CopyOnWriteArrayList<Integer> copyOnWriteArrayList = new CopyOnWriteArrayList<>();

    @Setup
    public void setup() {
        synchronizedList.clear();
        copyOnWriteArrayList.clear();

        for (int i = 1; i <= 1000; i++) {
            synchronizedList.add(i);
            copyOnWriteArrayList.add(i);
        }
    }

    @Benchmark
    public void moreWrite_SynchronizedList_Read2_Write8(Blackhole blackhole) throws InterruptedException {
        operateSyncList(2, 8, 1000);
    }

    @Benchmark
    public void moreWrite_CopyOnWriteArrayList_Read2_Write8(Blackhole blackhole) throws InterruptedException {
        operateCopyOnWriteList(2, 8, 1000);
    }

    @Benchmark
    public void equal_SynchronizedList_Read5_Write5(Blackhole blackhole) throws InterruptedException {
        operateSyncList(5, 5, 1000);
    }

    @Benchmark
    public void equal_CopyOnWriteArrayList_Read5_Write5(Blackhole blackhole) throws InterruptedException {
        operateCopyOnWriteList(5, 5, 1000);
    }

    @Benchmark
    public void moreRead_SynchronizedList_Read8_Write2(Blackhole blackhole) throws InterruptedException {
        operateSyncList(8, 2, 1000);
    }

    @Benchmark
    public void moreRead_CopyOnWriteArrayList_Read8_Write2(Blackhole blackhole) throws InterruptedException {
        operateCopyOnWriteList(8, 2, 1000);
    }

    private void operateSyncList(int read, int write, int requestCount) throws InterruptedException {
        var total = read + write;
        for (int i = 0; i < requestCount; i++) {
            if (i % total < read) {
                synchronizedList.get(500);
            } else {
                synchronizedList.add(i);
            }
        }
    }

    private void operateCopyOnWriteList(int read, int write, int requestCount) throws InterruptedException {
        var total = read + write;
        for (int i = 0; i < requestCount; i++) {
            if (i % total < read) {
                synchronizedList.get(500);
            } else {
                synchronizedList.add(i);
            }
        }
    }
}
