<?php
/**
 * Created by PhpStorm.
 * User: choecheon-il
 * Date: 2019-03-01
 * Time: 23:30
 */

// 데이터베이스 커넥터 생성
$link = mysqli_connect("localhost", "root", "7269", "SYSTEM");

/*
 * 커넥터 생성 여부 확인
 * 만약 연결 실패 할 시 에러 전달
 * 안드로이드 소스에 에러 발생 소스 미작성 (차후 작성 예정)
 */

if (!$link) {
    echo "MySQL 접속 에러 : ";
    echo mysqli_connect_error();
    exit();
}

// 유니코드 설정
mysqli_set_charset($link, "utf8");

$email = $_POST['user_email']; // 사용자 이메일
$in = boolval($_POST['user_in']); // 사용자의 탑승 여부
$trans = boolval($_POST['user_transfer']); // 사용자의 환승 여부
$bus = $_POST['last_bus_no']; // 직전 혹은 현재 탑승 중인 버스 번호


//if ($_POST['user_in'] == 'true') {
//    $in = boolval("1");
//} else {
//    $in = boolval("0");
//}
//
//if ($_POST['user_transfer'] == "true") {
//    $trans = boolval("1");
//} else {
//    $trans = boolval("0");
//}

// 사용자 정보 테이블의 내용을 수정하는 쿼리
$updateSql = "update GoogleUserInfoTBL set user_in = '$in',user_transfer='$trans' ,last_bus_no='$bus' where user_email = '$email'";
$updateResult = mysqli_query($link, $updateSql);

//if ($updateResult) {
//    echo "SUCCESS";
//} else {
//    echo "FAILED";
//}
