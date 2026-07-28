package com.app.work.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkDTO {
    private Integer workNumber;
    private String workStartTime;
    private String workEndTime;
    private String workDetail;
    private String workStatus;
    private String workSpentTime;
    private Integer memberNumber;
    private Integer workPlaceNumber;
}
