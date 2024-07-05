package de.florian.banking;

import io.javalin.http.staticfiles.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.javalin.Javalin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;


public class Main {

    public static ArrayList<Account> accounts = new ArrayList<>();
    public static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {

        Javalin app = Javalin.create(config -> {
                    config.staticFiles.add("/public", Location.CLASSPATH);
                })
                .start(7070);

        app.get("/", ctx -> ctx.redirect("/index.html"));

        app.post("/login", ctx -> {
            // This gets an input field whenever a user posts a request
            LOGGER.debug("output: {}", ctx.formParams("day"));
        });

        // This sends something back on a get-request
        app.get("/input", ctx -> {
            // some code
            ctx.status(201);
        });

        addAccount("Brian", 99);
        addAccount("Florian", 20);

    }

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



}