package com.tricolori.backend.core.services;

import com.tricolori.backend.core.domain.models.PriceList;
import com.tricolori.backend.core.domain.repositories.PriceListRepository;
import com.tricolori.backend.shared.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceListService {

    private final PriceListRepository priceListRepository;

    public PriceList getCurrentPriceList() {
        return priceListRepository.findLatest()
                .orElseThrow(() ->
                        new IllegalStateException("price list not configured"));
    }

    public Double calculateBasePrice(VehicleType type) {
        return getCurrentPriceList().getPriceForVehicleType(type);
    }

    public Double getKmPrice() {
        return getCurrentPriceList().getKmPrice();
    }
}
