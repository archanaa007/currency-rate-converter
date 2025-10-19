package pl.cleankod.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.cleankod.filter.TraceIdFilter;
import pl.cleankod.util.Result;
import pl.cleankod.model.Account;
import pl.cleankod.service.AccountService;


@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;
    
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(
            summary = "Find account by ID",
            description = "Returns account details for the given account ID. Optionally converts to the requested currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found and returned successfully"),
            @ApiResponse(responseCode = "400", description = "Account not found or invalid request")
    })
    @GetMapping(path = "/{id}")
    public ResponseEntity<?> findAccountById(
            @Parameter(description = "Account ID", required = true) @PathVariable String id,
            @Parameter(description = "Target currency code (optional)") @RequestParam(required = false) String currency) {
        logger.info("Find Account By Id Started for traceId={}, Account ID={}, Target currency code={}", TraceIdFilter.getCurrentTraceId(), id, currency);
        Result<Account, String> result = accountService.findAccountById(id, currency);
        logger.info("Find Account By Id Completed for traceId={}, Result={}", TraceIdFilter.getCurrentTraceId(), result);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getOrNull());
        } else {
            return ResponseEntity.badRequest().body(result.getErrorOrNull());
        }
    }

    @Operation(
            summary = "Find account by number",
            description = "Returns account details for the given account number. Optionally converts to the requested currency."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found and returned successfully"),
            @ApiResponse(responseCode = "400", description = "Account not found or invalid request")
    })
    @GetMapping(path = "/number={number}")
    public ResponseEntity<?> findAccountByNumber(
            @Parameter(description = "Account number", required = true) @PathVariable String number,
            @Parameter(description = "Target currency code (optional)") @RequestParam(required = false) String currency) {
        logger.info("Find Account By Number Started for traceId={}, Account ID={}, Target currency code={}", TraceIdFilter.getCurrentTraceId(), number, currency);
        Result<Account, String> result = accountService.findAccountByNumber(number, currency);
        logger.info("Find Account By Number Completed for traceId={}, Result={}", TraceIdFilter.getCurrentTraceId(), result);
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getOrNull());
        } else {
            return ResponseEntity.badRequest().body(result.getErrorOrNull());
        }
    }    

}
