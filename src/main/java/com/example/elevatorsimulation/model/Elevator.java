package com.example.elevatorsimulation.model;

import com.example.elevatorsimulation.Config;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@Data
@Slf4j
public class Elevator implements ElevatorOperation, ElevatorMove, ElevatorInnerRequest, ElevatorOuterRequest, Runnable {

    private final ElevatorControl controlInstance;
    private final BiConsumer<Integer, List<Customer>> leaveCustomerCallback;
    // inner requests + inner customers
    private Map<Integer, List<Customer>> innerRequestAndCustomersMap = new HashMap<>();
    private Direction direction = Direction.STOP;
    private Map<Integer, Integer> callUpMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> callDownMap = new ConcurrentHashMap<>();
    private int lastTargetFloor = 1;
    private int currentFloor = 1;
    private OperationState operationState = OperationState.OPERATE;
    private boolean globalThreadFlag = true;

    // 프로그램 종료를 위한 shutdown method
    public void dispose() {
        globalThreadFlag = false;
    }

    /**
     * Deprecate
     * inner request set을 inner customers map과 통합으로 사용 안함
     *
     * @param floor 엘리베이터 내부 버튼 숫자
     * @return 성공여부
     */
    @Override
    public boolean requestFloor(int floor) {
        if (!innerRequestAndCustomersMap.containsKey(floor)) {
            innerRequestAndCustomersMap.put(floor, new ArrayList<>());
        }
        return true;
    }

    @Override
    public boolean pause() {
        this.direction = Direction.STOP;
        this.lastTargetFloor = -1;
        log.debug("last target floor / convert direction : {}", this.direction);

        return true;
    }

    @Override
    public boolean move() {
        if (this.direction == Direction.UP && this.currentFloor <= Config.FLOOR_SIZE) {
            log.debug("before up current floor : {}", this.currentFloor);
            this.currentFloor += 1;
            log.debug("after up current floor : {}", this.currentFloor);

            return true;
        } else if (this.direction == Direction.DOWN && this.currentFloor >= 1) {
            log.debug("before down current floor : {}", this.currentFloor);
            this.currentFloor -= 1;
            log.debug("after down current floor : {}", this.currentFloor);

            return true;
        }

        return false;
    }

    /**
     * 고객 탑승
     *
     * @param customers
     * @return
     */
    @Override
    public boolean enterCustomer(List<Customer> customers) {
        log.debug("[{} floor] floor -> elevator / enter customers : {}", this.currentFloor, customers);

        if (customers == null || customers.size() == 0) {
            return false;
        }

        if (operationState == OperationState.REPAIR || operationState == OperationState.TERMINATE) {
            return false;
        }

        customers.forEach(x -> {
//            requestFloor(x.getTargetFloor());
            if (x.getTargetFloor() != currentFloor) {
                innerRequestAndCustomersMap.merge(x.getTargetFloor(), new ArrayList<>() {{
                    add(x);
                }}, (a, b) -> {
                    a.addAll(b);
                    b = null;
                    return a;
                });
            }

            // calculate lastTargetFloor
            if(this.lastTargetFloor == -1) {
               this.lastTargetFloor = x.getTargetFloor();
            }
            else if (this.direction == Direction.UP) {
                this.lastTargetFloor = x.getTargetFloor() > this.lastTargetFloor ? x.getTargetFloor() : this.lastTargetFloor;
            } else if (this.direction == Direction.DOWN) {
                this.lastTargetFloor = x.getTargetFloor() < this.lastTargetFloor ? x.getTargetFloor() : this.lastTargetFloor;
            }
            log.debug("last target floor : {}", this.lastTargetFloor);

        });

        return true;
    }

    @Override
    public List<Customer> leaveCustomer() {
        if (this.innerRequestAndCustomersMap.containsKey(this.currentFloor)) {
            return this.innerRequestAndCustomersMap.remove(this.currentFloor);
        }
        return new ArrayList<>();
    }

    @Override
    public OperationState terminate() {
        this.operationState = OperationState.TERMINATE;
        return this.operationState;
    }

    @Override
    public OperationState startup() {
        this.operationState = OperationState.OPERATE;
        return this.operationState;
    }

    @Override
    public boolean callDown(int floor) {
        if (!callDownMap.containsKey(floor)) {
            callDownMap.put(floor, floor);
        }
        return true;
    }

    @Override
    public boolean callUp(int floor) {
        if (!callUpMap.containsKey(floor)) {
            callUpMap.put(floor, floor);
        }
        return true;
    }


    @Override
    public void run() {
        while (globalThreadFlag) {
            try {
                if (this.operationState == OperationState.TERMINATE || this.operationState == OperationState.REPAIR) {
                    continue;
                }

                // 도착한 층이 내부에서 도착하기를 원하던 층인지 확인
                if (this.innerRequestAndCustomersMap.containsKey(this.currentFloor)) {
                    leaveCustomerCallback.accept(this.currentFloor, leaveCustomer());
                }

                if (this.direction == Direction.STOP) {
                    // 외부 요청 확인 ; 요청상황에 따라 방향 설정
                    if (!this.callUpMap.keySet().isEmpty()) {
                        int min = callUpMap.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
                        if (min < this.currentFloor) {
                            this.direction = Direction.DOWN;
                            log.debug("convert direction : {}", this.direction);
                        } else {
                            this.direction = Direction.UP;
                            log.debug("convert direction : {}", this.direction);
                        }
                    } else if (!this.callDownMap.keySet().isEmpty()) {
                        int max = callDownMap.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
                        if (max > this.currentFloor) {
                            this.direction = Direction.UP;
                            log.debug("convert direction : {}", this.direction);
                        } else {
                            this.direction = Direction.DOWN;
                            log.debug("convert direction : {}", this.direction);
                        }
                    }
                }

                boolean enterFlag = false;
                switch (this.direction) {
                    case UP:
                        // 위로 올라가는 상황에서 현재 층이 위로 올라가기 요청(올라가기 버튼)이 있는 층인지 확인
                        if (this.callUpMap.containsKey(this.currentFloor)) {
                            // 외부 요청 확인되었으니 요청 지우기
                            this.callUpMap.remove(this.currentFloor);
                            enterCustomer(controlInstance.arriveOutRequestUpFloor(this.currentFloor));

                            enterFlag = true;
                        }
                        // 위로 올라가는 상황에서 위로 올라가고자하는 외부 요청이 없고 아래로 내려가고자 하는 요청이 있는 가장 높은 층인지 검사
                        else if (/*this.callUpMap.isEmpty() && */!this.callDownMap.isEmpty()) {
                            int max = callDownMap.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
                            if (max == this.currentFloor) {
                                this.callDownMap.remove(this.currentFloor);
                                this.direction = Direction.DOWN;
                                log.debug("convert direction : {}", this.direction);
                                enterCustomer(controlInstance.arriveOutRequestDownFloor(this.currentFloor));

                                enterFlag = true;
                            }
                        }
                        // 방향대로 움직임

                        break;
                    case DOWN:
                        // 아래로 내려가는 상황에서 현재 층이 아래로 내려가기 요청(내려가기 버튼)이 있는 층인지 확인
                        if (this.callDownMap.containsKey(this.currentFloor)) {
                            // 외부 요청 확인되었으니 지우기
                            this.callDownMap.remove(this.currentFloor);
                            enterCustomer(controlInstance.arriveOutRequestDownFloor(this.currentFloor));

                            enterFlag = true;
                        } else if (/*this.callDownMap.isEmpty() && */!this.callUpMap.isEmpty()) {
                            int min = callUpMap.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
                            if (min == this.currentFloor) {
                                this.callUpMap.remove(this.currentFloor);
                                this.direction = Direction.UP;
                                log.debug("convert direction : {}", this.direction);
                                enterCustomer(controlInstance.arriveOutRequestUpFloor(this.currentFloor));

                                enterFlag = true;
                            }
                        }
                        // 방향대로 움직임
                        break;
                }

                if(enterFlag) {
                    Thread.sleep(Config.MOVING_SPEED);
                }

                // 목표한 층까지 도착했다면 멈춤 전환 여부 검사및 전환
                if (this.direction != Direction.STOP && this.currentFloor == this.lastTargetFloor) {
                    if (this.direction == Direction.UP && !this.callUpMap.keySet().isEmpty()) {
                        int max = callUpMap.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
                        if (max < this.currentFloor) {
                            pause();
                        }
                    } else if (this.direction == Direction.UP && !this.callDownMap.keySet().isEmpty()) {
                        int max = callDownMap.keySet().stream().mapToInt(Integer::intValue).max().getAsInt();
                        if (max < this.currentFloor) {
                            pause();
                        }
                    } else if (this.direction == Direction.DOWN && !this.callDownMap.keySet().isEmpty()) {
                        int min = callDownMap.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
                        if (min > this.currentFloor) {
                            pause();
                        }
                    } else if (this.direction == Direction.DOWN && !this.callUpMap.keySet().isEmpty()) {
                        int min = callUpMap.keySet().stream().mapToInt(Integer::intValue).min().getAsInt();
                        if (min > this.currentFloor) {
                            pause();
                        }
                    } else {
                        pause();
                    }

                }

                move();
                Thread.sleep(Config.MOVING_SPEED);

            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
