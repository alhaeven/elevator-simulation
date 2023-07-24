package com.example.elevatorsimulation.service;

import com.example.elevatorsimulation.Config;
import com.example.elevatorsimulation.model.Customer;
import com.example.elevatorsimulation.model.Direction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElevatorMonitoringService {

    private ElevatorControlManager elevatorControlManager = ElevatorControlManager.getInstance();

    public int currentFloorOfElevator() {
        return elevatorControlManager.getElevator().getCurrentFloor();
    }

    public Direction directionOfElevator() {
        return elevatorControlManager.getElevator().getDirection();
    }

    public boolean addNewCustomer(int floor, int target) {
        if(validateFloorNum(floor) && validateFloorNum(target)) {
            elevatorControlManager.requestFromFloor(floor, new Customer(target));
            return true;
        }
        return false;
    }

    private boolean validateFloorNum(int floor) {
        if(floor >= 1 && floor <= Config.FLOOR_SIZE) {
            return true;
        }
        return false;
    }

    public Long currentPersonCountInElevator() {
        return elevatorControlManager.getElevator().getInnerRequestAndCustomersMap().entrySet().stream()
                                     .flatMap(x -> x.getValue().stream()).count();
    }

    public List<Integer> currentActiveButton() {
        return elevatorControlManager.getElevator().getInnerRequestAndCustomersMap().entrySet().stream()
                                     .map(x -> x.getKey()).collect(
                        Collectors.toList());
    }
}
