package es.albertolongo.teamfinderapp.rest;

import es.albertolongo.teamfinderapp.api.TeamApi;
import es.albertolongo.teamfinderapp.exception.player.PlayerNotFound;
import es.albertolongo.teamfinderapp.exception.team.*;
import es.albertolongo.teamfinderapp.model.dto.TeamDTO;
import es.albertolongo.teamfinderapp.model.entity.Team;
import es.albertolongo.teamfinderapp.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.ConstraintViolationException;
import java.util.UUID;

@RestController
@RequestMapping("/")
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
    public ResponseEntity<UUID> teamPost(TeamDTO teamDTO) {
        UUID id = teamService.registerTeam(teamDTO);
        return ResponseEntity.status(HttpStatus.OK).body(id);
    }

    @Override
    public ResponseEntity<TeamDTO> teamTeamIdGet(UUID teamId) {
        Team team = teamService.getTeam(teamId);
        return ResponseEntity.status(HttpStatus.OK).body(team.teamDTO());
    }

    @Override
    public ResponseEntity<TeamDTO> teamTeamIdPlayerIdPatch(UUID teamId, UUID playerId, String action) {
        Team team = teamService.modifyTeamMember(teamId, playerId, action);
        return ResponseEntity.status(HttpStatus.OK).body(team.teamDTO());
    }

    @Override
    public ResponseEntity<Void> teamTeamIdDelete(UUID teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}