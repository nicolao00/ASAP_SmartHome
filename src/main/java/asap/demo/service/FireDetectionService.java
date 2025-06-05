package asap.demo.service;

import asap.demo.entity.FireDetectionEvent;
import asap.demo.entity.User;
import asap.demo.repository.FireDetectionEventRepository;
import asap.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final AmazonSQSAsync amazonSQSAsync;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fire-detection.queue.url}")
    private String queueUrl;

    @Transactional
    public void handleFireDetection() {
        log.info("화재 감지 이벤트 처리 시작");
        
        // 이벤트 저장
        FireDetectionEvent event = new FireDetectionEvent();
        event.setDetectedAt(LocalDateTime.now());
        fireDetectionEventRepository.save(event);
        
        try {
            String message = objectMapper.writeValueAsString(event);
            amazonSQSAsync.sendMessage(queueUrl, message);
            log.info("화재 감지 이벤트를 SQS 큐에 전송 완료");
        } catch (Exception e) {
            log.error("SQS 큐 전송 실패", e);
            throw new RuntimeException("SQS 큐 전송 실패", e);
        }
        
        log.info("화재 감지 이벤트 처리 완료");
    }
} 