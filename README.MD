# General design overview
The controller layer handles all API calls (for both exchange rates and metrics). 
It performs input validation, basic logging, and tracks incoming requests.

The business layer, ExchangeRateService, is responsible for:
- Caching responses
- Verifying whether currencies are supported
- Calculating average exchange rates across multiple providers
- Filtering out unsupported or unwanted data

At the integration layer, there is a shared ExchangeRateProvider
interface with two concrete implementations (for two different data sources). 
These use RestTemplate to fetch real-time exchange rates and supported
currencies from free third-party APIs.  

Additional components:

- GlobalExceptionHandler handles all exceptions in a centralized and consistent manner.
- MetricsTracker is a lightweight custom tracking utility. A simple in-memory tracker was chosen because the project scope is small and doesn't require a full metrics stack.
- Custom error handling and response formatting have been added for better API usability.

## Optimisation
On startup, the application preloads the list of supported currencies from each external provider.
This significantly reduces the response time when handling user
requests since the supported currency list doesn’t change often.
These lists are cached in memory and do not need to be reloaded unless explicitly invalidated.

## Time spend
It is unrealistic to write high-quality, well-tested code with complete 
coverage in just 2–3 hours while meeting all the functional requirements.

Currently:
- The implementation meets the minimum feature requirements.
- The controller unit test is not fully functional yet.
- Unit tests for fetchExchangeRates() were skipped due to the complexity of handling diverse API response formats.
- There wasn’t enough time to implement full test coverage or polish edge cases.



## Future improvement
- Refactor and tidy up the codebase.

- Add scheduled jobs to retry loading supported currencies if the initial startup attempt fails.

- Add scheduled tasks to preload exchange rates for the top 10 most used currencies and cache them.

- Extend the caching system to retain request histories for up to 5 minutes to avoid redundant external calls.