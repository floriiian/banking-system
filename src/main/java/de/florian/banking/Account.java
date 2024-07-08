package de.florian.banking;

public class Account {

    public String name;
    public String role;
    public String password;
    public int age;
    public long balance;
    public int accountId;

    public Account(String name, String role, String password,  int age, long balance, int accountId) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.age = age;
        this.balance = balance;
        this.accountId = accountId;
    }

    public void addBalance(long balance) {
        this.balance += balance;
    }

    public void removeBalance(long balance) {
        this.balance -= balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
