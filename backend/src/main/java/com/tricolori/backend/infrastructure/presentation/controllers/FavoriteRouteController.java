package com.tricolori.backend.infrastructure.presentation.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tricolori.backend.core.domain.models.Address;
import com.tricolori.backend.infrastructure.presentation.dtos.RouteDto;

// http://localhost:8080/api/v1/favorite-routes/2

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/favorite-routes")
public class FavoriteRouteController {
    
    // TODO: Use something other than userId...
    @GetMapping("/{id}")
    public ResponseEntity<List<RouteDto>> getFavoriteRoutes(@PathVariable("id") Long userId) {
        if (userId == 2) {
            List<RouteDto> mockRoutes = new ArrayList<>();

            // 1. Novi Sad - Beograd
            mockRoutes.add(new RouteDto(
                new Address("Bulevar Oslobođenja 10", "Novi Sad", 19.8335, 45.2544),
                new Address("Knez Mihailova 1", "Beograd", 20.4573, 44.8186),
                List.of(new Address("Glavna 1", "Inđija", 20.0815, 45.0482), 
                        new Address("Glavna 2", "Inđija", 20.0815, 45.0482),
                        new Address("Glavna 3", "Inđija", 20.0815, 45.0482),
                        new Address("Glavna 4", "Inđija", 20.0815, 45.0482))
            ));

            // 2. Niš - Kragujevac
            mockRoutes.add(new RouteDto(
                new Address("Trg Kralja Milana", "Niš", 21.8954, 43.3209),
                new Address("Trg Slobode", "Kragujevac", 20.9114, 44.0128),
                List.of(new Address("Vojvode Putnika", "Jagodina", 21.2612, 43.9771))
            ));

            // 3. Subotica - Novi Sad
            mockRoutes.add(new RouteDto(
                new Address("Korzo", "Subotica", 19.6651, 46.1001),
                new Address("Zmaj Jovina", "Novi Sad", 19.8451, 45.2555),
                List.of(new Address("Centar", "Bačka Topola", 19.6312, 45.8151))
            ));

            // 4. Kraljevo - Čačak
            mockRoutes.add(new RouteDto(
                new Address("Oktobarskih žrtava", "Kraljevo", 20.6839, 43.7258),
                new Address("Gradski Trg", "Čačak", 20.3497, 43.8914),
                List.of()
            ));

            // 5. Šabac - Loznica
            mockRoutes.add(new RouteDto(
                new Address("Gospodar Jevremova", "Šabac", 19.6908, 44.7553),
                new Address("Jovana Cvijića", "Loznica", 19.2234, 44.5312),
                List.of()
            ));

            // 6. Zrenjanin - Kikinda
            mockRoutes.add(new RouteDto(
                new Address("Trg Slobode", "Zrenjanin", 20.3911, 45.3836),
                new Address("Trg Srpskih Dobrovoljaca", "Kikinda", 20.4633, 45.8319),
                List.of()
            ));

            // 7. Pančevo - Vršac
            mockRoutes.add(new RouteDto(
                new Address("Vojvode Radomira Putnika", "Pančevo", 20.6403, 44.8708),
                new Address("Trg Pobede", "Vršac", 21.3036, 45.1167),
                List.of(new Address("Beogradski put", "Alibunar", 21.0039, 45.0819))
            ));

            // 8. Valjevo - Užice
            mockRoutes.add(new RouteDto(
                new Address("Karađorđeva", "Valjevo", 19.8847, 44.2711),
                new Address("Dimitrija Tucovića", "Užice", 19.8422, 43.8586),
                List.of(new Address("Vojvode Mišića", "Kosjerić", 19.9122, 43.9950))
            ));

            // 9. Novi Pazar - Raška
            mockRoutes.add(new RouteDto(
                new Address("Stevana Nemanje", "Novi Pazar", 20.5122, 43.1367),
                new Address("Ibarska", "Raška", 20.6153, 43.2861),
                List.of()
            ));

            // 10. Sombor - Apatin
            mockRoutes.add(new RouteDto(
                new Address("Kralja Petra I", "Sombor", 19.1122, 45.7733),
                new Address("Srpskih Vladara", "Apatin", 18.9856, 45.6711),
                List.of()
            ));

            return ResponseEntity.ok(mockRoutes);
        }
        return ResponseEntity.ok(List.of());        
    }
}
