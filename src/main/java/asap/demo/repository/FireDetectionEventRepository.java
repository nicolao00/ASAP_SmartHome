package asap.demo.repository;

import asap.demo.entity.FireDetectionEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FireDetectionEventRepository extends JpaRepository<FireDetectionEvent, Long> {
} 