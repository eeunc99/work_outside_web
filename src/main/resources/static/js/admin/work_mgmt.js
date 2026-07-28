$(document).ready(function (){
    const urlParams = new URL(location.href).searchParams;
    let paramWorkStartTime = urlParams.get("workStartTime") ? urlParams.get("workStartTime") : '';
    let paramWorkEndTime = urlParams.get("workEndTime") ? urlParams.get("workEndTime") : '';
    let paramWorkplaceNumber = urlParams.get("workplaceNumber") ? urlParams.get("workplaceNumber") : '';
    let paramTeamNumber = urlParams.get("teamNumber") ? urlParams.get("teamNumber") : '';
    let paramMemberNumber = urlParams.get("memberNumber") ? urlParams.get("memberNumber") : '';

    $('#work-start-time').val(paramWorkStartTime);
    $('#work-end-time').val(paramWorkEndTime);
    $('#name-list').val(paramMemberNumber);

    if ($('.prev-btn').length) {
        $('.prev-btn').attr('href', updateURL($('.prev-btn').attr('href')));
    }

    if ($('.number-btn').length) {
        $('.number-btn').each(function () {
            $(this).attr('href', updateURL($(this).attr('href')));
        });
    }

    if ($('.next-btn').length) {
        $('.next-btn').attr('href', updateURL($('.next-btn').attr('href')));
    }

    function updateURL(url) {
        let originalUrl = url.split('&');

        let first = originalUrl[0];

        return first + '&workStartTime=' + paramWorkStartTime + '&workEndTime=' + paramWorkEndTime + '&teamNumber=' + paramTeamNumber
            + '&workplaceNumber=' + paramWorkplaceNumber + '&memberNumber=' + paramMemberNumber;
    }

    statisticsList();
    workplaceList();

    $('#workplace-list').val(paramWorkplaceNumber).prop('selected', true);

    teams.forEach(team => {

        let text;

        text = `
            <option value="${team.teamNumber}">${team.teamName}</option>
        `;

        $('#team-list').append(text);
    });

    if (paramTeamNumber) {
        $('#team-list').val(paramTeamNumber).prop('selected', true);

        $.ajax({
            url: "/statistics/get-member-list",
            type: "post",
            data: {teamNumber: paramTeamNumber},
            success: function (results) {
                if (results) {

                    let text = `<option value="">이름을 선택해주세요</option>`;

                    results.forEach(result => {
                        text += `
                            <option value="${result.memberNumber}">${result.memberName}</option>
                        `;
                    });

                    $('#name-list').html(text);
                    $('#name-list').val(paramMemberNumber).prop('selected', true);
                }
            }
        });
    }

    $('#team-list').on('change', function () {
        $.ajax({
            url: "/statistics/get-member-list",
            type: "post",
            data: {teamNumber: $(this).val()},
            success: function (results) {
                if (results) {

                    let text = `<option value="">이름을 선택해주세요</option>`;

                    results.forEach(result => {
                        text += `
                            <option value="${result.memberNumber}">${result.memberName}</option>
                        `;
                    });

                    $('#name-list').html(text);
                }
            }
        });
    });

    $('.submit').on('click', function () {
        let workStartTime = $('#work-start-time').val();
        let workEndTime = $('#work-end-time').val();
        let workplaceNumber = $('#workplace-list').val();
        let teamNumber = $('#team-list').val();
        let memberNumber = $('#name-list').val();

        if (workEndTime && !workStartTime) {
            alert("날짜를 모두 고르거나 모두 고르지마세요");
            return;
        }

        if (checkLastDate(workStartTime, workEndTime)) {
            alert("검색 시작 날이 끝나는 날보다 큽니다.");
            return;
        }

        location.href = '/admin/work-mgmt?workStartTime=' + workStartTime + '&workEndTime=' + workEndTime + '&workplaceNumber=' + workplaceNumber
            + '&teamNumber=' + teamNumber + '&memberNumber=' + memberNumber;
    });
});


function statisticsList() {
    statistics.forEach(statistic => {

        let text;

        text = `
            <div class="st-list-sub" data-work-status="${statistic.workStatus}">
                <input type="hidden" class="work-number" value="${statistic.workNumber}">
                <input type="hidden" class="workplace-number" value="${statistic.workplaceNumber}">
                <li class="name name2">${statistic.memberName}</li>
                <li class="team team2">${statistic.teamName}</li>
                <li class="wo wo2">${statistic.workplaceName}</li>
                <li class="start-time start-time2">`;
                text += `${statistic.workStatus}` == '이동중' ? `${statistic.workStartTime}` : `${statistic.workRealStartTime}`;
                text += `
                        </li>
                        <li class="end-time end-time2">`;
                text += `${statistic.workEndTime}` == 'null' ? '진행중' : `${statistic.workEndTime}`;

                text += `
                     </li>
                    <li class="time_taken tt tt2">` + makeMinuteToHour(`${statistic.workSpentTime}`) +`</li>
            </div>
        `;

        $('.list-main-box').append(text);
    });
}

function workplaceList() {
    workplaces.forEach(workplace => {

        let text;

        text = `
                <option value="${workplace.workplaceNumber}">${workplace.workplaceName}</option>
            `;

        $('#workplace-list').append(text);
    });
}

function makeMinuteToHour(min) {
    if(min == '진행중') return '진행중';

    var h = Math.floor(min / 60);
    var m = Math.floor(min % 60);

    var hour = h > 0 ? h + '시간 ' : '';
    var minute = m > 0 ? m + '분' : '';

    return hour + minute;
}

function checkLastDate(firstDate, lastDate) {
    return lastDate < firstDate;
}
