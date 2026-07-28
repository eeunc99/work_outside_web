const adminCommon = (function() {

    function setPageBtn(use) {
        if($('.prev-btn').length) {
            $('.prev-btn').attr('href', updateURL($('.prev-btn').attr('href'), use));
        }

        if($('.number-btn').length) {
            $('.number-btn').each(function () {
                $(this).attr('href', updateURL($(this).attr('href'), use));
            });
        }

        if($('.next-btn').length) {
            $('.next-btn').attr('href', updateURL($('.next-btn').attr('href'), use));
        }
    }
    function updateURL(currentURL, workplaceUse) {

        let adminPart = currentURL.split('&')[0];

        let newURL = adminPart + workplaceUse;

        return newURL;
    }

    function getTeamList(deptNumber, async, callback) {
        $.ajax({
            url: "/admin/team-list",
            type: "post",
            async: async,
            data: { deptNumber : deptNumber },
            success: function(results) {
                if(callback) {
                    callback(results);
                }
            }
        });
    }

    function changeUse(url, data) {
        $.ajax({
            url: url,
            type: "post",
            data: data,
            success: function() {
                location.reload();
            }
        });
    }

    function appendTeamText(teams, choiceText, appendPlace) {
        if (teams) {
            let text = choiceText ? `<option value="">소속을 선택해주세요</option>` : ``;
            teams.forEach(team => {

                text += `
                    <option value="${team.teamNumber}">${team.teamName}</option>
                `;

            });
            appendPlace.html(text);
        }
    }

    function openModal(index) {
        $('.modal-container').eq(index).css('display', 'flex');
    }

    function closeModal(isWorkplace) {
        $(".modal-close, .modal-real-close").on("click", function() {
            $(this).parents('.modal-container').hide();
            $(this).parents('.modal-container').find('input').val('');
            isWorkplace ? $(this).parents('.modal-container').find('.warn').hide() : '';
        });
    }

    const editIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 20h4L18 10l-4-4L4 16z"/></svg>`;
    const deleteIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 7h16M9 7V4h6v3M6 7l1 13h10l1-13"/></svg>`;
    const plusIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 5v14M5 12h14"/></svg>`;
    const caretIcon = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4"><path d="M9 6l6 6-6 6"/></svg>`;

    return { setPageBtn : setPageBtn, getTeamList : getTeamList, changeUse : changeUse, appendTeamText : appendTeamText, openModal : openModal, closeModal : closeModal, editIcon : editIcon, deleteIcon : deleteIcon, plusIcon : plusIcon, caretIcon : caretIcon };
})();

$(document).ready(function () {

    let now = new Date();

    let dateStr = now.getFullYear() + '-' + ((now.getMonth() + 1) < 10 ? '0' : '') + (now.getMonth() + 1) + '-' + (now.getDate() < 10 ? '0' : '') + now.getDate();
    $('#work-start-time').val(dateStr);
    $('#work-end-time').val(dateStr);
});