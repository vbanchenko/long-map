package de.comparus.opensource.test.longmap;

import de.comparus.opensource.longmap.LongMapImpl;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

@Slf4j
public class LongMapTest {

    private static final int NUM_OF_ELEMENTS = 5000;

    private static LongMapImpl<String> longMap;

    @BeforeClass
    public static void init() {
        longMap = new LongMapImpl<>(String.class);
    }

    /**
     * Put values to map from 0 to NUM_OF_ELEMENTS in reverse order, put both negative and positive values
     */
    private void putElements() {
        int size = (int) longMap.size();
        final Random random = new Random();
        long startTime = System.currentTimeMillis();
        for (long i = NUM_OF_ELEMENTS; i >= 0; i--) {
            size = putElement(size, random, i);
            size = putElement(size, random, -i);
            Assert.assertEquals(size, longMap.size());
        }
        log.info("Put {} elements in {} ms", longMap.size(), System.currentTimeMillis() - startTime);
        Assert.assertFalse("Map must not be empty after adding elements", longMap.isEmpty());
    }

    /**
     * Put key-value to map, value equals String.valueOf(key) or null chosen by random.
     * @param size - current size
     * @param random - way to decide to put string value or null
     * @param key - map key
     * @return - new size (after adding element)
     */
    private int putElement(int size, Random random, long key) {
        if (!longMap.containsKey(key)) {
            size++;
        }
        // put some null values to test work with nulls
        longMap.put(key, random.nextBoolean() ? String.valueOf(key) : null);
        return size;
    }

    /**
     * Call to putElements() if map is empty.
     */
    private void putIfEmpty() {
        if (longMap.isEmpty()) {
            log.info("Map was empty, adding new elements...");
            putElements();
        }
    }

    @Test
    public void testPut() {
        if (!longMap.isEmpty()) {
            log.info("Clearing before put...");
            longMap.clear();
        }

        log.info("Putting elements...");
        putElements();

        final long size = longMap.size();
        log.info("Putting same elements...");
        putElements();

        Assert.assertEquals("Size must not be changed if put existing keys", size, longMap.size());
    }

    @Test
    public void testRemove() {
        putIfEmpty();
        final long size = longMap.size();
        long startTime = System.currentTimeMillis();
        for (long i = NUM_OF_ELEMENTS + 1; i < NUM_OF_ELEMENTS * 2; i++) {
            String removed = longMap.remove(i);
            Assert.assertNull("Such key must not be present", removed);
        }
        long i = 0;
        while (i <= NUM_OF_ELEMENTS) {
            String removed = longMap.remove(i);
            if (removed != null) {
                Assert.assertEquals(String.valueOf(i), removed);
            }
            i++;
        }
        log.info("Removed {} elements in {} ms", i, System.currentTimeMillis() - startTime);
        Assert.assertEquals(size - i, longMap.size());
    }

    @Test
    public void testContainsKey() {
        putIfEmpty();

        log.info("Collecting keys...");
        long[] keys = longMap.keys();
        Assert.assertEquals(longMap.size(), keys.length);

        log.info("Checking 'containsKey'...");
        long startTime = System.currentTimeMillis();
        for (long key : keys) {
            Assert.assertTrue(longMap.containsKey(key));
        }
        log.info("Checked contains {} keys in {} ms", keys.length, System.currentTimeMillis() - startTime);

        Assert.assertFalse(longMap.containsKey(Integer.MAX_VALUE));
    }

    @Test
    public void testContainsValue() {
        putIfEmpty();

        log.info("Collecting values...");
        String[] values = longMap.values();
        Assert.assertEquals(longMap.size(), values.length);

        log.info("Checking 'containsValue'...");
        long startTime = System.currentTimeMillis();
        for (String value : values) {
            Assert.assertTrue(longMap.containsValue(value));
        }
        log.info("Checked contains {} values in {} ms", values.length, System.currentTimeMillis() - startTime);

        Assert.assertFalse(longMap.containsValue(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test
    public void testGet() {
        putIfEmpty();
        long[] keys = longMap.keys();
        long startTime = System.currentTimeMillis();
        for (long key : keys) {
            String value = longMap.get(key);
            if (value != null) {
                Assert.assertEquals(String.valueOf(key), value);
            }
        }
        log.info("Read {} values in {} ms", keys.length, System.currentTimeMillis() - startTime);
    }

    @Test
    public void testClear() {
        putIfEmpty();
        log.info("Clearing Map...");
        longMap.clear();
        Assert.assertTrue(longMap.isEmpty());
        Assert.assertEquals(0, longMap.size());
    }
}