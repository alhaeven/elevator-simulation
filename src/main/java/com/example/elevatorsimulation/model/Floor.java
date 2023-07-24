package com.example.elevatorsimulation.model;

import com.example.elevatorsimulation.Config;
import com.example.elevatorsimulation.service.ElevatorControlManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Slf4j
public class Floor implements FloorButton {

    private final int floorNo;
    private final ElevatorOuterRequest outerRequest;

    private Queue<Customer> upWaitingCustomer = new ConcurrentLinkedQueue<>();
    private Queue<Customer> downWaitingCustomer = new ConcurrentLinkedQueue<>();

    private boolean downButton;
    private boolean upButton;

    public List<Customer> arriveRequestUp() {
        List<Customer> list = this.upWaitingCustomer.stream().toList();
        this.upWaitingCustomer.clear();
        this.upButton = false;
        return list;
    }

    public List<Customer> arriveRequestDown() {
        List<Customer> list = this.downWaitingCustomer.stream().toList();
        this.downWaitingCustomer.clear();
        this.downButton = false;
        return list;
    }

    public void addNewCustomer(Customer customer) {
        log.debug("[{} floor] add waiting new customer : {}", this.floorNo, customer);
        // validate
        if(customer.getTargetFloor() > Config.FLOOR_SIZE) {
            customer = new Customer(Config.FLOOR_SIZE);
        } else if(customer.getTargetFloor() < 1) {
            customer = new Customer(1);
        }

        if(customer.getTargetFloor() > floorNo) {
            upWaitingCustomer.add(customer);
            pushUpButton();
        } else if(customer.getTargetFloor() < floorNo) {
            downWaitingCustomer.add(customer);
            pushDownButton();
        }
    }

    public void enterFloorFromElevator(List<Customer> customers) {
        // temp
        log.debug("[{} floor] elevator -> floor / customer : {}", this.floorNo, customers);
        customers.clear();
    }

    @Override
    public boolean pushDownButton() {
        this.downButton = true;
        outerRequest.callDown(this.floorNo);
        return downButton;
    }

    @Override
    public boolean pushUpButton() {
        this.upButton = true;
        outerRequest.callUp(this.floorNo);
        return this.upButton;
    }

/*    @Override
    public boolean cancelUpButton() {
        return false;
    }

    @Override
    public boolean cancelDownButton() {
        return false;
    }*/
}
