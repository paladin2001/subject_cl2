package subject;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="Mypage_table")
public class Mypage {

        @Id
        @GeneratedValue(strategy=GenerationType.AUTO)
        private Long id;
        private String subjectNm;
        private String professorNm;
        private String studentNm;
        private String time;
        private Integer credit;
        private String grade;
        private String studentAccount;
        private Integer amount;
        private String subjectId;


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
        public String getProfessorNm() {
            return professorNm;
        }

        public void setProfessorNm(String professorNm) {
            this.professorNm = professorNm;
        }
        public String getStudentNm() {
            return studentNm;
        }

        public void setStudentNm(String studentNm) {
            this.studentNm = studentNm;
        }
        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
        public Integer getCredit() {
            return credit;
        }

        public void setCredit(Integer credit) {
            this.credit = credit;
        }
        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
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
