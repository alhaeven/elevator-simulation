package com.example.elevatorsimulation;


import com.example.elevatorsimulation.model.Customer;
import com.example.elevatorsimulation.model.Direction;
import com.example.elevatorsimulation.service.ElevatorControlManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ElevatorControlTests {

    private ElevatorControlManager control;

    @BeforeEach
    public void setUp() {
        control = ElevatorControlManager.getInstance();
    }

    @AfterEach
    public void tearDown() {
        control.dispose();
    }

    @Test
    public void initTest() {
        Assertions.assertEquals(control.getFloors().size(), Config.FLOOR_SIZE);
        Assertions.assertNotNull(control.getElevator());
        Assertions.assertEquals(control.getElevatorFutureMap().get(control.getElevator()).state(),
                Future.State.RUNNING);
        Assertions.assertEquals(control.getElevator().getCurrentFloor(), 1);
        Assertions.assertEquals(control.getElevator().getDirection(), Direction.STOP);
    }

    @Test
    public void addNewCustomerToBottomFloor() throws InterruptedException {
        int bottomFloor = 1;
        int targetFloor = 5;
        Customer customer = new Customer(targetFloor);
        control.requestFromFloor(bottomFloor, customer);
        Assertions.assertEquals(1, control.getFloors().get(bottomFloor).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(bottomFloor).isUpButton());

        Thread.sleep(2000L);

        Assertions.assertEquals(Direction.UP, control.getElevator().getDirection());
        Assertions.assertEquals(1, control.getElevator().getInnerRequestAndCustomersMap().size());
        Assertions.assertEquals(targetFloor, control.getElevator().getLastTargetFloor());

        Thread.sleep(6000L);

        Assertions.assertEquals(targetFloor, control.getElevator().getCurrentFloor());
        Assertions.assertEquals(Direction.STOP, control.getElevator().getDirection());
    }

    @Test
    public void addMultiCustomerUpSide() throws InterruptedException {
        int floor1 = 1;
        int target1 = 7;

        int floor2 = 4;
        int target2 = 6;

        int floor3 = 6;
        int target3 = 9;

        int floor4 = 10;
        int target4 = 15;

        int floor5 = 1;
        int target5 = 4;

        int floor6 = floor3;
        int target6 = 10;

        control.requestFromFloor(floor1, new Customer(target1));

        Assertions.assertEquals(1, control.getFloors().get(floor1).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor1).isUpButton());

        Thread.sleep(2000L);

        control.requestFromFloor(floor2, new Customer(target2));
        control.requestFromFloor(floor3, new Customer(target3));
        control.requestFromFloor(floor4, new Customer(target4));
        control.requestFromFloor(floor5, new Customer(target5));
        control.requestFromFloor(floor6, new Customer(target6));

        Assertions.assertEquals(1, control.getFloors().get(floor2).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor2).isUpButton());

        Assertions.assertEquals(2, control.getFloors().get(floor3).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor3).isUpButton());

        Assertions.assertEquals(1, control.getFloors().get(floor4).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor4).isUpButton());

        Assertions.assertEquals(1, control.getFloors().get(floor5).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor5).isUpButton());

        Thread.sleep(35000L);

        Assertions.assertEquals(target5, control.getElevator().getCurrentFloor());
        Assertions.assertEquals(Direction.STOP, control.getElevator().getDirection());
    }

    @Test
    public void addNewCustomerToTopFloor() throws InterruptedException {
        int topFloor = Config.FLOOR_SIZE;
        int targetFloor = 1;
        Customer customer = new Customer(targetFloor);
        control.requestFromFloor(topFloor, customer);

        Assertions.assertEquals(1, control.getFloors().get(topFloor).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(topFloor).isDownButton());

        Thread.sleep(2000L);

        Assertions.assertEquals(Direction.UP, control.getElevator().getDirection());
        Assertions.assertEquals(0, control.getElevator().getInnerRequestAndCustomersMap().size());

        Thread.sleep(18000L);

        Assertions.assertEquals(Direction.DOWN, control.getElevator().getDirection());
        Assertions.assertEquals(1, control.getElevator().getInnerRequestAndCustomersMap().size());
        Assertions.assertEquals(targetFloor, control.getElevator().getLastTargetFloor());

        Thread.sleep(17000L);

        Assertions.assertEquals(targetFloor, control.getElevator().getCurrentFloor());
        Assertions.assertEquals(Direction.STOP, control.getElevator().getDirection());

    }

    @Test
    public void addMultiCustomerDownSide() throws InterruptedException {
        int floor1 = 5;
        int target1 = 1;

        int floor2 = 4;
        int target2 = 2;

        int floor3 = 6;
        int target3 = 1;

        int floor4 = floor1;
        int target4 = 1;

        int floor5 = 7;
        int target5 = 1;

        control.requestFromFloor(floor1, new Customer(target1));

        Assertions.assertEquals(1, control.getFloors().get(floor1).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor1).isDownButton());

        Thread.sleep(3000L);

        control.requestFromFloor(floor2, new Customer(target2));
        control.requestFromFloor(floor3, new Customer(target3));
        control.requestFromFloor(floor4, new Customer(target4));

        Assertions.assertEquals(1, control.getFloors().get(floor2).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor2).isDownButton());

        Assertions.assertEquals(1, control.getFloors().get(floor3).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor3).isDownButton());

        Assertions.assertEquals(2, control.getFloors().get(floor4).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor4).isDownButton());


        Thread.sleep(5000L);

        control.requestFromFloor(floor5, new Customer(target5));
        Assertions.assertEquals(1, control.getFloors().get(floor5).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(floor5).isDownButton());

        Thread.sleep(17000L);

        Assertions.assertEquals(target5, control.getElevator().getCurrentFloor());
        Assertions.assertEquals(Direction.STOP, control.getElevator().getDirection());
    }

    @Test
    public void multiCustomUpDownSize1() throws InterruptedException {
        int upFloor = 1;
        int upTarget = 7;

        int downFloor = 6;
        int downTarget = 2;


        control.requestFromFloor(downFloor, new Customer(downTarget));

        Assertions.assertEquals(1, control.getFloors().get(downFloor).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(downFloor).isDownButton());

        Thread.sleep(2000L);

        control.requestFromFloor(upFloor, new Customer(upTarget));

        Assertions.assertEquals(1, control.getFloors().get(upFloor).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(upFloor).isUpButton());


        Thread.sleep(17000L);

        Assertions.assertEquals(upTarget, control.getElevator().getCurrentFloor());
        Assertions.assertEquals(Direction.STOP, control.getElevator().getDirection());
    }

    @Test
    public void multiCustomUpDownSide2() throws InterruptedException {
        int upFloor1 = 1;
        int upTarget1 = 8;

        int downFloor1 = 7;
        int downTarget1 = 2;

        int upFloor2 = 2;
        int upTarget2 = 8;

        int downFloor2 = 4;
        int downTarget2 = 1;

        control.requestFromFloor(downFloor1, new Customer(downTarget1));

        Assertions.assertEquals(1, control.getFloors().get(downFloor1).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(downFloor1).isDownButton());

        Thread.sleep(2000L);

        control.requestFromFloor(upFloor1, new Customer(upTarget1));
        control.requestFromFloor(upFloor2, new Customer(upTarget2));

        Assertions.assertEquals(1, control.getFloors().get(upFloor1).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(upFloor1).isUpButton());

        Assertions.assertEquals(1, control.getFloors().get(upFloor2).getUpWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(upFloor2).isUpButton());

        Thread.sleep(6000L);

        control.requestFromFloor(downFloor2, new Customer(downTarget2));

        Assertions.assertEquals(1, control.getFloors().get(downFloor2).getDownWaitingCustomer().size());
        Assertions.assertTrue(control.getFloors().get(downFloor2).isDownButton());

        Thread.sleep(17000L);

        Assertions.assertEquals(upTarget2, control.getElevator().getCurrentFloor());
        Assertions.assertEquals(Direction.STOP, control.getElevator().getDirection());

    }

    @Test
    public void concurrentRequestTest() throws InterruptedException {
        int upFloor1 = 1;
        int upTarget1 = 8;

        int downFloor1 = 7;
        int downTarget1 = 2;

        int upFloor2 = 6;
        int upTarget2 = 8;

        int downFloor2 = 4;
        int downTarget2 = 1;

        ExecutorService es = Executors.newFixedThreadPool(4);
        es.submit(() -> {
            control.requestFromFloor(downFloor1, new Customer(downTarget1));

            Assertions.assertEquals(1, control.getFloors().get(downFloor1).getDownWaitingCustomer().size());
            Assertions.assertTrue(control.getFloors().get(downFloor1).isDownButton());
        });

        es.submit(() -> {
            control.requestFromFloor(downFloor2, new Customer(downTarget2));

            Assertions.assertEquals(1, control.getFloors().get(downFloor2).getDownWaitingCustomer().size());
            Assertions.assertTrue(control.getFloors().get(downFloor2).isDownButton());
        });

        Thread.sleep(5000L);

        es.submit(() -> {
            control.requestFromFloor(upFloor1, new Customer(upTarget1));
            Assertions.assertEquals(1, control.getFloors().get(upFloor1).getUpWaitingCustomer().size());
            Assertions.assertTrue(control.getFloors().get(upFloor1).isUpButton());
        });

        es.submit(() -> {
            control.requestFromFloor(upFloor2, new Customer(upTarget2));

            Assertions.assertEquals(1, control.getFloors().get(upFloor2).getUpWaitingCustomer().size());
            Assertions.assertTrue(control.getFloors().get(upFloor2).isUpButton());
        });

        Thread.sleep(20000L);


    }
}
