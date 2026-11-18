package com.evie.search.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Behavior locks for the Google suggest client's pure parts. */
@RunWith(RobolectricTestRunner.class)
@Config(application = android.app.Application.class, sdk = 33)
public class WebSuggestClientTest {

    @Test
    public void parse_extractsSuggestionArray() {
        String body = "[\"cal\",[\"Calculator\",\"Calendar\",\"calorie counter\"],[],[]]";
        List<String> out = WebSuggestClient.parse(body);
        assertEquals(3, out.size());
        assertEquals("Calculator", out.get(0));
        assertEquals("Calendar", out.get(1));
        assertEquals("calorie counter", out.get(2));
    }

    @Test
    public void parse_capsAtMaxSuggestions() {
        StringBuilder sb = new StringBuilder("[\"x\",[");
        for (int i = 0; i < 20; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append("\"s").append(i).append('"');
        }
        sb.append("]]");
        List<String> out = WebSuggestClient.parse(sb.toString());
        assertEquals(WebSuggestClient.MAX_SUGGESTIONS, out.size());
    }

    @Test
    public void parse_skipsEmptyEntries() {
        List<String> out = WebSuggestClient.parse("[\"x\",[\"a\",\"\",\"b\"]]");
        assertEquals(2, out.size());
        assertEquals("a", out.get(0));
        assertEquals("b", out.get(1));
    }

    @Test
    public void parse_garbageYieldsNull() {
        assertNull(WebSuggestClient.parse("<html>blocked</html>"));
        assertNull(WebSuggestClient.parse("not json"));
        assertNull(WebSuggestClient.parse(""));
    }

    @Test
    public void urlFor_encodesQuery() {
        String url = WebSuggestClient.urlFor("calc ñ");
        assertTrue(url.startsWith(WebSuggestClient.ENDPOINT));
        assertTrue(url.contains("client=firefox"));
        assertTrue(url.contains("q=calc+%C3%B1"));
        assertTrue(!url.contains(" "));
    }
}
