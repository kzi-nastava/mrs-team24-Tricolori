package com.tricolori.backend.service;

import com.tricolori.backend.dto.pricelist.PriceConfigRequest;
import com.tricolori.backend.dto.pricelist.PriceConfigResponse;
import com.tricolori.backend.entity.PriceList;
import com.tricolori.backend.repository.PriceListRepository;
import com.tricolori.backend.enums.VehicleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PriceListService {

    private final PriceListRepository priceListRepository;

    @Transactional(readOnly = true)
    public PriceList getCurrentPriceList() {
        return priceListRepository.findLatest()
                .orElseThrow(() ->
                        new IllegalStateException("price list not configured"));
    }

    @Transactional(readOnly = true)
    public Double calculateBasePrice(VehicleType type) {
        return getCurrentPriceList().getPriceForVehicleType(type);
    }

    @Transactional(readOnly = true)
    public Double getKmPrice() {
        return getCurrentPriceList().getKmPrice();
    }

    @Transactional(readOnly = true)
    public PriceConfigResponse getCurrentPricing() {
        PriceList priceList = getCurrentPriceList();

        return new PriceConfigResponse(
                priceList.getPriceForVehicleType(VehicleType.STANDARD),
                priceList.getPriceForVehicleType(VehicleType.LUXURY),
                priceList.getPriceForVehicleType(VehicleType.VAN),
                priceList.getKmPrice(),
                priceList.getCreatedAt()
        );
    }

    @Transactional
    public void updatePricing(PriceConfigRequest request) {
        PriceList priceList = new PriceList();
        priceList.setKmPrice(request.kmPrice());
        priceList.setStandardPrice(request.standardPrice());
        priceList.setLuxuryPrice(request.luxuryPrice());
        priceList.setVanPrice(request.vanPrice());

        priceListRepository.save(priceList);
    }
}