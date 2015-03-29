package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

public class EligibilitySheetQueries {

	private String studentId;
	private String systemId;
	private String requirementNo;
	private String studentName;


	private CourseChartQueries chart;
	private DBConnector dbConnector;	

	private int prevTerm;	

	private String cgpa;
	private int cgpaCup, cgpaUnits;

	public EligibilitySheetQueries(String studentId, int term) {

		dbConnector = new DBConnector();

		this.studentId = studentId;
		setSystemId(this.studentId);
		setRequirementNo(this.studentId);
		setCgpa();
		chart = new CourseChartQueries(requirementNo);

		setPrevTerm(this.studentId);

		int pendingHuels = 0;
		int pendingDels = 0;
		int pendingOels = 0;

		ArrayList<Semester> semsInChart = chart.getSemsInChart();

		for (Semester sem : semsInChart)
		{				
			upgradeCompulsoryCoursesInSem(sem, term);
			addHuelsToSem(sem, term);
			addDelsToSem(sem,term);
			addOelsToSem(sem,term);
			addOptionalToSem(sem,term);
			if(isSummerTerm(sem)){
				addPracticeSchoolToSem(sem);
			}
			updatePendingElectives(sem, pendingHuels, pendingDels, pendingOels);	
			addAllCoursesToSem(sem);
		}

		updateCgpaCupAndUnits();

	}

	// Methods	

	public void updateCgpaCupAndUnits() {

		String query = "SELECT grade_points, total FROM student_terms WHERE sys_id = '" +systemId+ "' " ;
		//		System.out.println(query);
		// get cgpa cup and units from std_terms using systemId
		ResultSet r = dbConnector.queryExecutor(query, false);

		try {
			while(r.next()){
				this.cgpaCup += r.getInt(1);
				this.cgpaUnits += r.getInt(2);	
				//System.out.println(r.getInt(1) + " " +r.getInt(2));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addAllCoursesToSem(Semester sem) {
		sem.addAllCourses(sem.getCompulsoryCourses(), 
				sem.getHumanitiesCourses(), sem.getDelCourses(), 
				sem.getOpenElCourses(), sem.getOptionalCourse(), sem.getPS());
	}


	private void setStudentNameFromDatabase(){
		String query = "Select student_name from students where campus_id = '" + this.studentId + "'";

		ResultSet r = dbConnector.queryExecutor(query, false);

		try {

			while(r.next()){
				this.studentName = r.getString(1);
			}
		}
		catch(SQLException e) {

		}

	}


	private int maxof(int a, int b) {

		if (a > b)
			return a;
		else 
			return b;

	}


	public String getStudentName(){
		return this.studentName;
	}

	private String returnCenteredString(String s){

		int total_spaces = 135 - s.length();

		for(int i = 0; i < total_spaces/2; i++){
			s = " " + s + " ";
		}

		return s.toUpperCase();
	}



	@Override
	public String toString() {

		String s = "";
		int i = 0;

		//		s = s + "                                        "
		//				+ "                       BITS PILANI            "
		//				+ "                                                     \n";
		//		

		s = s + returnCenteredString("BITS Pilani") + "\n";

		s = s + returnCenteredString("ELIGIBLITY SHEET (VIDE A.R. 3.21)") + "\n\n";

		
		setStudentNameFromDatabase();

		String details = this.systemId + "  "  + this.studentId + "  " + getStudentName() + "  CGPA : "
				+ this.getCgpa() + "   REQ NUM : " + this.requirementNo + "   REQ GRP : " 
				+ this.getChart().getRequirementGroup() + "  ADMISSION : " + getAdmissionTerm();

		s = s + returnCenteredString(details) + "\n";


		s = s + "\nYEAR     COMP  COURSE NO  COURSE TITLE             UNITS  GRADES                ";
		s = s + "COMP  COURSE NO  COURSE TITLE             UNITS  GRADES                    ";

		s = s + "\n-----------------------------------------"
				+ "-----------------------------------------"
				+ "---------------------------------------------------------\n";
		//		for(Semester sem :this.getCh().getSemsInChart()){


		int rem = 1;
		for ( i = 0; i < this.getChart().getSemsInChart().size() - 1; i++) {

			if (this.getChart().getSemsInChart().get(i).isSummerTerm() != true) {

				if ( i % 2 == rem ) {
					continue;
				}

				else {

					Semester first = this.getChart().getSemsInChart().get(i);
					Semester second = this.getChart().getSemsInChart().get(i+1);


					ArrayList<String> sem1Courses = new ArrayList<String>();
					ArrayList<String> sem2Courses = new ArrayList<String>();


					// Add the Compulsory Courses string
					for (int j = 0; j < first.getCompulsoryCourses().size(); j++){
						if (j < first.getCompulsoryCourses().size()){
							sem1Courses.add(first.getCompulsoryCourses().get(j).toString());
						}
					}

					for (int j = 0; j < second.getCompulsoryCourses().size(); j++){
						if (j < second.getCompulsoryCourses().size()){
							sem2Courses.add(second.getCompulsoryCourses().get(j).toString());
						}
					}

					// Add the Humanities Courses Done
					for (int j = 0; j < first.getHumanitiesCourses().size(); j++){

						if (j < first.getHumanitiesCourses().size()){
							sem1Courses.add(first.getHumanitiesCourses().get(j).toString());
						}

					}

					for (int j = 0; j < second.getHumanitiesCourses().size(); j++){

						if (j < second.getHumanitiesCourses().size()){
							sem2Courses.add(second.getHumanitiesCourses().get(j).toString());
						}

					}


					// Add the Disp Electives done
					for (int j = 0; j < first.getDelCourses().size(); j++){

						if (j < first.getDelCourses().size()){
							sem1Courses.add(first.getDelCourses().get(j).toString());
						}

					}

					for (int j = 0; j < second.getDelCourses().size(); j++){

						if (j < second.getDelCourses().size()){
							sem2Courses.add(second.getDelCourses().get(j).toString());
						}

					}

					// Add the Open Electives done
					for (int j = 0; j < first.getOpenElCourses().size(); j++){

						sem1Courses.add(first.getOpenElCourses().get(j).toString());


					}

					for (int j = 0; j < second.getOpenElCourses().size(); j++){

						sem2Courses.add(second.getOpenElCourses().get(j).toString());

					}

					// Add the Hum Electives to be done
					for (int j = 0; j < first.getNoOfHUEL(); j++){
						String temp = "    ------------------------------------------------          HUEL  ";

						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfHUEL(); j++){
						String temp = "    ------------------------------------------------          HUEL  ";						
						sem2Courses.add(temp);

					}

					// Add the Open Electives to be done
					for (int j = 0; j < first.getNoOfOEL(); j++){
						String temp = "    ------------------------------------------------          EL   ";
						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfOEL(); j++){
						String temp = "    ------------------------------------------------          EL    ";
						sem2Courses.add(temp);
					}

					// Add the Disp Electives to be done
					/**
					for (int j = 0; j < first.getNoOfDEL(); j++){
						String temp = "   ------------------------------------------------          DEL   ";	
						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfDEL(); j++){
						String temp = "   ------------------------------------------------          DEL   ";

						sem2Courses.add(temp);

					}
					*/

					// For Optional Courses
					int t = 0;
					if (second.getOptionalCourse() != null) {
						t = 1;
					}

					for (int j = 0; j < t; j++){

						sem2Courses.add(second.getOptionalCourse().toString());

					}



					// Loop through both ArrayLists and concatenate


					for(int j = 0; j < maxof(sem1Courses.size(), sem2Courses.size()); j++){
						String temp = "";


						if (j < sem1Courses.size()) {
							if ( j == 0 ) {
								temp = "Year " + (i + 2)/2 + temp + sem1Courses.get(j);
							}
							else {
								temp = temp + "      " + sem1Courses.get(j);
							}

						}
						else {
							temp = temp + "      " + "                                                                   ";
						}


						if (j < sem2Courses.size()) {
							temp = temp + sem2Courses.get(j);
						}
						else {
							temp = temp + "                                                                   ";
						}

						temp = temp + "\n";

						s = s + temp;

					}

					s = s + "-----------------------------------------"
							+ "-----------------------------------------"
							+ "---------------------------------------------------------\n";
				}
			}

			else {

				Semester PS = this.getChart().getSemsInChart().get(i);
				s = s + "Summer" + PS.getPS().toString() + "\n";


				s = s + "-----------------------------------------"
						+ "-----------------------------------------"
						+ "---------------------------------------------------------\n";

				rem = 0;

			}

		}

		
		s =  s + "LEGEND : * - BACKLOG\t" + "$ - OPSC\t" + "|| - CURRENT SEM\t" 
				+ "% - PREV SEM PERFORMANCE\t" + "+ - EXEMPTED\t" + "# - DEBARRED TO REGISTER\n\n";
		
		s = s + "ACC CUP : " + this.cgpaCup + "  \tCGPA CUP : " 
				+ this.cgpaCup + "  \tACC UNITS : " + this.cgpaUnits
				+ "  \tCGPA UNITS : " + this.cgpaUnits + "\n";
		
		s = s + "-----------------------------------------"
				+ "-----------------------------------------"
				+ "---------------------------------------------------------\n";
		
		
		return "\n" + s;
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




	private void checkForRepeatAndSetFlag(Course c){
		int courseId = c.getCourseCode();

		// Go to std_enrl table. Give systemID_,courseID, &(units taken not equal to 0)
		/* DEV: AJINKYA
		SELECT sys_id, course, units_taken
		FROM student_enrollment
		WHERE sys_id = &systemID AND course_id = &courseID AND units_taken > 0;
		 */
		String sqlQuery = "SELECT * FROM student_enrollment WHERE sys_id = " 
				+systemId+ " AND course_id = " +courseId+ " AND units_taken > 0 ";
		ResultSet r2 = dbConnector.queryExecutor(sqlQuery, false);	

		int i=0;
		String repGrade = "";

		try {
			while(r2.next()) {
				if(i==0)
					repGrade = r2.getString(13);
				else
					repGrade = repGrade  + "/" + r2.getString(13) ;
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(i>1) {
			c.setRepeat("Y");
			c.setGrade(repGrade);
		}
	}

	private void upgradeCompulsoryCoursesInSem(Semester sem, int term) {

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		ArrayList<Course> compulsoryCourses = sem.getCompulsoryCourses();

		for(Course c : compulsoryCourses){
			int CourseID = c.getCourseCode();

			String query = "";

			if(yearNo==1) {
				query = "Select * from req_course_map where sys_id = '" + this.systemId +
						"' and sem_course_decription like '%ear " + yearNo+" %em "
						+ "%' and course_id = '" + CourseID + "'";
			}
			else {
				query = "Select * from req_course_map where sys_id = '" + this.systemId +
						"' and sem_course_decription like '%ear " + yearNo+" %em "+ semNo
						+ "%' and course_id = '" + CourseID + "'";
			}

			
			ResultSet r = dbConnector.queryExecutor(query, false);
			// Use systemID, yearNO,semNo, course code to get ResultSet
			// from req_coursemap table

			try {
				while(r.next()) {
					c.setGrade(r.getString(12));
					c.setClassNo(r.getInt(5));
					c.setEarnCredit(r.getString(6));
					c.setIncludeInGPA(r.getString(7));
					c.setInProgress(r.getString(20));
					c.setLine(r.getInt(16));
					c.setTerm(r.getInt(4));
					c.setIsDoneInPrevTerm(this.prevTerm);		
					c.setElDescr("");

					if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
						c.setOPSC(true);
					}
					else {
						c.setOPSC(false);
					}

					
				}				


	
				this.checkForRepeatAndSetFlag(c);


			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}

	private void addHuelsToSem(Semester sem, int term){	

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		String query = "Select * from req_course_map where descr_2 like '%um%lective%' and sys_id = '" 
				+ systemId + "' and sem_course_decription like '%ear " + yearNo+" %em "+ semNo
				+ "%'";

		int[] semYear = getSemTerm(term);

		ResultSet r = dbConnector.queryExecutor(query, false);
		try {
			while(r.next())
			{
				// take year no and sem no of sem and use hum elective-pilon and systemID to return
				//resultset of elective with term and other things from reqcourse_map table

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);
				c.setIsHuel(true);
				
				if (this.hasMinor(this.systemId)){
					this.setMinorDesc(c);
					c.setElDescr("HUEL" +  "/" + c.getElDescr() );
				}
				c.setElDescr("HUEL");
				if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
					c.setOPSC(true);
				}
				else {
					c.setOPSC(false);
				}
				
				sem.addHumanitiesElectives(c);

				this.checkForRepeatAndSetFlag(c);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addDelsToSem(Semester sem, int term){	

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		
		String query = "Select * from req_course_map where descr_2 like '%Disp%lective%' and sys_id = '" + systemId
				+ "' and sem_course_decription like '%ear " + yearNo+" %em "+ semNo
				+ "%'";
		ResultSet r = dbConnector.queryExecutor(query, false);
		
		
		try {
			while(r.next()) {
				for(Course c : sem.getDelCourses()){
					int counter =0;
					String s =  r.getString(19).substring(0, 2) + "EL";
					if(c.getElDescr().equalsIgnoreCase(s) &&
							c.getDescription()==null){
						int i = sem.getDelCourses().indexOf(c);
						Course cNew = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
								r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
								r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);
						cNew.setIsDel(true);
						cNew.setElDescr(s);
						sem.getDelCourses().set(i,cNew);
					counter++;
					}
					if (counter>0)
					break;
					
				}

		}} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
			
}



	private void addOelsToSem(Semester sem, int term){	

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		String query = "Select * from req_course_map where descr_2 like '%Open%lective%' and sys_id = '" + systemId
				+ "' and sem_course_decription like '%ear " + yearNo+" %em "+ semNo
				+ "%'";// and course_id = '" + CourseID + "'";
		ResultSet r = dbConnector.queryExecutor(query, false);

		try {
			while(r.next()){
				// take year no and sem no of sem and use Open elective- and systemID to return
				//resultset of elective with term and other things from reqcourse_map table

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);
				c.setIsOel(true);
				if (this.hasMinor(this.systemId)){
					this.setMinorDesc(c);
					c.setElDescr("EL" +  "/" + c.getElDescr() );
				}
				
				sem.addOpenElectives(c);

				c.setElDescr("EL");

				if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
					c.setOPSC(true);
				}
				else {
					c.setOPSC(false);
				}

		
				sem.addOpenElectives(c);

				
				this.checkForRepeatAndSetFlag(c);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addOptionalToSem(Semester sem, int term) {

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		String query = "Select * from req_course_map where descr_2 like '%POM%POE%Opti%' and sys_id = '" + systemId
				+ "' and sem_course_decription like '%ear " + yearNo+" %em "+ semNo
				+ "%'";
		ResultSet r = dbConnector.queryExecutor(query, false);

		try {
			while (r.next()){
				// take year no and sem no of sem and use POM & POE optional course- and systemID to return
				//resultset of elective with term and other things from reqcourse_map table

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);

				if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
					c.setOPSC(true);
				}
				else {
					c.setOPSC(false);
				}

				sem.setOptionalCourse(c);
			
				this.checkForRepeatAndSetFlag(c);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}	


	private boolean isSummerTerm(Semester sem) {	//Chart object has already set isSummerTerm	
		return sem.isSummerTerm;
	}

	private void addPracticeSchoolToSem(Semester sem) {	


		String query = "Select * from req_course_map where sys_id = '" + systemId
				+ "' and sem_course_decription like '%ummer%erm%'";

		ResultSet r = dbConnector.queryExecutor(query, false);

		try {
			while (r.next()){			

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);		

	
				sem.setPS(c);
				
				this.checkForRepeatAndSetFlag(c);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void updatePendingElectives(Semester sem, int pendingHuels, int pendingDels, int pendingOels) {		
		pendingHuels =  sem.getNoOfHUEL() - sem.getNumOfHuelCompleted();		
		sem.setNoOfHUEL(pendingHuels);		

		pendingDels = sem.getNoOfDEL() - sem.getNumOfDelCompleted();		
		sem.setNoOfDEL(pendingDels);		

		pendingOels = sem.getNoOfOEL() - sem.getNumOfOellCompleted();		
		sem.setNoOfOEL(pendingOels);		
	}

	// getters and setters

	public String getSystemId() {
		return systemId;
	}

	public String getRequirementNo() {
		return requirementNo;
	}

	public String getCgpa() {
		return cgpa;
	}

	public void setCgpa() {

		String query = "SELECT CGPA FROM student_terms"
				+ " where sys_id = '" + this.systemId 
				+ "' and semester = (select max(semester) from student_terms)";

		// get std_terms column 17 (Q) 

		ResultSet r = dbConnector.queryExecutor(query, false);

		try {
			while(r.next()) {
				this.cgpa = r.getString(1) ; // Assign value here !!
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setRequirementNo(String studentId) {
		String k = null;
		String sqlQuery = "SELECT rqrmnt FROM std_req_mapping"
				+ " WHERE sys_id = (SELECT sys_id FROM students WHERE campus_id = '"+ studentId +"');";		
		//System.out.println(sqlQuery);
		ResultSet rs = null;
		try {
			rs = dbConnector.queryExecutor(sqlQuery, false);
		}
		catch (Exception e){
			e.printStackTrace();
		}

		try {
			while (rs.next()){
				k =  rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.requirementNo = k;
	}

	private void setSystemId(String studentId) {
		/*
		SELECT sys_id
		FROM students
		WHERE campus_id  = &studentId;
		 */
		String k = null;
		String sqlQuery = "SELECT sys_id FROM students WHERE campus_id = '" + studentId + "'";
		ResultSet rs = dbConnector.queryExecutor(sqlQuery, false);
		try {
			while (rs.next())	{
				k = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.systemId = k;

	}

	public CourseChartQueries getChart() {
		return chart;
	}

	private void setCh(CourseChartQueries ch) {
		this.chart = ch;
	}

	public int getPrevTerm() {
		return prevTerm;
	}

	private void setPrevTerm(String StudentId) {
		// write a query to get the last term of the person done
		//subtract one from the Last term and set LastTerm

		String query ="SELECT MAX(term_taken) from req_course_map where sys_id = '" +systemId+ "'";

		ResultSet rs = dbConnector.queryExecutor(query, false);

		int term=0 ;
		try {
			while (rs.next()){
				try {
					term = rs.getInt(1);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.prevTerm = term -1 ;

	}
	
	
	
	private String getAdmissionTerm(){
	
		String s = "";
		
		String query = "SELECT DISTINCT sem_description from terms where semester = ("
				+ "SELECT min(semester) from student_enrollment where sys_id = '" + this.systemId + "'"
				+ ") LIMIT 1";
		
		ResultSet rs = dbConnector.queryExecutor(query, false);
		
		try {
			while (rs.next()){
				s = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;
		
	
	}
	

	public boolean hasMinor(String sysId){
		String query ="SELECT * from student_minor where sys_id = '" + sysId + "'";
				// Use systemID to write query for returning row from 
				//std_minor table
		ResultSet rs = dbConnector.queryExecutor(query, false);
		int i=0;
		try {
			while(rs.next()){
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(i>0)
			return true;
		else 
			return false;
		
	}
	
	public void setMinorDesc (Course c){
		int CourseId = c.getCourseCode();
		String query = "SELECT * FROM minor_course_list where course_id = '" + c.getCourseCode() + "'";
				//Use courseId to write a query to return row from minor course list
				//table
		ResultSet rs = dbConnector.queryExecutor(query, false);
		int i = 0;
		String desc = "";
		try {
			
			while(rs.next()){
				i++;
				String s = rs.getString(1);
				int indexOfP = s.indexOf("P");
				desc =	s.substring(0, indexOfP);
				desc += rs.getString(3);			
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(i>0){
			c.setElDescr(desc);
		}
		
	}
	
}