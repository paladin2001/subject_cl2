package subject;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Scholarship_table")
public class Scholarship {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private String subjectNm;
    private String grade;
    private String studentNm;
    private String studentAccount;
    private Integer amount;
    private String subjectId;

    @PostPersist
    public void onPostPersist(){
        Gave gave = new Gave();
        BeanUtils.copyProperties(this, gave);
        gave.setAmount(gave.getAmount());
        gave.setGrade(gave.getGrade());
        gave.publishAfterCommit();


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
    public String getStudentAccount() {
        return studentAccount;
    }

    public void setStudentAccount(String studentAccount) {
        this.studentAccount = studentAccount;
    }
    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }




}
