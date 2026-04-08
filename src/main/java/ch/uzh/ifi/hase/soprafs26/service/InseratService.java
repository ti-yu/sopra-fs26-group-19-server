package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InseratRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;

@Service
@Transactional
public class InseratService {

    private final Logger log = LoggerFactory.getLogger(InseratService.class);

    private final InseratRepository inseratRepository;
    private final UserRepository userRepository;

    public InseratService(
        @Qualifier("inseratRepository") InseratRepository inseratRepository,
        @Qualifier("userRepository") UserRepository userRepository
    ) {
        this.inseratRepository = inseratRepository;
        this.userRepository = userRepository;
    }

    private User checkUserExists(String recipientId) {
        return userRepository.findById(recipientId).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "The user with id " + recipientId + " was not found!"
            )
        );
    }

    public List<Inserat> getInseratsByRecipientId(String recipientId) {
        User recipient = checkUserExists(recipientId);
        return inseratRepository.findByRecipient(recipient);
    }

    public Inserat createInserat(Inserat newInserat, String recipientId) {
        User recipient = checkUserExists(recipientId);
        newInserat.setRecipient(recipient);
        checkValidInseratData(newInserat);

        newInserat = inseratRepository.save(newInserat);
        inseratRepository.flush();

        log.debug("Created inserat: {}", newInserat);
        return newInserat;
    }

    private void checkValidInseratData(Inserat inserat) {
        String baseErrorMessage = "%s must not be empty. Therefore, the inserat could not be created!";

        if (isBlank(inserat.getDescription())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format(baseErrorMessage, "Description")
            );
        }

        if (isBlank(inserat.getLocation())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format(baseErrorMessage, "Location")
            );
        }

        if (inserat.getDate() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format(baseErrorMessage, "Date")
            );
        }

        if (isBlank(inserat.getTimeframe())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format(baseErrorMessage, "Timeframe")
            );
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}