package com.lopreti.ledger.flow.account.infrastructure.web;

import com.lopreti.ledger.flow.account.application.port.in.CreateAccountUseCase;
import com.lopreti.ledger.flow.account.domain.CustomerId;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.AccountResponse;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.CreateAccountRequest;
import com.lopreti.ledger.flow.shared.domain.Currency;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;

    public AccountController(
            CreateAccountUseCase createAccountUseCase
    ) {
        this.createAccountUseCase = createAccountUseCase;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request
    ) {

        var account = createAccountUseCase.execute(
                CustomerId.of(request.customerId()),
                Currency.of(request.currency())
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AccountResponse.from(account));
    }
}