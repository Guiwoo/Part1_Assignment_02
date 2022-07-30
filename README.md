# Misson2 Account

## Project Init
    - Sping 2.7.2
    - Validation
    - H2 DB
    - Lombok
    - JPA

- Add gradle redis client
  - implementation 'org.redisson:redisson:3.17.5'

- Add gradle embedded redis
  - implementation('it.ozimov:embedded-redis:0.7.3'){
  exclude group: "org.slf4j",module: "slf4j-simple"
  }

### Settings and Info
- Lombok ? 
  - @ 을 이용해 코드 를 자동생성해줌, 코드 를 간결하게 작성할수 있다.
  - AllArgsConstructor, RequiredArgsConstructor, Data(전부다)
  - UtilityClass "static method 제공해주는 클래스"

- H2 DB Setting
  - src > main > resources > application.properties
  - application.yml
```java
// In application.yml for setting
spring:
  datasource:
    url:  jdbc:h2:mem:test
    username: sa
    password:
    driverClassName: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    defer-datasource-initialization: true
    database-platform: H2
    hibernate:ctor @Build
    ddl-auto: create-drop
    open-in-view: false
    properties:
    hibernate:
    format_sql: true
    show_sql: true

```

- Embedded Redis
  - Memory Db ,No sql "휘발성 데이터"
  - 동시성 제어를 위한 곳에서 활용됨 
  ```java
  ///LocalRedisConfig
  @Configuration
  public class LocalRedisConfig {
  @Value("${spring.redis.port}")
  private int redisPort;
  
      private RedisServer redisServer;
  
      @PostConstruct
      public void startRedis(){
          redisServer = new RedisServer(redisPort);
          redisServer.start();
      }
  
      @PreDestroy
      public void stopRedis(){
          if(redisServer != null){
              redisServer.stop();
          }
      }
  }
  // RedisRepositryCOnfig
  @Configuration
  public class RedisRepositoryConfig {
  @Value("${spring.redis.host}")
  private String redisHost;
  
      @Value("${spring.redis.port}")
      private int redisPort;
  
      @Bean
      public RedissonClient redisClient(){
          Config config = new Config();
          config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort);
          return Redisson.create(config);
      }
  
  }
  ```

### Package Structures
- aop : Aop 로 중복거래 방지 락을 걸때 사용
- config : Redis 관련 설정 및 클라이언트 빈등록,jpa 관련 설정 등록
- controller : Api의 endPoint 등록, 요청/응답형식의 클래스
- domain : jap entity
- dto : DTO 를 위치 시키는 곳
  - Contorller 에서 요청 / 응답 에 사용할 클래스
  - 로직 내부에서 데이터 전송에 사용할 클래스
- exception : 커스텀 exception , exception 해들러 클래스 패키지
- repository : repository db연결 인터페이스가 위치
- service : 비즈니스 로직을 담는 서비스 클래스 패키지
- type : 상태타입, 에러코드, 거래종류 등의 다양한 enum class 패키지


#### 계좌생성    API
#### 계좌해지    API
#### 잔액사용    API
#### 잔액사용취소 API
