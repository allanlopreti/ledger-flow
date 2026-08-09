package com.lopreti.ledger.flow.ledger.application.port.out;

import com.lopreti.ledger.flow.account.domain.AccountId;
import com.lopreti.ledger.flow.ledger.domain.Posting;

import java.util.List;

public interface PostingRepository {

    Posting save(Posting posting);
    List<Posting> findByAccountId(AccountId accountId);

}