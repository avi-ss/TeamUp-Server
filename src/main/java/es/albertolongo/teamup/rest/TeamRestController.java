package es.albertolongo.teamup.rest;

import es.albertolongo.teamup.api.TeamApi;
import es.albertolongo.teamup.exception.player.PlayerNotFound;
import es.albertolongo.teamup.exception.team.*;
import es.albertolongo.teamup.model.dto.TeamDTO;
import es.albertolongo.teamup.model.dto.TeamPreferencesDTO;
import es.albertolongo.teamup.model.entity.Team;
import es.albertolongo.teamup.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@CrossOrigin
public class TeamRestController implements TeamApi {

    @Autowired
    TeamService teamService;

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handlerRestrictions(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({PlayerNotFound.class, TeamNotFound.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handlerNotFound(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({InvalidTeam.class, MemberAlreadyInTeam.class,
            MemberNotInTeam.class, MemberNumberIsLow.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handlerTeamExceptions(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @Override
    // The founder has to be the one who create the team
    @PreAuthorize("#teamDTO.founder == authentication.principal.uuid")
    public ResponseEntity<UUID> addTeam(TeamDTO teamDTO) {
        UUID id = teamService.registerTeam(teamDTO);
        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<TeamDTO> getTeamById(UUID teamId) {
        Team team = teamService.getTeam(teamId);
        return ResponseEntity.ok(team.toDTO());
    }

    @Override
    public ResponseEntity<TeamPreferencesDTO> getTeamPreferencesById(UUID teamId) {
        Team team = teamService.getTeam(teamId);
        return ResponseEntity.ok(team.getTeamPreferences().toDTO());
    }

    @Override
    public ResponseEntity<Boolean> checkTeamWithName(String teamName) {
        boolean response = teamService.checkTeamWithName(teamName);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Set<TeamDTO>> getAllTeamsForPlayer(UUID playerId) {
        Set<Team> teams = teamService.getAllTeamsForPlayer(playerId);

        Set<TeamDTO> teamDTOS = teams.stream().map(team -> team.toDTO()).collect(Collectors.toSet());

        return ResponseEntity.ok(teamDTOS);
    }

    @Override
    @PreAuthorize("#teamId == authentication.principal.teamId")
    public ResponseEntity<TeamDTO> addMemberToTeam(UUID teamId, UUID playerId) {
        Team team = teamService.addTeamMember(teamId, playerId);
        return ResponseEntity.ok(team.toDTO());
    }

    @Override
    // Either the founder can kick anyone, or the own player can kick itself
    @PreAuthorize("#teamId == authentication.principal.teamId || #playerId == authentication.principal.uuid")
    public ResponseEntity<TeamDTO> deleteMemberOfTeam(UUID teamId, UUID playerId) {
        Team team = teamService.deleteTeamMember(teamId, playerId);
        return ResponseEntity.ok(team.toDTO());
    }

    @Override
    @PreAuthorize("#teamId == authentication.principal.teamId")
    public ResponseEntity<Void> deleteTeam(UUID teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
