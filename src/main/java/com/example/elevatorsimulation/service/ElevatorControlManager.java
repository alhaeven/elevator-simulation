package com.example.elevatorsimulation.service;

import com.example.elevatorsimulation.Config;
import com.example.elevatorsimulation.model.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Getter
@Slf4j
public class ElevatorControlManager implements ElevatorOuterRequest, ElevatorControl {

    private static ElevatorControlManager instance = new ElevatorControlManager();
    //    private List<Elevator> elevatorGroup;
    private Elevator elevator;
    private Map<Elevator, ExecutorService> elevatorThreadMap;
    private Map<Elevator, Future> elevatorFutureMap;
    private Map<Integer, Floor> floors;

    private ElevatorControlManager() {
        init();
    }

    public static ElevatorControlManager getInstance() {
        return instance;
    }

    @Override
    public void init() {
        elevator = new Elevator(this, this::arriveInnerRequest);

        ExecutorService elevatorThreadService = Executors.newSingleThreadExecutor();
        Future future = elevatorThreadService.submit(elevator);

        elevatorFutureMap = new HashMap<>();
        elevatorFutureMap.put(elevator, future);

        elevatorThreadMap = new HashMap<>();
        elevatorThreadMap.put(elevator, elevatorThreadService);

        // only get ; not concurrent
        floors = new HashMap<>();
        for (int i = 0; i < Config.FLOOR_SIZE; i++) {
            int t = i + 1;
//            log.debug(ng());
            floors.put(t, new Floor(t, this));
        }
    }
    @Override
    public void dispose() {
        for(Map.Entry<Elevator, ExecutorService> entry : elevatorThreadMap.entrySet()) {
            entry.getKey().dispose();
            entry.getValue().shutdown();
        }
    }

    @Override
    public void requestFromFloor(int from, Customer customer) {
        if(this.floors.containsKey(from)) {
            this.floors.get(from).addNewCustomer(customer);
        }
    }

    @Override
    public void arriveInnerRequest(int floor, List<Customer> customers) {
        if (floors.containsKey(floor)) {
            floors.get(floor).enterFloorFromElevator(customers);
        }
    }

    @Override
    public List<Customer> arriveOutRequestUpFloor(int floor) {
        if (floors.containsKey(floor)) {
            return floors.get(floor).arriveRequestUp();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Customer> arriveOutRequestDownFloor(int floor) {
        if (floors.containsKey(floor)) {
            return floors.get(floor).arriveRequestDown();
        }
        return new ArrayList<>();
    }


    /**
     * 추후 elevator가 여러대가 되었을때 요청 elevator 선택 로직 추가 필요
     *
     * @param floor
     * @return
     */
    @Override
    public boolean callDown(int floor) {
        return elevator.callDown(floor);
    }

    /**
     * 추후 elevator가 여러대가 되었을때 요청 elevator 선택 로직 추가 필요
     *
     * @param floor
     * @return
     */
    @Override
    public boolean callUp(int floor) {
        return elevator.callUp(floor);
    }
}
