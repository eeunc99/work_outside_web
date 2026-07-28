package com.app.work.domain.dto;

import com.app.work.domain.Criteria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageDTO {
    private int pageStart;
    private int pageEnd;
    private boolean next;
    private boolean prev;
    private int total;
    private Criteria criteria;

    public PageDTO(Criteria criteria, int total) {
        this.criteria = criteria;
        this.total = total;
        this.pageEnd = (int)(Math.ceil(criteria.getPageNum() / 5.0)) * 5;
        this.pageStart = this.pageEnd - 4;

        int realEnd = (int)(Math.ceil(total * 1.0 / criteria.getAmount()));

        if(realEnd < pageEnd) {
            this.pageEnd = realEnd;
        }

        this.prev = this.pageStart > 1;
        this.next = this.pageEnd < realEnd;
    }
}
