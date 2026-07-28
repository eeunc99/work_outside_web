const statisticsCommon = (function() {

    function statisticsList() {
        statistics.forEach(statistic => {

            let text;

            let workEndTimeCss = `<span style="padding-left: 65px;">진행중</span>`;

            text = `
            <div class="st-list-sub">
                <input type="hidden" class="work-number" value="${statistic.workNumber}">
                <li class="name">${statistic.memberName}</li>
                <li class="team">${statistic.teamName}</li>
                <li class="wo">${statistic.workplaceName}</li>
                <li class="start-time">`;
                text += `${statistic.workStatus}` == '이동중' ? `${statistic.workStartTime}` : `${statistic.workRealStartTime}`;
                text += `
                        </li>
                        <li class="end-time">`;

            text += `${statistic.workEndTime}` == 'null' ? workEndTimeCss : `${statistic.workEndTime}`;

            text += `
                </li>
                <li class="time_taken tt">` + statisticsCommon.makeMinuteToHour(`${statistic.workSpentTime}`) +`</li>
                <!-- <li class="work_off">${statistic.workOff}</li> -->
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

    return { statisticsList : statisticsList, workplaceList : workplaceList, makeMinuteToHour : makeMinuteToHour, checkLastDate : checkLastDate };
})();