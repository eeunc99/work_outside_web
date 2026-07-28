package com.app.work.mapper.widget;

import com.app.work.domain.vo.WidgetTokenVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WidgetTokenMapper {

    void insertToken(WidgetTokenVO widgetTokenVO);

    WidgetTokenVO selectByToken(String token);

    List<WidgetTokenVO> selectByMember(Integer memberNumber);

    List<WidgetTokenVO> selectAll();

    void updateRevoke(Integer tokenId);

    void updateLastUsed(Integer tokenId);
}
