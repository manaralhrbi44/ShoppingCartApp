import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Calls the Anthropic (Claude) Messages API to get smart product suggestions.
 *  - API key is read from the ANTHROPIC_API_KEY environment variable (Replit "Secrets"),
 *    it is NEVER written in the source code.
 *  - Short timeouts keep the app responsive; every failure becomes a RecommendationException
 *    so the caller can switch to the fallback.
 */
public class AiRecommendationProvider implements RecommendationProvider {
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final Duration TIMEOUT = Duration.ofSeconds(8);

    private final String apiKey;
    private final String model;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();

    public AiRecommendationProvider() {
        this(System.getenv("ANTHROPIC_API_KEY"),
             System.getenv().getOrDefault("ANTHROPIC_MODEL", "claude-sonnet-5-5"));
    }

    public AiRecommendationProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override public String getName() { return "AI (" + model + ")"; }

    @Override
    public List<String> recommend(List<Product> cartItems) throws RecommendationException {
        if (apiKey == null || apiKey.isBlank())
            throw new RecommendationException("ANTHROPIC_API_KEY environment variable is not set");

        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                .timeout(TIMEOUT)
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestJson(model, buildPrompt(cartItems))))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200)
                throw new RecommendationException("AI API returned HTTP " + response.statusCode());
            List<String> suggestions = parseSuggestions(response.body());
            if (suggestions.isEmpty()) throw new RecommendationException("AI API returned no suggestions");
            return suggestions;
        } catch (IOException e) {
            throw new RecommendationException("Could not reach AI API: " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : " - " + e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RecommendationException("AI request interrupted", e);
        }
    }

    /** The prompt sent to the AI: cart contents + strict output format. */
    static String buildPrompt(List<Product> cartItems) {
        StringBuilder sb = new StringBuilder(
                "Based on the following cart items, suggest additional products that the customer might be interested in:\n");
        for (Product p : cartItems) {
            sb.append("- ").append(p.getName()).append(" (").append(p.getCategory())
              .append(", SAR ").append(String.format("%.2f", p.getPrice())).append(")\n");
        }
        sb.append("\nReply with exactly 3 suggestions, one per line, in the format: Product name - short reason. ")
          .append("Do not add an introduction, numbering or any other text.");
        return sb.toString();
    }

    static String buildRequestJson(String model, String prompt) {
        return "{\"model\":\"" + jsonEscape(model) + "\"," +
               "\"max_tokens\":300," +
               "\"messages\":[{\"role\":\"user\",\"content\":\"" + jsonEscape(prompt) + "\"}]}";
    }

    /** Extracts the AI text from the API JSON and turns it into a clean list of suggestions (max 3). */
    static List<String> parseSuggestions(String responseJson) throws RecommendationException {
        String text = extractJsonString(responseJson, "text");
        if (text == null) throw new RecommendationException("Unexpected AI response format");
        List<String> result = new ArrayList<>();
        for (String line : text.split("\\R")) {
            String s = line.replaceFirst("^\\s*(?:[-*\u2022]|\\d+[.)])\\s*", "").trim();
            if (!s.isEmpty()) result.add(s);
            if (result.size() == 3) break;
        }
        return result;
    }

    // ---- tiny JSON helpers (no external libraries needed on Replit) ----
    static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    static String extractJsonString(String json, String field) {
        int k = json.indexOf("\"" + field + "\"");
        if (k < 0) return null;
        int colon = json.indexOf(':', k);
        int start = json.indexOf('"', colon + 1);
        if (colon < 0 || start < 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') return sb.toString();
            if (c == '\\' && i + 1 < json.length()) {
                char n = json.charAt(++i);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'u':
                        sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                        i += 4;
                        break;
                    default:  sb.append(n);
                }
            } else sb.append(c);
        }
        return null;
    }
}
