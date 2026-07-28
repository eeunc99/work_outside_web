package com.app.work.domain.dao;

import com.app.work.domain.Criteria;
import com.app.work.domain.dto.WorkplaceDTO;
import com.app.work.mapper.workplace.WorkplaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WorkplaceDAO {

    private final WorkplaceMapper workplaceMapper;

    public List<WorkplaceDTO> selectWorkplaceList(String workplaceUse, Criteria criteria) {
        return workplaceMapper.selectWorkplaceList(workplaceUse, criteria);
    }

    public int selectWorkplaceCount(String workplaceUse) {
        return workplaceMapper.selectWorkplaceCount(workplaceUse);
    }

    public int selectWorkplace(String workplaceName) {
        return workplaceMapper.selectWorkplace(workplaceName);
    }

    public void saveWorkplace(String workplaceName) {
        workplaceMapper.saveWorkplace(workplaceName);
    }

    public void updateWorkplace(String workplaceName, String originName) {
        workplaceMapper.updateWorkplace(workplaceName, originName);
    }

    public void updateWorkplaceUse(String workplaceName, String workplaceUse) {
        workplaceMapper.updateWorkplaceUse(workplaceName, workplaceUse);
    }
}
