const listService = (function() {
    function appendLists(lists) {
        lists.forEach(workList => {
            let now = new Date();
            let hours = now.getHours();
            let minute = now.getMinutes();

            if(hours < 10) {
                hours = '0' + hours;
            }
            if(minute < 10) {
                minute = '0' + minute;
            }

            let dateStr = now.getFullYear() + '-' + (now.getMonth() + 1) + '-' + now.getDate();

            let timestamp = dateStr + ' ' + hours + ':' + minute + ':00.000';
            console.log(timestamp);

            let text = `
        <div class="list1" title="`;

            if(`${workList.workStatus}` == '진행중') {
                text += `작업중`;
            } else {
                text += `${workList.workStatus}`;
            }

            text += `">
            <input type="hidden" class="workNumber" value="${workList.workNumber}"/>
            <input type="hidden" class="workStartTime" value="${workList.workStartTime}">
            <input type="hidden" class="workPredictTime" value="${workList.workPredictTime}">
            <input type="hidden" class="memberName" value="${workList.memberName}"/>
            <input type="hidden" class="memberNumber" value="${workList.memberNumber}"/>
            <input type="hidden" class="workPlace" value="${workList.workPlaceName}"/>
            <input type="hidden" class="workTitle" value="${workList.workTitle}"/>
            <input type="hidden" class="workDetail" value="${workList.workDetail}"/>
            <input type="hidden" class="workStatus" value="${workList.workStatus}"/>
            <input type="hidden" class="workRealStartTime" value="${workList.workRealStartTime}"/>
            <input type="hidden" class="workEndTime" value="${workList.workEndTime}"/>
            `;
            let showTime;
            if(`${workList.workStatus}` == '이동중') {
                if(new Date(`${workList.workStartTime}`) > new Date(new Date(timestamp).getTime() + 7200000)){
                    text += `<li class="point1 badge-standby">대기중</li>`;
                }
                else {
                    text += `<li class="point1">이동중</li>`;
                }
                showTime = `${workList.workStartTime}`;
            } else if(`${workList.workStatus}` == '진행중') {
                text += `<li class="point1 badge-active">작업중</li>`;
                showTime = `${workList.workRealStartTime}`;
            } else if(`${workList.workStatus}` == '야간휴식') {
                text += `<li class="point1 badge-night">야간휴식</li>`;
                showTime = `${workList.workStartTime}`;
            } else if(new Date(`${workList.workStartTime}`) > new Date(timestamp) && `${workList.workStatus}` == '이동중') {
                text += `<li class="point1 badge-standby">대기중</li>`;
                showTime = `${workList.workStartTime}`;
            }
            text += `
            <li class="username"><span>${workList.memberName}</span> <span class="memberRank">${workList.memberRank}님</span></li>
            <li class="usertime"><span>` + formatDateTime(`${showTime}`) + `</span> ~ <span class="workPredictTime">` + formatDateTime(`${workList.workEndTime == null? workList.workPredictTime : workList.workEndTime}`) + `</span></li>
            <li class="ow"><span>${workList.workPlaceName}</span></li>
            <li class="work"><span>${workList.workTitle}</li>
        </div>
    `;

            for(let i = 0; i < $('.memberNumber').length; i++) {
                if($('.memberNumber').eq(i).val() == `${workList.memberNumber}`) {
                    if($('.memberNumber').eq(i + 1).val() == `${workList.memberNumber}`) continue;
                    $('.list1').eq(i).after(text);
                    $('.list1').eq(i + 1).hide();
                    return;
                }
            }

            $('.listloop').append(text);
        });
    }

    function appendFinishLists(finishLists) {
        finishLists.forEach(workList => {
            let text = `<div class="list1 finished" title="${workList.workStatus}">
            <input type="hidden" class="workNumber" value="${workList.workNumber}"/>
            <input type="hidden" class="workStartTime" value="${workList.workStartTime}">
            <input type="hidden" class="workPredictTime" value="${workList.workPredictTime}">
            <input type="hidden" class="memberName" value="${workList.memberName}"/>
            <input type="hidden" class="memberNumber" value="${workList.memberNumber}"/>
            <input type="hidden" class="workPlace" value="${workList.workPlaceName}"/>
            <input type="hidden" class="workTitle" value="${workList.workTitle}"/>
            <input type="hidden" class="workDetail" value="${workList.workDetail}"/>
            <input type="hidden" class="workStatus" value="${workList.workStatus}"/>
            <input type="hidden" class="workRealStartTime" value="${workList.workRealStartTime}"/>
            <input type="hidden" class="workEndTime" value="${workList.workEndTime}"/>

            <li class="point2">종료</li>
            <li class="username"><span>${workList.memberName}</span> <span class="memberRank">${workList.memberRank}님</span></li>
            <li class="usertime"><span>` + formatDateTime(`${workList.workRealStartTime}`) + `</span> ~ <span class="workPredictTime">` + formatDateTime(`${workList.workEndTime == null? workList.workPredictTime : workList.workEndTime}`) + `</span>
            <li class="ow"><span>${workList.workPlaceName}</span></li>
            <li class="work"><span>${workList.workTitle}</li>
        </div>
    `;

            for(let i = 0; i < $('.list1').length; i++) {
                if(($('.list1').eq(i).find('.memberNumber').val() == `${workList.memberNumber}`)) {
                    if($('.memberNumber').eq(i + 1).val() == `${workList.memberNumber}` && ($('.workStartTime').eq(i + 1).val() < `${workList.workRealStartTime}`)) continue;
                    $('.list1').eq(i).after(text);
                    $('.list1').eq(i + 1).hide();
                    return;
                }

            }

            for(let i = 0; i < $('.list1').length; i++) {
                if(($('.workStartTime').eq(i).val() > `${workList.workRealStartTime}`) && ($('.list1').eq(i).css('display') == 'flex')) {
                    $('.list1').eq(i).before(text);
                    return;
                }
            }

            $('.listloop').append(text);
        });
    }


    function paintColor() {
        $('.memberName').each((i, e) => {
            // if($('.memberName').eq(i).val() == $('.memberName').eq(i - 1).val() && $('.memberName').eq(i).parents('.list1').prev().length != 0 && $('.list1').eq(i).find('.workStatus').next().attr('class') != `point2`) {
            //     // $('.username').prev().eq(i).attr('class', 'point4');
            //     // $('.username').prev().eq(i).removeAttr('style');
            //     // $('.list1').eq(i).attr('title', '대기중');
            //     return;
            // }

            if($('.memberName').eq(i).parents('.list1').prev().length == 0 && ($('.memberName').eq(i).val() == $('.memberName').eq(i + 1).val())) {
                if($('.workStatus').eq(i).val() == '종료') {
                    $('.username').prev().eq(i).attr('class', 'point point2');
                    return;
                }
                $('.username').prev().eq(i).attr('class', 'point point1');
                return;
            }

            if(($('.memberName').eq(i).val() != $('.memberName').eq(i - 1).val()) && ($('.memberName').eq(i).val() == $('.memberName').eq(i + 1).val())) {
                if($('.workStatus').eq(i).val() == '종료') {
                    $('.username').prev().eq(i).attr('class', 'point point2');
                    return;
                }
                $('.username').prev().eq(i).attr('class', 'point point1');
                return;
            }
        });
    }

    function plusMinus() {
        $('.point').on('click', function() {
            let memberName = $(this).parents('.list1').find('.memberName').val();

            if($(this).parents('.list1').next().css('display') == 'flex') {
                let count = 0;
                for (let i = 0; i < $('.list1').length; i++) {
                    if((memberName == $('.memberName').eq(i).val())) {
                        if(count == 0) {
                            if($('.workStatus').eq(i).val() == '종료') {
                                $('.list1').eq(i).find('.point-').attr('class','point point2');
                            } else {
                                $('.list1').eq(i).find('.point-').attr('class','point point1');
                            }
                            count++;
                            continue;
                        }
                        $('.memberName').eq(i).parents('.list1').slideUp();
                    }
                }
            } else {
                let count = 0;
                for (let i = 0; i < $('.list1').length; i++) {
                    if((memberName == $('.memberName').eq(i).val())) {
                        if(count == 0) {
                            if($('.workStatus').eq(i).val() == '종료') {
                                $('.list1').eq(i).find('.point').attr('class','point- point2');
                            } else {
                                $('.list1').eq(i).find('.point').attr('class','point- point1');
                            }
                            count++;
                            continue;
                        }
                        $('.memberName').eq(i).parents('.list1').slideDown();
                    }
                }
            }
        });
    }

    function showDetail() {
        $('.list1').on('click', function (e){
            if($(e.target).hasClass('point1') || $(e.target).hasClass('point2')) {
                return;
            }

            $(".modal-container-popup").css("display", "flex");

            let modalName = $(this).closest('.list1').find('.memberName').val();
            // let modalST = $(this).closest('.list1').find('.workStartTime').val();
            let modalST;
            if($(this).closest('.list1').attr('title') == '이동중' || $(this).closest('.list1').attr('title') == '야간휴식') {
                modalST = $(this).closest('.list1').find('.workStartTime').val();
            } else {
                modalST = $(this).closest('.list1').find('.workRealStartTime').val();
            }

            let modalEnd = $(this).closest('.list1').find('.workEndTime').val();
            console.log(modalEnd);
            let modalET = $(this).closest('.list1').find('.workPredictTime').val();
           /* if(modalEnd == null) {
                modalET = $(this).closest('.list1').find('.workPredictTime').val();
            } else {
                modalET = modalEnd;
            }*/
            let modalWP = $(this).closest('.list1').find('.workPlace').val();
            let modalTitle = $(this).closest('.list1').find('.workTitle').val();
            let modalDetail = $(this).closest('.list1').find('.workDetail').val();

            $('.modal-name').text(modalName + '님 일정 상세');
            $('.modal-st').text(modalST);
            $('.modal-et').text(modalEnd === 'null' ? modalET : modalEnd);
            $('.modal-wp').text(modalWP);
            $('.modal-worktitle').text("제목 : " + modalTitle);
            $('.modal-detail').text(modalDetail);
        });
    }

    function formatDateTime(dateTimeString) {
        const dateTime = new Date(dateTimeString);

        const period = dateTime.getHours() >= 12 ? '오후' : '오전';
        const formattedHours = dateTime.getHours().toString().padStart(2, '0');
        const formattedMinutes = dateTime.getMinutes().toString().padStart(2, '0');

        return `${period} ${formattedHours}:${formattedMinutes}`;
    }

    return { appendLists : appendLists, appendFinishLists : appendFinishLists, paintColor : paintColor, plusMinus : plusMinus, showDetail : showDetail };
})();
