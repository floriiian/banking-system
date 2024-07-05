package de.florian.banking;

import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.javalin.Javalin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;


public class Main {

    public static ArrayList<Account> accounts = new ArrayList<>();
    public static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
                    config.staticFiles.add("/public", Location.CLASSPATH);
                })
                .start(7070);

        app.get("/", ctx -> ctx.redirect("/login.html"));

        app.post("/login", handleLogin);
    }

    private static final Handler handleLogin = ctx -> {
        String accountId = ctx.formParam("account_id");
        String password = ctx.formParam("password");

        LOGGER.debug(accountId);
        LOGGER.debug(password);

        if (accountId != null && password != null) {
            Account account = getAccountById(Integer.parseInt(accountId));

            if (account != null) {
                if (data[1].equals(accountId)) {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    encoder.encode(data[2]).;
                }
            }

        }
    };



    public static void addAccount(String name, int age) {
        Account newAccount = new Account(name, age, 0, accounts.size() + 1);
        accounts.add(newAccount);

        LOGGER.debug("New account added: {} with ID: {}", newAccount.name, newAccount.accountId);
    }

    // Gets an account by ID
    public static Account getAccountById(int accountId){
        for(Account account : accounts){
            if(account.accountId == accountId){
                return account;
            }
        }
        return null;
    }

    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}