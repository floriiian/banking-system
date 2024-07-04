package de.florian.banking;

public class Account {

    public String name;
    public int age;
    public long balance;
    public int accountId;

    // Constructor
    public Account(String name, int age, long balance, int accountId) {
        this.name = name;
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

    public void setBalance(long balance) {
        this.balance = balance;
    }
}
