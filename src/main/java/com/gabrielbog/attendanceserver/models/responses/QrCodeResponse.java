package com.gabrielbog.attendanceserver.models.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class QrCodeResponse {
    private int code;
    private long duration; //in milliseconds
    private String qrString;
    private String subjectString;
    private int grup;
}
