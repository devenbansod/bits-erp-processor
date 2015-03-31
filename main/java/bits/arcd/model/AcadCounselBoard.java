package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AcadCounselBoard {
	private String studentId;
	private EligibilitySheetQueries elSheet;
	private DBConnector dbConnector = DBConnector.getInstance();
	private boolean cgpaCondition;
	private boolean eGradeCondition;
	private boolean isACB;
	private boolean courseNumCondition;

	public boolean isCourseNumCondition() {
		return courseNumCondition;
	}

	private void setCourseNumCondition() {
		int prevTerm = elSheet.getPrevTerm();
		int[] ys = getSemTerm(prevTerm);
		if(ys[0] == 0 && ys[1] == 0){
			//TODO What to do if PS returned
			//Check if PS is Done
			ys = getSemTerm(prevTerm - 1);
		}
		int yearNo = 0, semNo = 0;
		int courseToBeDone = 0, coursesDone;
		while(yearNo <= ys[0]){
			if(yearNo == ys[0]){
				while(semNo < ys[1]){
					
				}
			}
			while(semNo <=2){
				
			}
		}
	}

	//	private ArrayList<Course> eGradeList;
	private ArrayList<String> reasonList;
	
	public boolean iseGradeCondition() {
		return eGradeCondition;
	}

	private void seteGradeCondition() {
		if(eGrades().size() >=2){
			this.eGradeCondition = true;
		} else {
			this.eGradeCondition = false;
		}
	}



	public AcadCounselBoard(String studentId){
		setStudentId(studentId);
		elSheet = new EligibilitySheetQueries(studentId, 1131);
		setCgpaConstraint();

	}

	public String getStudentId() {
		return studentId;
	}

	private void setStudentId(String studentId) {
		this.studentId = studentId;
	}

	public boolean getCgpaConstraint() {
		return cgpaCondition;
	}

	public void setCgpaConstraint() {
		String systemId = elSheet.getSystemId();
		double cgpa = -1;
		String query = "SELECT CGPA FROM student_terms st WHERE sys_id = '" +systemId+ "' AND semester = (SELECT MAX(semester) FROM student_terms WHERE sys_id = '" +systemId+ "')";
		ResultSet rs = null;
		try {
			rs = dbConnector.queryExecutor(query, false);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		try {
			while (rs.next()){
				cgpa =  Double.parseDouble(rs.getString(1));

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(cgpa <= 4.5){
			this.cgpaCondition = true;
		} else {
			this.cgpaCondition = false;
		}
	}

	private ArrayList<Course> eGrades(){
		//eGrades
		ArrayList<Course> eGradesList = null;
		for(Semester sem: elSheet.getChart().getSemsInChart()){
			for(Course c : sem.getAllCourses()){
				if(c.getIsDoneInPrevSem() && c.getGrade().equalsIgnoreCase("E")){
					eGradesList.add(c);
				}
			}
		}
		return eGradesList;
	}


	private void isAcb(){
		
		int reasons = 0;
		//CGPA Condition
		if(cgpaCondition){
			this.isACB = true;
			reasons++;
			String sr = reasons + ". " + "CGPA is less than 4.5";
			reasonList.add(sr);
		}
		
		//Number of E Grades Condition
		if(eGradeCondition){
			if(eGradeCondition){
				reasons++;
				String sr1 = reasons + ". " + "E grade in " +eGrades().size()+ " courses:";
				int i = 0;
				for(Course c: eGrades()){
					i++;
					sr1 = sr1 + "\n\t\t" +i+ ". " + c.getSubject()+ " " +c.getCatalog()+ "\t" +c.getDescription();
				}
				reasonList.add(sr1);
			}
			
		}
		if(courseNumCondition){
			reasons++;
			String sr2 = reasons + ". " + "More than 2/3rd structured courses have not been completed";
			
			reasonList.add(sr2);
		}
	}

	public int[] getSemTerm(int term){
		int[] ys = {-1,-1};
		String query = "SELECT sem_description FROM terms WHERE semester = '" +term+ "'";
		ResultSet rs = null;
		String x = null;
		try {
			rs = dbConnector.queryExecutor(query, false);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		try {
			while (rs.next()){
				x = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		if(x.contains("Summer")){
			ys[0] = 0;
			ys[1] = 0;
		} else {
			if(x.contains("First")){
				ys[0] = Integer.parseInt(x.substring(x.length() - 2)) + 2000 - Integer.parseInt(studentId.substring(0, 4));
				ys[1] = 1;
			}
			if(x.contains("Second")){
				ys[0] = Integer.parseInt(x.substring(x.length() - 2)) + 2000 - Integer.parseInt(studentId.substring(0, 4));
				ys[1] = 2;
			}
		}
		return ys;
	}
}

