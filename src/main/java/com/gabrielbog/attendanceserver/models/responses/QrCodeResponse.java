package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class QrCodeResponse {
    private int code;
    private String qrString;
    private String additionalString;
}
