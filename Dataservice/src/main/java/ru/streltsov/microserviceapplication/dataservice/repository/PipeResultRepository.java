package ru.streltsov.microserviceapplication.dataservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.streltsov.microserviceapplication.dataservice.model.PipeResult;

import java.util.Optional;

/**
 * Репозиторий для работы с результатами анализа труб
 */
@Repository
public interface PipeResultRepository extends JpaRepository<PipeResult, Long> {

    /**
     * Найти последний результат анализа (с максимальным ID)
     */
    Optional<PipeResult> findFirstByOrderByIdDesc();

    /**
     * Найти результат по номеру трубы
     */
    Optional<PipeResult> findByPipeId(Long pipeId);
}
