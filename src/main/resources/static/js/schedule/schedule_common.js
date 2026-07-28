const scheduleCommon = (function() {

    function scheduleList() {

        schedules.forEach(schedule => {
            let text = '';
            text += `
             <div class="list1"> 
             <input type="hidden" class="workNumber" value="${schedule.workNumber}"/> 
             <input type="hidden" class="work_start_time" value="${schedule.workStartTime}"> <!-- 일반 형식 업무 시작 시간 | 오전 09:00-->
             <input type="hidden" class="work_start_time2" value="${schedule.workStartTime2}"> <!-- DB 형식의 업무 시작 시간 | 2024-01-01 00:00 --> 
             <input type="hidden" class="work_end_time2" value="${schedule.workEndTime2 != null ? schedule.workEndTime2 : ''}"/> <!-- DB 형식의 업무 끝나는 시간 | 2024-01-01 00:00 -->
             <input type="hidden" class="work_real_start_time workBtn" value="${schedule.workRealStartTime != null ? schedule.workRealStartTime : ''}"/> <!-- 업무 시작 버튼 누른 시간 --> 
             <input type="hidden" class="work_real_start_time2" value="${schedule.workRealStartTime2}"/> <!-- 자동 업무 시작 시간 -->
             <input type="hidden" class="work_predict_time" value="${schedule.workPredictTime != null ? schedule.workPredictTime : ''}"> <!-- 업무 종료 예상 시간 -->  
             <input type="hidden" class="real_work_predict_time" value="${schedule.realWorkPredictTime != null ? schedule.realWorkPredictTime : ''}"> <!-- 실제 업무 끝난 시간 --> 
             <input type="hidden" class="work_detail" value="${schedule.workDetail}">`;
            if (`${schedule.workStatus}` == '이동중') {
                text += `<li class="point1">●</li>`;
            } else if (`${schedule.workStatus}` == '진행중') {
                text += `<li class="point1" style="color: #F06292;">●</li>`;
            } else if (`${schedule.workStatus}` == '종료'){
                text += `<li class="point2 point">●</li>`;
            }
            text += ` 
            <li class="username"><span>${schedule.memberName}</span><span>${schedule.memberRank + '님'}</span></li> `;
            if(`${schedule.workStatus}` == '이동중') {
                text += `<li class="usertime"> <span class="workST">${schedule.workStartTime}</span> ~ `;
            } else if(`${schedule.workStatus}` == '진행중' || `${schedule.workStatus}` == '종료'){
                text += `<li class="usertime"> <span class="workST">${schedule.workRealStartTime3}</span> ~ `;
            }

            if(`${schedule.workStatus}` == '이동중' || `${schedule.workStatus}` == '진행중') {
                text += `<span class="workET">${schedule.workPredictTime}</span>`; <!-- 업무 종료 예상 시간 -->
            }
            else {
                text += `<span class="workET">${schedule.workEndTime}</span>`;
            }
            text += `    
                    </li>
                    <li class="ow"><span>${schedule.workPlaceName}</span>
                        <input type="hidden" class="workNum" value="${schedule.workPlaceNumber}"/>
                    </li>
                    <li class="work"><span class="workDT">${schedule.workTitle}</span></li>
                    <input type="hidden" value="${schedule.workStatus}" class="work-status"/>
                    <li class="btn">
                `;
            if (`${schedule.workStatus}` == '이동중') {
                text += `<button class="wo-submit workBtn" type="button">업무시작</button>`;
            } else if (`${schedule.workStatus}` == '진행중') {
                text += `<button class="wo-submit" type="button">완료</button>`;
            }
            text += `</div>`;
            $('.work-list').append(text);
        });
    }

    function scheduleList2() {
        let previous = []; // 이전 날짜 리스트
        let todays = [];   // 당일 날짜 리스트
        let upcoming = []; // 이후 날짜 리스트

        // 오늘 날짜 가져오기
        let today = new Date($(".endTime").val());
        today.setHours(0, 0, 0, 0); // 시간 부분을 0으로 설정

        schedules.forEach(schedule => {
            const workStartTime2 = new Date(schedule.workStartTime2); // DB 형식의 업무 시작 시간
            workStartTime2.setHours(0, 0, 0, 0); // 시간 부분을 0으로 설정하여 날짜 비교

            // 일정 분류
            if (workStartTime2 < today) {
                previous.push(schedule); // 이전 날짜 리스트에 추가
            } else if (workStartTime2.toDateString() === today.toDateString()) {
                todays.push(schedule); // 오늘 일정
            } else {
                upcoming.push(schedule); // 이후 일정
            }
        });

        if (previous.length == 0){
            $(".previous").css('display', 'none'); // 이전 일정 타이틀 숨기기
        }
        // 이전 날짜 리스트 렌더링
        else if (previous.length > 0) {
            previous.forEach(schedule => {
                let text = createScheduleHTML(schedule);
                $('.work-list.previous .toggle-content').append(text); // 이전 일정 리스트에 추가
            });
        }

        if (todays.length === 0 ) {
            $(".todays").css('display', 'none'); // 이전 일정 타이틀 숨기기
        }
        // 오늘 일정 렌더링
        else if (todays.length > 0) {
            todays.forEach(schedule => {
                let text = createScheduleHTML(schedule);
                $('.work-list.todays').append(text); // 당일 일정 리스트에 추가
            });
        }

        if(upcoming.length === 0 ) {
            $(".upcoming").find('.toggle-title').css('display', 'none'); // 이후 일정 타이틀 숨기기
        }
        // 이후 날짜 리스트 렌더링
        else if (upcoming.length > 0) {
            upcoming.forEach(schedule => {
                let text = createScheduleHTML(schedule);
                $('.work-list.upcoming .toggle-content').append(text); // 이후 일정 리스트에 추가
            });
        }

        // 접기/펼치기 기능 추가
        $('.toggle-title').click(function() {
            let $content = $(this).next('.toggle-content'); // 다음 요소
            $content.slideToggle(); // 해당 리스트 토글
            $(this).toggleClass('expanded'); // expanded 클래스 추가/제거
        });
    }

    function createScheduleHTML(schedule) {
        let isNightRest = `${schedule.workStatus}` === '야간휴식';
        let text = '';
        text += `
        <div class="list1${isNightRest ? ' night-rest' : ''}" style="margin-top: 10px;${isNightRest ? ' cursor: default;' : ''}">
            <input type="hidden" class="workNumber" value="${schedule.workNumber}"/>
            <input type="hidden" class="work_start_time" value="${schedule.workStartTime}">
            <input type="hidden" class="work_start_time2" value="${schedule.workStartTime2}">
            <input type="hidden" class="work_end_time2" value="${schedule.workEndTime2 != null ? schedule.workEndTime2 : ''}"/>
            <input type="hidden" class="work_real_start_time workBtn" value="${schedule.workRealStartTime != null ? schedule.workRealStartTime : ''}"/>
            <input type="hidden" class="work_real_start_time2" value="${schedule.workRealStartTime2}"/>
            <input type="hidden" class="work_predict_time" value="${schedule.workPredictTime != null ? schedule.workPredictTime : ''}">
            <input type="hidden" class="real_work_predict_time" value="${schedule.realWorkPredictTime != null ? schedule.realWorkPredictTime : ''}">
            <input type="hidden" class="work_detail" value="${schedule.workDetail}">`;

        if (isNightRest) {
            text += `<li class="point5">●</li>`;
        } else if (`${schedule.workStatus}` === '이동중') {
            text += `<li class="point1">●</li>`;
        } else if (`${schedule.workStatus}` === '진행중') {
            text += `<li class="point1" style="color: #F06292;">●</li>`;
        } else if (`${schedule.workStatus}` === '종료') {
            text += `<li class="point2 point">●</li>`;
        }

        text += `
        <li class="username"><span>${schedule.memberName}</span><span>${schedule.memberRank + '님'}</span></li>`;

        if (`${schedule.workStatus}` === '이동중' || isNightRest) {
            text += `<li class="usertime"> <span class="workST">${schedule.workStartTime}</span> ~ `;
        } else {
            text += `<li class="usertime"> <span class="workST">${schedule.workRealStartTime3}</span> ~ `;
        }

        if (`${schedule.workStatus}` === '이동중' || `${schedule.workStatus}` === '진행중' || isNightRest) {
            text += `<span class="workET">${schedule.workPredictTime}</span>`;
        } else {
            text += `<span class="workET">${schedule.workEndTime}</span>`;
        }

        if (isNightRest) {
            text += `
            </li>
            <li class="ow"></li>
            <li class="work"><span class="workDT">${schedule.workTitle}</span></li>
            <input type="hidden" value="${schedule.workStatus}" class="work-status"/>
            <li class="btn">
                <button class="night-rest-edit" type="button" title="수정">✎</button>
                <button class="night-rest-del" type="button" title="삭제">✕</button>
            </li>`;
        } else {
            text += `
            </li>
            <li class="ow"><span>${schedule.workPlaceName}</span>
                <input type="hidden" class="workNum" value="${schedule.workPlaceNumber}"/>
            </li>
            <li class="work"><span class="workDT">${schedule.workTitle}</span></li>
            <input type="hidden" value="${schedule.workStatus}" class="work-status"/>
            <li class="btn">`;

            if (`${schedule.workStatus}` === '이동중') {
                text += `<button class="wo-submit workBtn" type="button">업무시작</button>`;
            } else if (`${schedule.workStatus}` === '진행중') {
                text += `<button class="wo-submit" type="button">완료</button>`;
            }

            text += `</li>`;
        }

        text += `</div>`;
        return text;
    }


    //일정 완료하기
    function scheduleSubmit(){
        $('.wo-list').on('click','.wo-submit',function (){
            let workNumber = $(this).closest('.list1').find('.workNumber').val();
            let memberNumber = $('.memberNumber').val();
            let value2 = $(this).text();

            // ----------------------------------------------------------------------
            let workST2 = $(this).closest('.list1').find('.work_start_time2').val();
            let workRealST2 = $(this).closest('.list1').find('.work_start_time').val();
            let workRealST3 = $(this).closest('.list1').find('.work_start_time2').val();

            let now = new Date();
            let hours = now.getHours();
            let minute = now.getMinutes();

            if(hours < 10) {
                hours = '0' + hours;
            }
            if(minute < 10) {
                minute = '0' + minute;
            }

            let dateStr2 = now.getFullYear() + '-' + (now.getMonth() + 1) + '-' + now.getDate();
            let timestamp2 = dateStr2 + ' ' + hours + ':' + minute + ':00.000';

            // ----------------------------------------------------------------------

            if(value2 == '업무시작'){
                $.ajax({
                    url: '/schedules/status',
                    type: 'post',
                    data: {
                        workNumber: workNumber,
                        memberNumber: memberNumber,
                        workStatus: value2,
                        workRealStartTime : timestamp2
                    },
                    success: function () {
                        location.reload();
                    },
                    error: function () {
                        console.log("실패");
                    }
                });
            } else if(value2 == '완료') {
                if(new Date(workRealST2) >= new Date(timestamp2)){
                    alert("종료 시간이 시작 시간보다 작거나 같습니다.");
                    $('.modal-container-edit').css("display", "none");
                    return false;
                }

                let timestamp = workST2 + ':00.000';
                let ts = workRealST3 + ':00.000';

                let workStartTime = new Date(timestamp);
                let workRST = new Date(ts);
                let workEndTime2 = $('.endTime').val();

                let workRTime = new Date(ts);
                let workEndTime = new Date(workEndTime2);

                let workSpentTime2 = workEndTime.getTime() - workRST.getTime();
                let minutes = workSpentTime2 / (1000*60);

                let workRealStartTime = $(this).closest('.list1').find('.work_real_start_time').val();
                let timestamp3 = workRealStartTime + '.000';
                $.ajax({
                    url: '/schedules/status',
                    type: 'post',
                    data: {
                        workNumber: workNumber,
                        memberNumber: memberNumber,
                        workStatus: value2,
                        workSpentTime : minutes,
                        workRealStartTime : timestamp3
                    },
                    success: function () {
                        $('.modal-container-edit').css("display", "none");
                        location.reload();
                    },
                    error: function () {
                        console.log("실패");
                    }
                });
            }
        });
    }

    // 이동중 일정 삭제 (등록 모달을 재사용한 수정 모달의 '취소' 버튼을 '삭제'로 재활용) - 진행중과 동일하게 삭제 확인 모달을 띄우고 scheduleDelete()가 처리
    function movingScheduleDelete(){
        $('.modal-container-add').on('click', '.cal-btn', function () {
            if (!$(this).hasClass('moving-del-mode')) return;
            let workNumber = $(this).data('workNumber');
            if (!workNumber) return;
            $(".modal-container").eq(2).css("display", "flex");
            $('.del-number').val(workNumber);
        });
    }

    function scheduleCreate(){
        // 일정 추가
        $('.modal-container-add').on('click', '.add-btn' ,function (){
            let workStartTime = $('.workStartTime').val();
            let workEndTime = $('.workEndTime').val();
            let workTitle = $('#workTitle').val();
            let workDetail = $('#workDetail').val();
            let memberNumber =$('.memberNumber').val();
            let workPlaceNumber = $('.select-selected').attr('data-value');
            let workRealStartTime =  $('.workAutoCheck').val();
            let editWorkNumber = $('.editWorkNumber').val();
            //----------------------------------------------------------------------------
            // 등록시 조건 확인
            if(!workEndTime) {
                alert("근무 예상 종료시간을 입력해주세요.");
                $(".modal-container-add").eq(0).css("display", "flex");
                $(".modal-container-add").eq(0).find('.workEndTime').focus();
                return false;
            }

            if (new Date(workEndTime) <= new Date(workStartTime)) {
                alert("종료 시간이 시작 시간보다 작거나 같습니다.");
                $(".modal-container-add").eq(0).css("display", "flex");
                return false;
            }

            if (!workPlaceNumber) {
                alert("근무지를 선택해주세요.");
                $(".modal-container-add").eq(0).css("display", "flex");
                return false;
            }

            if(!workTitle) {
                alert("근무 제목을 입력해주세요.");
                $(".modal-container-add").eq(0).css("display", "flex");
                $(".modal-container-add").eq(0).find('#workTitle').focus();
                return false;
            }

            if(!workDetail) {
                alert("근무 내용을 입력해주세요.");
                $(".modal-container-add").eq(0).css("display", "flex");
                $(".modal-container-add").eq(0).find('.workDetail').focus();
                return false;
            }
            //----------------------------------------------------------------------------
            //휴가 등록 되어 있고 새로운 일정 등록하기 전 시간 비교 (복수 휴가 지원)
            let vacationCnt = parseInt($("#vacationCnt").val()) || 0;
            if (vacationCnt >= 1) {
                let overlap = false;
                $('.list3').each(function() {
                    let vCheck = $(this).data('vacation-check');
                    let vSt    = $(this).data('vacation-st');
                    if (vCheck === "H") {
                        if (new Date(workStartTime).getTime() === new Date(vSt).getTime()) {
                            overlap = true;
                            return false;
                        }
                    } else if (vCheck === "O") {
                        if (new Date(workStartTime).getDate() === new Date(vSt).getDate()
                            && new Date(workStartTime).getMonth() === new Date(vSt).getMonth()
                            && new Date(workStartTime).getFullYear() === new Date(vSt).getFullYear()) {
                            overlap = true;
                            return false;
                        }
                    }
                });
                if (overlap) {
                    alert("등록할 일정 날짜가 등록된 휴가 날짜와 겹칩니다.");
                    $(".modal-container-add").eq(0).css("display", "flex");
                    return false;
                }
            }

            //----------------------------------------------------------------------------
            let workSpentTime = '진행중';
            let value = '이동중';

            if (editWorkNumber) {
                $.ajax({
                    url: '/schedules/update2',
                    type: 'patch',
                    data: {
                        workStartTime: workStartTime,
                        workEndTime: null,
                        workPlaceNumber: workPlaceNumber,
                        workDetail: workDetail,
                        workNumber: editWorkNumber,
                        workStatus: value,
                        workSpentTime: workSpentTime,
                        workPredictTime: workEndTime,
                        workTitle: workTitle,
                        workRealStartTime: workRealStartTime
                    },
                    success: function () {
                        location.reload();
                    },
                    error: function () {
                        console.log("실패");
                    }
                });
                return;
            }

            $.ajax({
                url: '/schedules/sdAdd',
                type: 'post',
                data: {
                    workStartTime: workStartTime,
                    workDetail: workDetail,
                    workPlaceNumber: workPlaceNumber,
                    memberNumber: memberNumber,
                    workStatus: value,
                    workSpentTime: workSpentTime,
                    workTitle : workTitle,
                    workPredictTime : workEndTime,
                    workRealStartTime : workRealStartTime
                },
                success: function () {
                    location.reload();
                },
                error: function () {
                    console.log("실패");
                }
            });

        });
     }


    // 야간 후 휴식 삭제
    function nightRestDelete() {
        $('.wo-list').on('click', '.night-rest-del', function(e) {
            e.stopPropagation();
            if (!confirm('야간 근무 후 휴식 일정을 삭제하시겠습니까?')) return;
            $.ajax({
                url: '/schedules/nightRestDelete',
                type: 'delete',
                success: function() { location.reload(); },
                error: function() { console.log("야간 후 휴식 삭제 실패"); }
            });
        });
    }

    // 야간 후 휴식 수정
    function nightRestEdit() {
        $('.wo-list').on('click', '.night-rest-edit', function(e) {
            e.stopPropagation();
            let card = $(this).closest('.list1');
            let stDate = card.find('.work_start_time2').val();
            let endDate = card.find('.real_work_predict_time').val();

            $('#nr_edit_st_date').val(stDate);
            $('#nr_edit_end_date').val(endDate);

            $(function() {
                $(".datetimepicker").datetimepicker({
                    format: "Y-m-d H:i",
                    step: 60 // 1시간 단위
                });
            });

            $('.modal-container-nr-edit').css('display', 'flex');
        });

        $('.modal-container-nr-edit').on('click', '.nr-edit-save-btn', function() {
            let stDate = $('#nr_edit_st_date').val();
            let endDate = $('#nr_edit_end_date').val();

            if (!stDate || !endDate) {
                alert("시간을 입력해주세요.");
                return false;
            }
            if (new Date(endDate) <= new Date(stDate)) {
                alert("종료 시간이 시작 시간보다 작거나 같습니다.");
                return false;
            }

            $.ajax({
                url: '/schedules/nightRestEdit',
                type: 'patch',
                data: {
                    vacationStDate: stDate + ':00.000',
                    vacationEndDate: endDate + ':00.000'
                },
                success: function() { location.reload(); },
                error: function() { console.log("야간 후 휴식 수정 실패"); }
            });
        });

        $('.modal-container-nr-edit').on('click', '.nr-edit-cancel-btn', function() {
            $('.modal-container-nr-edit').css('display', 'none');
        });
    }

    // 이동중 일정 수정 - 등록 모달을 재사용해서 프리필 후 오픈
    function openMovingEditModal(card) {
        let workNumber = card.find('.workNumber').val();
        let workST = card.find('.work_start_time2').val(); // 계획된 시작 시간
        let workPT = card.find('.real_work_predict_time').val(); // 예상 종료 시간 (YYYY-MM-DD HH:mm)
        let workRealStartTime = card.find('.work_real_start_time').val(); // 자동 시작 트리거 시간
        let workNum = card.find('.workNum').val();
        let workName = card.find('.ow').text();
        let workTitleText = card.find('.workDT').text();
        let workDetailText = card.find('.work_detail').val();

        let $modal = $('.modal-container-add').eq(0);
        $modal.find('.editWorkNumber').val(workNumber);
        $modal.find('.modal-title span').text('일정 수정');
        $modal.find('.add-btn').text('수정');
        $modal.find('.cal-btn').text('삭제').addClass('moving-del-mode').data('workNumber', workNumber);
        $modal.find('#workStartTime').val(workST);
        $modal.find('.workEndTime').val(workPT);
        $modal.find('.select-selected').text(workName).attr('data-value', workNum);
        $modal.find('#workTitle').val(workTitleText);
        $modal.find('#workDetail').val(workDetailText);

        if (workRealStartTime) {
            $modal.find('#workAuto').prop('checked', true).trigger('click');
            $modal.find('.workAutoCheck').val(workRealStartTime);
            $modal.find('.backBtn').hide();
        }

        $(function() {
            $(".datetimepicker").datetimepicker({
                format: "Y-m-d H:i",
                step: 10
            });
        });

        $modal.css("display", "flex");
    }

    // 일정 업데이트(데이터 넣어주기 및 수정)
    function scheduleUpdate() {
        $('.wo-list').on('click', '.list1:not(.night-rest)', function (){
            let workSS = $(this).closest('.list1').find('.work-status').val();
            if (workSS === '이동중') {
                openMovingEditModal($(this).closest('.list1'));
                return;
            }
            // ----------------------------------------------------------------------
            // 해당 리스트의 시작 시간 데이터 넣어주기
            let workST2 = $(this).closest('.list1').find('.work_start_time2').val();
            let workRealStartTime2 = $(this).closest('.list1').find('.work_real_start_time2').val();
            $('#workStartTime2').val(workRealStartTime2 === 'null' ? workST2 : workRealStartTime2);
            // 해당 리스트의 끝나는 시간 데이터 넣어주기
            let workET = $(this).closest('.list1').find('.work_end_time2').val();
            $('#workEndTime2').val(workET);
            // 해당 리스트의 끝나는 시간 데이터 넣어주기
            let workPT = $(this).closest('.list1').find('.real_work_predict_time').val();
            $('#workPredictTime').val(workPT);
            // 해당 리스트의 근무지 번호 넣어주기(근무지 이름 포함)
            let workNum = $(this).closest('.list1').find('.workNum').val();
            let workName = $(this).closest('.list1').find('.ow').text();
            $('#work1').val(workNum);
            $('#work1').text(workName);
            // 해당 리스트의 근무 제목 넣어주기
            let workText = $(this).closest('.list1').find('.workDT').text();
            $('.work-title').text(workText);
            // 해당 리스트의 근무 내용 넣어주기
            workText = $(this).closest('.list1').find('.work_detail').val();
            $('.workDT2').text(workText);
            // 해당 리스트의 일정 번호 가져오기
            let workNumber = $(this).closest('.list1').find('.workNumber').val();
            // 해당 리스트의 업무 시작 시간 가져오기
            let workRealStartTime = $(this).closest('.list1').find('.work_real_start_time').val();
            $('#workRealStartTime').val(workRealStartTime);

            let submit = $(this).closest('.list1').find('.wo-submit').text();

            console.log(workST2);
            console.log(workRealStartTime2);

            //----------------------------------------------------------------------------
            // 해당 리스트의 상태값에 따라 진행중 체크 표시
            if (workSS == '이동중' || workSS == '진행중'){
                $('.workIng').prop('checked', true).change();
            }
            else if(workSS == '종료'){
                $('.workIng').prop('checked', false).change();
            }

            $(function() {
                $(".datetimepicker").datetimepicker({
                    format: "Y-m-d H:i",
                    step: 10
                });
            });

            //----------------------------------------------------------------------------
            // 진행중이 클릭되었을 때 체크 해제되면 끝나는 시간에 현재시간 넣고, 체크되면 시간 없애기
            $('.workIng').on('click', function() {
                let now = new Date();

                let hours = ('0' + now.getHours()).slice(-2);
                let minutes = ('0' + now.getMinutes()).slice(-2);
                let currentTime = hours + ":" + minutes;

                let endTime = $('.endTime').val();

                if($(this).is(':checked')) {
                    $(this).parents('.modal-container').find('[name=workStartTime2]').val(workST2);
                    $(this).parents('.modal-container').find('input[name=workEndTime]').val('');
                    $(this).parents('.modal-container').find('input[name=workRealStartTime]').val(workRealStartTime);
                } else {
                    $(this).parents('.modal-container').find('input[name=workRealStartTime]').val(workRealStartTime);
                    $(this).parents('.modal-container').find('input[name=workEndTime]').val(endTime);
                }
            });

            // 끝나는 시간을 선택했을 때 진행중 체크 해제.
            $('input[name=workEndTime]').on('change', function() {
                $(this).parents('.modal-container').find('.workIng').prop('checked', false);
            });
            //----------------------------------------------------------------------------
            // 버튼을 눌렀을 때
            $('.edit-btn').on('click', function (){
                // 일정 수정 모달의 근무지 select 박스 값 가져오기
                let workPlaceNumber = $('#work1').val();
                // 일정 수정 모달의 근무 제목 값
                let workTitle = $('.work-title').val();
                // 일정 수정 모달의 근무 내용 값
                let workDT2 = $('.workDT2').val();
                // 일정 수정 모달의 시작 시간 값
                let workST2 = $('#workStartTime2').val();
                // 일정 수정 모달의 끝나는 시간 값
                let workET2 = $('#workEndTime2').val();
                // 일정 수정 모달의 근무 예상종료시간 값
                let workPredictTime = $('#workPredictTime').val();
                // 모든 시간 계산은 workRealStartTime으로 함.
                let workRealStartTime = $('.workRealStartTime').val();

                if(!workPredictTime) {
                    alert("근무 예상종료시간을 입력해주세요.");
                    $(".modal-container-edit").eq(1).css("display", "flex");
                    $(".modal-container-edit").eq(1).find('.workEndTime').focus();
                    return false;
                }

                if(!workPlaceNumber) {
                    alert("근무지를 입력해주세요.");
                    $(".modal-container-edit").eq(1).css("display", "flex");
                    return false;
                }

                if(!workTitle) {
                    alert("근무 제목을 입력해주세요.");
                    $(".modal-container-edit").eq(1).css("display", "flex");
                    $(".modal-container-edit").eq(1).find('.work-title').focus();
                    return false;
                }

                if(!workDT2) {
                    alert("근무 내용을 입력해주세요.");
                    $(".modal-container-edit").eq(1).css("display", "flex");
                    $(".modal-container-edit").eq(1).find('.workDT2').focus();
                    return false;
                }

                // 일정 수정 모달의 시작 시간 값 DB 형식이랑 맞춰주기
                let timestamp = workRealStartTime + ':00.000';

                // 일정 수정 모달의 끝나는 시간 값 DB 형식이랑 맞춰주기
                let timestamp2 = workET2 + ':00.000';

                workPredictTime = workPredictTime + ":00.000";

                // ----------------------------------------------------------------------
                if (timestamp2 < timestamp) {
                    console.log(timestamp);
                    console.log(timestamp2);
                    alert("종료 시간이 시작 시간보다 작거나 같습니다.");
                    return false;
                } else if (workPlaceNumber == ''){
                    alert("근무지를 선택해주세요.");
                    return false;
                }
                //----------------------------------------------------------------------------

                if($('.workIng').is(":checked") && submit == '업무시작'){
                    let value = '이동중';
                    let workET = null;
                    let spentTime = '진행중';

                    $.ajax({
                        url : '/schedules/update2',
                        type : 'patch',
                        data : {
                            workStartTime : timestamp,
                            workEndTime : workET,
                            workPlaceNumber : workPlaceNumber,
                            workDetail : workDT2,
                            workNumber : workNumber,
                            workStatus : value,
                            workSpentTime : spentTime,
                            workPredictTime: workPredictTime,
                            workTitle : workTitle
                        },
                        success : function (){
                            console.log("성공");
                            location.reload();
                        },
                        error : function (){
                            console.log("실패");
                        }
                    });
                }
                else if($('.workIng').is(":checked")) {
                    let value = '진행중';
                    let workET = null;
                    let spentTime = '진행중';

                    $.ajax({
                        url : '/schedules/update2',
                        type : 'patch',
                        data : {
                            workStartTime : timestamp,
                            workEndTime : workET,
                            workPlaceNumber : workPlaceNumber,
                            workDetail : workDT2,
                            workNumber : workNumber,
                            workStatus : value,
                            workSpentTime : spentTime,
                            workPredictTime: workPredictTime,
                            workTitle : workTitle,
                            workRealStartTime : workRealStartTime
                        },
                        success : function (){
                            console.log("성공");
                            location.reload();
                        },
                        error : function (){
                            console.log("실패");
                        }
                    });
                }
                else if(!$('.workIng').is(":checked")) {
                    let workStatusEnd = '종료';
                    let workStartTime = new Date(timestamp);
                    let workEndTime = new Date(timestamp2);
                    let workSpentTime = workEndTime.getTime() - workStartTime.getTime();
                    let minutes = workSpentTime / (1000 * 60);

                    console.log(minutes);

                    $.ajax({
                        url : '/schedules/update',
                        type : 'patch',
                        data : {
                            workStartTime : timestamp,
                            workEndTime : timestamp2,
                            workPlaceNumber : workPlaceNumber,
                            workDetail : workDT2,
                            workNumber : workNumber,
                            workStatus : workStatusEnd,
                            workSpentTime : minutes,
                            workPredictTime: workPredictTime,
                            workTitle : workTitle,
                            workRealStartTime : workRealStartTime
                        },
                        success : function (){
                            console.log("성공?");
                            location.reload();
                        },
                        error : function (){
                            console.log("실패");
                        }
                    });
                }
            });
            $('.del-btn').on('click', function (){
                $(".modal-container").eq(2).css("display", "flex");
                $('.del-number').val(workNumber);
            });

        });
    }

    // 일정 삭제
    function scheduleDelete(){
        $('.modal-container-del').on('click', '.del-btn2', function (){
            let workNumber = $('.del-number').val();

            $.ajax({
                url : '/schedules/delete',
                type : 'delete',
                data : {
                    workNumber : workNumber
                },
                success : function (){
                    location.reload();
                },
                error : function (){
                    console.log("실패");
                }
            });

        });
    }

    function vacationCheck() {
        $(".only_vacation").hide();
        $(".half_vacation").hide();
        $(".night_rest_section").hide();

        $('#only_vacation').on("click", function () {
            $(".only_vacation").show();
            $(".half_vacation").hide();
            $(".night_rest_section").hide();
            $("#vacation_st_date2").val("");
            $("#vacation_end_date2").val("");
            $("#night_rest_st_date").val("");
            $("#night_rest_end_date").val("");
            $('#half_vacation').prop('checked', false);
            $('#night_rest').prop('checked', false);
        });
        $("#half_vacation").on("click", function() {
            $(".only_vacation").hide();
            $(".half_vacation").show();
            $(".night_rest_section").hide();
            $("#vacation_st_date").val("");
            $("#vacation_end_date").val("");
            $("#night_rest_st_date").val("");
            $("#night_rest_end_date").val("");
            $('#only_vacation').prop('checked', false);
            $('#night_rest').prop('checked', false);

            $(function() {
                $(".datetimepicker").datetimepicker({
                    format: "Y-m-d H:i",
                    step: 100,
                    onClose: function(selectedDate) {
                        workAutoValue = moment(selectedDate).format("YYYY-MM-DD HH:mm");
                        $('.workAutoCheck').val(workAutoValue);
                    }
                });
            });
        });
        $("#night_rest").on("click", function() {
            $(".only_vacation").hide();
            $(".half_vacation").hide();
            $(".night_rest_section").show();
            $("#vacation_st_date").val("");
            $("#vacation_end_date").val("");
            $("#vacation_st_date2").val("");
            $("#vacation_end_date2").val("");
            $('#only_vacation').prop('checked', false);
            $('#half_vacation').prop('checked', false);

            $(function() {
                $(".datetimepicker").datetimepicker({
                    format: "Y-m-d H:i",
                    step: 60 // 1시간 단위
                });
            });
        });
    }

    function vacation() {
        // 휴가 등록
        $('.modal-container-v').on('click', '.v-add-btn' ,function (){
            let memberNumber =$('.memberNumber').val();

            if(!$('#only_vacation').is(":checked") && !$('#half_vacation').is(":checked") && !$('#night_rest').is(":checked")) {
                alert("하루 휴가, 반차 또는 야간 후 휴식을 선택해주세요");
                $(".modal-container-v").eq(0).css("display", "flex");
                return false;
            }

            if($('#only_vacation').is(":checked")) {
                if (parseInt($("#vacationCnt").val()) >= 3) {
                    alert("휴가(연차/반차)는 최대 3개까지 등록 가능합니다.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                let vacationStDate = $('#vacation_st_date').val();
                let vacationEndDate = $('#vacation_end_date').val();

                // 연차일 경우 1시간 배치를 생각하여 종료 날짜 시간은 23시로 설정한다.
                let timestamp = vacationStDate + ' 00:00:00.000';
                let timestamp2 = vacationEndDate + ' 23:00:00.000';

                if (new Date(vacationEndDate) < new Date(vacationStDate)) {
                    alert("종료 날짜가 시작 날짜보다 작습니다.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                if (!vacationStDate) {
                    alert("날짜를 선택해주세요");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                $.ajax({
                    url: '/schedules/vacationAdd',
                    type: 'post',
                    data: {
                        vacationStDate: timestamp,
                        vacationEndDate : timestamp2,
                        memberNumber: memberNumber,
                        vacationCheck : "O" // 연차(하루종일)
                    },
                    success: function () {
                        location.reload();
                    },
                    error: function () {
                        console.log("실패");
                    }
                });
            }

            if($('#half_vacation').is(":checked")) {
                if (parseInt($("#vacationCnt").val()) >= 3) {
                    alert("휴가(연차/반차)는 최대 3개까지 등록 가능합니다.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                let vacationStDate = $('#vacation_st_date2').val();
                let vacationEndDate = $('#vacation_end_date2').val();

                let timestamp = vacationStDate + ':00.000';
                let timestamp2 = vacationEndDate + ':00.000';

                if (new Date(vacationEndDate) < new Date(vacationStDate)) {
                    alert("종료 날짜가 시작 날짜보다 작습니다.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                if (!vacationStDate || !vacationEndDate) {
                    alert("날짜를 선택해주세요");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                $.ajax({
                    url: '/schedules/vacationAdd',
                    type: 'post',
                    data: {
                        vacationStDate: timestamp,
                        vacationEndDate : timestamp2,
                        memberNumber: memberNumber,
                        vacationCheck : "H" //반차
                    },
                    success: function () {
                        location.reload();
                    },
                    error: function () {
                        console.log("실패");
                    }
                });

            }

            if($('#night_rest').is(":checked")) {
                // 이미 등록된 야간 후 휴식이 있는지 확인
                let hasNightRest = schedules.some(function(s) { return s.workStatus === '야간휴식'; });
                if (hasNightRest) {
                    alert("이미 등록된 야간 후 휴식이 있습니다.\n기존 야간 후 휴식을 수정하거나 삭제 후 다시 등록해주세요.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                let nightRestStDate = $('#night_rest_st_date').val();
                let nightRestEndDate = $('#night_rest_end_date').val();

                if (!nightRestStDate || !nightRestEndDate) {
                    alert("야간 후 휴식 시간을 입력해주세요.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                if (new Date(nightRestEndDate) <= new Date(nightRestStDate)) {
                    alert("종료 시간이 시작 시간보다 작거나 같습니다.");
                    $(".modal-container-v").eq(0).css("display", "flex");
                    return false;
                }

                let timestamp = nightRestStDate + ':00.000';
                let timestamp2 = nightRestEndDate + ':00.000';

                $.ajax({
                    url: '/schedules/vacationAdd',
                    type: 'post',
                    data: {
                        vacationStDate: timestamp,
                        vacationEndDate: timestamp2,
                        memberNumber: memberNumber,
                        vacationCheck: "N"
                    },
                    success: function() { location.reload(); },
                    error: function() { console.log("야간 후 휴식 등록 실패"); }
                });
            }
        });

        vacationEdit();
    }

    // 휴가 수정 (vacation_number는 work_mgmt_modal.js의 .list3 클릭 시 #editVacationNumber에 저장됨)
    function vacationEdit() {
        $('#only_vacation2').on("click", function () {
            $(".only_vacation2").show();
            $(".half_vacation2").hide();
            $('#half_vacation2').prop('checked', false);
        });
        $("#half_vacation2").on("click", function() {
            $(".only_vacation2").hide();
            $(".half_vacation2").show();
            $('#only_vacation2').prop('checked', false);

            $(".datetimepicker").datetimepicker({
                format: "Y-m-d H:i",
                step: 100,
                onClose: function(selectedDate) {
                    workAutoValue = moment(selectedDate).format("YYYY-MM-DD HH:mm");
                    $('.workAutoCheck').val(workAutoValue);
                }
            });
        });

        $('.modal-container-v-edit').on('click', '.v-edit-btn', function () {
            let vacationNumber = $('#editVacationNumber').val();

            if($("#only_vacation2").is(":checked")) {
                let vacationUpdateStDate = $("#vacation_st_date3").val();
                let vacationUpdateEndDate = $("#vacation_end_date3").val();
                let timestamp = vacationUpdateStDate + ' 00:00:00.000';
                let timestamp2 = vacationUpdateEndDate + ' 23:00:00.000';

                if (new Date(vacationUpdateEndDate) < new Date(vacationUpdateStDate)) {
                    alert("종료 날짜가 시작 날짜보다 작습니다.");
                    $(".modal-container-v-edit").eq(0).css("display", "flex");
                    return false;
                }

                $.ajax({
                    url: '/schedules/vacationEdit',
                    type: 'post',
                    data: {
                        vacationNumber: vacationNumber,
                        vacationStDate: timestamp,
                        vacationEndDate: timestamp2,
                        vacationCheck: "O"
                    },
                    success: function () { location.reload(); },
                    error: function () { console.log("실패"); }
                });
            } else if ($("#half_vacation2").is(":checked")) {
                let vacationUpdateStDate = $("#vacation_st_date4").val();
                let vacationUpdateEndDate = $("#vacation_end_date4").val();
                let timestamp = vacationUpdateStDate + ':00.000';
                let timestamp2 = vacationUpdateEndDate + ':00.000';

                if (new Date(vacationUpdateEndDate) < new Date(vacationUpdateStDate)) {
                    alert("종료 날짜가 시작 날짜보다 작습니다.");
                    $(".modal-container-v-edit").eq(0).css("display", "flex");
                    return false;
                }

                $.ajax({
                    url: '/schedules/vacationEdit',
                    type: 'post',
                    data: {
                        vacationNumber: vacationNumber,
                        vacationStDate: timestamp,
                        vacationEndDate: timestamp2,
                        vacationCheck: "H"
                    },
                    success: function () { location.reload(); },
                    error: function () { console.log("실패"); }
                });
            }
        });

        $('.v-del-btn').on('click', function (){
            $(".modal-container-v-del").eq(0).css("display", "flex");
        });
    }

    // 휴가 삭제 (vacation_number 기준)
    function vacationDelete(){
        $('.modal-container-v-del').on('click', '.del-v-btn2', function (){
            let memberNumber = $('.memberNumber').val();
            let vacationNumber = $('#editVacationNumber').val();

            $.ajax({
                url : '/schedules/vacationDelete',
                type : 'delete',
                data : {
                    vacationNumber : vacationNumber
                },
                success : function (){
                    $.ajax({
                        url: '/schedules/vacation',
                        type: 'patch',
                        data: {
                            memberNumber: memberNumber,
                            memberUse : "Y"
                        },
                        success: function () {},
                        error: function () { console.log("실패"); }
                    });
                    location.reload();
                },
                error : function (){
                    console.log("실패");
                }
            });
        });
    }

    // 자동 업무 시작 버튼
    function AutoCheck() {
        var workAutoValue = '';
        $('#workAuto').on('click', function() {
            // 체크박스가 클릭될 때마다 모달 내용 초기화
            let text = `
            <div class="modal-content-time-auto">
                <input type="text" class="workTime workAutoCheck datetimepicker" value="${workAutoValue}" name="workAutoCheck" readonly style="background-color: white;">
                <button type="button" class="backBtn modal-btn">돌아가기</button>
            </div>
            `;

            // datetimepicker 초기화
            $(function() {
                $(".datetimepicker").datetimepicker({
                    format: "Y-m-d H:i",
                    step: 10, // 10분 단위로 설정
                    onClose: function(selectedDate) {
                        // 선택된 날짜를 "YYYY-MM-DD HH:mm" 형식으로 변환
                        workAutoValue = moment(selectedDate).format("YYYY-MM-DD HH:mm");
                        $('.workAutoCheck').val(workAutoValue); // input 필드에 값 설정
                    }
                });
            });

            $('.checkboxoption').hide();
            $('.modal-content-workautocheck-select').append(text);
        });

        // 돌아가기 버튼 클릭 시
        $(document).on('click', '.backBtn', function() {
            $('.workAutoCheck').val("");
            $('.checkboxoption').show();
            $('#workAuto').prop('checked', false);
            $('.modal-content-time-auto').hide();
        });
    }

    // 이외 옵션들
    function option() {
        $('.plus-btn').on('click', function (){
            let $addModal = $('.modal-container-add').eq(0);
            $addModal.find('.editWorkNumber').val('');
            $addModal.find('.modal-title span').text('일정 등록');
            $addModal.find('.add-btn').text('등록');
            $addModal.find('.cal-btn').text('취소').removeClass('moving-del-mode').removeData('workNumber');
            $addModal.find('.backBtn').show();

            // 이전에 입력하다 취소했거나 수정 모달로 재사용된 값이 남지 않도록 초기화
            $addModal.find('.select-selected').text('근무지 선택').removeAttr('data-value');
            $addModal.find('#workTitle').val('');
            $addModal.find('#workDetail').val('');
            $addModal.find('.workEndTime').val('');
            $addModal.find('#workAuto').prop('checked', false);
            $addModal.find('.modal-content-time-auto').remove();
            $addModal.find('.checkboxoption').show();

            const offset = new Date().getTimezoneOffset() * 60000;
            const today = new Date(Date.now() - offset);
            $('#workStartTime').val(today.toISOString().slice(0, -8));

            $(function() {
                $(".datetimepicker").datetimepicker({
                    format: "Y-m-d H:i",
                    step: 10
                });
            });
        });

        $('.modal-btn').on('click', function (){
            $('.modal-container-add').css("display", "none");
        });

        $('.wo-list').on('click', '.list1' ,function (){
            let workSS = $(this).closest('.list1').find('.work-status').val();
            if (workSS === '이동중') return; // 이동중은 scheduleUpdate()에서 등록 모달을 대신 연다
            $(".modal-container").eq(1).css("display", "flex");
        });

        if(/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
            $('.main2').css('display', 'none');
        }

        // 근무지 검색 후 선택 (동적 추가 항목 포함)
        $(document).on('click', '.workplaceNum', function (event) {
            event.stopPropagation();
            let workValue = $(this).attr('value');
            let workName = $(this).find('span').text();
            let workplaceNumber = $(this).attr('data-id');

            $(this).closest('.workList1').find('.select-selected').text(workName);

            if (workValue) {
                $(this).closest('.workList1').find('.select-selected').attr('data-value', workValue);
            }

            $(this).closest('.workList1').find('.select-selected').attr('data-workplace-number', workplaceNumber);
            $(this).closest('.select-items').hide();
        });

        $(document).on('click', function(event) {
            // 클릭한 요소가 .select-selected 또는 .select-items가 아닐 경우
            if (!$(event.target).closest('.select-selected').length && !$(event.target).closest('.select-items').length) {
                $('.select-items').hide(); // 선택 옵션 숨김
            }
        });
    }

    // 근무지 검색
    function search() {
        $('.select-selected').on('click', function() {
            let $items = $(this).siblings('.select-items');
            $items.show();
            $items.find('.search-input').focus();
        });

        $(document).on('input', '.search-input', function() {
            let text = $(this).val().toUpperCase();
            let $listItems = $(this).closest('.select-items').find('.workplace-list-items');
            let $noResult = $(this).closest('.select-items').find('.no-result');
            let items = $listItems.find('.workplaceNum');
            let visibleCount = 0;

            items.each(function() {
                let optionText = $(this).find('span').text().toUpperCase();
                if (optionText.indexOf(text) > -1) {
                    $(this).show();
                    visibleCount++;
                } else {
                    $(this).hide();
                }
            });

            if (visibleCount === 0 && text !== '') {
                $noResult.show();
            } else {
                $noResult.hide();
            }
        });

        // 근무지 추가하기 버튼
        $(document).on('click', '.add-workplace-btn', function(event) {
            event.stopPropagation();
            let $selectItems = $(this).closest('.select-items');
            let workplaceName = $selectItems.find('.search-input').val().trim();
            if (!workplaceName) return;

            let $btn = $(this);
            $btn.prop('disabled', true).text('추가 중...');

            $.ajax({
                url: '/schedules/workplaceAdd',
                type: 'post',
                data: { workplaceName: workplaceName },
                success: function(data) {
                    // 새 항목을 목록에 추가
                    let newItem = $('<div></div>')
                        .attr('data-id', data.workplaceNumber)
                        .attr('value', data.workplaceNumber)
                        .addClass('workplaceNum')
                        .append($('<span></span>').text(data.workplaceName));
                    $selectItems.find('.workplace-list-items').append(newItem);

                    // 자동 선택
                    let $workList1 = $selectItems.closest('.workList1');
                    $workList1.find('.select-selected')
                        .text(data.workplaceName)
                        .attr('data-value', data.workplaceNumber);

                    // 검색창 초기화 후 닫기
                    $selectItems.find('.search-input').val('');
                    $selectItems.find('.workplaceNum').show();
                    $selectItems.find('.no-result').hide();
                    $selectItems.hide();
                },
                error: function() {
                    alert('근무지 추가에 실패했습니다.');
                    $btn.prop('disabled', false).text('+ 추가하기');
                }
            });
        });
    }


    return { scheduleList : scheduleList2, scheduleSubmit : scheduleSubmit , scheduleCreate : scheduleCreate , scheduleUpdate : scheduleUpdate , scheduleDelete : scheduleDelete ,  vacation : vacation , AutoCheck : AutoCheck, option : option, search : search, vacationCheck : vacationCheck, vacationDelete : vacationDelete, nightRestDelete : nightRestDelete, nightRestEdit : nightRestEdit, movingScheduleDelete : movingScheduleDelete };
})();