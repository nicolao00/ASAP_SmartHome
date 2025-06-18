package asap.demo.service;

import asap.demo.entity.FireDetectionEvent;
import asap.demo.entity.User;
import asap.demo.repository.FireDetectionEventRepository;
import asap.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FireDetectionService {
    private final UserRepository userRepository;
    private final FireDetectionEventRepository fireDetectionEventRepository;
    private final SMSService smsService;

    @Transactional
    public void handleFireDetection() {
        log.info("화재 감지 이벤트 처리 시작");
        
        // 이벤트 저장
        FireDetectionEvent event = new FireDetectionEvent();
        event.setDetectedAt(LocalDateTime.now());
        fireDetectionEventRepository.save(event);
        
        log.info("화재 감지 이벤트가 발생했습니다. 감지 시간: {}", 
            event.getDetectedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        log.info("화재 감지 이벤트 처리 완료");
    }
} 