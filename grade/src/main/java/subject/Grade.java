package subject;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Grade_table")
public class Grade {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String subjectNm;
    private String grade;
    private String studentNm;
    private String subjectId;

    @PostPersist
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
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
    public String getStudentNm() {
        return studentNm;
    }

    public void setStudentNm(String studentNm) {
        this.studentNm = studentNm;
    }
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }




}
