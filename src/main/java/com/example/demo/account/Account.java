package com.example.demo.account;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
public class Account {
    @Id
    private Long id;
    private String login;
    private String password;

    public Account() {

    }
}
