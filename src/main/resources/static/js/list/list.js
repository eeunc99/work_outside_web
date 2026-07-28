$(function() {
    listService.appendLists(lists);
    listService.appendFinishLists(finishLists);
    listService.paintColor();
    listService.plusMinus();
    listService.showDetail();

    setInterval(function (){
        $('.listloop').html('');
        $.ajax({
            url : '/list/work-list',
            type : 'post',
            async : false,
            success : function(results){
                if(results) {
                    listService.appendLists(results);
                }
            },
            error : function (){
                console.log("실패");
            }
        });

        $.ajax({
            url : '/list/end-list',
            type : 'post',
            async : false,
            success : function(results){
                if(results) {
                    listService.appendFinishLists(results);
                }
            },
            error : function (){
                console.log("실패");
            }
        });
        listService.paintColor();
        listService.plusMinus();
        listService.showDetail();
    }, 60000);


    $('.modal-close').on('click', function (){
        $(".modal-container-popup").css("display", "none");
    });
});