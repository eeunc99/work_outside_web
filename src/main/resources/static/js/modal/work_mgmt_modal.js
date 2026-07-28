$(".modal-open").on("click", function() {
   $(".modal-container").eq(0).css("display", "flex");
});

$(".modal-open2").on("click", function() {
    $(".modal-container-v").eq(0).css("display", "flex");
});


$(".cal-btn").on("click", function() {
    $('form')[0].reset();
    $('.modal-container-add').css("display", "none");
});

$(".cal-btn2").on("click", function() {
    // $('form')[0].reset();
    $('.modal-container-del').css("display", "none");
});

$(".modal-close").on("click", function() {
    $(this).parent().parent().parent().parent().css("display", "none");
});

$('.list1').on('click', function (){
    $(".modal-container").eq(1).css("display", "flex");
});

$(".v-cal-btn").on("click", function() {
    $('form')[0].reset();
    $('.modal-container-v').css("display", "none");
});

$('.list3').on('click', function (){
    let $item = $(this);
    let vacationNumber = $item.data('vacation-number');
    let vacationSt    = $item.data('vacation-st');
    let vacationEnd   = $item.data('vacation-end');
    let vacationCheck = $item.data('vacation-check');

    $('#editVacationNumber').val(vacationNumber);

    if (vacationCheck === 'O') {
        $(".only_vacation2").show();
        $(".half_vacation2").hide();
        $("#only_vacation2").prop('checked', true);
        $("#half_vacation2").prop('checked', false);
        $("#vacation_st_date3").val(vacationSt);
        $("#vacation_end_date3").val(vacationEnd);
    } else if (vacationCheck === 'H') {
        $(".only_vacation2").hide();
        $(".half_vacation2").show();
        $("#half_vacation2").prop('checked', true);
        $("#only_vacation2").prop('checked', false);
        $("#vacation_st_date4").val(vacationSt);
        $("#vacation_end_date4").val(vacationEnd);
    }

    $(".modal-container-v-edit").eq(0).css("display", "flex");
});
