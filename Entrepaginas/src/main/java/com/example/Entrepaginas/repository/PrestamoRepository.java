package com.example.Entrepaginas.repository;

import com.example.Entrepaginas.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

@Repository

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    Long countByActivoTrue();

    Long countByActivoFalse();

    Long countByActivo(boolean activo);

    Long countByFechaDevolucionBeforeAndActivoTrue(LocalDate fecha);
    
    List<Prestamo> findByClienteId(Long clienteId);
}

