package com.example.repository;

import com.example.entity.RepositorioArchivo;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON) // important
public class RepositorioArchivoRepository implements PanacheRepository<RepositorioArchivo> {

    public Uni<RepositorioArchivo> findRepositorioArchivoById(final Long id) {
        return Uni.createFrom().item(() -> find("id", id).firstResult());
    }

    public List<RepositorioArchivo> findRepositorioArchivoByCuilInscripcionInstancia(
            final String cuil, Long inscripcionId, Long instanciaSedeId, String nombre) {

        Map<String, Object> params = new HashMap<>();
        params.put("cuil", cuil);
        params.put("inscripcionId", inscripcionId);
        params.put("instanciaSedeId", instanciaSedeId);

        PanacheQuery<RepositorioArchivo> query = find(
                "cuil = :cuil AND inscripcionId = :inscripcionId AND instanciaSedeId = :instanciaSedeId",
                params
        );
        return query.list();
    }

    public List<RepositorioArchivo> findRepositorioArchivoByInstanciaSedeId(
            final Long instanciaSedeId) {

        Map<String, Object> params = new HashMap<>();
        params.put("instanciaSedeId", instanciaSedeId);

        PanacheQuery<RepositorioArchivo> query = find(
                "instanciaSedeId = :instanciaSedeId",
                params
        );
        return query.list();
    }

    public List<RepositorioArchivo> findRepositorioArchivoByInscripcionId(
            final Long inscripcionId) {

        Map<String, Object> params = new HashMap<>();
        params.put("inscripcionId", inscripcionId);

        PanacheQuery<RepositorioArchivo> query = find(
                "inscripcionId = :inscripcionId",
                params
        );
        return query.list();
    }
}
