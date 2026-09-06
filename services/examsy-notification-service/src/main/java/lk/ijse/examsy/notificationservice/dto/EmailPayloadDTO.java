package lk.ijse.examsy.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailPayloadDTO {
    private String to;
    private String recipientName;
    private String subject;
    private String body;
    private boolean isHtml;
    private String senderAlias;
}
