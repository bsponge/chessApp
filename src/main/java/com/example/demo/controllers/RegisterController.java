package com.example.demo.controllers;

import com.example.demo.account.Account;
import com.example.demo.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Slf4j
public class RegisterController {
    private AccountRepository accountRepository;

    @Autowired
    public RegisterController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @GetMapping("/register")
    public String getRegister() {
        return "register";
    }

    @PostMapping("/register")
    public void postRegister(String username, String email, String password) {
        log.info(username);
        log.info(email);
        log.info(password);
        accountRepository.save(new Account(username, password, email));
    }
}
