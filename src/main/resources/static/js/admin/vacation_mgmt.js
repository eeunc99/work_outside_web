$(function () {

    const CHECK_LABEL = { 'O': '연차', 'H': '반차', 'N': '야간 후 휴식' };
    let pendingDeleteVacationNumber = null;

    $('.datetimepicker').datetimepicker({
        format: 'Y-m-d H:i',
        step: 10
    });

    // 구분(연차/반차/야간 후 휴식)에 따라 날짜 입력 형태 전환 - 연차는 날짜만, 나머지는 날짜+시간
    function toggleVacationDateFields(check) {
        if (check === 'O') {
            $('.only_vacation').show();
            $('.half_vacation').hide();
        } else {
            $('.only_vacation').hide();
            $('.half_vacation').show();
        }
    }

    $('.vacation-check-select').on('change', function () {
        toggleVacationDateFields($(this).val());
    });

    function renderMembers(list) {
        $('.member-list').empty();

        list.forEach(function (member) {
            let text = `
                <ul class="member">
                    <div class="add-list">
                        <input type="hidden" id="member-number" value="${member.memberNumber}">
                        <div class="member-name" data-name="${member.memberName}">
                            <span class="arrow">${adminCommon.caretIcon}</span>
                            <span class="member-name-text">${member.memberName}</span>
                        </div>
                        <span class="va-team">${member.teamName || '해당없음'}</span>
                        <span class="badge badge-on va-remain">잔여 ${member.vacationCount}일</span>
                        <span class="badge badge-off va-carry">이월 ${member.carryoverCount}일</span>
                    </div>
                </ul>
            `;
            $('.member-list').append(text);
        });

        $('.vacation-empty').css('display', list.length === 0 ? 'block' : 'none');
    }

    renderMembers(members);

    $('.vacation-search-btn').on('click', function () {
        let keyword = $('.vacation-search-input').val().trim();
        let filtered = keyword ? members.filter(m => m.memberName.includes(keyword)) : members;
        renderMembers(filtered);
    });

    $('.vacation-search-input').on('keydown', function (e) {
        if (e.key === 'Enter') {
            $('.vacation-search-btn').trigger('click');
        }
    });

    // 사원 이름 눌렀을 때 휴가 내역 펼치기
    $('.member-list').on('click', '.member-name', function () {
        let appendPlace = $(this).parents('.member');
        let memberNumber = $(this).prev().val();

        if (appendPlace.children('.vacation-detail').length !== 0) {
            appendPlace.children('.vacation-detail').remove();
            appendPlace.removeClass('active');
            return;
        }

        loadMemberVacations(memberNumber, appendPlace);
    });

    function loadMemberVacations(memberNumber, appendPlace) {
        $.ajax({
            url: '/admin/vacation-list',
            type: 'post',
            data: { memberNumber: memberNumber },
            success: function (results) {
                appendPlace.addClass('active');
                appendPlace.children('.vacation-detail').remove();
                appendPlace.append(renderVacationDetail(memberNumber, results || []));
            }
        });
    }

    function renderVacationDetail(memberNumber, list) {
        let rows = '';
        if (list.length === 0) {
            rows = `<div class="vacation-row-empty">등록된 휴가가 없습니다.</div>`;
        } else {
            list.forEach(function (v) {
                rows += `
                    <li class="vacation-row">
                        <input type="hidden" class="row-vacation-number" value="${v.vacationNumber}">
                        <input type="hidden" class="row-vacation-check" value="${v.vacationCheck}">
                        <input type="hidden" class="row-vacation-st-date" value="${v.vacationStDate2}">
                        <input type="hidden" class="row-vacation-end-date" value="${v.vacationEndDate2}">
                        <span class="badge badge-off">${CHECK_LABEL[v.vacationCheck] || v.vacationCheck}</span>
                        <span class="vacation-row-date">${formatDisplayDate(v.vacationStDate2)} ~ ${formatDisplayDate(v.vacationEndDate2)}</span>
                        <span class="vacation-row-actions">
                            <button type="button" class="edit-btn row-edit-btn" title="수정">${adminCommon.editIcon}수정</button>
                            <button type="button" class="del-btn row-del-btn" title="삭제">${adminCommon.deleteIcon}삭제</button>
                        </span>
                    </li>
                `;
            });
        }

        return `
            <div class="vacation-detail">
                <div class="vacation-detail-add">
                    <button type="button" class="vacation-add-btn row-primary-btn" data-member-number="${memberNumber}">${adminCommon.plusIcon}휴가 등록</button>
                </div>
                ${rows}
            </div>
        `;
    }

    $('.member-list').on('click', '.vacation-add-btn', function () {
        let memberNumber = $(this).data('member-number');
        let memberName = $(this).closest('.member').find('.member-name').data('name');
        openVacationModal(memberNumber, memberName);
    });

    $('.member-list').on('click', '.edit-btn', function () {
        let row = $(this).closest('.vacation-row');
        let memberNumber = $(this).closest('.member').find('#member-number').val();
        let memberName = $(this).closest('.member').find('.member-name').data('name');

        openVacationModal(memberNumber, memberName, {
            vacationNumber: row.find('.row-vacation-number').val(),
            vacationCheck: row.find('.row-vacation-check').val(),
            vacationStDate: row.find('.row-vacation-st-date').val(),
            vacationEndDate: row.find('.row-vacation-end-date').val()
        });
    });

    function openVacationModal(memberNumber, memberName, existing) {
        let check = existing ? existing.vacationCheck : 'O';

        $('.vacation-modal-title-text').text(existing ? '휴가 수정' : '휴가 등록');
        $('.vacation-number').val(existing ? existing.vacationNumber : '');
        $('.vacation-target-member-number').val(memberNumber);
        $('.vacation-member-display-name').text(memberName);
        $('.vacation-check-select').val(check);
        toggleVacationDateFields(check);

        $('#vacation_st_date').val(existing ? existing.vacationStDate.substring(0, 10) : '');
        $('#vacation_end_date').val(existing ? existing.vacationEndDate.substring(0, 10) : '');
        $('#vacation_st_date2').val(existing ? existing.vacationStDate.substring(0, 16) : '');
        $('#vacation_end_date2').val(existing ? existing.vacationEndDate.substring(0, 16) : '');

        loadMemberRemain(memberNumber);
        adminCommon.openModal(0);
    }

    function loadMemberRemain(memberNumber) {
        let member = members.find(m => String(m.memberNumber) === String(memberNumber));
        if (member) {
            $('.vacation-remain-badge').text('잔여 ' + member.vacationCount + '일');
            $('.vacation-carryover-badge').text('이월 ' + member.carryoverCount + '일');
            $('.vacation-member-remain').css('display', 'flex');
        }
    }

    $('.member-list').on('click', '.del-btn', function () {
        pendingDeleteVacationNumber = $(this).closest('.vacation-row').find('.row-vacation-number').val();
        adminCommon.openModal(1);
    });

    $('.vacation-delete-confirm-btn').on('click', function () {
        if (!pendingDeleteVacationNumber) return;

        $.ajax({
            url: '/admin/vacation-delete',
            type: 'post',
            data: { vacationNumber: pendingDeleteVacationNumber },
            success: function () {
                location.reload();
            }
        });
    });

    $('.vacation-save-btn').on('click', function () {
        let memberNumber = $('.vacation-target-member-number').val();
        if (!memberNumber) return;

        let vacationNumber = $('.vacation-number').val();
        let vacationCheck = $('.vacation-check-select').val();

        let stValue = vacationCheck === 'O' ? $('#vacation_st_date').val() : $('#vacation_st_date2').val();
        let endValue = vacationCheck === 'O' ? $('#vacation_end_date').val() : $('#vacation_end_date2').val();

        if (!stValue || !endValue) {
            alert('시작/종료 일시를 입력해주세요.');
            return;
        }
        if (new Date(endValue) < new Date(stValue)) {
            alert('종료 일시가 시작 일시보다 빠릅니다.');
            return;
        }

        let vacationStDate, vacationEndDate;
        if (vacationCheck === 'O') {
            vacationStDate = stValue + ' 00:00:00.000';
            vacationEndDate = endValue + ' 23:00:00.000';
        } else {
            vacationStDate = stValue + ':00.000';
            vacationEndDate = endValue + ':00.000';
        }

        let data = {
            memberNumber: memberNumber,
            vacationStDate: vacationStDate,
            vacationEndDate: vacationEndDate,
            vacationCheck: vacationCheck
        };

        let url = '/admin/vacation-regist';
        if (vacationNumber) {
            url = '/admin/vacation-update';
            data.vacationNumber = vacationNumber;
        }

        $.ajax({
            url: url,
            type: 'post',
            data: data,
            success: function () {
                location.reload();
            }
        });
    });

    adminCommon.closeModal(false);

    function formatDisplayDate(dateStr) {
        if (!dateStr) return '';
        return dateStr.length > 16 ? dateStr.substring(0, 16) : dateStr;
    }
});
