# 수강신청 MSA

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
![헥사고날2](https://user-images.githubusercontent.com/25020453/106844529-242a5080-66ec-11eb-9d30-735e4eee6092.png)

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
- subject 서비스는 고성능 & INSERT 위주의 DB라서, HSQL을 사용하여 구현. 이를 통해, 마이크로 서비스 간 서로 다른 종류의 데이터베이스를 사용해도 문제 없이 동작하여 폴리그랏 퍼시스턴스를 충족
- subjectp/pom.xml
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
- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하여, 장학금/위로금 시스템에 장애가 나면 학점부여도 되지 않는다는 것을 확인
    - Grade에서 직접 입력시 Scholarship에도 신호 전달
        - Grade에서 직접 호출
          ![image](https://user-images.githubusercontent.com/25020453/106847628-9ef66a00-66f2-11eb-9269-c57dacbe80ad.PNG)        
        - Scholarship에서 수신
          ![image](https://user-images.githubusercontent.com/25020453/106847639-a3228780-66f2-11eb-9e65-18b45e0d5a30.PNG)
          ![image](https://user-images.githubusercontent.com/25020453/106847644-a584e180-66f2-11eb-826e-c530bd7f780b.PNG)
    - Scholarship 서비스 중지시 Grade 호출
        - Scholarship 서비스 중지 (Ctrl-C)  
          ![image](https://user-images.githubusercontent.com/25020453/106847654-a7e73b80-66f2-11eb-9f8c-0050a61551e8.PNG)
        - Grade에서 호출시 에러발생  
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
    - Subject 서비스와 Grade 서비스가 둘 다 동시에 돌아가고 있을때 Recipe 서비스 실행시 이상 없음
        - Subject에서 요청시
          ![image](https://user-images.githubusercontent.com/25020453/106848737-e1b94180-66f4-11eb-9e30-c0b0f88bc0a2.PNG)
        - Grade 서비스 응답 
          ![image](https://user-images.githubusercontent.com/25020453/106848741-e3830500-66f4-11eb-8446-bd551baf6389.PNG)
          ![image](https://user-images.githubusercontent.com/25020453/106848748-e5e55f00-66f4-11eb-9d61-4131b2bab373.PNG)
    - Grade 서비스 장애시에, Subject에서 신호를 보내도 정상실행           
        - Grade 서비스를 내림      
          ![image](https://user-images.githubusercontent.com/25020453/106848766-ec73d680-66f4-11eb-8590-03baa5c6611a.PNG)
        - Subject 호출시에도 장애없이 진행
          ![image](https://user-images.githubusercontent.com/25020453/106848773-ef6ec700-66f4-11eb-99d5-bf21d72366c8.PNG)


## CQRS
viewer를 별도로 구현하여 아래와 같이 view 가 출력된다.
- Register Subject 수행 후의 Mypage  
  ![image](https://user-images.githubusercontent.com/25020453/106905358-8f514280-673f-11eb-88d0-c183801bc03f.png)
- Give Scholarship 수행 후의 Mypage
  ![image](https://user-images.githubusercontent.com/25020453/106906448-87de6900-6740-11eb-9636-80adbd0de5d3.png)


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
cd grade
az acr build --registry skccuser06 --image skccuser06.azurecr.io/grade:v1 .

cd ..
cd scholarship
az acr build --registry skccuser06 --image skccuser06.azurecr.io/scholarship:v1 .

cd ..
cd gateway
az acr build --registry skccuser06 --image skccuser06.azurecr.io/gateway:v1 .

cd ..
cd mypage
az acr build --registry skccuser06 --image skccuser06.azurecr.io/mypage:v1 .
```

- Azure 클러스터(AKS)에 레지스트리(ACR) 붙이기
```  
  az aks update -n skccuser06-aks -g skccuser06-rsrscgrp --attach-acr skccuser06
```  

- ACR에서 이미지 가져와서 Kubernetes에서 Deploy하기
```
kubectl create deploy subject --image=skccuser06.azurecr.io/subject:v1
kubectl create deploy grade --image=skccuser06.azurecr.io/grade:v1
kubectl create deploy scholarship --image=skccuser06.azurecr.io/scholarship:v1
kubectl create deploy gateway --image=skccuser06.azurecr.io/gateway:v1
kubectl create deploy mypage --image=skccuser06.azurecr.io/mypage:v1
kubectl get all
```
- Kubectl Deploy 결과 확인
  ![image](https://user-images.githubusercontent.com/25020453/106864996-eb02d800-670d-11eb-8122-9ee803a8793d.png)

- Kubernetes에서 서비스 생성하기 (Docker 생성이라서 Port는 8080으로, Gateway는 LoadBalancer로 생성)
```
kubectl expose deploy subject --type="ClusterIP" --port=8080
kubectl expose deploy grade --type="ClusterIP" --port=8080
kubectl expose deploy scholarship --type="ClusterIP" --port=8080
kubectl expose deploy gateway --type="LoadBalancer" --port=8080
kubectl expose deploy mypage --type="ClusterIP" --port=8080
kubectl get all
``` 

## 무정지 재배포
- Readiness 설정, 미설정 yml 파일을 각각 준비 
  (기존 deployment 파일로 변경시에는 변경적용 안되어서 2개 신규 생성))
  
  ![image](https://user-images.githubusercontent.com/25020453/106894919-ed2b5d80-6732-11eb-995e-5f810707cec4.png)
  ![image](https://user-images.githubusercontent.com/25020453/106895128-3085cc00-6733-11eb-8423-04327b74a87a.png)
- Readiness가 미설정된 deployment yml 파일로 중간에 배포할 준비  
```
 kubectl apply -f deployment_without_readiness.yml
```
- siege 명령어 준비 및 실행
```
siege -c5 -t60S -v --content-type "application/json" 'http://subject:8080 {"subjectNm": "math"}'
```
- siege 중간에 배포 변경시 Socket 끊김 및 siege 명령 종료 (서비스 정지)
  ![image](https://user-images.githubusercontent.com/16534043/106564722-fb318080-6570-11eb-92d5-181e50772e8b.png)
  
- Readiness가 설정된 yml 파일로 배포 준비  
  ![image](https://user-images.githubusercontent.com/16534043/106564838-22884d80-6571-11eb-8cf1-dd0e53b547d7.png)
- Readiness 설정된 deployment yml 파일로 중간에 배포할 준비
```
 kubectl apply -f deployment_readiness.yml
```
- siege 명령어 준비 및 실행
```
siege -c5 -t60S -v --content-type "application/json" 'http://subject:8080 {"subjectNm": "math"}'
```
- siege 가 중단되지 않고, Availability는 100%를 유지하며 무정지 재배포가 됨을 확인함
  ![image](https://user-images.githubusercontent.com/25020453/106892939-27473000-6730-11eb-9b5a-c343dbfc6679.png)

## 오토스케일 아웃
- 서킷 브레이커는 시스템을 안정되게 운영할 수 있게 해줬지만, 사용자의 요청이 급증하는 경우, 오토스케일 아웃이 필요하다.
- 단, 부하가 제대로 걸리기 위해서, Subject 서비스의 리소스를 줄여서 재배포한다.
```
kubectl apply -f - <<EOF
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: subject
    namespace: default
    labels:
      app: subject
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: subject
    template:
      metadata:
        labels:
          app: subject
      spec:
        containers:
          - name: subject
            image: skccuser06.azurecr.io/subject:v1
            ports:
              - containerPort: 8080
            resources:
              limits:
                cpu: 500m
              requests:
                cpu: 200m
EOF
```
- 다시 expose
```
kubectl expose deploy subject --type="ClusterIP" --port=8080
```
- subject 시스템에 replica를 자동으로 늘려줄 수 있도록 HPA를 설정한다. 설정은 CPU 사용량이 15%를 넘어서면 replica를 10개까지 늘려주는 것으로 작업 
```
kubectl autoscale deploy subject --min=1 --max=10 --cpu-percent=15
```
- hpa 설정 확인  
  ![image](https://user-images.githubusercontent.com/25020453/106895766-fc5edb00-6733-11eb-9ba4-51a8f8e6eadf.png)
- hpa 상세 설정 확인  
- siege를 활용해서 워크로드를 2분간 걸어준다. (Cloud 내 siege pod에서 부하줄 것)
```
kubectl exec -it (siege POD 이름) -- /bin/bash
siege -c200 -t120S -v --content-type "application/json" 'http://subject:8080 {"subjectNm": "math"}'
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다.
```
watch kubectl get all
```
- siege 실행 결과 표시 (스케일 아웃이 자동으로 되었음을 확인)
  ![701_autoscale](https://user-images.githubusercontent.com/25020453/106972298-ed0f7a00-6793-11eb-905a-cd37018472f8.png)

## Self-healing (Liveness Probe)
- delivery 시스템 yml 파일의 liveness probe 설정을 바꾸어서, liveness probe가 동작함을 확인하는 것이 목적
- liveness probe 옵션을 추가하되, 서비스 포트가 아닌 8100으로 설정
```
        livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8100
            initialDelaySeconds: 120
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```
- liveness 설정된 deployment yml 파일로 적용할 준비
```
 kubectl apply -f deployment_readiness.yml
```
- 시작전 상황 (이전 단계인 오토스케일에 따른 다수 POD 존재)
  ![image](https://user-images.githubusercontent.com/25020453/106898705-dd624800-6737-11eb-93c9-caacb995d79b.png)
- 적용 후 Subject에 liveness가 발동되었고, 8100 포트에 응답이 없기에 Restart가 발생함   
  ![702_liveness](https://user-images.githubusercontent.com/25020453/106972543-673ffe80-6794-11eb-9529-c29f17c24b73.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리
- Istio 사용 (Destination Rule)
```
kubectl create namespace istio-test-ns
kubectl label namespace istio-test-ns istio-injection=enabled
```  
![image](https://user-images.githubusercontent.com/25020453/106902563-55327180-673c-11eb-9f98-0ef9719d7e19.png)

- 해당 namespace에 기존 서비스 재배포
```
# kubectl로 deploy 실행 (실행 위치는 상관없음)
kubectl create deploy subject --image=skccuser06.azurecr.io/recipe:v1 -n istio-test-ns
kubectl create deploy grade --image=skccuser06.azurecr.io/recipe:v1 -n istio-test-ns
kubectl create deploy scholarship --image=skccuser06.azurecr.io/recipe:v1 -n istio-test-ns
kubectl create deploy gateway --image=skccuser06.azurecr.io/recipe:v1 -n istio-test-ns
kubectl create deploy mypage --image=skccuser06.azurecr.io/recipe:v1 -n istio-test-ns
```
- expose
```
# (주의) expose할 때, gateway만 LoadBalancer고, 나머지는 ClusterIP임
kubectl expose deploy subject --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy grade --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy scholarship --type="ClusterIP" --port=8080 -n istio-test-ns
kubectl expose deploy gateway --type="LoadBalancer" --port=8080 -n istio-test-ns
kubectl expose deploy mypage --type="ClusterIP" --port=8080 -n istio-test-ns
```
- Retry 적용
- Pool Ejection

## ConfigMap 적용
- ConfigMap을 활용하여 변수를 서비스에 이식
- ConfigMap 생성하기
```
kubectl create configmap subjectword --from-literal=word=SystemReady
```  
- Configmap 생성 확인  
