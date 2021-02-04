package subject;

import subject.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MypageViewHandler {


    @Autowired
    private MypageRepository mypageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRegistered_then_CREATE_1(@Payload Registered registered) {
        try {
            if (registered.isMe()) {
                // view 객체 생성
                Mypage mypage = new Mypage();
                // view 객체에 이벤트의 Value 를 set 함
                mypage.setId(registered.getId());
                mypage.setSubjectNm(registered.getSubjectNm());
                mypage.setProfessorNm(registered.getProfessorNm());
                mypage.setStudentNm(registered.getStudentNm());
                mypage.setTime(registered.getTime());
                mypage.setCredit(registered.getCredit());
                // view 레파지 토리에 save
                mypageRepository.save(mypage);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenReceived_then_UPDATE_1(@Payload Received received) {
        try {
            if (received.isMe()) {
                // view 객체 조회
                List<Mypage> mypageList = mypageRepository.findBySubjectId(received.getSubjectid());
                for(Mypage mypage : mypageList){

                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setGrade(received.getGrade());
                    // view 레파지 토리에 save
                    mypageRepository.save(mypage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenGave_then_UPDATE_2(@Payload Gave gave) {
        try {
            if (gave.isMe()) {
                // view 객체 조회
                List<Mypage> mypageList = mypageRepository.findBySubjectId(gave.getSubjectId());
                for(Mypage mypage : mypageList){
                    // view 객체에 이벤트의 eventDirectValue 를 set 함
                    mypage.setStudentAccount(gave.getStudentAccount());
                    mypage.setAmount(gave.getAmount());
                    // view 레파지 토리에 save
                    mypageRepository.save(mypage);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCancelled_then_DELETE_1(@Payload Cancelled cancelled) {
        try {
            if (cancelled.isMe()) {
                // view 레파지 토리에 삭제 쿼리
                mypageRepository.deleteById(cancelled.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}