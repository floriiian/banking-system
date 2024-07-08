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
        app.get("/index", ctx -> {ctx.redirect("/index.html");});

        app.get("/register", ctx -> ctx.redirect("/register.html"));
        app.get("/login", ctx -> ctx.redirect("/login.html"));

        app.get("/transfer", ctx -> {ctx.redirect("/transfer.html");});
        app.get("/deposit", ctx -> {ctx.redirect("/deposit.html");});
        app.get("/transactions", ctx -> {ctx.redirect("/transactions.html");});

        app.get("/admin", ctx -> {ctx.redirect("/admin.html");});
        app.get("/addaccount", ctx -> {ctx.redirect("/addaccount.html");});
        app.get("/accountlist", ctx -> {ctx.redirect("/accountlist.html");});
        app.get("/setbalance", ctx -> {ctx.redirect("/setbalance.html");});

        // Handles Get Requests
        app.get("/get_balance", handleGetBalance);
        app.get("/get_transactions", handleGetTransactions);
        app.get("/get_accounts", handleGetAccounts);
        app.get("/check_login", checkIfLoggedIn);
        app.get("/logout", handleLogout);

        // Handles Post Requests
        app.post("/login", handleLogin);
        app.post("/register", handleRegister);
        app.post("transfer", handleTransferMoney);
        app.post("/deposit", handleDepositMoney);
        app.post("/addaccount", handleAddAccount);
        app.post("/setbalance", handleSetBalance);
    }

    public static Handler handleLogout = ctx -> {
        ctx.cookieStore().set("id", 0);
        ctx.cookieStore().set("role", "");
        ctx.redirect("/login.html");
    };

    public static Handler checkIfLoggedIn = ctx -> {
        if (isLoggedOut(ctx.cookieStore().get("id"), ctx.cookieStore().get("role"))) {
            ctx.status(500);
        }
    };

    public static Handler handleSetBalance = ctx -> {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(ctx.body());

        String id  = node.get("id").asText();
        String amount = node.get("amount").asText();

        if(id.isEmpty() || amount.isEmpty()){
            ctx.result("INSUFFICIENT_DATA");
            return;
        }
        if(!amount.matches(("[0-9]+"))) {
            ctx.result("INVALID_AMOUNT");
            return;
        }

        if(!id.matches(("[0-9]+"))){
            ctx.result("INVALID_ID");
        }
        else if(getAccountById(Integer.parseInt(id)) == null){
            ctx.result("INVALID_ID");
        }
        else{
            Account account = getAccountById(Integer.parseInt(id));
            assert account != null;
            account.setBalance(Integer.parseInt(amount));
            ctx.result("SUCCESSFUL_SET");
        }
    };

    public static Handler handleAddAccount = ctx -> {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(ctx.body());

        String name = node.get("name").asText();
        String age = node.get("age").asText();
        String password = node.get("password").asText();
        String role = node.get("role").asText();

        if(name.isEmpty() || password.isEmpty() || age.isEmpty()){
            ctx.result("INSUFFICIENT_DATA");
            return;
        }

        if(role.isEmpty() || role.equalsIgnoreCase("User")){
            role = "ROLE_USER";
        }
        else if(role.equalsIgnoreCase("Admin")){
            role = "ROLE_ADMIN";
        }
        else{
            ctx.result("INVALID_ROLE");
            return;
        }

        if(!age.matches(("[0-9]+"))) {
            ctx.result("INVALID_AGE");
        }
        else if(Integer.parseInt(age) < 18){
            ctx.result("UNDER_18");
        }
        else{
            String encodedPassword = encoder().encode(password);
            Account newAccount = new Account(name, role, encodedPassword,  Integer.parseInt(age), 0, accounts.size() + 1);
            accounts.add(newAccount);

            ctx.result("SUCCESSFUL_CREATION:" + newAccount.accountId);
        }
    };

    public static Handler handleGetTransactions = ctx -> {

        Integer accountId = ctx.cookieStore().get("id");
        String transactionLines = "";

        for (String[] transaction : transactions) {
            if (transaction[0].equals(accountId.toString())){
                String receiver = transaction[1];
                String amount = transaction[2];
                transactionLines = transactionLines.concat("<h4>Receiver-ID: " + receiver + " : Amount: $" + amount + "</h4>");
            }
            else if(transaction[1].equals(accountId.toString())){
                String sender = transaction[0];
                String amount = transaction [2];
                transactionLines = transactionLines.concat("<h4>Sender-ID: " + sender + " : Amount: $" + amount + "</h4>");
            }
        }
        ctx.result(transactionLines);
    };

    public static Handler handleGetAccounts = ctx -> {

        String accountLines = "";
        for (Account account : accounts) {
            String id = String.valueOf(account.accountId);
            String role = account.role;
            String name = account.name;
            String balance = String.valueOf(account.balance);
            accountLines = accountLines.concat("<h4>ID: " + id + " : Name: " + name +  " : Role: " + role + " : Balance: $" + balance + "</h4>");
        }
        ctx.result(accountLines);
    };

    public static Handler handleTransferMoney = ctx -> {

        Integer accountId = ctx.cookieStore().get("id");
        String role = ctx.cookieStore().get("role");

        if (isLoggedOut(accountId, role)) {
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
            return;
        }
        if(accountId == Integer.parseInt(recipientId)){
            ctx.result("SAME_ID");
        }
        else{
            getAccountById(Integer.parseInt(recipientId)).addBalance(Long.parseLong(transferAmount));
            getAccountById(accountId).removeBalance(Long.parseLong(transferAmount));

            // Add Transaction to transactions

            String[] transaction = {accountId.toString(), recipientId, transferAmount};
            transactions.add(transaction);
            ctx.result("SUCCESSFUL_TRANSACTION");
        }
    };

    public static Handler handleDepositMoney = ctx -> {

        Integer accountId = ctx.cookieStore().get("id");
        String role = ctx.cookieStore().get("role");
        if (isLoggedOut(accountId, role)) {
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

        if(isLoggedOut(accountId, role)){
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

    public static boolean isLoggedOut(int accountId, String role){
        return accountId == 0 && role.isEmpty() && getAccountById((accountId)) == null;
    }

    public static Handler handleLogin = ctx -> {

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