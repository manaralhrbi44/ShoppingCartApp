# ShoppingCartApp (CPIS350 - Lab 3)

Console shopping-cart application in Java that combines three design patterns
and an AI-powered recommendation component.

| Pattern  | Classes |
|----------|---------|
| Factory  | `ProductFactory`, `Product`, `Electronics`, `Book`, `Clothing` |
| Observer | `CartSubject`, `ShoppingCart`, `CartObserver`, `CartDisplayObserver`, `InventoryObserver` |
| Strategy | `Checkout`, `PaymentStrategy`, `CreditCardPayment`, `PayPalPayment` |
| AI component | `RecommendationService`, `RecommendationProvider`, `AiRecommendationProvider`, `RuleBasedRecommendationProvider` (fallback) |

## Run (Replit / terminal)
```
javac *.java
java Main          # demo of the whole shopping experience
java TestRunner    # 34 automated checks for all patterns + AI component
```

## Enable the AI recommendations
1. In Replit open **Tools -> Secrets** and add `ANTHROPIC_API_KEY` = your key.
   (Optional: `ANTHROPIC_MODEL`, default `claude-sonnet-5-5`.)
2. Run `java Main`. The checkout section prints `source: AI (...)`.
3. Without a key, or if the API fails, the app prints `source: Fallback (rule-based)`
   and keeps working - the key is never written in the source code.
