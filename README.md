# Elevator-Simulation

## Environment

- Java 
- Spring Boot 2
- Gradle

<br>
설계 - 테스트 - 외부api

### 주요 Class
- Elevator
  - elevator 구현 객체
- Customer
  - elevator 사용자
- Floor
  - 이동하는 층
  - 위/아래 버튼 elevator호출
- ElevatorControl
  - 외부및 내부요청 연결 interface
<br>

그외 interface나 enum은 code 참조

### Config.java
- FLOOR_SIZE : 최고층 설정
- MOVING_SPEED : elevator 이동시 sleep 값

### Start
1. gradle 빌드 : gradle build -x test
2. jar 실행 : java -jar buildfile.jar
3. 브라우저 http://localhost:8080 접속
4. web page내 원하는 층수 입력후 요청 클릭

