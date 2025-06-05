package asap.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SMSService {
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper;

    @Value("${aligo.api-key}")
    private String apiKey;

    @Value("${aligo.sender}")
    private String sender;

    public void sendSMS(String phoneNumber, String message) {
        try {
            String url = "https://apis.aligo.in/send/";
            
            // 파라미터 설정
            Map<String, String> params = new HashMap<>();
            params.put("key", apiKey);
            params.put("user_id", "asap");  // 알리고 계정 아이디
            params.put("sender", sender);
            params.put("receiver", phoneNumber);
            params.put("msg", message);
            params.put("msg_type", "SMS");  // 단문 메시지
            params.put("testmode_yn", "Y");  // 테스트 모드 (실제 발송 시 "N"으로 변경)

            // URL 인코딩된 폼 데이터 생성
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(entry.getKey())
                       .append("=")
                       .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            }

            // HTTP 요청 설정
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new StringEntity(formData.toString(), "UTF-8"));

            // 요청 실행 및 응답 처리
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = new String(response.getEntity().getContent().readAllBytes());
                JsonNode jsonResponse = objectMapper.readTree(responseBody);

                int resultCode = jsonResponse.get("result_code").asInt();
                String resultMessage = jsonResponse.get("message").asText();

                if (resultCode == 1) {
                    log.info("SMS sent successfully to: {}. Message ID: {}", 
                            phoneNumber, 
                            jsonResponse.get("msg_id").asText());
                } else {
                    log.error("Failed to send SMS to: {}. Error: {}", phoneNumber, resultMessage);
                    throw new RuntimeException("Failed to send SMS: " + resultMessage);
                }
            }
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", phoneNumber, e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
} 