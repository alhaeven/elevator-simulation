package com.example.elevatorsimulation.controller;

import com.example.elevatorsimulation.Config;
import com.example.elevatorsimulation.service.ElevatorMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final ElevatorMonitoringService service;

    @GetMapping("/maxfloor")
    public int getMaxFloor() {
        int max = Config.FLOOR_SIZE;
        log.debug("config max floor : {}", max);
        return max;
    }

    @GetMapping("/direction")
    public String getDirection() {
        String direction = service.directionOfElevator().toString();
        log.debug("direction : {}", direction);
        return direction;
    }

    @GetMapping("/current")
    public int getCurrentFloor() {
        int currentFloor = service.currentFloorOfElevator();
        log.debug("current floor : {}", currentFloor);
        return currentFloor;
    }

    @GetMapping("/person")
    public long getPersonCount() {
        long personCnt = service.currentPersonCountInElevator();
        log.debug("person count in elevator : {}", personCnt);
        return personCnt;
    }

    @GetMapping("inner/req")
    public List<Integer> getInnerReqList() {
        List<Integer> reqFloorList = service.currentActiveButton();
        log.debug("active floor : {}", reqFloorList);
        return reqFloorList;
    }

    @PostMapping("/from/{from}/to/{to}")
    public ResponseEntity createCustomer(@PathVariable("from") String from, @PathVariable("to") String to) {
        try {
            int fromi = Integer.valueOf(from);
            int toi = Integer.valueOf(to);
            log.debug("request new customer / from : {} / to : {}", fromi, toi);

            boolean re = service.addNewCustomer(fromi, toi);
            if(!re) {
                return new ResponseEntity("Not validate number", HttpStatus.BAD_REQUEST);
            }

            return ResponseEntity.ok("success");
        } catch (NumberFormatException e) {
            return new ResponseEntity("Not Number",HttpStatus.BAD_REQUEST);
        }
    }

}
