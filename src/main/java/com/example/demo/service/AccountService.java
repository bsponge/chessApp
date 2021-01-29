package com.example.demo.service;

import com.example.demo.account.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUser(String login, String password, String email) {
        boolean loginExists = accountRepository.existsAccountByLogin(login);
        boolean emailExists = accountRepository.existsAccountByEmail(email);
        if (!loginExists || !emailExists) {
            Account account = new Account();
            account.setLogin(login);
            account.setEmail(email);
            account.setPassword(passwordEncoder.encode(password));
            accountRepository.save(account);
        }
    }
}
