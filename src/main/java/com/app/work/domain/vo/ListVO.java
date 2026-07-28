package com.app.work.domain.vo;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
public class ListVO {
    private String toDay;
    private Integer workNumber;
    private String workStartTime;
    private String workEndTime;
    private String workDetail;
    private String workStatus;
    private Integer memberNumber;
    private String memberName;
    private String memberRank;
    private String workSpentTime;
    private Integer workPlaceNumber;
    private String workPlaceName;
    private String toTime;
    private String endTime;
    private Integer teamNumber;
    private String workStartTime2;
    private String memberUse;
    private String workTitle;
    private String workPredictTime;
    private String rowNum;
    private String workRealStartTime;
    private boolean listAllTeams;
}
