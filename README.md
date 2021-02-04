# subject_cl2
# Subject

# 서비스 시나리오
## 기능적 요구사항
1. 학생이 수강신청을 한다 (register Subject)
2. 학생은 수강신청을 취소할 수 있다 (cancel Register)
3. 교수는 수강신청되면 학점을 부여할 수 있다 (receive Grade)
4. 학생은 장학금 혹은 위로금을 받을 수 있다 (give Scholarship)
5. 학생은 수강상태 및 장학금/위로금 수신현황을 확인할 수 있어야 한다 (Mypage) 
    

## 비기능적 요구사항
1. 트랜잭션
    1. 학생은 장학금 혹은 위로금을 받을 수 있다 → Sync 호출
1. 장애격리
    1. 학점 서비스가 정상 기능이 되지 않더라도 수강신청을 받을 수 있다 → Async (event-driven), Eventual Consistency
    1. 수강신청 서비스가 과중되면 수강신청을 잠시동안 받지 않고 잠시후에 하도록 유도한다 → Circuit breaker, fallback
1. 성능
    1. 학생은 수강상태 및 장학금/위로금 수신현황을 확인할 수 있어야 한다 → CQRS

# 체크포인트
https://workflowy.com/s/assessment/qJn45fBdVZn4atl3

# 분석/설계
## AS-IS 조직 (Horizontally-Aligned)
![image](https://user-images.githubusercontent.com/16534043/106468971-f7a2e880-64e1-11eb-9e3e-faf334166094.png)
## TO-BE 조직 (Vertically-Aligned)
![image](https://user-images.githubusercontent.com/16534043/106469623-de4e6c00-64e2-11eb-9c5d-bd3d43fa6340.png)
## EventStorming 결과
### 완성된 1차 모형
![image]![모델](https://user-images.githubusercontent.com/25020453/106843894-70749100-66ea-11eb-8313-ae7831ef1361.PNG)

### 1차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증
![모델_검증](https://user-images.githubusercontent.com/25020453/106844222-50919d00-66eb-11eb-9d44-04fd2144782e.PNG)
```
1. 학생이 수강신청을 한다 (register Subject) -> OK
2. 학생은 수강신청을 취소할 수 있다 (cancel Register) -> OK
3. 교수는 수강신청되면 학점을 부여할 수 있다 (receive Grade) -> OK
4. 학생은 장학금 혹은 위로금을 받을 수 있다 (give Scholarship) -> OK
5. 학생은 수강상태 및 장학금/위로금 수신현황을 확인할 수 있어야 한다 (Mypage) -> OK 
```
## 헥사고날 아키텍쳐 다이어그램 도출 (Polyglot)
!![헥사고날2](https://user-images.githubusercontent.com/25020453/106844529-242a5080-66ec-11eb-9d30-735e4eee6092.png)

# 구현
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8084, 8088 이다)
```
cd subject
mvn spring-boot:run  

cd grade
mvn spring-boot:run

cd scholarship
mvn spring-boot:run 

cd mypage
mvn spring-boot:run  

cd gateway
mvn spring-boot:run  
```

## DDD 의 적용
msaez.io 를 통해 구현한 Aggregate 단위로 Entity 를 선언 후, 구현을 진행하였다.

Entity Pattern 과 Repository Pattern 을 적용하기 위해 Spring Data REST 의 RestRepository 를 적용하였다.

```java
package subject;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Subject_table")
public class Subject {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String subjectNm;
    private String time;
    private String professorNm;
    private Integer credit;
    private String studentNm;

    @PostPersist
    public void onPostPersist(){
        Registered registered = new Registered();
        BeanUtils.copyProperties(this, registered);
        registered.publishAfterCommit();

        Cancelled cancelled = new Cancelled();
        BeanUtils.copyProperties(this, cancelled);
        cancelled.publishAfterCommit();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getSubjectNm() {
        return subjectNm;
    }

    public void setSubjectNm(String subjectNm) {
        this.subjectNm = subjectNm;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public String getProfessorNm() {
        return professorNm;
    }

    public void setProfessorNm(String professorNm) {
        this.professorNm = professorNm;
    }
    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }
    public String getStudentNm() {
        return studentNm;
    }

    public void setStudentNm(String studentNm) {
        this.studentNm = studentNm;
    }
}

```

- 적용 후 REST API의 테스트를 통하여 정상적으로 동작하는 것을 확인할 수 있었다.
    - 수강신청 (register Subject)

  ![image](https://user-images.githubusercontent.com/25020453/106845642-b895b280-66ee-11eb-9584-312580e8eea2.PNG)

    - 수강신청내역 조회
  ![1_MYPAGE](https://user-images.githubusercontent.com/25020453/106845648-ba5f7600-66ee-11eb-8be4-ce88e3293a02.PNG)
  ![1_MYPAGE2](https://user-images.githubusercontent.com/25020453/106845651-bc293980-66ee-11eb-979a-60a8743fa27d.PNG)


## Gateway 적용
API Gateway를 통하여 마이크로 서비스들의 진입점을 통일하였다.
```yaml
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: subject
          uri: http://localhost:8081
          predicates:
            - Path=/subjects/**
        - id: grade
          uri: http://localhost:8082
          predicates:
            - Path=/grades/**
        - id: mypage
          uri: http://localhost:8083
          predicates:
            - Path= /mypages/**
        - id: scholarship
          uri: http://localhost:8084
          predicates:
            - Path=/scholarships/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: subject
          uri: http://subject:8080
          predicates:
            - Path=/subjects/**
        - id: grade
          uri: http://grade:8080
          predicates:
            - Path=/grades/**
        - id: mypage
          uri: http://mypage:8080
          predicates:
            - Path= /mypages/**
        - id: scholarship
          uri: http://scholarship:8080
          predicates:
            - Path=/scholarships/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080

```


## 폴리그랏 퍼시스턴스
- subject 서비스는 고성능 & INSERT 위주의 DB라서, HSQL을 사용하여 구현하였다. 이를 통해, 마이크로 서비스 간 서로 다른 종류의 데이터베이스를 사용해도 문제 없이 동작하여 폴리그랏 퍼시스턴스를 충족시켰다.
- subject 서비스의 pom.xml 일부
```java
		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<version>2.4.1</version>
		</dependency>
```


## 유비쿼터스 랭귀지
- 조직명, 서비스 명에서 사용되고, 업무현장에서도 쓰이며, 모든 이해관계자들이 직관적으로 의미를 이해할 수 있도록 영어 단어를 Full Name으로 사용 (subject, grade, scholarship 등)

## 동기식 호출(Req/Res 방식)과 Fallback 처리
- 분석단계에서의 조건 중 하나로 학점부여(receivded)와 장학금/위로금 부여(give Scholarship) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.
- 배송 취소 서비스를 호출하기 위하여 FeignClient를 이용하여 Service 대행 인터페이스(Proxy)를 구현
```java
package subject.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="scholarship", url="${api.scholarship.url}")
public interface ScholarshipService {

    @RequestMapping(method= RequestMethod.GET, path="/scholarships")
    public void giveScholarship(@RequestBody Scholarship scholarship);

}
```

- 학점이 부여된 후(@PostPersist) 장학금/위로금 처리되도록 처리
```java
@PrePersist
public void onPostPersist(){
        Received received = new Received();
        BeanUtils.copyProperties(this, received);
        received.publishAfterCommit();

        System.out.println("***********CHECK 1******************");

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        subject.external.Scholarship scholarship = new subject.external.Scholarship();
        // mappings goes here
        scholarship.setStudentNm(this.getStudentNm());
        scholarship.setGrade(this.getGrade());
        scholarship.setSubjectNm(this.getSubjectNm());
        GradeApplication.applicationContext.getBean(subject.external.ScholarshipService.class).giveScholarship(scholarship);
        }
```        
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하여, 주문 취소 시스템에 장애가 나면 배송도 취소되지 않는다는 것을 확인
    - grade에서 직접 입력시 scholarship에도 신호 전달
      - grade에서 호출
    - Recipe 서비스와 Order 서비스가 둘 다 동시에 돌아가고 있을때 Recipe 서비스 실행시 이상 없음  
      - grade 호출
        ![image](https://user-images.githubusercontent.com/12531980/106556204-5f007d00-6562-11eb-8087-e0260a54d7bd.png)
      - scholarship에서 수신
        ![image](https://user-images.githubusercontent.com/25020453/106847639-a3228780-66f2-11eb-9e65-18b45e0d5a30.PNG)
        ![image](https://user-images.githubusercontent.com/25020453/106847644-a584e180-66f2-11eb-826e-c530bd7f780b.PNG)
    - scholarship 서비스 중지시 grade 호출
      - scholarship 서비스 중지 (Ctrl-C)  
        ![image](https://user-images.githubusercontent.com/25020453/106847654-a7e73b80-66f2-11eb-9f8c-0050a61551e8.PNG)
      - 에러발생  
        ![image](https://user-images.githubusercontent.com/25020453/106847656-a9b0ff00-66f2-11eb-9a59-b230c0dfca68.PNG)

## 비동기식 호출 (Pub/Sub 방식)
- Recipe.java 내에서 아래와 같이 서비스 Pub 구현
```java
//...
public class Recipe {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String recipeNm;
    private String cookingMethod;
    private String materialNm;
    private Integer qty;

    @PostPersist
    public void onPostPersist(){
        MaterialOrdered materialOrdered = new MaterialOrdered();
        BeanUtils.copyProperties(this, materialOrdered);
        materialOrdered.publishAfterCommit();
    }
    //...
}
```
- Grade.java 내 Policy Handler 에서 아래와 같이 Sub 구현
```java
//...
@Service
public class PolicyHandler{
    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @Autowired
    GradeRepository gradeRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegistered_(@Payload Registered registered){

        if(registered.isMe()){
            Grade grade = new Grade();
            grade.setSubjectNm(registered.getSubjectNm());

            grade.setStudentNm(registered.getStudentNm());
            gradeRepository.save(grade);

            System.out.println("##### listener  : " + registered.toJson());
        }
    }
}
```
- 비동기식 호출은 다른 서비스가 비정상이여도 이상없이 동작가능하여, grade 서비스에 장애발생시에도 수강신청 서비스는 정상 동작을 확인
    - Recipe 서비스와 Order 서비스가 둘 다 동시에 돌아가고 있을때 Recipe 서비스 실행시 이상 없음  
        - subject 서비스 실행시 grade 정상 전달
      ![image](https://user-images.githubusercontent.com/25020453/106848737-e1b94180-66f4-11eb-9e30-c0b0f88bc0a2.PNG)
      ![image](https://user-images.githubusercontent.com/25020453/106848741-e3830500-66f4-11eb-8446-bd551baf6389.PNG)
      ![image](https://user-images.githubusercontent.com/25020453/106848748-e5e55f00-66f4-11eb-9d61-4131b2bab373.PNG)
        - grade 서비스를 내림      
      ![image](https://user-images.githubusercontent.com/25020453/106848766-ec73d680-66f4-11eb-8590-03baa5c6611a.PNG)
        - subject 호출시에도 장애없이 진행
      ![image](https://user-images.githubusercontent.com/25020453/106848773-ef6ec700-66f4-11eb-99d5-bf21d72366c8.PNG)      
      

## CQRS
viewer를 별도로 구현하여 아래와 같이 view 가 출력된다.
- MaterialOrdered 수행 후의 mypage  
  ![image](https://user-images.githubusercontent.com/12531980/106606835-ecb18c00-65a5-11eb-85fa-9342cc8bef3d.png)
- OrderCanceled 수행 후의 mypage
  ![image](https://user-images.githubusercontent.com/12531980/106606970-17034980-65a6-11eb-91e3-55c4e31a7e36.png)


# 운영
## CI/CD 설정
- git에서 소스 가져오기
```
git clone http://github.com/paladin2001/subject_cl2
```
- Build 하기
```
cd /subject
cd subject
mvn package

cd ..
cd grade
mvn package

cd ..
cd scholarship
mvn package

cd ..
cd mypage
mvn package

cd ..
cd gateway
mvn package

```
- Dockerlizing, ACR(Azure Container Registry에 Docker Image Push하기
```
cd /subject
cd subject
az acr build --registry skccuser06 --image skccuser06.azurecr.io/subject:v1 .

cd ..
cd order
az acr build --registry skccuser06 --image skccuser06.azurecr.io/grade:v1 .

cd ..
cd delivery
az acr build --registry skccuser06 --image skccuser06.azurecr.io/scholarship:v1 .

cd ..
cd gateway
az acr build --registry skccuser06 --image skccuser06.azurecr.io/gateway:v1 .

cd ..
cd mypage
az acr build --registry skccuser06 --image skccuser06.azurecr.io/mypage:v1 .
```
- ACR에서 이미지 가져와서 Kubernetes에서 Deploy하기
```
kubectl create deploy recipe --image=skccteam02.azurecr.io/recipe:v1
kubectl create deploy order --image=skccteam02.azurecr.io/order:v1
kubectl create deploy delivery --image=skccteam02.azurecr.io/delivery:v1
kubectl create deploy gateway --image=skccteam02.azurecr.io/gateway:v1
kubectl create deploy mypage --image=skccteam02.azurecr.io/mypage:v1
kubectl get all
```
- Kubectl Deploy 결과 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106553685-34f88c00-655d-11eb-87cb-e59a6f920a5b.png)
- Kubernetes에서 서비스 생성하기 (Docker 생성이기에 Port는 8080이며, Gateway는 LoadBalancer로 생성)
```
kubectl expose deploy recipe --type="ClusterIP" --port=8080
kubectl expose deploy order --type="ClusterIP" --port=8080
kubectl expose deploy delivery --type="ClusterIP" --port=8080
kubectl expose deploy gateway --type="LoadBalancer" --port=8080
kubectl expose deploy mypage --type="ClusterIP" --port=8080
kubectl get all
```
- Kubectl Expose 결과 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106554016-e0a1dc00-655d-11eb-8439-f4326cecda5a.png)
- 테스트를 위해서 Kafka zookeeper와 server도 별도로 실행 필요
- deployment.yaml 편집 후 배포 방안 적어두기
## 무정지 재배포
- 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 CB 설정을 제거함
- siege 로 배포작업 직전에 워크로드를 모니터링 함
```
siege -c100 -t60S -r10 -v http get http://delivery:8080/deliveries
```
- Readiness가 설정되지 않은 yml 파일로 배포 진행  
  ![image](https://user-images.githubusercontent.com/16534043/106564492-a261e800-6570-11eb-9b2b-31fca5350825.png)
```
kubectl apply -f deployment_without_readiness.yml
```
- 아래 그림과 같이, Kubernetes가 준비가 되지 않은 delivery pod에 요청을 보내서 siege의 Availability 가 100% 미만으로 떨어짐
- 중간에 socket에 끊겨서 siege 명령어 종료됨 (서비스 정지 발생)  
  ![image](https://user-images.githubusercontent.com/16534043/106564722-fb318080-6570-11eb-92d5-181e50772e8b.png)
- 무정지 재배포 여부 확인 전에, siege 로 배포작업 직전에 워크로드를 모니터링
```
siege -c100 -t60S -r10 -v http get http://delivery:8080/deliveries
```
- Readiness가 설정된 yml 파일로 배포 진행  
  ![image](https://user-images.githubusercontent.com/16534043/106564838-22884d80-6571-11eb-8cf1-dd0e53b547d7.png)
```
kubectl apply -f deployment_with_readiness.yml```
```
- 배포 중 pod가 2개가 뜨고, 새롭게 띄운 pod가 준비될 때까지, 기존 pod가 유지됨을 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106564937-52375580-6571-11eb-994f-b69acceb64b0.png)  
  ![image](https://user-images.githubusercontent.com/16534043/106565031-75620500-6571-11eb-9028-bd05d8125f04.png)
- siege 가 중단되지 않고, Availability가 높아졌음을 확인하여 무정지 재배포가 됨을 확인함  
  ![image](https://user-images.githubusercontent.com/16534043/106565135-a80bfd80-6571-11eb-943e-b3bd77c519db.png)

## 오토스케일 아웃
- 서킷 브레이커는 시스템을 안정되게 운영할 수 있게 해줬지만, 사용자의 요청이 급증하는 경우, 오토스케일 아웃이 필요하다.
- 단, 부하가 제대로 걸리기 위해서, recipe 서비스의 리소스를 줄여서 재배포한다.
```
kubectl apply -f - <<EOF
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: recipe
    namespace: default
    labels:
      app: recipe
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: recipe
    template:
      metadata:
        labels:
          app: recipe
      spec:
        containers:
          - name: recipe
            image: skccteam02.azurecr.io/recipe:v1
            ports:
              - containerPort: 8080
            resources:
              limits:
                cpu: 500m
              requests:
                cpu: 200m
EOF
```
- 다시 expose 해준다.
```
kubectl expose deploy recipe --type="ClusterIP" --port=8080

```
- recipe 시스템에 replica를 자동으로 늘려줄 수 있도록 HPA를 설정한다. 설정은 CPU 사용량이 15%를 넘어서면 replica를 10개까지 늘려준다.
```
kubectl autoscale deploy recipe --min=1 --max=10 --cpu-percent=15
```
- hpa 설정 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106558142-9709bf00-6566-11eb-9340-12959204fee8.png)
- hpa 상세 설정 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106558218-b3a5f700-6566-11eb-9b74-0c93679d2b31.png)
  ![image](https://user-images.githubusercontent.com/16534043/106558245-c0c2e600-6566-11eb-89fe-8a6178e1f976.png)
- - siege를 활용해서 워크로드를 2분간 걸어준다. (Cloud 내 siege pod에서 부하줄 것)
```
kubectl exec -it (siege POD 이름) -- /bin/bash
siege -c1000 -t120S -r100 -v --content-type "application/json" 'http://recipe:8080/recipes POST {"recipeNm": "apple_Juice"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다.
```
watch kubectl get all
```
- siege 실행 결과 표시  
  ![image](https://user-images.githubusercontent.com/16534043/106560612-a12dbc80-656a-11eb-8213-5a07a0a03561.png)
- 오토스케일이 되지 않아, siege 성공률이 낮다.

- 스케일 아웃이 자동으로 되었음을 확인
  ![image](https://user-images.githubusercontent.com/16534043/106560501-75aad200-656a-11eb-99dc-fe585ef7e741.png)
- siege 재실행
```
kubectl exec -it (siege POD 이름) -- /bin/bash
siege -c1000 -t120S -r100 -v --content-type "application/json" 'http://recipe:8080/recipes POST {"recipeNm": "apple_Juice"}'
```
- siege 의 로그를 보아도 전체적인 성공률이 높아진 것을 확인 할 수 있다.  
  ![image](https://user-images.githubusercontent.com/16534043/106560930-3335c500-656b-11eb-8165-bcb066a03f15.png)

## Self-healing (Liveness Probe)
- delivery 시스템 yml 파일의 liveness probe 설정을 바꾸어서, liveness probe가 동작함을 확인
- liveness probe 옵션을 추가하되, 서비스 포트가 아닌 8090으로 설정
```
        livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8090
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```

- delivery에 liveness가 적용된 것을 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106566682-f7ebc400-6573-11eb-8452-ed693bdf1f17.png)
- delivery에 liveness가 발동되었고, 8090 포트에 응답이 없기에 Restart가 발생함   
  ![image](https://user-images.githubusercontent.com/16534043/106566789-210c5480-6574-11eb-8e71-ae11755e274f.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리
- istio 사용 (Destination Rule)
- Retry 적용
- Pool Ejection
## 모니터링, 앨럿팅
- Kiali 활용
- Jager 활용
## Canary Deploy
- istio로 실행
## 운영 유연성 - Persistence Volume, Persistence Volume Claim 적용
- yaml 파일로 만들어서 붙이기
## ConfigMap 적용
- ConfigMap을 활용하여 변수를 서비스에 이식한다.
- ConfigMap 생성하기
```
kubectl create configmap deliveryword --from-literal=word=Preparing
```  
- Configmap 생성 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106593940-c505f800-6594-11eb-9284-8e896b531f04.png)

- 소스 수정에 따른 Docker 이미지 변경이 필요하기에, 기존 Delivery 서비스 삭제
```
kubectl delete pod,deploy,service delivery
```
- Delivery 서비스의 PolicyHandler.java (delivery\src\main\java\searchrecipe) 수정
```
#30번째 줄을 아래와 같이 수정
#기존 항목 주석처리 후, Configmap으로 이식된 환경변수 호출
// delivery.setStatus("\"+process.env.delivery_status+ \"");
delivery.setStatus(" Delivery Status is " + System.getenv("STATUS"));
```
- Delivery 서비스의 Deployment.yml 파일에 아래 항목 추가하여 deployment_configmap.yml 생성 (아래 코드와 그림은 동일 내용)
```
          env:
            - name: STATUS
              valueFrom:
                configMapKeyRef:
                  name: deliveryword
                  key: word

```  
![image](https://user-images.githubusercontent.com/16534043/106592668-275df900-6593-11eb-9007-fb31717f34e8.png)
- Docker Image 다시 빌드하고, Repository에 배포하기
- Kubernetes에서 POD 생성할 때, 설정한 deployment_configmap.yml 파일로 생성하기
```
kubectl create -f deployment_config.yml
``` 
- Kubernetes에서 POD 생성 후 expose
- 해당 POD에 접속하여 Configmap 항목이 ENV에 있는지 확인  
  ![image](https://user-images.githubusercontent.com/16534043/106595482-faabe080-6596-11eb-9a73-f66fb5d61382.png)
- http로 전송 후, Status에 Configmap의 Key값이 찍히는지 확인
```
http post http://20.194.26.128:8080/recipes recipeNm=apple_Juice cookingMethod=Using_Mixer materialNm=apple qty=3
``` 
![image](https://user-images.githubusercontent.com/16534043/106603485-ae19d280-65a1-11eb-9fe5-773e1ad46790.png)


## Secret 적용
- secret 적용





