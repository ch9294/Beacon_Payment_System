<?php
/**
 * Created by PhpStorm.
 * User: choecheon-il
 * Date: 2019-03-27
 * Time: 21:04
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

$uuid = $_POST['uuid'];
// 쿼리문 작성
$sql = "select bus_num,uuid from BusInfoTBL where uuid = '$uuid' ";

// 쿼리 실행
$result = mysqli_query($link, $sql);
$dataArray = array();

if ($result) {
    echo SUCCESS;
} else {
    echo FAIL;
}