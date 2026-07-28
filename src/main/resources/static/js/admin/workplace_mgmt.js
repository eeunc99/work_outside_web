$(function() {

    const urlParams = new URL(location.href).searchParams;

    if(urlParams.get('workplaceUse') == 'N') {
        $('.show-all').prop('checked', true);

        adminCommon.setPageBtn('&workplaceUse=N')
    }

    workplaces.forEach(workplace => {
        let text;

        if(`${workplace.workplaceUse}` == 'Y') {
            text = `
                <div class="list1">
                    <div class="point">
                        <li class="point1 badge badge-on">사용중</li>
                        <li class="wo-name">${workplace.workplaceName}</li>
                    </div>
                    <div class="list1_1">
                        <button class="edit-btn row-edit-btn" type="button">${adminCommon.editIcon}수정</button>
                        <button class="del-btn row-del-btn" type="button">${adminCommon.deleteIcon}삭제</button>
                    </div>
                </div>
            `;
        } else {
            text = `
                <div class="list1">
                    <div class="point unused">
                        <li class="point2 badge badge-off">미사용</li>
                        <li class="wo-name">${workplace.workplaceName}</li>
                    </div>
                    <div>
                        <button class="use" type="button">사용</button>
                    </div>
                </div>
            `;
        }

        $('.wo-list').append(text);
    });

    $('.wo-add').on('click', () => {
        adminCommon.openModal(0);
    });

    $('.edit-btn').on('click', function() {
        $('#workplaceName, #originName').val($(this).parents('.list1').find('.wo-name').text());
        adminCommon.openModal(1);
    });

    $('.del-btn').on('click', function() {
        $('#delete-name').val($(this).parents('.list1').find('.wo-name').text());
        adminCommon.openModal(2);
    });

    adminCommon.closeModal(true);

    $('form[name=workplaceForm]').on('submit', function(e) {
        e.preventDefault();

        $.ajax({
            url: "/admin/workplace-name-check",
            type: "post",
            data: { workplaceName : $('#insert-workplace-name').val() },
            success: function(result) {
                if(result != 0) {
                    $('#insert-warn').css('display', 'flex');
                    return;
                } else {
                    document.workplaceForm.submit();
                }
            }
        });
    });

    $('#update-btn').on('click', function() {
        let workplaceName = $(this).parents('.modal-container').find('#workplaceName').val();
        let originName = $(this).parents('.modal-container').find('#originName').val();

        if(workplaceName == originName) {
            return;
        }

        $.ajax({
            url: "/admin/workplace-name-check",
            type: "post",
            async: false,
            data: { workplaceName : workplaceName },
            success: function(result) {
                if(result != 0) {
                    $('#update-warn').css('display', 'flex');
                    return false;
                } else {
                    $.ajax({
                        url: "/admin/workplace-update",
                        type: "post",
                        data: { workplaceName : workplaceName, originName : originName },
                        success: function() {
                            location.reload();
                        }
                    });
                }
            }
        });
    });

    $('#delete-btn').on('click', function() {
        adminCommon.changeUse("/admin/change-workplace-use", { workplaceName : $(this).parents('.modal-container').find('#delete-name').val(), workplaceUse : 'N' });
    });

    $('.use').on('click', function() {
        adminCommon.changeUse("/admin/change-workplace-use", { workplaceName : $(this).parents('.list1').find('.wo-name').text(), workplaceUse : 'Y' });
    });

    $('.show-all').on('click', function() {
        if($(this).is(':checked')) {
            location.href = "/admin/workplace-mgmt?workplaceUse=N";
        } else {
            location.href = "/admin/workplace-mgmt?workplaceUse=Y";
        }
    });
});