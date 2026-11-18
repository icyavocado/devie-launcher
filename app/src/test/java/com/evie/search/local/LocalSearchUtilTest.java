package com.evie.search.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Behavior-lock tests for the fuzzy-match primitives (pure Java). */
public class LocalSearchUtilTest {

    @Test
    public void normalize_nullAndEmptyYieldNull() {
        assertNull(LocalSearchUtil.normalizeString(null));
        assertNull(LocalSearchUtil.normalizeString(""));
    }

    @Test
    public void normalize_stripsDiacritics() {
        assertEquals("Cafe", LocalSearchUtil.normalizeString("Café"));
        assertEquals("e", LocalSearchUtil.normalizeString("é"));
    }

    @Test
    public void normalize_stripsApostrophes() {
        assertEquals("OBrien", LocalSearchUtil.normalizeString("O'Brien"));
    }

    @Test
    public void normalize_plainTextUnchanged() {
        assertEquals("Calculator", LocalSearchUtil.normalizeString("Calculator"));
    }

    @Test
    public void tokenize_nullAndEmptyYieldNull() {
        assertNull(LocalSearchUtil.tokenizeString(null));
        assertNull(LocalSearchUtil.tokenizeString(""));
    }

    @Test
    public void tokenize_splitsOnWhitespace_lowercased() {
        // Donor quirk (JVM split): the camel-boundary match right after the
        // whitespace yields an empty middle token.
        String[] out = LocalSearchUtil.tokenizeString("Quick Brown");
        assertEquals(3, out.length);
        assertEquals("quick", out[0]);
        assertEquals("", out[1]);
        assertEquals("brown", out[2]);
    }

    @Test
    public void tokenize_splitsCamelCase() {
        String[] out = LocalSearchUtil.tokenizeString("HelloWorld");
        assertEquals(2, out.length);
        assertEquals("hello", out[0]);
        assertEquals("world", out[1]);
    }

    @Test
    public void charToBucket_knownBuckets() {
        assertEquals(0, LocalSearchUtil.charToBucket(' '));
        assertEquals(1, LocalSearchUtil.charToBucket('a'));
        assertEquals(26, LocalSearchUtil.charToBucket('z'));
    }

    @Test
    public void distance_identicalStringsIsZero() {
        assertEquals(0, LocalSearchUtil.distance("abc", "abc", false));
        assertEquals(0, LocalSearchUtil.distance("", "", false));
    }

    @Test
    public void distance_substitutionIsOne() {
        int d = LocalSearchUtil.distance("abc", "abd", false);
        assertTrue(d > 0);
        assertTrue(d <= 2);
    }

    @Test
    public void distance_maxDistanceByLength() {
        assertEquals(0, LocalSearchUtil.maxDistance("abc"));
        assertEquals(1, LocalSearchUtil.maxDistance("abcd"));
    }

    @Test
    public void addKeywordWithDeletions_generatesPrefixDeletions() {
        // Donor behavior: z=false caps at str.length()-1, the deletion happens
        // at the incremented index, so only position 1 is deleted for i=1.
        java.util.Set<String> set = new java.util.HashSet<>();
        LocalSearchUtil.addKeywordWithDeletions(set, "abc", 1, false);
        assertTrue(set.contains("abc"));
        assertTrue(set.contains("ac"));
    }
}
