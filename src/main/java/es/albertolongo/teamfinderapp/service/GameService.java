package es.albertolongo.teamfinderapp.service;

import es.albertolongo.teamfinderapp.exception.game.GameAlreadyExists;
import es.albertolongo.teamfinderapp.exception.game.GameNotFound;
import es.albertolongo.teamfinderapp.exception.game.RankAlreadyInGame;
import es.albertolongo.teamfinderapp.exception.game.RoleAlreadyInGame;
import es.albertolongo.teamfinderapp.model.dto.GameDTO;
import es.albertolongo.teamfinderapp.model.dto.RankDTO;
import es.albertolongo.teamfinderapp.model.dto.RoleDTO;
import es.albertolongo.teamfinderapp.model.entity.Game;
import es.albertolongo.teamfinderapp.model.entity.Rank;
import es.albertolongo.teamfinderapp.model.entity.Role;
import es.albertolongo.teamfinderapp.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Validated
public class GameService {

    @Autowired
    GameRepository gameRepository;

    public GameService() {
    }

    @Transactional
    public Long addGame(@NotNull @Valid GameDTO gameDTO){
        Optional<Game> byName = gameRepository.findByName(gameDTO.getName());

        if(byName.isPresent()){
            throw new GameAlreadyExists("Game with name: " + gameDTO.getName() + " already exists");
        }

        Game game = new Game(gameDTO.getName());

        Set<Role> roles = gameDTO.getRoles().stream()
                .map(role -> new Role(role.getName(), game))
                .collect(Collectors.toSet());

        Set<Rank> ranks = gameDTO.getRanks().stream()
                .map(rank -> new Rank(rank.getValue(), rank.getName(), game))
                .collect(Collectors.toSet());

        game.setRoles(roles);
        game.setRanks(ranks);

        return gameRepository.save(game).getId();
    }

    public Game getGame(@NotNull Long id){
        Optional<Game> game = gameRepository.findById(id);

        if(!game.isPresent()){
            throw new GameNotFound("Game not found");
        }

        return game.get();
    }

    public Game getGame(@NotBlank String name){
        Optional<Game> game = gameRepository.findByName(name);

        if(!game.isPresent()){
            throw new GameNotFound("Game not found");
        }

        return game.get();
    }

    @Transactional
    public Game addRole(@NotNull Long gameId, @NotNull @Valid RoleDTO roleDTO){
        Optional<Game> game = gameRepository.findById(gameId);

        if(!game.isPresent()){
            throw new GameNotFound("Game not found");
        }

        Role role = new Role(roleDTO.getName(), game.get());

        Optional<Role> any = game.get().getRoles().stream()
                .filter(r -> roleDTO.getName().equals(r.getRole()))
                .findAny();

        if(any.isPresent()){
            throw new RoleAlreadyInGame("Role already exists in " + game.get().getName());
        }

        game.get().getRoles().add(role);

        return gameRepository.save(game.get());
    }

    @Transactional
    public Game addRank(@NotNull Long gameId, @NotNull @Valid RankDTO rankDTO){
        Optional<Game> game = gameRepository.findById(gameId);

        if(!game.isPresent()){
            throw new GameNotFound("Game not found");
        }

        Rank rank = new Rank(rankDTO.getValue(), rankDTO.getName(), game.get());

        Optional<Rank> any = game.get().getRanks().stream()
                .filter(r -> rankDTO.getValue() == r.getValue()
                        || rankDTO.getName().equals(r.getRank()))
                .findAny();

        if(any.isPresent()){
            throw new RankAlreadyInGame("Rank already exists in " + game.get().getName());
        }

        game.get().getRanks().add(rank);

        return gameRepository.save(game.get());
    }
}
