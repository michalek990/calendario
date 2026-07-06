package com.calendario.hrnest.application.facility;

import com.calendario.hrnest.domain.facility.FacilityRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ListFacilitiesUseCase {

    private final FacilityRepository facilityRepository;

    public ListFacilitiesUseCase(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public List<FacilityView> execute() {
        return facilityRepository.findAll().stream()
                .map(FacilityView::from)
                .sorted(Comparator.comparing(FacilityView::name))
                .toList();
    }
}
