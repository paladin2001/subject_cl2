package subject;

public class Received extends AbstractEvent {

    private Long id;
    private String subjectId;
    private String subjectNm;
    private String studentNm;
    private String grade;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getSubjectid() {
        return subjectId;
    }

    public void setSubjectid(String subjectId) {
        this.subjectId = subjectId;
    }
    public String getSubjectNm() {
        return subjectNm;
    }

    public void setSubjectNm(String subjectNm) {
        this.subjectNm = subjectNm;
    }
    public String getStudentNm() {
        return studentNm;
    }

    public void setStudentNm(String studentNm) {
        this.studentNm = studentNm;
    }
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}