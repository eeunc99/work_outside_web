-- 이름/등급 하드코딩 대신 데이터로 예외 권한을 부여하기 위한 테이블.
-- permission_code: 'ADMIN_ACCESS'(관리자 메뉴/통계-관리자), 'LIST_ALL_TEAMS'(외근 현황 전체 팀 열람)
create table tbl_member_permission (
    member_number integer not null references tbl_member(member_number),
    permission_code varchar(30) not null,
    granted_at timestamp not null default now(),
    primary key (member_number, permission_code)
);

-- 기존 코드에 하드코딩되어 있던 예외(옥민정, 최예은, 정현진)를 동일하게 데이터로 이관
insert into tbl_member_permission (member_number, permission_code)
select member_number, 'ADMIN_ACCESS' from tbl_member where member_name in ('A', 'B', 'C')
union all
select member_number, 'LIST_ALL_TEAMS' from tbl_member where member_name in ('A', 'B', 'C');
