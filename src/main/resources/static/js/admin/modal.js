// 상태에 따라 실제 시작/종료 시간 입력란을 활성화하고, 해당 상태에서 의미 없는 필드는 비활성화 + 비움
function applyStatusFields() {
     let status = $('.work-status-select').val();
     $('#workRealStartTime').prop('disabled', status === '이동중');
     $('#workEndTime').prop('disabled', status === '이동중' || status === '진행중');
     if (status === '이동중') {
          $('#workRealStartTime').val('');
          $('#workEndTime').val('');
     } else if (status === '진행중') {
          $('#workEndTime').val('');
     }
}

$('.work-status-select').on('change', applyStatusFields);

$('.list-main-box').on('click', '.st-list-sub', function (){
     let workNumber = $(this).find('.work-number').val();
     let workPlaceNumber = $(this).find('.workplace-number').val();
     $('.work-status-select').val($(this).data('workStatus'));
     search();

     $.ajax({
          url: "/schedules/data",
          type: "get",
          data: { workNumber : workNumber },
          success: function(result) {
               if(result) {
                    $('.work-number').val(result.workNumber);
                    $('#workStartTime').val(result.workStartTime);
                    $('#workPredictTime').val(result.workPredictTime);
                    $('#workRealStartTime').val(result.workRealStartTime);
                    $('#workEndTime').val(result.workEndTime);
                    $('#work1').val(result.workPlaceNumber);
                    $('.workplace-number').val(result.workPlaceNumber);
                    $('.work-title').val(result.workTitle);
                    $('.workDT2').val(result.workDetail);
                    if (result.workStatus) {
                         $('.work-status-select').val(result.workStatus);
                    }
                    applyStatusFields();
               }
          }
     });

     $(".modal-container-edit").css("display", "flex");
});

$('.modal-close').on('click', function (){
     $('.modal-container-edit').css("display", "none");
});

$('.edit-btn').on('click', function() {
     let workNumber = $('.work-number').val();
     let workStatus = $('.work-status-select').val();
     let workStartTime = $('#workStartTime').val();
     let workPredictTime = $('#workPredictTime').val();
     let workRealStartTime = $('#workRealStartTime').val();
     let workEndTime = $('#workEndTime').val();
     let workplaceNumber = $('.workplace-number').val();
     let workTitle = $('.work-title').val();
     let workDetail = $('.workDT2').val();
     let workSpentTime = '';

     // '종료' 상태만 실제 시작/종료 시간이 모두 필요 - 소요시간 검증도 이때만 적용
     if (workStatus === '종료') {
          workSpentTime = Math.floor((new Date(workEndTime).getTime() - new Date(workRealStartTime).getTime()) / (1000 * 60));

          if (!workRealStartTime || !workEndTime) {
               alert("종료 상태는 실제 시작/종료 시간을 모두 입력해주세요.");
               return;
          }

          if (workSpentTime <= 0) {
               alert("종료시간이 시작시간보다 작거나 같습니다.")
               return;
          }
     } else if (workStatus === '진행중' && !workRealStartTime) {
          alert("진행중 상태는 실제 시작 시간을 입력해주세요.");
          return;
     }

     if(!$('.work-title').val()) {
          alert("근무 제목을 입력해주세요.");
          $(".modal-container-edit").eq(0).find('.work-title').focus();
          return false;
     }

     if(!$('.workDT2').val()) {
          alert("근무 내용을 입력해주세요.");
          $(".modal-container-edit").eq(0).find('.workDT2').focus();
          return false;
     }

     $.ajax({
          url: "/schedules/update",
          type: "patch",
          data: { workNumber : workNumber, workStartTime : workStartTime, workPredictTime : workPredictTime,
               workRealStartTime : workRealStartTime, workEndTime : workEndTime, workPlaceNumber : workplaceNumber,
               workTitle : workTitle, workDetail : workDetail, workSpentTime : workSpentTime, workStatus : workStatus },
          success: function() {
               location.reload();
          }
     });
});

$('.del-btn').on('click', () => {
     $('.del-number').val($('.work-number').val());
     $('.modal-container-del').css('display', 'flex');
});

$('.del-btn2').on('click', function() {
     let workNumber = $('.del-number').val();

     $.ajax({
          url: "/schedules/delete",
          type: "delete",
          data: { workNumber : workNumber },
          success: function() {
               location.reload();
          }
     });
});

$('.cal-btn2').on('click', () => {
     $('.modal-container-del').hide();
});

$('.list-main-box').on('click', '.st-list-sub', function () {
     let workplaceNumber2 = $('.workplace-number').val();
     // 해당 리스트의 근무지 번호 넣어주기(근무지 이름 포함)
     let workName = $(this).closest('.st-list-sub').find('.wo').text();
     console.log(workName);
     $('#work1').val(workplaceNumber2);
     $('#work1').text(workName);
});

$('.workplaceNum').off('click').on('click', function (event) {
     event.stopPropagation();
     let workValue = $(this).attr('value');
     let workName = $(this).find('span').text();
     let workplaceNumber = $(this).attr('data-id'); // workplaceNumber 속성 가져오기

     // 선택한 요소의 텍스트로 대체
     $(this).closest('.workList1').find('.select-selected').text(workName);

     // 선택한 값으로 설정 (옵션의 value 속성 활용)
     if (workValue) {
          $(this).closest('.workList1').find('.select-selected').attr('data-value', workValue);
     } else {
          console.error('workValue가 유효하지 않습니다.');
     }

     // 선택한 workplaceNumber 값으로 설정
     $(this).closest('.workList1').find('.select-selected').attr('data-workplace-number', workplaceNumber);

     // 선택한 요소를 숨김
     $(this).closest('.select-items').hide();

     search();
});

$(document).on('click', function(event) {
     // 클릭한 요소가 .select-selected 또는 .select-items가 아닐 경우
     if (!$(event.target).closest('.select-selected').length && !$(event.target).closest('.select-items').length) {
          $('.select-items').hide(); // 선택 옵션 숨김
     }
});


function search() {
     $('.select-selected').on('click', function() {
          $('.select-items').show();
     });

     $('.search-input').on('input', function (){
          let text = $(this).val().toUpperCase(); // 검색어를 가져옵니다.
          let items = $('.select-items').find('.workplaceNum'); // 모든 선택 옵션을 가져옵니다.

          // 각 요소의 텍스트와 검색어 비교하여 보이기/숨기기 처리
          for (let i = 0; i < items.length; i++) {
               let optionText = $(items[i]).find('span').text().toUpperCase();
               if (optionText.indexOf(text) > -1) {
                    $(items[i]).show();
               } else {
                    $(items[i]).hide();
               }
          }
     });
}