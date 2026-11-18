package com.evie.search.web;

import androidx.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;

/* Google suggest client for the opt-in web-suggestions Experiment.
 * One-shot per typing pause; no caching, no retries. */
public class WebSuggestClient {

    static final String ENDPOINT = "https://suggestqueries.google.com/complete/search";
    static final int MAX_SUGGESTIONS = 5;

    @Nullable
    public static List<String> fetch(String query, int timeoutMs) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(urlFor(query)).openConnection();
            if (connection instanceof HttpsURLConnection) {
                connection.setConnectTimeout(timeoutMs);
                connection.setReadTimeout(timeoutMs);
            }
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            int code = connection.getResponseCode();
            if (code != 200) {
                return null;
            }
            InputStream in = connection.getInputStream();
            List<String> out = parse(readAll(in));
            return out;
        } catch (Exception unused) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    static String urlFor(String query) {
        return ENDPOINT + "?client=firefox&q=" + java.net.URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    /** Firefox-client response: ["query", ["s1","s2",...], ...]. */
    @Nullable
    public static List<String> parse(String body) {
        try {
            JSONArray root = new JSONArray(body);
            JSONArray suggestions = root.getJSONArray(1);
            List<String> out = new ArrayList<>();
            for (int i = 0; i < suggestions.length() && out.size() < MAX_SUGGESTIONS; i++) {
                String s = suggestions.optString(i, null);
                if (s != null && !s.isEmpty()) {
                    out.add(s);
                }
            }
            return out;
        } catch (Exception unused) {
            return null;
        }
    }

    private static String readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
        in.close();
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private WebSuggestClient() {
    }
}
