<?php
/**
 * Created by PhpStorm.
 * User: choecheon-il
 * Date: 2019-02-11
 * Time: 15:53
 */

/*
 * 현재 캡스톤 프로젝트와 연동 중...
 * 안드로이드와 데이터베이스 간의 간단한 통신 예제
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

$email = $_POST['user_email'];
$recharge = $_POST['user_cash'];

$selectSql = "select * from GoogleUserInfoTBL where user_email = '$email'";

$selectResult = mysqli_query($link, $selectSql);
$curCash;

if ($selectResult) {
    while ($row = mysqli_fetch_array($selectResult)) {
        $curCash = (int)$row['user_cash'];
    }

    $curCash += (int)$recharge;
// 쿼리문 작성
    $updateSql = "update GoogleUserInfoTBL set user_cash = '$curCash' where user_email = '$email'  ";

// 쿼리 실행
    $result = mysqli_query($link, $updateSql);

    if ($result) {
        echo true;
    } else {
        echo false;
    }
}
