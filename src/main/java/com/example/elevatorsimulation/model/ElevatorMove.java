package com.example.elevatorsimulation.model;

import java.util.List;

public interface ElevatorMove {

    boolean pause();
    boolean move();

    boolean enterCustomer(List<Customer> customers);

    List<Customer> leaveCustomer();

}
