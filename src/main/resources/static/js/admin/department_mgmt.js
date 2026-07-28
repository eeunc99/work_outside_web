$(function() {

    departments.forEach(department => {

        let text;

        text = `
            <ul class="dept">
                <div class="add-list">
                    <input type="hidden" id="department-number" value="${department.deptNumber}">
                    <div class="dept-name"><span class="arrow">${adminCommon.caretIcon}</span><span class="dept-name-text">${department.deptName}</span></div>
                    <div class="dept-actions">
                        <button class="plus" type="button" title="팀 추가">${adminCommon.plusIcon}</button>
                        <button class="edit" type="button" title="부서 수정">${adminCommon.editIcon}</button>
                    </div>
                </div>
            </ul>
        `;

        $('.company-list').append(text);
    });

    $('.edit').on('click', function() {
        $('#dept-edit').val($(this).parents('.dept').find('.dept-name-text').text());
        $('#dept-number').val($(this).parents('.dept').find('#department-number').val());
        adminCommon.openModal(4);
    });

    $('#dept-update').on('click', function() {
        adminCommon.changeUse("/admin/dept-update", { deptNumber : $('#dept-number').val(), deptName : $('#dept-edit').val() })
    });

    $('#dept-delete-btn').on('click', function() {
        adminCommon.openModal(5);
    });

    $('#remove-dept').on('click', function() {
        adminCommon.changeUse("/admin/dept-delete", { deptNumber : $('#dept-number').val() })
    });

    // 부서 이름 눌렀을 때 아래 요소 나타내기
    $('.dept-name').on('click', function() {
        const appendPlace = $(this).parents('.dept');
        const arrow = $(this).find('.arrow');

        if(appendPlace.children('.team-list').length != 0) {
            appendPlace.children('.team-list').remove();
            arrow.removeClass('open');
            return;
        }

        adminCommon.getTeamList($(this).prev().val(), true, function(results) {
            if(results) {
                arrow.addClass('open');
                let text = `<div class="team-list">`;
                results.forEach(result => {

                    text += `
                        <li class="team"><input type="hidden" value="${result.teamNumber}"><span class="team-name">${result.teamName}</span></li>
                    `;

                });
                text += `</div>`;

                appendPlace.append(text);

                $('.team-name').on('click', function() {
                    $('#team-name').val($(this).text());
                    $('#team-number-update').val($(this).prev().val());
                    adminCommon.openModal(2);
                });

                $('#team-update').on('click', function() {
                    adminCommon.changeUse("/admin/team-update", { teamNumber : $('#team-number-update').val(), teamName : $('#team-name').val() })
                });

                $('#team-delete').on('click', function() {
                    adminCommon.openModal(3);
                });

                $('#delete-submit').on('click', function() {
                    adminCommon.changeUse("/admin/team-delete", { teamNumber : $('#team-number-update').val() })
                });
            }
        });
    });

    $('.dept-btn').on('click', function() {
        adminCommon.openModal(0);
    });

    adminCommon.closeModal(false);

    $('.plus').on('click', function() {
        $('input[name=deptNumber]').val($(this).parents('.dept').find('#department-number').val());
        adminCommon.openModal(1);
    });

    $('.team-name').on('click', function() {
        adminCommon.openModal(2);
    });
});