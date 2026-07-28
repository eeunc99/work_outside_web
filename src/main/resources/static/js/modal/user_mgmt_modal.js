$(".modal-open").on("click", function() {
   $(".modal-container").eq($(this).index()).css("display", "flex");
});

$(".modal-btn").on("click", function() {
    $(this).parent().parent().parent().parent().css("display", "none");
});

$(".modal-close").on("click", function() {
    $(this).parent().parent().parent().parent().css("display", "none");
});