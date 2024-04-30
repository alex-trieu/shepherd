package com.shepherdmoney.interviewproject.controller;

import com.shepherdmoney.interviewproject.model.CreditCard;
import com.shepherdmoney.interviewproject.model.User;
import com.shepherdmoney.interviewproject.repository.UserRepository;
import com.shepherdmoney.interviewproject.vo.request.CreateUserPayload;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
public class UserController {

    // TODO: wire in the user repository (~ 1 line)
    private static UserRepository userRepository;
    /**
     * Creates a new user entity with information provided in the payload,
     * stores it in the database, and returns the ID of the user in a 200 OK response.
     *
     * @param payload The payload containing user information (name and email)
     * @return A ResponseEntity containing the ID of the newly created user
     */
    @PutMapping("/user")
    public ResponseEntity<Integer> createUser(@RequestBody CreateUserPayload payload) {
        // Retrieves user information from the payload
        String userName = payload.getName();
        String userEmail = payload.getEmail();

        // Generates a unique user ID using payload's hashCode
        int userID = payload.hashCode();

        // Constructs new User utilizing the user's ID, name, and email.
        User newUser = new User(userID, userName, userEmail);

        // Stores newUser into our userRepository
        userRepository.save(newUser);

        // Returns the ID of the new User in a ResponseEntity in a 200 OK response
        return ResponseEntity.ok(userID);
    }

    /**
     * Deletes a user with the given ID.
     *
     * @param userId The ID of the user to be deleted
     * @return A ResponseEntity indicating the success or failure of the deletion operation
     */
    @DeleteMapping("/user")
    public ResponseEntity<String> deleteUser(@RequestParam int userId) {
        // Checks if userId exists in our userRepository and deletes if so
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);

            // Returns a 200 OK response indicating a successful user deletion
            return ResponseEntity.ok("Successful deletion of User with ID: " + userId);
        }

        // Return a 400 Bad Request response with a body indicating that
        // the requested user does not exist within our userRepository
        return ResponseEntity.badRequest().body("User with ID: " + userId + " does not exist");
    }
}
