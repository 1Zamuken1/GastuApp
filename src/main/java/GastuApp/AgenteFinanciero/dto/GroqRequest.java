package GastuApp.AgenteFinanciero.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroqRequest {
    
    private List<Message> messages;
    private String model;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}