package com.tricolori.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tricolori.backend.dto.route.FavoriteRouteRequest;
import com.tricolori.backend.dto.route.FavoriteRouteResponse;
import com.tricolori.backend.entity.FavoriteRoute;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.entity.Route;
import com.tricolori.backend.exception.FavoriteRouteAlreadyExistsException;
import com.tricolori.backend.exception.FavoriteRouteNotFoundException;
import com.tricolori.backend.exception.RouteNotFoundException;
import com.tricolori.backend.mapper.FavoriteRouteMapper;
import com.tricolori.backend.repository.FavoriteRouteRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteRouteService {
    private final FavoriteRouteRepository repository;
    private final FavoriteRouteMapper mapper;
    private final RouteService routeService;

    @Transactional(readOnly = true)
    public List<FavoriteRouteResponse> getFavoriteRoutes(Person person) {
        List<FavoriteRoute> routes = repository.findAllByPersonId(person.getId());
        return mapper.toResponseList(routes);
    }

    @Transactional
    public void removeFavoriteRoute(Person person, Long routeId) {
        FavoriteRoute deleteRoute = repository.findByPersonIdAndRouteId(
            person.getId(), 
            routeId
        ).orElseThrow(() -> new FavoriteRouteNotFoundException("Favorite route to be deleted not found."));

        repository.delete(deleteRoute);
    }

    @Transactional
    public void addFavoriteRoute(Person person, FavoriteRouteRequest request) {
        Long personId = person.getId();
        Long routeId = request.getRouteId();

        Route route = routeService.findById(routeId)
            .orElseThrow(() -> new RouteNotFoundException(
                String.format("Route with id: %d not found.", routeId)
            ));

        if (repository.existsByPersonIdAndRouteId(personId, routeId)) {
            throw new FavoriteRouteAlreadyExistsException(
                String.format("User with id: %d already has route with id: %d in favorites.", personId, routeId)
            );
        }

        FavoriteRoute favoriteRoute = new FavoriteRoute();
        favoriteRoute.setPerson(person);
        favoriteRoute.setRoute(route);
        favoriteRoute.setTitle(request.getTitle());

        repository.save(favoriteRoute);
    }
}
