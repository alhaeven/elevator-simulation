package com.example.elevatorsimulation.model;

public interface ElevatorInnerRequest {

    // 엘리베이터 내부 요청
    boolean requestFloor(int floor);

    // 필요시 확장 구현
    // boolean cancelRequestFloor(int floor);


}
