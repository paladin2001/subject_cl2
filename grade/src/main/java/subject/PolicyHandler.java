package subject;

import subject.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            //grade.setGrade(registered.getGrade());
            grade.setStudentNm(registered.getStudentNm());
            gradeRepository.save(grade);

            System.out.println("##### listener  : " + registered.toJson());
        }
    }

}
