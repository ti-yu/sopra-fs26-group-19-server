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

/**
 * DTOMapper
 * Responsible for mapping between internal entity representations and
 * external API representations (DTOs).
 *
 * Notes on boolean field mapping:
 * - User entity and all DTOs use getIsVolunteer()/setIsVolunteer() naming
 *   so that Jackson serializes/deserializes the JSON field as "isVolunteer".
 * - MapStruct sees the property name as "isVolunteer" on both sides,
 *   so no explicit @Mapping is needed for this field.
 */
@Mapper
public interface DTOMapper {

    DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

    // PostDTO -> Entity: all fields match by name (including isVolunteer)
    User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

    // Entity -> GetDTO: all fields match by name. No "name" field exists
    // on User entity, so we don't map it.
    UserGetDTO convertEntityToUserGetDTO(User user);

    // PutDTO -> Entity: isVolunteer maps automatically since both sides
    // use getIsVolunteer()/setIsVolunteer().
    User convertUserPutDTOtoEntity(UserPutDTO userPutDTO);

    // InseratPostDTO -> Entity: straightforward field mapping
    Inserat convertInseratPostDTOtoEntity(InseratPostDTO inseratPostDTO);

    // Entity -> InseratGetDTO: recipient is a User object on the entity,
    // but recipientId is a String on the DTO, so we need an explicit mapping
    // to extract the nested id.
    @Mapping(source = "recipient.id", target = "recipientId")
    InseratGetDTO convertEntityToInseratGetDTO(Inserat inserat);
}
