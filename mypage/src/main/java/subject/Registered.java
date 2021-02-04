package subject;

public class Registered extends AbstractEvent {

    private Long id;
    private String subjectNm;
    private String professorNm;
    private String studentNm;
    private String time;
    private Integer credit;

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
}