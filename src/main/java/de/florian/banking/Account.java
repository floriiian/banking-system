package de.florian.banking;

public class Account {

    public String name;
    public String role;
    public String password;
    public int age;
    public long balance;
    public int accountId;

    // Constructor
    public Account(String name, String role, String password,  int age, long balance, int accountId) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.age = age;
        this.balance = balance;
        this.accountId = accountId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void addBalance(long balance) {
        this.balance += balance;
    }
    public void removeBalance(long balance) {
        this.balance -= balance;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
