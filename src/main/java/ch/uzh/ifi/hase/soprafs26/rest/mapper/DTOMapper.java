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
    @Mapping(source = "recipient.age", target = "recipientAge")
    @Mapping(source = "recipient.phoneNumber", target = "recipientPhone")
    @Mapping(source = "recipient.emailAddress", target = "recipientEmail")
    @Mapping(source = "volunteerApplied", target = "volunteerAppliedCount", qualifiedByName = "listSize")
    @Mapping(source = "volunteerAccepted.username", target = "volunteerAcceptedUsername")
    @Mapping(source = "volunteerAccepted.phoneNumber", target = "volunteerAcceptedPhone")
    @Mapping(source = "volunteerAccepted.emailAddress", target = "volunteerAcceptedEmail")
    InseratGetDTO convertEntityToInseratGetDTO(Inserat inserat);

    ApplicantDTO convertEntityToApplicantDTO(User user);

    @Named("listSize")
    default int listSize(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
