package com.shepherdmoney.interviewproject.model;

import com.shepherdmoney.interviewproject.repository.UserRepository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "MyUser")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String name;

    private String email;

    private LocalDate dob;


    // TODO: User's credit card
    // HINT: A user can have one or more, or none at all. We want to be able to query credit cards by user
    //       and user by a credit card.
    @ManyToMany(mappedBy = "owners")
    private List<CreditCard> ownedCreditCards; // List of the users owned Credit Cards

    // User constructor with the user's id, name, email, and a List of their owned credit cards
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.ownedCreditCards = new ArrayList<>();
    }

    public void addCreditCard(CreditCard creditCard) {
        this.ownedCreditCards.add(creditCard);
    }
}
