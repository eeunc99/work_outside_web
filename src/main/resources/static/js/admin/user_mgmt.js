$(function() {

    const urlParams = new URL(location.href).searchParams;
    let paramDeptNumber = urlParams.get("deptNumber") ? urlParams.get("deptNumber") : '';
    let paramTeamNumber = urlParams.get("teamNumber") ? urlParams.get("teamNumber") : '';
    let paramMemberName = urlParams.get("memberName") ? urlParams.get("memberName") : '';
    let paramMemberUse = urlParams.get("memberUse") ? urlParams.get("memberUse") : '';

    if(paramMemberName) {
        $('.search-name').val(paramMemberName);
    }

    if(paramMemberUse == 'N') {
        $('.show-all').prop('checked', true);

        adminCommon.setPageBtn('&memberUse=N');
    } else {
        adminCommon.setPageBtn('&memberUse=Y');
    }

    members.forEach(member => {
        let text;

        if(`${member.memberUse}` == 'Y') {
            text = `
                <div class="user-list-sub">
                    <li class="name-cell">
                        <span class="name">${member.memberName}</span>
                    </li>
                    <li class="dept-team" title="${member.deptName} · ${member.teamName}">${member.deptName} · ${member.teamName}</li>
                    <li class="identity">${member.memberRank}</li>
                    <li class="vacation">${member.vacationCount}</li>
                    <input type="hidden" class="member-number" value="${member.memberNumber}">
                    <input type="hidden" class="member-id" value="${member.memberId}">
                    <input type="hidden" class="dept-number" value="${member.deptNumber}">
                    <input type="hidden" class="team-number" value="${member.teamNumber}">
                    <input type="hidden" class="vacation-count" value="${member.vacationCount}">
                    <input type="hidden" class="employment-date" value="${member.employmentDate ?? ''}">
                    <input type="hidden" class="reset-date" value="${member.resetDate ?? ''}">
                    <input type="hidden" class="carryover-expire-date" value="${member.carryoverExpireDate ?? ''}">
                    <input type="hidden" class="permission-admin-access" value="${(member.permissionCodes ?? []).includes('ADMIN_ACCESS') ? '1' : '0'}">
                    <input type="hidden" class="permission-list-all-teams" value="${(member.permissionCodes ?? []).includes('LIST_ALL_TEAMS') ? '1' : '0'}">
                    <li class="btn">
                        <button type="button" class="edit row-edit-btn">${adminCommon.editIcon}수정</button>
                        <button type="button" class="delete row-del-btn">${adminCommon.deleteIcon}삭제</button>
                    </li>
                </div>
            `;
        } else {
            text = `
                <div class="user-list-sub">
                    <li class="name-cell">
                        <span class="name unused">${member.memberName}</span>
                        <span class="badge badge-off">미사용</span>
                    </li>
                    <li class="dept-team unused" title="${member.deptName} · ${member.teamName}">${member.deptName} · ${member.teamName}</li>
                    <li class="identity unused">${member.memberRank}</li>
                    <li class="vacation unused"></li>
                    <input type="hidden" class="member-number" value="${member.memberNumber}">
                    <li class="btn">
                        <button type="button" class="use">사용</button>
                    </li>
                </div>
            `;
        }
        $('.list-main-box').append(text);
    });

    departments.forEach(department => {

        let text;

        text = `
            <option value="${department.deptNumber}">${department.deptName}</option>
        `;

        $('.dept-list').append(text);
        $('#member-department').append(text);
        $(editTemplate().content).find('.modify-member-department').append(text);

        if(paramDeptNumber) {
            $('.dept-list').val(paramDeptNumber).prop('selected', true);
        }

        if(paramDeptNumber) {
            adminCommon.getTeamList($('.dept-list').val(), false, function(teams) {
                adminCommon.appendTeamText(teams, true, $('.team-list'));
            });

            $('.team-list').val(paramTeamNumber).prop('selected', true);
        }
    });

    $('.dept-list').on('change', function() {
        adminCommon.getTeamList($('.dept-list').val(), true, function(teams) {
            adminCommon.appendTeamText(teams, true, $('.team-list'));
        });
    });

    $('.use').on('click', function() {
        var memberNumber;

        memberNumber = $(this).parent().prev().val();

        adminCommon.changeUse("/admin/change-member-use", { memberNumber : memberNumber, memberUse : 'Y'})
    });

    $('.show-all').on('click', function() {
        if($(this).is(':checked')) {
            location.href = "/admin/user-mgmt?deptNumber=" + paramDeptNumber + "&teamNumber=" + paramTeamNumber + "&memberName=" + paramMemberName + "&memberUse=N";
        } else {
            location.href = "/admin/user-mgmt?deptNumber=" + paramDeptNumber + "&teamNumber=" + paramTeamNumber + "&memberName=" + paramMemberName + "&memberUse=Y";
        }
    });

    $('.submit').on('click', function() {
        let deptNumber = $('.dept-list option:selected').val();
        let teamNumber = $('.team-list option:selected').val();
        let memberName = $('.search-name').val();

        location.href = '/admin/user-mgmt?deptNumber=' + deptNumber + '&teamNumber=' + teamNumber + '&memberName=' + memberName + '&memberUse=' + (paramMemberUse ? paramMemberUse : 'Y');
    });

    adminCommon.closeModal(false);

    $('.delete').on('click', function() {
        let memberNumber = $(this).parents('.user-list-sub').find('.member-number').val();

        $('#member-delete-number').val(memberNumber);

        adminCommon.openModal(1);
    });

    $('#member-delete').on('click', function() {
        adminCommon.changeUse("/admin/change-member-use", { memberNumber : $('#member-delete-number').val(), memberUse : 'N'});
    });

    // 사원 수정 — 리스트 안에서 슬라이드로 펼쳐지는 인라인 패널
    function editTemplate() {
        return document.getElementById('user-edit-template');
    }

    function closeEditRow(row) {
        row.slideUp(200, function() {
            $(this).remove();
        });
    }

    $('.edit').on('click', function() {
        let userDiv = $(this).parents('.user-list-sub');

        let already = userDiv.next('.user-edit-row');
        if(already.length) {
            closeEditRow(already);
            return;
        }

        $('.user-edit-row').each(function() {
            $(this).remove();
        });

        let row = $(editTemplate().content.cloneNode(true)).children('.user-edit-row');

        row.find('.modify-member-number').val(userDiv.find('.member-number').val());
        row.find('.modify-member-name').val(userDiv.find('.name').text());
        row.find('.modify-member-id').val(userDiv.find('.member-id').val());

        row.find('.modify-member-department').val(userDiv.find('.dept-number').val()).prop('selected', true);

        adminCommon.getTeamList(userDiv.find('.dept-number').val(), true, function(teams) {
            adminCommon.appendTeamText(teams, false, row.find('.modify-member-team'));

            row.find('.modify-member-team').val(userDiv.find('.team-number').val()).prop('selected', true);
        });

        row.find('.modify-member-rank').val(userDiv.find('.identity').text()).prop('selected', true);
        row.find('.modify-employment-date').val(userDiv.find('.employment-date').val());
        row.find('.modify-reset-date').val(userDiv.find('.reset-date').val()).prop('selected', true);
        row.find('.modify-vacation-count').val(userDiv.find('.vacation-count').val());
        row.find('.modify-carryover-expire-date').val(userDiv.find('.carryover-expire-date').val());
        row.find('.modify-admin-access').prop('checked', userDiv.find('.permission-admin-access').val() === '1');
        row.find('.modify-list-all-teams').prop('checked', userDiv.find('.permission-list-all-teams').val() === '1');

        row.hide().insertAfter(userDiv).slideDown(200);
    });

    $('.list-main-box').on('click', '.edit-cancel-btn', function() {
        closeEditRow($(this).closest('.user-edit-row'));
    });

    $('.list-main-box').on('change', '.modify-member-department', function() {
        let row = $(this).closest('.user-edit-row');

        adminCommon.getTeamList($(this).val(), true, function(teams) {
            adminCommon.appendTeamText(teams, false, row.find('.modify-member-team'));
        });
    });

    $('.list-main-box').on('click', '.modify-btn', function() {
        let row = $(this).closest('.user-edit-row');
        let check = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,16}$/;
        const memberName = row.find('.modify-member-name').val();
        const memberPassword = row.find('.modify-member-password').val();
        const teamName = row.find('.modify-member-team').val();

        if(!memberName) {
            alert("이름을 입력하세요.");
            return;
        }

        if(memberPassword) {
            if(!check.test(memberPassword)) {
                alert("비밀번호는 8자 이상, 16자 이하이고 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자가 들어가야 합니다.");
                return;
            }
        }

        if(!teamName) {
            alert("팀을 선택해주세요.")
            return;
        }

        const vacationCountVal = row.find('.modify-vacation-count').val();
        if(vacationCountVal !== '' && parseFloat(vacationCountVal) < 0) {
            alert("휴가 개수는 0 이상이어야 합니다.");
            return;
        }

        const memberDTO = {
            memberNumber    : row.find('.modify-member-number').val(),
            memberId        : row.find('.modify-member-id').val(),
            memberPassword  : row.find('.modify-member-password').val(),
            memberName      : row.find('.modify-member-name').val(),
            memberRank      : row.find('.modify-member-rank').val(),
            deptNumber      : row.find('.modify-member-department').val(),
            teamNumber      : row.find('.modify-member-team').val(),
            employmentDate       : row.find('.modify-employment-date').val(),
            resetDate            : row.find('.modify-reset-date').val(),
            vacationCount        : vacationCountVal,
            carryoverExpireDate  : row.find('.modify-carryover-expire-date').val(),
        }

        $.ajax({
            url: "/admin/change-member-info",
            type: 'post',
            data: JSON.stringify(memberDTO),
            contentType: "application/json; charset=utf-8",
            success: function() {
                $.ajax({
                    url: "/admin/set-member-permissions",
                    type: 'post',
                    data: {
                        memberNumber: memberDTO.memberNumber,
                        adminAccess: row.find('.modify-admin-access').is(':checked'),
                        listAllTeams: row.find('.modify-list-all-teams').is(':checked')
                    },
                    success: function() {
                        location.reload();
                    }
                });
            }
        });
    });

    $('.create').on('click', function() {
        adminCommon.getTeamList($('#member-department').val(), true, function(teams) {
            adminCommon.appendTeamText(teams, false, $('#member-team'));
        });

        adminCommon.openModal(0);
    });

    $('#member-department').on('change', function() {
        adminCommon.getTeamList($('#member-department').val(), true, function(teams) {
            adminCommon.appendTeamText(teams, false, $('#member-team'));
        });
    });

    $('#join-btn').on('click', function() {
        let check = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[$@$!%*#?&])[A-Za-z\d$@$!%*#?&]{8,16}$/;
        const memberName = $('input[name=memberName]').val();
        const memberId = $('input[name=memberId]').val();
        const memberPassword = $('input[name=memberPassword]').val();

        if(!memberName) {
            alert("이름을 입력하세요.");
            return;
        }

        if(!memberId) {
            alert("아이디를 입력하세요.");
            return;
        }

        if(memberId.length > 16) {
            alert("아이디는 최대 16글자입니다.");
            return;
        }

        if(!memberPassword) {
            alert("비밀번호를 입력하세요.");
            return;
        }
        
        if(!check.test(memberPassword)) {
            alert("비밀번호는 8자 이상, 16자 이하이고 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자가 들어가야 합니다.");
            return;
        }

        document.joinForm.submit();
    });
});