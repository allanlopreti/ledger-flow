package com.lopreti.ledger.flow.ledger.infrastructure.web;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.account.infrastructure.web.dto.AccountBalanceResponse;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateDepositUseCase;
import com.lopreti.ledger.flow.ledger.application.port.in.CreateTransactionUseCase;
import com.lopreti.ledger.flow.ledger.domain.Transaction;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.CreateDepositRequest;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.CreateTransactionRequest;
import com.lopreti.ledger.flow.ledger.infrastructure.web.dto.TransactionResponse;
import com.lopreti.ledger.flow.shared.domain.Currency;
import com.lopreti.ledger.flow.shared.domain.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final CreateDepositUseCase createDepositUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            CreateDepositUseCase createDepositUseCase
    ) {
        this.createTransactionUseCase =
                createTransactionUseCase;

        this.createDepositUseCase =
                createDepositUseCase;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request
    ) {

        var debitMoney = Money.of(
                request.debitAmount(),
                Currency.of(request.debitCurrency())
        );

        var debitFunctionalMoney = Money.of(
                request.debitFunctionalAmount(),
                Currency.of(request.debitFunctionalCurrency())
        );

        var creditMoney = Money.of(
                request.creditAmount(),
                Currency.of(request.creditCurrency())
        );

        var creditFunctionalMoney = Money.of(
                request.creditFunctionalAmount(),
                Currency.of(request.creditFunctionalCurrency())
        );

        Transaction transaction =
                createTransactionUseCase.execute(
                        AccountId.of(request.debitAccountId()),
                        debitMoney,
                        debitFunctionalMoney,
                        AccountId.of(request.creditAccountId()),
                        creditMoney,
                        creditFunctionalMoney
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transaction));
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountBalanceResponse> deposit(
            @Valid @RequestBody CreateDepositRequest request
    ) {

        var balance =
                createDepositUseCase.execute(
                        AccountId.of(request.accountId()),
                        request.amount(),
                        Currency.of(request.currency())
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        AccountBalanceResponse.from(balance)
                );
    }
}