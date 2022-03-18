package es.albertolongo.teamfinderapp.rest;

import es.albertolongo.teamfinderapp.api.PlayerApi;
import es.albertolongo.teamfinderapp.exception.EmailAlreadyInUse;
import es.albertolongo.teamfinderapp.exception.NicknameAlreadyInUse;
import es.albertolongo.teamfinderapp.model.dto.PlayerDTO;
import es.albertolongo.teamfinderapp.model.entity.Player;
import es.albertolongo.teamfinderapp.service.PlayerService;
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
public class PlayerRestController implements PlayerApi {

    @Autowired
    PlayerService playerService;

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handlerRestrictions(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({EmailAlreadyInUse.class, NicknameAlreadyInUse.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handlerAlreadyInUse(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handlerRuntimeExceptions(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @Override
    public ResponseEntity<UUID> playerPost(PlayerDTO playerDTO) {
        try {
            UUID id = playerService.registerPlayer(playerDTO);
            return ResponseEntity.status(HttpStatus.OK).body(id);
        } catch (EmailAlreadyInUse | NicknameAlreadyInUse e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<PlayerDTO> playerPlayerIdGet(UUID playerId) {
        try {
            Player player = playerService.getPlayer(playerId);
            return ResponseEntity.status(HttpStatus.OK).body(player.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<PlayerDTO> playerPlayerIdPut(UUID playerId, PlayerDTO playerDTO) {
        try {
            Player player = playerService.modifyPlayer(playerId, playerDTO);
            return ResponseEntity.status(HttpStatus.OK).body(player.toDTO());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @Override
    public ResponseEntity<Void> playerPlayerIdDelete(UUID playerId) {
        try {
            playerService.deletePlayer(playerId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
