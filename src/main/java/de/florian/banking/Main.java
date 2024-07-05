package de.florian.banking;

import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.javalin.Javalin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class Main {

    public static ArrayList<Account> accounts = new ArrayList<>();
    public static final Logger LOGGER = LogManager.getLogger();

    public static void main() {

        Javalin app = Javalin.create(config -> {
                    config.staticFiles.add("/public", Location.CLASSPATH);
                })
                .start(7070);

        app.get("/", ctx -> ctx.redirect("/login.html"));
        app.get("/register", ctx -> ctx.redirect("/register.html"));
        app.get("/login", ctx -> ctx.redirect("/login.html"));

        app.post("/login", handleLogin);
        app.post("/register", handleRegister);
    }


    public static Handler handleLogin = ctx -> {
        String accountId = ctx.formParam("account_id");
        String password = ctx.formParam("password");

        if (accountId != null && password != null) {
            Account account = getAccountById(Integer.parseInt(accountId));

            if (account != null) {

                if(encoder().matches(password, account.password)){

                    ctx.cookieStore().set("id", account.accountId);
                    ctx.cookieStore().set("role", account.role);
                    LOGGER.debug(Optional.ofNullable(ctx.cookieStore().get("id")));
                    LOGGER.debug(Optional.ofNullable(ctx.cookieStore().get("role")));

                    ctx.redirect("/index.html");
                }
            }
        }
    };

    public static Handler handleRegister = ctx -> {

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
        final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

        String name = ctx.formParam("name");
        String password = ctx.formParam("password");
        int age = Integer.parseInt(Objects.requireNonNull(ctx.formParam("age")));

        if(name == null ||  password == null){
            // TODO: Show that data isn't valid somehow.
            LOGGER.debug("Name or Password is empty.");
            return;
        }
        if(age < 18){
            // TODO: Ask to enter valid age
            LOGGER.debug("Invalid Age entered.");
            return;
        }
        if(password.isEmpty() || !pattern.matcher(password).matches()){
            // TODO: Password not valid, return.
            LOGGER.debug("Weak or empty Password.");
        }
        else{
            String encodedPassword = encoder().encode(password);
            addAccount(name, encodedPassword, age);
            ctx.redirect("/login.html");
        }

    };


    public static void addAccount(String name, String password, int age) {
        String role = name.equals("Brian") ? "ROLE_ADMIN" : "ROLE_USER";
        Account newAccount = new Account(name, role, password,  age, 0, accounts.size() + 1);
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

    public static PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

}