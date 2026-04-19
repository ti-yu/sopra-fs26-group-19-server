package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.InseratStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.InseratRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Inserat> inserats = inseratRepository.findByRecipient(recipient);
        autoFinishPastInserats(inserats);
        return inserats;
    }

    public List<Inserat> getApplicationsByVolunteerId(String volunteerId) {
        User volunteer = checkUserExists(volunteerId);
        List<Inserat> inserats = inseratRepository.findByVolunteerAppliedContaining(volunteer);
        autoFinishPastInserats(inserats);
        return inserats;
    }

    public Inserat applyToInserat(String inseratId, String volunteerId) {
        Inserat inserat = inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));
        User volunteer = checkUserExists(volunteerId);

        if (!volunteer.getIsVolunteer()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only volunteers can apply to help requests");
        }
        if (inserat.getRecipient().getId().equals(volunteerId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot apply to your own help request");
        }
        if (inserat.getStatus() != InseratStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This help request is no longer open");
        }
        if (inserat.getVolunteerApplied().contains(volunteer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have already applied to this help request");
        }

        inserat.getVolunteerApplied().add(volunteer);
        inseratRepository.save(inserat);
        inseratRepository.flush();
        return inserat;
    }

    public Inserat editInserat(String inseratId, Inserat updatedData) {
        Inserat inserat = inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));

        if (updatedData.getDescription() != null) inserat.setDescription(updatedData.getDescription());
        if (updatedData.getLocation() != null) inserat.setLocation(updatedData.getLocation());
        if (updatedData.getLatitude() != null) inserat.setLatitude(updatedData.getLatitude());
        if (updatedData.getLongitude() != null) inserat.setLongitude(updatedData.getLongitude());
        if (updatedData.getDate() != null) inserat.setDate(updatedData.getDate());
        if (updatedData.getTimeframe() != null) inserat.setTimeframe(updatedData.getTimeframe());
        if (updatedData.getWorkType() != null) inserat.setWorkType(updatedData.getWorkType());
        if (updatedData.getTime() != null) inserat.setTime(updatedData.getTime());

        checkValidInseratData(inserat);
        inseratRepository.save(inserat);
        inseratRepository.flush();
        return inserat;
    }

    public Inserat getInseratById(String inseratId) {
        return inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));
    }

    public List<User> getApplicants(String inseratId) {
        Inserat inserat = inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));
        return inserat.getVolunteerApplied();
    }

    public Inserat acceptVolunteer(String inseratId, String volunteerId) {
        Inserat inserat = inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));
        User volunteer = checkUserExists(volunteerId);

        if (!inserat.getVolunteerApplied().contains(volunteer)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This volunteer has not applied to this inserat");
        }

        inserat.setVolunteerAccepted(volunteer);
        inserat.setStatus(InseratStatus.ACCEPTED);
        inseratRepository.save(inserat);
        inseratRepository.flush();
        return inserat;
    }

    public Inserat dismissVolunteer(String inseratId, String volunteerId) {
        Inserat inserat = inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));
        User volunteer = checkUserExists(volunteerId);

        inserat.getVolunteerApplied().remove(volunteer);
        inseratRepository.save(inserat);
        inseratRepository.flush();
        return inserat;
    }

    public User getRecipientByInserat(String inseratId) {
        Inserat inserat = inseratRepository.findById(inseratId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Inserat not found"));
        return inserat.getRecipient();
    }

    private void autoFinishPastInserats(List<Inserat> inserats) {
        LocalDate today = LocalDate.now();
        for (Inserat inserat : inserats) {
            if (inserat.getDate().isBefore(today)
                    && inserat.getStatus() != InseratStatus.DONE) {
                inserat.setStatus(InseratStatus.DONE);
                inseratRepository.save(inserat);
            }
        }
        inseratRepository.flush();
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


    public List<Inserat> getAllInserats() {
    return inseratRepository.findAll().stream()
        .filter(inserat -> inserat.getLatitude() != null && inserat.getLongitude() != null)
        .collect(Collectors.toList());
    }
}