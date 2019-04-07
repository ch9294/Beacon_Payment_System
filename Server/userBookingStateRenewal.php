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

$email = $_POST['user_email'];
$balance_account = $_POST['balance_account'];
$busNumber = $_POST['busNumber'];

// 쿼리문 작성
$selectSql = "select * from GoogleUserInfoTBL where user_email = $email";

// 쿼리 실행
$result = mysqli_query($link, $selectSql);

$row1;
if ($result) {
    $row1 = mysqli_fetch_array($result);
} else {
    echo "failed";
}

// 잔액 계산
$cash = (int)$row1['user_cash'] - (int)$balance_account;

$updateSql = "update GoogleUserInfoTBL set user_cash='$cash',user_book=true,user_transfer=true ,last_bus_no='$busNumber'";
$updateResult = mysqli_query($link,$updateSql);

if($updateResult){
    echo "SUCCESS";
}else{
    echo "FAILED";
}
