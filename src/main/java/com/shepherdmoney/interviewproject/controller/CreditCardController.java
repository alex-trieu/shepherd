package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import jakarta.xml.ws.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class CreditCardController {

    // TODO: wire in CreditCard repository here (~1 line)
    @Autowired
    private CreditCardRepository creditCardRepository;
    @Autowired
    private UserRepository userRepository;

    private boolean userDuplicateCredit(User user, CreditCard newCredit) {
        for (CreditCard existingCredit : user.getOwnedCreditCards()) {
            if (existingCredit.getIssuanceBank().equals(newCredit.getIssuanceBank())
                    && existingCredit.getNumber().equals(newCredit.getNumber())) {
                return true; // Found duplicate credit card
            }
        }
        return false; // No duplicate credit card found
    }
    @PostMapping("/credit-card")
    public ResponseEntity<Integer> addCreditCardToUser(@RequestBody AddCreditCardToUserPayload payload) {
        // Retrieve or create the credit card entity
        CreditCard newCredit = creditCardRepository.findByIssuanceBankAndNumber(payload.getCardIssuanceBank(), payload.getCardNumber());
        boolean newCard = false;
        if (newCredit == null) {
            newCredit = new CreditCard(payload.getCardIssuanceBank(), payload.getCardNumber());
            newCard = true;
        }

        // Retrieve the user entity
        int userId = payload.getUserId();
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User associatedUser = optionalUser.get();

            // Check if the user already owns the credit card
            if (associatedUser.getOwnedCreditCards().contains(newCredit)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Conflict: Credit card already associated with the user
            }

            // Add the credit card to the user's collection
            associatedUser.addCreditCard(newCredit);
            // Save the user entity first
            newCredit.addUser(associatedUser);
            // Then save the credit card entity
            if (newCard) {
                creditCardRepository.save(newCredit);
            }
            userRepository.save(associatedUser);
            // Return the credit card id
            return ResponseEntity.ok(newCredit.getId());
        }

        // User not found
        return ResponseEntity.notFound().build();
    }



    @GetMapping("/credit-card:all")
    public ResponseEntity<List<CreditCardView>> getAllCardOfUser(@RequestParam int userId) {
        // TODO: return a list of all credit card associated with the given userId, using CreditCardView class
        //       if the user has no credit card, return empty list, never return null
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            List<CreditCardView> creditCardViews = new ArrayList<>();
            for (CreditCard credit : user.getOwnedCreditCards()) {
                creditCardViews.add(new CreditCardView(credit.getIssuanceBank(), credit.getNumber()));
            }
            return ResponseEntity.ok().body(creditCardViews);
        } else {
            return ResponseEntity.ok().body(new ArrayList<>());
        }
    }

    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // TODO: Given a credit card number, efficiently find whether there is a user associated with the credit card
        //       If so, return the user id in a 200 OK response. If no such user exists, return 400 Bad Request
        CreditCard currCredit = creditCardRepository.findByNumber(creditCardNumber);
        if (!currCredit.getOwners().isEmpty()) {
            User firstOwner = currCredit.getOwners().get(0);
            return ResponseEntity.ok(firstOwner.getId());
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        //TODO: Given a list of transactions, update credit cards' balance history.
        //      1. For the balance history in the credit card
        //      2. If there are gaps between two balance dates, fill the empty date with the balance of the previous date
        //      3. Given the payload `payload`, calculate the balance different between the payload and the actual balance stored in the database
        //      4. If the different is not 0, update all the following budget with the difference
        //      For example: if today is 4/12, a credit card's balanceHistory is [{date: 4/12, balance: 110}, {date: 4/10, balance: 100}],
        //      Given a balance amount of {date: 4/11, amount: 110}, the new balanceHistory is
        //      [{date: 4/12, balance: 120}, {date: 4/11, balance: 110}, {date: 4/10, balance: 100}]
        //      Return 200 OK if update is done and successful, 400 Bad Request if the given card number
        //        is not associated with a card.
        
        return null;
    }
    
}
