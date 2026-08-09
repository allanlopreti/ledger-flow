package com.lopreti.ledger.flow.account.infrastructure.web;

import com.lopreti.ledger.flow.account.application.port.in.CreateAccountUseCase;
import com.lopreti.ledger.flow.account.application.port.in.GetAccountBalanceUseCase;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.AccountBalanceResponse;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.AccountResponse;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.CreateAccountRequest;
import com.lopreti.ledger.flow.shared.domain.Currency;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;

    public AccountController(
            CreateAccountUseCase createAccountUseCase,
            GetAccountBalanceUseCase getAccountBalanceUseCase
    ) {
        this.createAccountUseCase =
                createAccountUseCase;

        this.getAccountBalanceUseCase =
                getAccountBalanceUseCase;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request
    ) {

        var account =
                createAccountUseCase.execute(
                        CustomerId.of(request.customerId()),
                        Currency.of(request.currency())
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AccountResponse.from(account));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(
            @PathVariable UUID accountId
    ) {

        var balance =
                getAccountBalanceUseCase.execute(
                        com.lopreti.ledger.flow.account.domain.AccountId
                                .of(accountId)
                );

        return ResponseEntity.ok(
                AccountBalanceResponse.from(balance)
        );
    }
}