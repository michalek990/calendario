package com.calendario.hrnest.api.facility;

import com.calendario.hrnest.application.facility.CreateFacilityCommand;
import com.calendario.hrnest.application.facility.CreateFacilityUseCase;
import com.calendario.hrnest.application.facility.DeleteFacilityUseCase;
import com.calendario.hrnest.application.facility.FacilityView;
import com.calendario.hrnest.application.facility.ListFacilitiesUseCase;
import com.calendario.hrnest.application.facility.UpdateFacilityCommand;
import com.calendario.hrnest.application.facility.UpdateFacilityUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final CreateFacilityUseCase createFacilityUseCase;
    private final ListFacilitiesUseCase listFacilitiesUseCase;
    private final UpdateFacilityUseCase updateFacilityUseCase;
    private final DeleteFacilityUseCase deleteFacilityUseCase;

    public FacilityController(CreateFacilityUseCase createFacilityUseCase, ListFacilitiesUseCase listFacilitiesUseCase,
                               UpdateFacilityUseCase updateFacilityUseCase, DeleteFacilityUseCase deleteFacilityUseCase) {
        this.createFacilityUseCase = createFacilityUseCase;
        this.listFacilitiesUseCase = listFacilitiesUseCase;
        this.updateFacilityUseCase = updateFacilityUseCase;
        this.deleteFacilityUseCase = deleteFacilityUseCase;
    }

    @PostMapping
    public ResponseEntity<FacilityView> create(@Valid @RequestBody CreateFacilityRequest request) {
        FacilityView created = createFacilityUseCase.execute(new CreateFacilityCommand(request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<FacilityView>> listAll() {
        return ResponseEntity.ok(listFacilitiesUseCase.execute());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacilityView> update(@PathVariable Long id, @Valid @RequestBody UpdateFacilityRequest request) {
        FacilityView updated = updateFacilityUseCase.execute(new UpdateFacilityCommand(id, request.name()));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteFacilityUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
