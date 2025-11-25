package server.database;

import org.springframework.data.jpa.repository.JpaRepository;
import commons.Step;

public interface StepRepository extends JpaRepository<Step, Long> {
}

