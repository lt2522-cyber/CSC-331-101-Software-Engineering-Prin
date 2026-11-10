package sportsseer.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * JUnit tests for the dataset converter's season labels.
 */
class DatasetConverterTest {

    /** 2017 is the 2016-17 season. */
    @Test
    void normalSeasonLabel() {
        assertEquals("2016-17", DatasetConverter.seasonLabel(2017));
    }

    /** Century change keeps two digits. */
    @Test
    void centuryLabel() {
        assertEquals("1999-00", DatasetConverter.seasonLabel(2000));
    }

    /** The first BAA season. */
    @Test
    void firstSeasonLabel() {
        assertEquals("1946-47", DatasetConverter.seasonLabel(1947));
    }
}
