package com.app.work.domain.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WidgetTokenVO {
    private Integer tokenId;
    private Integer memberNumber;
    private String memberName;
    private String token;
    private String scope;
    private String label;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime revokedAt;
}
