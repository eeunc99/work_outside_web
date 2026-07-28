package com.app.work.mapper;

import com.app.work.domain.Criteria;
import com.app.work.domain.dto.MemberDTO;
import com.app.work.mapper.member.MemberMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Slf4j
public class MemberMapperTest {

    @Autowired
    MemberMapper memberMapper;
    MemberDTO memberDTO;

//    @Before()
    @Test
    public void test(){
        log.info(String.valueOf(memberMapper.login("test2", "test2")));
    }

//    @Test
//    public void selectMemberTest() {
//        Criteria criteria = new Criteria();
//        log.info(memberMapper.selectMemberListWithPaging("해당없음", "", "테스트1", "Y", criteria).toString());
//    }

//    @Test
//    public void countMemberForPagingTest() {
//        Criteria criteria = new Criteria();
//        log.info(memberMapper.countMemberForPaging("해당없음", "", "", "Y", criteria) + "=======================");
//    }
}
