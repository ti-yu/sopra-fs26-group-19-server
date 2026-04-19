package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Inserat;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ApplicantDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.InseratPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.InseratService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class InseratController {

    private final InseratService inseratService;

    public InseratController(InseratService inseratService) {
        this.inseratService = inseratService;
    }

    @PostMapping("/help-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public InseratGetDTO createInserat(@RequestBody InseratPostDTO inseratPostDTO) {
        Inserat newInserat = DTOMapper.INSTANCE.convertInseratPostDTOtoEntity(inseratPostDTO);
        Inserat createdInserat = inseratService.createInserat(newInserat, inseratPostDTO.getRecipientId());
        return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(createdInserat);
    }

    @GetMapping("/users/{id}/help-requests")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<InseratGetDTO> getInseratsByRecipientId(@PathVariable String id) {
        List<Inserat> inserats = inseratService.getInseratsByRecipientId(id);
        return inserats.stream()
            .map(DTOMapper.INSTANCE::convertEntityToInseratGetDTO)
            .collect(Collectors.toList());
    }

    @GetMapping("/help-requests/{inseratId}/applicants")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<ApplicantDTO> getApplicants(@PathVariable String inseratId) {
        List<User> applicants = inseratService.getApplicants(inseratId);
        return applicants.stream()
            .map(DTOMapper.INSTANCE::convertEntityToApplicantDTO)
            .collect(Collectors.toList());
    }

    @PutMapping("/help-requests/{inseratId}/accept/{volunteerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public InseratGetDTO acceptVolunteer(@PathVariable String inseratId, @PathVariable String volunteerId) {
        Inserat updated = inseratService.acceptVolunteer(inseratId, volunteerId);
        return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(updated);
    }

    @PutMapping("/help-requests/{inseratId}/dismiss/{volunteerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public InseratGetDTO dismissVolunteer(@PathVariable String inseratId, @PathVariable String volunteerId) {
        Inserat updated = inseratService.dismissVolunteer(inseratId, volunteerId);
        return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(updated);
    }

    @GetMapping("/users/{id}/applications")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<InseratGetDTO> getApplicationsByVolunteerId(@PathVariable String id) {
        List<Inserat> inserats = inseratService.getApplicationsByVolunteerId(id);
        return inserats.stream()
            .map(DTOMapper.INSTANCE::convertEntityToInseratGetDTO)
            .collect(Collectors.toList());
    }

    @PostMapping("/help-requests/{inseratId}/apply/{volunteerId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public InseratGetDTO applyToInserat(@PathVariable String inseratId, @PathVariable String volunteerId) {
        Inserat updated = inseratService.applyToInserat(inseratId, volunteerId);
        return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(updated);
    }

    @GetMapping("/help-requests/{inseratId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public InseratGetDTO getInseratById(@PathVariable String inseratId) {
        Inserat inserat = inseratService.getInseratById(inseratId);
        return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(inserat);
    }

    @PutMapping("/help-requests/{inseratId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public InseratGetDTO editInserat(@PathVariable String inseratId, @RequestBody InseratPostDTO inseratPostDTO) {
        Inserat updatedData = DTOMapper.INSTANCE.convertInseratPostDTOtoEntity(inseratPostDTO);
        Inserat updated = inseratService.editInserat(inseratId, updatedData);
        return DTOMapper.INSTANCE.convertEntityToInseratGetDTO(updated);
    }

    @GetMapping("/help-requests-map")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<InseratGetDTO> getAllInserats() {
        List<Inserat> inserats = inseratService.getAllInserats();
        return inserats.stream()
            .map(DTOMapper.INSTANCE::convertEntityToInseratGetDTO)
            .collect(Collectors.toList());
    }
}