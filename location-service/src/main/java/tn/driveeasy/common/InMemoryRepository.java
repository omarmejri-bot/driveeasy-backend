package tn.driveeasy.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dépôt générique EN MÉMOIRE (aucune base de données, conformément au cahier des charges).
 * <ul>
 *   <li>{@link ConcurrentHashMap} : accès concurrents sûrs (plusieurs requêtes HTTP en parallèle) ;</li>
 *   <li>{@link AtomicLong} : génération d'identifiants auto-incrémentés sans doublon.</li>
 * </ul>
 * Les données sont perdues à l'arrêt du service (limite assumée du prototype).
 */
public abstract class InMemoryRepository<T extends Identifiable> {

    protected final Map<Long, T> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<T> findAll() {
        List<T> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(Identifiable::getId));
        return list;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }

    /** Création (id null => nouvel id) ou mise à jour (id existant). */
    public T save(T entity) {
        if (entity.getId() == null) {
            entity.setId(sequence.incrementAndGet());
        }
        store.put(entity.getId(), entity);
        return entity;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }

    public long count() {
        return store.size();
    }
}
