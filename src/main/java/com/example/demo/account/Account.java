package com.example.demo.account;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Data
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue
    private Long id;
    private String login;
    private String password;
    private String email;

    public Account(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public Account() {

    }
}
