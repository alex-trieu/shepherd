package com.shepherdmoney.interviewproject.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String issuanceBank;

    private String number;

    @ElementCollection
    private TreeMap<LocalDate, Double> balanceHistory;

    // TODO: Credit card's owner. For detailed hint, please see User class
    // Some field here <> owner;
    @ManyToMany
    @JoinTable(name = "user_credit_card",
            joinColumns = @JoinColumn(name = "credit_card_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    List<User> owners = new ArrayList<>();

    public CreditCard(String issuanceBank, String number) {
        this.issuanceBank = issuanceBank;
        this.number = number;
        this.balanceHistory = new TreeMap<>();
    }
    public void addUser(User user) {
        this.owners.add(user);
    }

    // Add a balance entry to the balance history
    public void addBalanceTransaction(LocalDate date, double balance) {
        balanceHistory.put(date, balance);
    }

    // Retrieve the balance for a specific date
    public Double getBalanceForDate(LocalDate date) {
        return balanceHistory.get(date);
    }
    // Method to retrieve the closest balance date for a given date
    public LocalDate getClosestBalanceDate(LocalDate date) {
        return balanceHistory.floorKey(date);
    }




    // TODO: Credit card's balance history. It is a requirement that the dates in the balanceHistory
    //       list must be in chronological order, with the most recent date appearing first in the list.
    //       Additionally, the last object in the "list" must have a date value that matches today's date,
    //       since it represents the current balance of the credit card. For example:
    //       [
    //         {date: '2023-04-10', balance: 800},
    //         {date: '2023-04-11', balance: 1000},
    //         {date: '2023-04-12', balance: 1200},
    //         {date: '2023-04-13', balance: 1100},
    //         {date: '2023-04-16', balance: 900},
    //       ]
    // ADDITIONAL NOTE: For the balance history, you can use any data structure that you think is appropriate.
    //        It can be a list, array, map, pq, anything. However, there are some suggestions:
    //        1. Retrieval of a balance of a single day should be fast
    //        2. Traversal of the entire balance history should be fast
    //        3. Insertion of a new balance should be fast
    //        4. Deletion of a balance should be fast
    //        5. It is possible that there are gaps in between dates (note the 04-13 and 04-16)
    //        6. In the condition that there are gaps, retrieval of "closest" balance date should also be fast. Aka, given 4-15, return 4-16 entry tuple
}
