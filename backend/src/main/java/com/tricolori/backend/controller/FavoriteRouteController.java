package com.tricolori.backend.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.tricolori.backend.entity.Stop;
import com.tricolori.backend.entity.Location;
import com.tricolori.backend.entity.Person;
import com.tricolori.backend.dto.ride.FavoriteRouteDto;
import com.tricolori.backend.dto.ride.RouteDto;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/favorite-routes")
public class FavoriteRouteController {

    @GetMapping()
    public ResponseEntity<List<FavoriteRouteDto>> getFavoriteRoutes(
        @AuthenticationPrincipal Person person
    ) {
        List<FavoriteRouteDto> favoriteRoutes = new ArrayList<>();

        // 1. Novi Sad - Beograd
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Bulevar Oslobođenja 10, Novi Sad", new Location(19.8335, 45.2544)),
                        new Stop("Knez Mihailova 1, Beograd", new Location(20.4573, 44.8186)),
                        List.of(
                                new Stop("Glavna 1, Inđija", new Location(20.0815, 45.0482)),
                                new Stop("Glavna 2, Inđija", new Location(20.0815, 45.0482))
                        )
                ), "Put za posao"));

        // 2. Niš - Kragujevac
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Trg Kralja Milana, Niš", new Location(21.8954, 43.3209)),
                        new Stop("Trg Slobode, Kragujevac", new Location(20.9114, 44.0128)),
                        List.of(new Stop("Vojvode Putnika, Jagodina", new Location(21.2612, 43.9771)))
                ), "Vikend kod babe"));

        // 3. Subotica - Novi Sad
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Korzo, Subotica", new Location(19.6651, 46.1001)),
                        new Stop("Zmaj Jovina, Novi Sad", new Location(19.8451, 45.2555)),
                        List.of(new Stop("Centar, Bačka Topola", new Location(19.6312, 45.8151)))
                ), "Fakultet ruta"));

        // 4. Kraljevo - Čačak
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Oktobarskih žrtava, Kraljevo", new Location(20.6839, 43.7258)),
                        new Stop("Gradski Trg, Čačak", new Location(20.3497, 43.8914)),
                        List.of()
                ), "Brza ruta KV-ČA"));

        // 5. Šabac - Loznica
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Gospodar Jevremova, Šabac", new Location(19.6908, 44.7553)),
                        new Stop("Jovana Cvijića, Loznica", new Location(19.2234, 44.5312)),
                        List.of()
                ), "Šabac - Loznica"));

        // 6. Zrenjanin - Kikinda
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Trg Slobode, Zrenjanin", new Location(20.3911, 45.3836)),
                        new Stop("Trg Srpskih Dobrovoljaca, Kikinda", new Location(20.4633, 45.8319)),
                        List.of()
                ), "Banatska ruta"));

        // 7. Pančevo - Vršac
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Vojvode Radomira Putnika, Pančevo", new Location(20.6403, 44.8708)),
                        new Stop("Trg Pobede, Vršac", new Location(21.3036, 45.1167)),
                        List.of(new Stop("Beogradski put, Alibunar", new Location(21.0039, 45.0819)))
                ), "Put za Vršac"));

        // 8. Valjevo - Užice
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Karađorđeva, Valjevo", new Location(19.8847, 44.2711)),
                        new Stop("Dimitrija Tucovića, Užice", new Location(19.8422, 43.8586)),
                        List.of(new Stop("Vojvode Mišića, Kosjerić", new Location(19.9122, 43.9950)))
                ), "Preko Kosjerića"));

        // 9. Novi Pazar - Raška
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Stevana Nemanje, Novi Pazar", new Location(20.5122, 43.1367)),
                        new Stop("Ibarska, Raška", new Location(20.6153, 43.2861)),
                        List.of()
                ), "NP - Raška"));

        // 10. Sombor - Apatin
        favoriteRoutes.add(new FavoriteRouteDto(
                new RouteDto(
                        new Stop("Kralja Petra I, Sombor", new Location(19.1122, 45.7733)),
                        new Stop("Srpskih Vladara, Apatin", new Location(18.9856, 45.6711)),
                        List.of()
                ), "Sombor - Apatin"));

        return ResponseEntity.ok(favoriteRoutes);
    }
}