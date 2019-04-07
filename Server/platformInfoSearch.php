<?php
/**
 * Created by PhpStorm.
 * User: choecheon-il
 * Date: 2019-02-08
 * Time: 14:15
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

// 쿼리문 작성
$sql = "select * from PlatFormTBL";

// 쿼리 실행
$result = mysqli_query($link, $sql);

if ($result) {
    $dataArray = array();

    while ($row = mysqli_fetch_array($result)) {
        if (is_null($row['platform_num'])) {
            array_push($dataArray, array(
                "platId" => $row['platform_id'],
                "platNo" => "no_search",
                "platName" => $row['platform_name'],
            ));
        } else {
            array_push($dataArray, array(
                "platId" => $row['platform_id'],
                "platNo" => $row['platform_num'],
                "platName" => $row['platform_name'],
            ));
        }
    }
    header('Content-Type: application/json; charset=utf8');
    $json = json_encode($dataArray, JSON_PRETTY_PRINT + JSON_UNESCAPED_UNICODE);

    echo $json;
} else {
    echo $sql;
}