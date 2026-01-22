package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.Panic;
import com.tricolori.backend.core.domain.repositories.PanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PanicService {

    private final PanicRepository panicRepository;

    public List<Panic> findAll() {
        return panicRepository.findAll();
    }

}
