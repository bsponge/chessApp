package com.example.demo.repository;

import com.example.demo.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByLogin(String login);
    void removeAccountByLogin(String login);
}
