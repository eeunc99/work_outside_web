// 호스트/포트(로컬, 사내망 IP, 운영 서버 등)에 관계없이 경로만으로 현재 메뉴 판별
let path = window.location.pathname;

if (path.indexOf('/admin/') != -1) {
    $('.adminpage').attr("id", "click");
}

if (path == '/list/list') {
    $('.listpage').attr("id", "click");
}

if (path == '/sd') {
    $('.schedulepage').attr("id", "click");
}

if (path.indexOf('/statistics/') != -1) {
    $('.statisticspage').attr("id", "click");
}

$(document).ready(function() {
    let savedColor = getCookie('template_color');

    if (savedColor) {
        $('.template_color').val(savedColor); // 쿠키 값으로 템플릿 색상 설정
        setTemplateColor(savedColor); // 템플릿 스타일 적용
    } else {
        // 기본 템플릿 색상 설정
        $('.template_color').val('default');
        setTemplateColor('default');
    }
});

$('.color_template_item').on('click', function () {
    if($('.click_color').css('display') == 'none') {
        $('.click_color').show();
    }
    else {
        $('.click_color').hide();
    }
});

$('.template_color').on('change', function (){
    let color = $(this).val();
    setTemplateColor(color); // 템플릿 스타일 적용

    // 쿠키 설정
    document.cookie = `template_color=${color}; expires=Fri, 31 Dec 9999 23:59:59 GMT; path=/`;
});

function setTemplateColor(color) {
    let baseUrl = window.location.origin;
    let link = $('<link>');
    let css = `css/total/${color}.css`;
    let Path = `${baseUrl}/${css}`;

    link.attr('rel', 'stylesheet');
    link.attr('href', Path);

    link.addClass('custom_css');

    // 기존 스타일 시트 제거
    $('head').find('.custom_css').remove();
``
    $('head').append(link);
}

function getCookie(name) {
    const value = "; " + document.cookie;
    const parts = value.split("; " + name + "=");
    if (parts.length === 2) {
        return parts.pop().split(";").shift();
    }
    return null;
}