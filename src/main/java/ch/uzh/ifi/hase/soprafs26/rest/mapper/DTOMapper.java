package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.mapstruct.ReportingPolicy;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    @Mapping(source = "volunteer", target = "isVolunteer")
    @Mapping(target = "name", ignore = true)
    UserGetDTO convertEntityToUserGetDTO(User user);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "surname", target = "surname")
    @Mapping(source = "lastname", target = "lastname")
    @Mapping(source = "emailAddress", target = "emailAddress")
    @Mapping(source = "isVolunteer", target = "volunteer")
    @Mapping(source = "bio", target = "bio")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(source = "dateOfBirth", target = "dateOfBirth")
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);


    Inserat convertInseratPostDTOtoEntity(InseratPostDTO inseratPostDTO);
    InseratGetDTO convertEntityToInseratGetDTO(Inserat inserat);
}