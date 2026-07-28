$(document).ready( function (){
    document.cookie = `expires=Fri, 31 Dec 9999 23:59:59 GMT;`;
    
    // 야간 후 휴식은 카운트 무관하게 열리므로 버튼 레벨 체크 제거
    // 연차/반차 3개 제한은 schedule_common.js vacation() 내부에서 처리

});

$(function() {
    scheduleCommon.scheduleSubmit();
    scheduleCommon.scheduleUpdate();
    scheduleCommon.scheduleDelete();
    scheduleCommon.vacation();
    scheduleCommon.AutoCheck();
    scheduleCommon.scheduleCreate();
    scheduleCommon.scheduleList();
    scheduleCommon.option();
    scheduleCommon.search();
    scheduleCommon.vacationCheck();
    scheduleCommon.vacationDelete();
    scheduleCommon.nightRestDelete();
    scheduleCommon.nightRestEdit();
    scheduleCommon.movingScheduleDelete();
});