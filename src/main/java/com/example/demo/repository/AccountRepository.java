package com.example.demo.repository;

import com.example.demo.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.annotation.PostConstruct;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Account findByLogin(String login);
    void removeAccountByLogin(String login);
    boolean existsAccountByLogin(String login);
    boolean existsAccountByEmail(String email);
}
