package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ApplicantDTO;

import java.util.List;
import java.time.LocalDate;
import java.time.Period;

@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    UserGetDTO convertEntityToUserGetDTO(User user);

    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    Inserat convertInseratPostDTOtoEntity(InseratPostDTO inseratPostDTO);

    @Mapping(source = "recipient.id", target = "recipientId")
    @Mapping(source = "recipient.username", target = "recipientUsername")
    @Mapping(source = "recipient.surname", target = "recipientSurname")
    @Mapping(source = "recipient.lastname", target = "recipientLastname")
    @Mapping(target = "recipientAge", ignore = true)
    @Mapping(source = "recipient.phoneNumber", target = "recipientPhone")
    @Mapping(source = "recipient.emailAddress", target = "recipientEmail")
    @Mapping(target = "volunteerAppliedCount", ignore= true)
    @Mapping(source = "volunteerAccepted.username", target = "volunteerAcceptedUsername")
    @Mapping(source = "volunteerAccepted.phoneNumber", target = "volunteerAcceptedPhone")
    @Mapping(source = "volunteerAccepted.emailAddress", target = "volunteerAcceptedEmail")
    InseratGetDTO convertEntityToInseratGetDTO(Inserat inserat);

    @AfterMapping
    default void fillComputedFields(Inserat inserat, @MappingTarget InseratGetDTO dto) {
        // volunteer count
        if (inserat.getVolunteerApplied() != null) {
            dto.setVolunteerAppliedCount(inserat.getVolunteerApplied().size());
        }

        // recipient age
        if (inserat.getRecipient() != null && inserat.getRecipient().getDateOfBirth() != null) {
            LocalDate dob = inserat.getRecipient().getDateOfBirth();
            int age = Period.between(dob, LocalDate.now()).getYears();
            dto.setRecipientAge(age);
        }
    }

    ApplicantDTO convertEntityToApplicantDTO(User user);

    @Named("listSize")
    default int listSize(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
