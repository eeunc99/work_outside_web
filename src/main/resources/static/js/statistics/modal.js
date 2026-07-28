$('.list-main-box').on('click', '.st-list-sub', function (){
     let workNumber = $(this).find('.work-number').val();

     $.ajax({
          url: "/schedules/data",
          type: "get",
          data: { workNumber : workNumber },
          success: function(result) {
               if(result) {
                    // $('.work-number').val(result.workNumber);
                    $('.workStartTime').text(result.workStartTime);
                    $('.workEndTime').text(result.workEndTime);
                    $('.work1').text(result.workPlaceName);
                    $('.workTitle').text(result.workTitle);
                    $('.workDT2').text(result.workDetail);
               }
          }
     });

     $(".modal-container-popup").css("display", "flex");
});

$('.modal-close').on('click', function (){
     $('.modal-container-popup').css("display", "none");
});