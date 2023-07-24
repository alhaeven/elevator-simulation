package com.example.elevatorsimulation.model;

public interface ElevatorOuterRequest {

    // 외부 호출
    boolean callDown(int floor);
    boolean callUp(int floor);

}
