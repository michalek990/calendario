package com.calendario.hrnest.application.facility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListFacilitiesUseCaseTest {

    @Mock
    private FacilityRepository facilityRepository;

    private ListFacilitiesUseCase useCase() {
        return new ListFacilitiesUseCase(facilityRepository);
    }

    @Test
    void execute_returnsFacilities_sortedByName() {
        when(facilityRepository.findAll()).thenReturn(List.of(
                Facility.create("Warszawa"), Facility.create("Krakow")));

        List<FacilityView> result = useCase().execute();

        assertThat(result).extracting(FacilityView::name).containsExactly("Krakow", "Warszawa");
    }

    @Test
    void execute_returnsEmptyList_whenNoFacilities() {
        when(facilityRepository.findAll()).thenReturn(List.of());

        assertThat(useCase().execute()).isEmpty();
    }
}
