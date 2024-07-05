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
import java.util.regex.Pattern;

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
        app.post("/register", handleRegister);
    }


    public static Handler handleLogin = ctx -> {
        String accountId = ctx.formParam("account_id");
        String password = ctx.formParam("password");

        if (accountId != null && password != null) {
            Account account = getAccountById(Integer.parseInt(accountId));

            if (account != null) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                if(encoder.matches(account.password, password)){
                    LOGGER.debug("Matches");
                    ctx.cookieStore().set("id", account.accountId);
                    ctx.cookieStore().set("role", account.role);

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
            // Show that data isn't valid somehow.
            return;
        }
        if(age < 18 || age > 200){
            // Ask to enter valid age
            return;
        }
        if(password.isEmpty() || !pattern.matcher(password).matches()){
            // Password not valid, return.
            return;
        }
        try{
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(password);
            addAccount(name, encodedPassword, age);
        }finally {
            ctx.redirect("/login.html");
        }
    };


    public static void addAccount(String name, String password, int age) {

        Account newAccount = new Account(name, "user", password,  age, 0, accounts.size() + 1);
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