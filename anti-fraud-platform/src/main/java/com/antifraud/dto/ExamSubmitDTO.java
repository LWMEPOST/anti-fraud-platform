package com.antifraud.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ExamSubmitDTO {
    private Long recordId;
    private List<Map<String, Object>> answers;
}
