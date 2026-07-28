package com.app.work.main.controller;

import com.app.work.domain.vo.ListVO;
import com.app.work.main.service.MainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping("board")
    public String main(ListVO listVO, Model model){

        try {
            String toDay = listVO.getToDay();
            String toDay2 = mainService.findToDay(toDay);
            model.addAttribute("toDay", toDay2);

            List<ListVO> mainList =mainService.findMainList(listVO);
            model.addAttribute("mainList", mainList);
            List<ListVO> vacationList = mainService.findVacationMember(listVO);
            model.addAttribute("vacationList", vacationList);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "main";
    }

}
