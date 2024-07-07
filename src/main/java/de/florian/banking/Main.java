package de.florian.banking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Handler;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.javalin.Javalin;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Pattern;

public class Main {

    public static ArrayList<Account> accounts = new ArrayList<>();
    public static ArrayList<String[] > transactions = new ArrayList<>();
    public static final Logger LOGGER = LogManager.getLogger();

    public static void main() {

        Javalin app = Javalin.create(config -> {
                    config.staticFiles.add("/public", Location.CLASSPATH);
                    config.bundledPlugins.enableCors(cors -> {
                        cors.addRule(CorsPluginConfig.CorsRule::anyHost);
                    });
        }).start(7070);



        app.get("/", ctx -> ctx.redirect("/login.html"));
        app.get("/register", ctx -> ctx.redirect("/register.html"));
        app.get("/login", ctx -> ctx.redirect("/login.html"));
        app.get("/index", ctx -> {ctx.redirect("/index.html");});
        app.get("/transfer", ctx -> {ctx.redirect("/transfer.html");});
        app.get("/deposit", ctx -> {ctx.redirect("/deposit.html");});

        // Handles Get Requests
        app.get("/get_balance", handleGetBalance);
        app.get("/check_login", checkIfLoggedIn);
        app.get("/logout", handleLogout);

        // Handles Post Requests
        app.post("/login", handleLogin);
        app.post("/register", handleRegister);
        app.post("transfer", handleTransferMoney);
        app.post("/deposit", handleDepositMoney);
    }

    public static Handler handleLogout = ctx -> {
        ctx.cookieStore().clear();
        ctx.redirect("/login.html");
    };

    public static Handler checkIfLoggedIn = ctx -> {
        if (!hasCookies( ctx.cookieStore().get("id"), ctx.cookieStore().get("role"))) {
            ctx.status(500);
        }
    };

    public static Handler handleTransferMoney = ctx -> {

        Integer accountId = ctx.cookieStore().get("id");
        String role = ctx.cookieStore().get("role");
        if (!hasCookies(accountId, role)) {
            ctx.result("NOT_LOGGED_IN");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(ctx.body());

        String recipientId = node.get("recipientID").asText();
        String transferAmount = node.get("transferAmount").asText();

        if (recipientId.isEmpty() || transferAmount.isEmpty()) {
            ctx.result("INSUFFICIENT_DATA");
            return;
        }

        if (!recipientId.matches("\\d+")) {
            ctx.result("INVALID_RECIPIENT_ID");
            return;
        } else if (getAccountById(Integer.parseInt(recipientId)) == null) {
            ctx.result("INVALID_RECIPIENT_ID");
            return;
        }

        if (!transferAmount.matches(("[0-9]+")) ) {
            ctx.result("INVALID_AMOUNT");
            return;
        }
        else if(Long.parseLong(transferAmount) <= 0){
            ctx.result("INVALID_AMOUNT");
            return;
        }
        if (getAccountById(accountId) != null && getAccountById(accountId).balance < Long.parseLong(transferAmount)) {
            ctx.result("INSUFFICIENT_FUNDS");
        }
        else{
            getAccountById(Integer.parseInt(recipientId)).addBalance(Long.parseLong(transferAmount));
            getAccountById(accountId).removeBalance(Long.parseLong(transferAmount));

            // Add Transaction to transactions

            String[] transaction = {accountId.toString(), recipientId, "-" + transferAmount};
            transactions.add(transaction);
            ctx.result("SUCCESSFUL_TRANSACTION");
        }
    };

    public static Handler handleDepositMoney = ctx -> {

        Integer accountId = ctx.cookieStore().get("id");
        String role = ctx.cookieStore().get("role");
        if (!hasCookies(accountId, role)) {
            ctx.result("NOT_LOGGED_IN");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(ctx.body());

        String depositAmount = node.get("depositAmount").asText();


        if (!depositAmount.matches(("[0-9]+")) ) {
            ctx.result("INVALID_AMOUNT");
            return;
        }
        else if(Long.parseLong(depositAmount) <= 0){
            ctx.result("INVALID_AMOUNT");
            return;
        }
        else{
            getAccountById(accountId).addBalance(Long.parseLong(depositAmount));
            ctx.result("SUCCESSFUL_DEPOSIT");
        }
    };














    public static Handler handleGetBalance = ctx -> {
        Integer accountId = ctx.cookieStore().get("id");
        String role = ctx.cookieStore().get("role");

        if(!hasCookies(accountId, role)){
            LOGGER.debug("Account not found.");
            ctx.result("NOT_LOGGED_IN");
            return;
        }

        Account userAccount = getAccountById((accountId));

        assert userAccount != null;
        Long balance = userAccount.balance;

        DecimalFormat formatter = new DecimalFormat("$#,##0.00");
        BigDecimal amt = new BigDecimal(balance);

        ctx.result(formatter.format(amt) + ":" + role);
    };

    public static boolean hasCookies(int accountId, String role){
        return accountId != 0 && !role.isEmpty() && getAccountById((accountId)) != null;
    }

    public static Handler handleLogin = ctx -> {

        // Get Parameters, check them, show errors

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(ctx.body());

        String accountId = node.get("accountId").asText();
        String password = node.get("password").asText();

        if(accountId.isEmpty() || Objects.requireNonNull(password).isEmpty()){
            ctx.result("INSUFFICIENT_DATA");
            return;
        }
        Account account = getAccountById(Integer.parseInt(accountId));
        if (account == null) {
            ctx.result("ACCOUNT_NOT_FOUND");
        }
        else{
            if(!encoder().matches(password, account.password)){
                ctx.result("INVALID_CREDENTIALS");
            }
            else{
                ctx.cookieStore().set("id", account.accountId);
                ctx.cookieStore().set("role", account.role);
                ctx.result("LOGIN_SUCCESSFUL");
            }
        }
    };

    public static Handler handleRegister = ctx -> {

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
        final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(ctx.body());

        String name = node.get("name").asText();
        String password = node.get("password").asText();
        String age = node.get("age").asText();

        assert name != null;
        if(name.isEmpty() || Objects.requireNonNull(password).isEmpty() || Objects.requireNonNull(age).isEmpty()){
            ctx.result("INSUFFICIENT_DATA");
            return;
        }
        if(!age.matches(("[0-9]+")) || Integer.parseInt(age) < 18){
            ctx.result("INVALID_AGE");
            return;
        }
        if(!pattern.matcher(password).matches()){
            ctx.result("WEAK_PASSWORD");
        }
        else{
            String encodedPassword = encoder().encode(password);
            Integer id = addAccount(name, encodedPassword, Integer.parseInt(age));

            ctx.result("REGISTRATION_SUCCESSFUL:" + id);
        }
    };


    public static int addAccount(String name, String password, int age) {

        String role = name.equals("Brian") ? "ROLE_ADMIN" : "ROLE_USER";
        Account newAccount = new Account(name, role, password,  age, 0, accounts.size() + 1);
        accounts.add(newAccount);

        LOGGER.debug("New account added: {} with ID: {}", newAccount.name, newAccount.accountId);
        return newAccount.accountId;
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