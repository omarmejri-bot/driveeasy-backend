package tn.driveeasy.location.repository;

import org.springframework.stereotype.Repository;
import tn.driveeasy.common.InMemoryRepository;
import tn.driveeasy.location.model.Location;

/** Dépôt en mémoire des locations. */
@Repository
public class LocationRepository extends InMemoryRepository<Location> {
}
