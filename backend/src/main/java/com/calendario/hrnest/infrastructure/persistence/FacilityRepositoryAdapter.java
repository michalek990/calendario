package com.calendario.hrnest.infrastructure.persistence;

import com.calendario.hrnest.domain.facility.Facility;
import com.calendario.hrnest.domain.facility.FacilityRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class FacilityRepositoryAdapter implements FacilityRepository {

    private final SpringDataFacilityRepository springDataFacilityRepository;

    public FacilityRepositoryAdapter(SpringDataFacilityRepository springDataFacilityRepository) {
        this.springDataFacilityRepository = springDataFacilityRepository;
    }

    @Override
    public Facility save(Facility facility) {
        return toDomain(springDataFacilityRepository.save(toEntity(facility)));
    }

    @Override
    public Optional<Facility> findById(Long id) {
        return springDataFacilityRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Facility> findAll() {
        return springDataFacilityRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByName(String name) {
        return springDataFacilityRepository.existsByName(name);
    }

    @Override
    public void deleteById(Long id) {
        springDataFacilityRepository.deleteById(id);
    }

    private FacilityJpaEntity toEntity(Facility facility) {
        FacilityJpaEntity entity = new FacilityJpaEntity();
        entity.setId(facility.getId());
        entity.setName(facility.getName());
        entity.setCreatedAt(facility.getCreatedAt());
        return entity;
    }

    private Facility toDomain(FacilityJpaEntity entity) {
        return Facility.reconstitute(entity.getId(), entity.getName(), entity.getCreatedAt());
    }
}
