package asap.demo.listener;

import asap.demo.entity.FireDetectionEvent;
import asap.demo.entity.User;
import asap.demo.repository.UserRepository;
import asap.demo.service.SMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FireDetectionListener {
    private final UserRepository userRepository;
    private final SMSService smsService;

    @Scheduled(fixedDelay = 5000)
    public void pollFireDetectionQueue() {
        log.info("화재 감지 모니터링 중...");
    }

    public void processFireDetection(String message) {
        try {
            log.info("화재 감지 이벤트 수신: {}", message);
            List<User> users = userRepository.findAll();
            
            String smsMessage = String.format(
                "[화재감지] 화재가 감지되었습니다.\n시간: %s",
                DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalDateTime.now())
            );

            for (User user : users) {
                try {
                    smsService.sendSMS(user.getPhoneNumber(), smsMessage);
                    log.info("화재 감지 알림 발송 완료 - 수신자: {}", user.getPhoneNumber());
                } catch (Exception e) {
                    log.error("화재 감지 알림 발송 실패 - 수신자: {}", user.getPhoneNumber(), e);
                }
            }
        } catch (Exception e) {
            log.error("화재 감지 이벤트 처리 실패", e);
            throw new RuntimeException("화재 감지 이벤트 처리 실패", e);
        }
    }
} 