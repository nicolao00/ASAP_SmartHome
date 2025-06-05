package asap.demo.listener;

import asap.demo.entity.FireDetectionEvent;
import asap.demo.entity.User;
import asap.demo.repository.UserRepository;
import asap.demo.service.SMSService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class FireDetectionListener {
    private final UserRepository userRepository;
    private final SMSService smsService;
    private final AmazonSQSAsync amazonSQSAsync;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${fire-detection.queue.url}")
    private String queueUrl;

    @Scheduled(fixedDelay = 5000)
    public void pollFireDetectionQueue() {
        try {
            var messages = amazonSQSAsync.receiveMessage(queueUrl).getMessages();
            for (Message sqsMessage : messages) {
                processFireDetection(sqsMessage.getBody());
                amazonSQSAsync.deleteMessage(queueUrl, sqsMessage.getReceiptHandle());
            }
        } catch (Exception e) {
            log.error("SQS 큐 폴링 중 오류", e);
        }
    }

    public void processFireDetection(String message) {
        try {
            FireDetectionEvent event = objectMapper.readValue(message, FireDetectionEvent.class);
            List<User> users = userRepository.findAll();
            
            String smsMessage = String.format(
                "[화재감지] 화재가 감지되었습니다.\n시간: %s",
                event.getDetectedAt().format(DateTimeFormatter.ofPattern("HH:mm"))
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