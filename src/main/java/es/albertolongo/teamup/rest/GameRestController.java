package es.albertolongo.teamup.rest;

import es.albertolongo.teamup.api.GameApi;
import es.albertolongo.teamup.exception.game.*;
import es.albertolongo.teamup.model.dto.GameDTO;
import es.albertolongo.teamup.model.dto.RankDTO;
import es.albertolongo.teamup.model.dto.RoleDTO;
import es.albertolongo.teamup.model.entity.Game;
import es.albertolongo.teamup.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
@CrossOrigin
public class GameRestController implements GameApi {

    @Autowired
    GameService gameService;

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handlerRestrictions(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({GameNotFound.class, RankNotFound.class, RoleNotFound.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handlerNotFound(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({GameAlreadyExists.class, RankAlreadyInGame.class, RoleAlreadyInGame.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handlerGameExceptions(RuntimeException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /*
    @Override
    public ResponseEntity<Long> addGame(GameDTO gameDTO) {
        Long id = gameService.addGame(gameDTO);
        return ResponseEntity.ok(id);
    }
     */

    @Override
    public ResponseEntity<GameDTO> getGameById(Long gameId) {
        Game game = gameService.getGame(gameId);
        return ResponseEntity.ok(game.toDTO());
    }

    @Override
    public ResponseEntity<Set<GameDTO>> getAllGames() {
        Set<Game> games = gameService.getAllGames();

        Set<GameDTO> gameDTOS = games.stream()
                .map(game -> game.toDTO())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(gameDTOS);
    }

    /*
    @Override
    public ResponseEntity<GameDTO> addRankToGame(Long gameId, RankDTO rankDTO) {
        Game game = gameService.addRank(gameId, rankDTO);
        return ResponseEntity.ok(game.toDTO());
    }

    @Override
    public ResponseEntity<GameDTO> addRoleToGame(Long gameId, RoleDTO roleDTO) {
        Game game = gameService.addRole(gameId, roleDTO);
        return ResponseEntity.ok(game.toDTO());
    }
     */
}
