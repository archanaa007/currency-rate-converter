package pl.cleankod;

import feign.Feign;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;
import pl.cleankod.security.JwtAuthenticationFilter;
import pl.cleankod.security.JwtUtil;
import pl.cleankod.security.SecurityConfig;
import pl.cleankod.web.rest.AccountController;
import pl.cleankod.model.Account;
import pl.cleankod.repository.AccountRepository;
import pl.cleankod.helper.CurrencyConversionService;
import pl.cleankod.service.usecase.FindAccountAndConvertCurrencyUseCase;
import pl.cleankod.service.usecase.FindAccountUseCase;
import pl.cleankod.exception.ExceptionHandlerAdvice;
import pl.cleankod.repository.AccountInMemoryRepository;
import pl.cleankod.helper.CurrencyConversionNbpService;
import pl.cleankod.cache.NbpRateCacheService;
import pl.cleankod.remote.ExchangeRatesNbpClient;
import pl.cleankod.service.AccountService;
import pl.cleankod.service.impl.AccountServiceImpl;
import pl.cleankod.service.strategy.AccountRetrievalStrategy;
import pl.cleankod.web.rest.AuthenticationController;

import java.util.Currency;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableCaching
@EnableRetry
public class ApplicationInitializer {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationInitializer.class, args);
    }

    @Bean
    AccountRepository accountRepository() {
        return new AccountInMemoryRepository();
    }

    @Bean
    ExchangeRatesNbpClient exchangeRatesNbpClient(Environment environment) {
        String nbpApiBaseUrl = environment.getRequiredProperty("provider.nbp-api.base-url");
        return Feign.builder()
                .client(new ApacheHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .target(ExchangeRatesNbpClient.class, nbpApiBaseUrl);
    }

    @Bean
    CurrencyConversionService currencyConversionService(NbpRateCacheService nbpRateCacheService) {
        return new CurrencyConversionNbpService(nbpRateCacheService);
    }

    @Bean
    NbpRateCacheService nbpRateCacheService(ExchangeRatesNbpClient exchangeRatesNbpClient) {
        return new NbpRateCacheService(exchangeRatesNbpClient);
    }

    @Bean
    FindAccountUseCase findAccountUseCase(AccountRepository accountRepository) {
        return new FindAccountUseCase(accountRepository);
    }

    @Bean
    FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase(
            AccountRepository accountRepository,
            CurrencyConversionService currencyConversionService,
            Environment environment
    ) {
        Currency baseCurrency = Currency.getInstance(environment.getRequiredProperty("app.base-currency"));
        return new FindAccountAndConvertCurrencyUseCase(accountRepository, currencyConversionService, baseCurrency);
    }

    @Bean
    ExceptionHandlerAdvice exceptionHandlerAdvice() {
        return new ExceptionHandlerAdvice();
    }

    @Bean
    AccountService accountQueryService(
            AccountRetrievalStrategy<Account.Id> findByIdStrategy,
            AccountRetrievalStrategy<Account.Number> findByNumberStrategy) {
        return new AccountServiceImpl(findByIdStrategy, findByNumberStrategy);
    }

    @Bean
    AccountController accountController(AccountService accountQueryService) {
        return new AccountController(accountQueryService);
    }

    @Bean
    AccountRetrievalStrategy<Account.Id> findByIdStrategy(
            FindAccountUseCase findAccountUseCase,
            FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase) {
        return new pl.cleankod.service.strategy.FindByIdStrategy(findAccountUseCase, findAccountAndConvertCurrencyUseCase);
    }

    @Bean
    AccountRetrievalStrategy<Account.Number> findByNumberStrategy(
            FindAccountUseCase findAccountUseCase,
            FindAccountAndConvertCurrencyUseCase findAccountAndConvertCurrencyUseCase) {
        return new pl.cleankod.service.strategy.FindByNumberStrategy(findAccountUseCase, findAccountAndConvertCurrencyUseCase);
    }

    @Bean
    AuthenticationController authenticationController(JwtUtil jwtUtil) {
        return new AuthenticationController(jwtUtil);
    }

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }

    @Bean
    public SecurityConfig securityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        return new SecurityConfig(jwtAuthenticationFilter);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter(jwtUtil);
    }

}
