const urlParams = new URL(location.href).searchParams;
if(urlParams.get('interceptor') == 'Y') {
    alert("권한이 없습니다.");
}

$(document).ready(function() {
    const $memberIdInput = $('#memberId');
    const $loginCheck = $('#loginCheck');

    // 페이지 로드 시 저장된 아이디 불러오기
    if (localStorage.getItem('savedId')) {
        $memberIdInput.val(localStorage.getItem('savedId'));
        $loginCheck.prop('checked', true); // 체크박스 체크
    }

    // 로그인 버튼 클릭 시 아이디 저장
    $('.loginok').on('click', function() {
        saveIdAndLogin();
    });

    // 엔터로 로그인 처리하기
    $(document).on('keyup', function(e) {
        if (e.keyCode === 13) { // Enter 키 코드
            saveIdAndLogin(); // 로그인 처리 함수 호출
        }
    });

    function saveIdAndLogin() {
        console.log("Checkbox checked:", $loginCheck.is(':checked'));
        console.log("Input value:", $memberIdInput.val());

        if ($loginCheck.is(':checked')) {
            localStorage.setItem('savedId', $memberIdInput.val());
            console.log("ID saved:", $memberIdInput.val());
        } else {
            localStorage.removeItem('savedId');
            console.log("ID removed from storage");
        }

        // 여기에 로그인 로직을 추가하세요
        loginOk(); // 로그인 함수 호출
    }
});

/*$('.loginok').on('click', function (){
    loginOk();
});
*/

// 엔터로 로그인 처리하기
$(document).on('keyup', function (e){
    if(e.keyCode === 13) {
        loginOk();
    }
});

function loginOk() {
    let memberId = $('.member_id').val();
    let memberPassword = $('.member_password').val();

    console.log(memberId , memberPassword);

    $.ajax({
        url : '/logins/loginOk',
        type : 'post',
        data : JSON.stringify({memberId : memberId , memberPassword : memberPassword}),
        contentType : "application/json; charset=utf-8",
        async : false,
        success: function (result) {
            // Param();
            if (result == -1 || result == 0) {
                alert("아이디와 비밀번호를 확인해주세요.");
                let url = window.location.href;
                window.location.href = url;
            } else if (result == 2) {
                if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
                    document.cookie = `expires=Fri, 31 Dec 9999 23:59:59 GMT; path=/`;
                    //window.location.href = 'http://YOUR_PROD_HOST:8080/sd';
                    window.location.href = 'http://' + serverIp + ':' + serverPort + '/sd';
                } else {
                    //window.location.href = 'http://YOUR_PROD_HOST:8080/list/list';
                    window.location.href = 'http://' + serverIp + ':' + serverPort + '/list/list';
                }
            } else if (result == 3) {
                document.cookie = `expires=Fri, 31 Dec 9999 23:59:59 GMT; path=/`;
                //window.location.href = 'http://YOUR_PROD_HOST:8080/sd';
                window.location.href = 'http://' + serverIp + ':' + serverPort + '/sd';
            }
        },
        error: function () {
            console.log("실패");
        }
    });
}