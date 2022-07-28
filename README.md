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
    - Controller
    - Service
    - Domain
    - Repository

- Domain
  - Entity Table setting class "자바 객체가 아닌 설정 의 클래스"
  - @Getter @Setter @NoArgsConstructor @AllArgsConstruer
- Repository
  - Entity 내용을 저장 interface 임
- Service
  - Repository 를 활용할 아이
  - Transactional
- Controller
  - 외부에서 접속할떄 여기로 접속해서 통해서 service 로 이동
  - GetMapping