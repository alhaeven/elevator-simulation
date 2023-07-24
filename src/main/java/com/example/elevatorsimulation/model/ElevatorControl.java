package com.example.elevatorsimulation.model;

import java.util.List;

public interface ElevatorControl {

    void init();

    void dispose();

    void requestFromFloor(int from, Customer customer);

    void arriveInnerRequest(int floor, List<Customer> customers);

    // floor에서 up 호출한 elevator 도착시 사용자 정보 전달
    List<Customer> arriveOutRequestUpFloor(int floor);

    // floor에서 down 호출한 elevator 도착시 사용자 정보 전달
    List<Customer> arriveOutRequestDownFloor(int floor);
}
