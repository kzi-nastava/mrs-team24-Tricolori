package com.tricolori.backend.controller;

import com.tricolori.backend.entity.Panic;
import com.tricolori.backend.service.PanicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/panics")
@RequiredArgsConstructor
public class PanicController {

    private final PanicService panicService;

    @GetMapping()
    public List<Panic> getAll() {
        return panicService.findAll();
    }

}
