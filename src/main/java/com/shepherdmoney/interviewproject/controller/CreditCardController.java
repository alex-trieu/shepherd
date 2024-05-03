package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.BalanceHistory;
import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.CreditCardRepository;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.AddCreditCardToUserPayload;
import com.shepherdmoney.interviewproject.vo.request.UpdateBalancePayload;
import com.shepherdmoney.interviewproject.vo.response.CreditCardView;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.xml.ws.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

import java.sql.Array;
import java.util.*;

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
        // Retrieve the user entity based on the provided userId
        Optional<User> optionalUser = userRepository.findById(userId);

        // Check if the user exists
        if (optionalUser.isPresent()) {
            // If the user exists, retrieve the user entity
            User user = optionalUser.get();

            // Initialize a list to store CreditCardView objects
            List<CreditCardView> creditCardViews = new ArrayList<>();

            // Iterate through each credit card owned by the user
            for (CreditCard credit : user.getOwnedCreditCards()) {
                // Create a CreditCardView object for each credit card and add it to the list
                creditCardViews.add(new CreditCardView(credit.getIssuanceBank(), credit.getNumber()));
            }

            // Return a response entity with the list of CreditCardView objects
            return ResponseEntity.ok().body(creditCardViews);
        } else {
            // If the user does not exist or has no credit cards, return an empty list
            return ResponseEntity.ok().body(new ArrayList<>());
        }
    }


    @GetMapping("/credit-card:user-id")
    public ResponseEntity<Integer> getUserIdForCreditCard(@RequestParam String creditCardNumber) {
        // Retrieve the credit card entity based on the provided credit card number
        CreditCard currCredit = creditCardRepository.findByNumber(creditCardNumber);

        // Check if the credit card exists and is associated with any user
        if (!currCredit.getOwners().isEmpty()) {
            // If the credit card is associated with a user, retrieve the user id of the first owner
            User firstOwner = currCredit.getOwners().get(0);

            // Return the user id in a 200 OK response
            return ResponseEntity.ok(firstOwner.getId());
        }

        // If no such user exists for the credit card, return 400 Bad Request
        return ResponseEntity.badRequest().build();
    }


    @PostMapping("/credit-card:update-balance")
    public ResponseEntity<String> postMethodName(@RequestBody UpdateBalancePayload[] payload) {
        for (UpdateBalancePayload transaction : payload) {
            CreditCard creditCard = creditCardRepository.findByNumber(transaction.getCreditCardNumber());
            if (creditCard == null) {
                return ResponseEntity.badRequest().body("Credit card not found for credit card number: " + transaction.getCreditCardNumber());
            }
            TreeMap<LocalDate, Double> balanceHistory = creditCard.getBalanceHistory();
            balanceHistory.put(transaction.getBalanceDate(), transaction.getBalanceAmount());
            // Fill the gaps between two balance dates
            fillBalanceHistoryGaps(balanceHistory);

            // Update all the following balance entries if needed
            updateFollowingBalances(balanceHistory, transaction.getBalanceDate(), transaction.getBalanceAmount());

            // Save the updated credit card entity
            creditCardRepository.save(creditCard);
        }
        return ResponseEntity.ok("Credit card balance updated");
    }
    // Helper function to fill gaps between two balance dates
    private void fillBalanceHistoryGaps(TreeMap<LocalDate, Double> balanceHistory) {
        // Get the set of dates in the balance history
        Set<LocalDate> dates = balanceHistory.keySet();
        // Iterate through the dates and fill gaps
        LocalDate previousDate = null;
        for (LocalDate currentDate : dates) {
            if (previousDate != null) {
                // Fill the gap between previous date and current date
                LocalDate date = previousDate.minusDays(1);
                while (!date.isEqual(currentDate)) {
                    balanceHistory.put(date, balanceHistory.get(previousDate)); // Fill with previous balance
                    date = date.minusDays(1);
                }
            }
            previousDate = currentDate;
        }
    }
    // Helper function to update following balance entries if needed
    private void updateFollowingBalances(TreeMap<LocalDate, Double> balanceHistory, LocalDate balanceDate, double balanceAmount) {
        // Get the entry set of the balance history
        Set<Map.Entry<LocalDate, Double>> entrySet = balanceHistory.entrySet();
        // Iterate through the entries and update following balances
        for (Map.Entry<LocalDate, Double> entry : entrySet) {
            LocalDate date = entry.getKey();
            if (date.isAfter(balanceDate)) {
                // Calculate the difference between the new balance and the existing balance
                double difference = balanceAmount - entry.getValue();
                // Update the balance with the difference
                entry.setValue(entry.getValue() + difference);
            }
        }
    }
    }


