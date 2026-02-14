package com.tech.aidocqna.dto.file;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimestampSearchResponse {
    private Long startTime;
    private Long endTime;
    private String excerpt;

}
