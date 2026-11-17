import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Same job as AiRecommendationProvider but calls the Google Gemini API (free tier available).
 *  - API key is read from the GEMINI_API_KEY environment variable (Replit Secrets).
 *  - Model can be changed with GEMINI_MODEL (default: gemini-2.5-flash).
 *  - Any failure becomes a RecommendationException so the fallback takes over.
 */
public class GeminiRecommendationProvider implements RecommendationProvider {
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String apiKey;
    private final String model;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();

    public GeminiRecommendationProvider() {
        this(System.getenv("GEMINI_API_KEY"),
             System.getenv().getOrDefault("GEMINI_MODEL", "gemini-2.5-flash"));
    }

    public GeminiRecommendationProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override public String getName() { return "AI (Gemini " + model + ")"; }

    @Override
    public List<String> recommend(List<Product> cartItems) throws RecommendationException {
        if (apiKey == null || apiKey.isBlank())
            throw new RecommendationException("GEMINI_API_KEY environment variable is not set");

        // the key travels in a header (never in the URL or in the source code)
        HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + model + ":generateContent"))
                .timeout(TIMEOUT)
                .header("content-type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(
                        buildRequestJson(AiRecommendationProvider.buildPrompt(cartItems))))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                String body = response.body() == null ? "" : response.body().replaceAll("\\s+", " ");
                throw new RecommendationException("Gemini API returned HTTP " + response.statusCode()
                        + (body.isEmpty() ? "" : ": " + body.substring(0, Math.min(160, body.length()))));
            }
            List<String> suggestions = AiRecommendationProvider.parseSuggestions(response.body());
            if (suggestions.isEmpty()) throw new RecommendationException("Gemini returned no suggestions");
            return suggestions;
        } catch (IOException e) {
            throw new RecommendationException("Could not reach Gemini API: " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : " - " + e.getMessage()), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RecommendationException("Gemini request interrupted", e);
        }
    }

    /** Request body of generateContent. Thinking is switched off so the short answer is not truncated. */
    static String buildRequestJson(String prompt) {
        return "{\"contents\":[{\"parts\":[{\"text\":\"" + AiRecommendationProvider.jsonEscape(prompt) + "\"}]}],"
             + "\"generationConfig\":{\"maxOutputTokens\":400,\"thinkingConfig\":{\"thinkingBudget\":0}}}";
    }
}
