package com.tricolori.backend.service;

import com.tricolori.backend.entity.Panic;
import com.tricolori.backend.repository.PanicRepository;
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
