package com.gabrielbog.attendanceserver.models;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class QrCodeResponse {
    private int code;
    private String qrString;
}
