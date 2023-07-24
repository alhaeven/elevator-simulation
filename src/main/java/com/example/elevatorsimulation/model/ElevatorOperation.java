package com.example.elevatorsimulation.model;

public interface ElevatorOperation {

    // about operation state
    OperationState terminate();
    OperationState startup();

}
