package bits.arcd.model;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import sun.misc.Perf.GetPerfAction;

public class EligibilitySheetQueries {

	private String studentId;
	private String systemId;
	private String requirementNo;
	private String studentName;


	private CourseChartQueries chart;
	private DBConnector dbConnector;	

	private int prevTerm;	

	private String cgpa;
	private double cgpaCalculated;
	private int cgpaCup, cgpaUnits;

	private int termProducedIn;

	private ArrayList<Course> unaccountedCourses = new ArrayList<Course>();



	public EligibilitySheetQueries(String studentId, int term) {

		dbConnector = DBConnector.getInstance();

		this.termProducedIn = term;
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

		addUnaccountedCourses();

		updateCgpaCupAndUnits();

	}




	// Methods	

	public void updateCgpaCupAndUnits() {

		String query = "SELECT grade_points, total FROM student_terms WHERE sys_id = '" + systemId + "' " ;
		//		System.out.println(query);
		// get cgpa cup and units from std_terms using systemId
		ResultSet r = dbConnector.queryExecutor(query, false);

		try {
			while(r.next()){

				this.cgpaCup += r.getInt(1);
				this.cgpaUnits += r.getInt(2);	

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
	
	public String getStudentId(){
		return this.studentId;
	}

	private String returnCenteredString(String s){

		int total_spaces = 150 - s.length();

		for(int i = 0; i < total_spaces/2; i++){
			s = " " + s + " ";
		}

		return s.toUpperCase();
	}



	@Override
	public String toString() {

		String s = "";
		int i = 0;

		s = s + returnCenteredString("BITS Pilani") + "\n";

		s = s + returnCenteredString("PRODUCED FOR : " + getPrintingTerm(termProducedIn)) + "\n";

		s = s + returnCenteredString("ELIGIBLITY SHEET (VIDE A.R. 3.21)") + "\n\n";
		GraduationRequirements g = new GraduationRequirements(this);
		
		AcadCounselBoard a = new AcadCounselBoard(this);
		
		String statuses = "";
		
		if ( ! a.getIsAcb()) {
			statuses = statuses + "STATUS : NORMAL";
		}
		else {
			statuses = statuses + "STATUS : ACB";
		}

		if (g.isLikelyToGraduate())
			statuses = statuses + " \tLIKELY TO GRADUATE";

		else if (g.isGraduated())
			statuses = statuses + " \tGRADUATED";
		
		s = s + returnCenteredString(statuses) + "\n";

		setStudentNameFromDatabase();

		String details = this.systemId + "  "  + this.studentId + "  " + getStudentName() + "  CGPA : "
				+ this.getCgpa() + "   REQ NUM : " + this.requirementNo + "   REQ GRP : " 
				+ this.getChart().getRequirementGroup() + "  ADMISSION : " + getAdmissionTerm();

		s = s + returnCenteredString(details) + "\n";


		s = s + "\nYEAR      CODE  COURSE NO    COURSE TITLE            UNITS  GRADES                  ";
		s = s + "CODE  COURSE NO    COURSE TITLE            UNITS  GRADES             ";

		s = s + "\n-----------------------------------------"
				+ "-----------------------------------------"
				+ "----------------------------------------------------------------------\n";
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
						String temp = "    ................................................           HUEL      ";

						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfHUEL(); j++){
						String temp = "    ................................................           HUEL      ";						
						sem2Courses.add(temp);

					}

					// Add the Open Electives to be done
					for (int j = 0; j < first.getNoOfOEL(); j++){
						String temp = "    ................................................           EL        ";
						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfOEL(); j++){
						String temp = "    ................................................           EL         ";
						sem2Courses.add(temp);
					}

					// Add the Disp Electives to be done
					/**
					for (int j = 0; j < first.getNoOfDEL(); j++){
						String temp = "   ------------------------------------------------          DEL        ";	
						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfDEL(); j++){
						String temp = "   ------------------------------------------------          DEL        ";

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
							temp = temp + "      " + "                                                                         ";
						}


						if (j < sem2Courses.size()) {
							temp = temp + sem2Courses.get(j);
						}
						else {
							temp = temp + "                                                                         ";
						}

						temp = temp + "\n";

						s = s + temp;
						

					}
					

					s = s + "-----------------------------------------"
							+ "-----------------------------------------"
							+ "----------------------------------------------------------------------\n";
				}
			}

			else {

				Semester PS = this.getChart().getSemsInChart().get(i);
				s = s + "Summer" + PS.getPS().toString() + "\n";

				s = s + "-----------------------------------------"
						+ "-----------------------------------------"
						+ "----------------------------------------------------------------------\n";

				rem = 0;

			}

		}


		s =  s + "LEGEND : * - BACKLOG\t" + "$ - OPSC\t" + "|| - REGISTERED CURRENT SEM\t" 
				+ "% - PREV SEM PERFORMANCE\t" + "+ - EXEMPTED\t" + "# - DEBARRED TO REGISTER\n\n";


		int[] noAndUnitsofDELs1 = g.getNumCoursesAndUnitsforDELs(this.getChart().getStream1()); 
		int[] noAndUnitsofDELs2;

		s = s + "COMPLETED REQ (NOS, UNITS) : \tHUELS : (" + g.getNoOfHuelsCOIP() +"/3, " + g.getUnitsOFnoOfHuelsCOIP() + "/8)\t";

		if (this.getChart().getStream2() != null) {
			noAndUnitsofDELs2 = g.getNumCoursesAndUnitsforDELs(this.getChart().getStream2());

			s = s + this.getChart().getStream1()+ "ELS : (" + g.getNoOfDelsType1COIP() + "/" + noAndUnitsofDELs1[0] 
					+ ", " + g.getUnitsOfnoOfDelsType1COIP() + "/" + noAndUnitsofDELs1[1] + ")\t";

			s = s + this.getChart().getStream2()+ "ELS : (" + g.getNoOfDelsType2COIP() + "/" + noAndUnitsofDELs2[0] 
					+ ", " + g.getUnitsOfnoOfDelsType2COIP() + "/" + noAndUnitsofDELs2[1] + ")\n";

		}

		else {

			s = s + "DELS : (" + g.getNoOfDelsType1COIP() + "/" + noAndUnitsofDELs1[0] 
					+ ", " + g.getUnitsOfnoOfDelsType1COIP() + "/" + noAndUnitsofDELs1[1] + ")\t";


			s = s + "OELS : (" + g.getNoOfOelsCOIP() + "/5," + g.getUnitsOfnoOfOelsCOIP() + "/15)\n";  

		}

		s = s + "ACC CUP : " + this.cgpaCup + "\t\tCGPA CUP : " 
				+ this.cgpaCup + "\t\tACC UNITS : " + this.cgpaUnits
				+ "\t\tCGPA UNITS : " + this.cgpaUnits + "\n";
		
		s = s + "-----------------------------------------"
				+ "-----------------------------------------"
				+ "----------------------------------------------------------------------\n";


		if (unaccountedCourses.size() > 0) {
			s = s + "UNACCOUNTED COURSES : \n";

			for (int j = 0; j < unaccountedCourses.size(); j++) {

				if (j % 2 == 0)
					s = s + "      "; 

				s = s + unaccountedCourses.get(j);

				if (j % 2 == 1) 
					s = s + "\n";
			}

			s = s + "\n-----------------------------------------"
					+ "-----------------------------------------"
					+ "----------------------------------------------------------------------\n";

		}
		
		return "\n" + s;

	}


	public int[] getSemTerm(int term){
		int[] ys = {-1,-1};
		ResultSet rs = null;
		String x = null;
		try {
			rs = dbConnector.getSemTerm(term);
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


		try {
			rs.close();
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

		ResultSet r2 = dbConnector.checkAndSetRepeatFlag(systemId, courseId);	

		int i=0;
		String repGrade = "";

		try {
			while(r2.next()) {
				if(i==0)
					repGrade = r2.getString(13);
				else
					repGrade = repGrade  + "/" + r2.getString(13);
				i++;

				if (r2.getString(13) == null || r2.getString(13).equals(""))
					c.setInProgress("Y");

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

			ResultSet r = dbConnector.updateCompulsoryCourses(this.systemId,
					yearNo, semNo, CourseID);
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
					this.checkForRepeatAndSetFlag(c);
					c.checkAndSetGradeValidAndGradeComplete();

					if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
						c.setOPSC(true);
					}
					else {
						c.setOPSC(false);
					}
				}				




			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}		
	}


	private void addUnaccountedCourses() {

		ResultSet rs =  dbConnector.addUnaccountedCourses(systemId);

		try {
			while(rs.next()) {

				Course c = new Course(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getInt(5), rs.getInt(5));
				c.setGrade(rs.getString(6));
				c.setUnaccountedCourse(true);
				c.setElDescr("UT");
				c.setIsDoneInPrevTerm(this.prevTerm);
				this.unaccountedCourses.add(c);				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private void addHuelsToSem(Semester sem, int term){	

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		ResultSet r = dbConnector.addELTypes("%um%lective%", systemId, yearNo, semNo);
		try {
			while(r.next())
			{
				// take year no and sem no of sem and use hum elective-pilon and systemID to return
				//resultset of elective with term and other things from reqcourse_map table

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);
				c.setIsHuel(true);

				String s = this.hasMinor(this.systemId);
				if (s != null && ! s.equals("")){
					setMinorDesc(c,s);
					if((c.getElDescr() != null)) {
						c.setElDescr("HUEL" +  "/" + c.getElDescr() );
					}
					else {
						c.setElDescr("HUEL");

					}
				}
				else {
					c.setElDescr("HUEL");
				}
				if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
					c.setOPSC(true);
				}
				else {
					c.setOPSC(false);
				}

				int years = this.getChart().getSemsInChart().size() / 2;
				if (years == semYear[0] && c.isInProgress() != null && c.isInProgress().equals("Y"))
					c.setOPSC(true);
				this.checkForRepeatAndSetFlag(c);
				c.checkAndSetGradeValidAndGradeComplete();
				

				sem.addHumanitiesElectives(c);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			r.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void addDelsToSem(Semester sem, int term){	

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		ResultSet r = dbConnector.addELTypes("%Disp%lective%", systemId, yearNo, semNo);


		try {
			while(r.next()) {
				for(Course c : sem.getDelCourses()){
					int counter = 0;
					String s =  r.getString(19).substring(0, 2) + "EL";
					if(c.getElDescr().equalsIgnoreCase(s) &&
							c.getDescription()==null){
						int i = sem.getDelCourses().indexOf(c);
						Course cNew = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
								r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
								r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);
						cNew.setIsDel(true);
						cNew.setElDescr(s);

						this.checkForRepeatAndSetFlag(cNew);
						//						System.out.println( cNew.getDescription() + "  : " + cNew.isInProgress());
						//						System.out.println(yearNo);

						int years = this.getChart().getSemsInChart().size() / 2;

						if (yearNo == semYear[0] && semNo == semYear[1] 
								&& cNew.isInProgress() != null && cNew.isInProgress().equals("Y")){

							cNew.setOPSC(true);
						}
						else {
							cNew.setOPSC(false);
						}

						if (yearNo == semYear[0] && cNew.isInProgress() != null && cNew.isInProgress().equals("Y")) {
							cNew.setOPSC(true);
						}

						checkForRepeatAndSetFlag(cNew);
						cNew.checkAndSetGradeValidAndGradeComplete();

						sem.getDelCourses().set(i,cNew);
						counter++;
					}
					if (counter>0)
						break;



				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			


		try {
			r.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}



	private void addOelsToSem(Semester sem, int term){	

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		ResultSet r = dbConnector.addELTypes("%Open%lective%", systemId, yearNo, semNo);

		try {
			while(r.next()){
				// take year no and sem no of sem and use Open elective- and systemID to return
				//resultset of elective with term and other things from reqcourse_map table

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);
				c.setIsOel(true);

				String s = this.hasMinor(this.systemId);
				if (s != null && ! s.equals("")){
					setMinorDesc(c,s);
					if(!(c.getElDescr()==null)) {
						c.setElDescr("EL" +  "/" + c.getElDescr() );
					}
					else {
						c.setElDescr("EL");
					}
				}
				else {
					c.setElDescr("EL");
				}
				if (yearNo == semYear[0] && semNo == semYear[1] && c.isInProgress().equals("Y")){
					c.setOPSC(true);
				}
				else {
					c.setOPSC(false);
				}

				int years = this.getChart().getSemsInChart().size() / 2;
				if (years == semYear[0] && c.isInProgress() != null && c.isInProgress().equals("Y"))
					c.setOPSC(true);

				this.checkForRepeatAndSetFlag(c);
				c.checkAndSetGradeValidAndGradeComplete();
				sem.addOpenElectives(c);


			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		try {
			r.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	private void addOptionalToSem(Semester sem, int term) {

		int yearNo = sem.getYearNo();
		int semNo = sem.getSemNo();

		int[] semYear = getSemTerm(term);

		ResultSet r = dbConnector.OptionalCourses(systemId, yearNo, semNo);

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

				int years = this.getChart().getSemsInChart().size() / 2;
				if (years == semYear[0] && c.isInProgress() != null && c.isInProgress().equals("Y"))
					c.setOPSC(true);

				this.checkForRepeatAndSetFlag(c);
				c.checkAndSetGradeValidAndGradeComplete();
				c.setIsOptional(true);
				sem.setOptionalCourse(c);


			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			r.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}	


	private boolean isSummerTerm(Semester sem) {	//Chart object has already set isSummerTerm	
		return sem.isSummerTerm;
	}

	private void addPracticeSchoolToSem(Semester sem) {	

		ResultSet r = dbConnector.addPS1toSem(systemId);
		
		try {
			while (r.next()){			

				Course c = new Course(r.getInt(8), r.getString(9), r.getString(10), r.getString(11),
						r.getInt(13), r.getInt(13), r.getString(12), r.getInt(4), r.getInt(5), r.getString(7),
						r.getString(6), r.getInt(16), r.getString(20), this.prevTerm);		


				this.checkForRepeatAndSetFlag(c);
				c.checkAndSetGradeValidAndGradeComplete();
				c.setIsPS1();
				sem.setPS(c);

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			r.close();
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

		ResultSet r = dbConnector.setCGPA(systemId);

		try {
			while(r.next()) {
				this.cgpa = r.getString(1) ; // Assign value here !!
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			r.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void setRequirementNo(String studentId) {
		String k = null;

		ResultSet rs = null;
		try {
			rs = dbConnector.setRequirementNo(studentId);
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

		

		try {
			rs.close();
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
		ResultSet rs = dbConnector.setSystemId(studentId);
		try {
			while (rs.next())	{
				k = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.systemId = k;

		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
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

		ResultSet rs = dbConnector.setPrevTerm(this.systemId);

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


		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}



	private String getAdmissionTerm(){

		String s = "";

		ResultSet rs = dbConnector.getAdmissionTerm(systemId);

		try {
			while (rs.next()){
				s = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return s;


	}

	private String getPrintingTerm(int term){

		String s = "";

		ResultSet rs = dbConnector.getPrintingTerm(term);

		try {
			while (rs.next()){
				s = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s;


	}



	public String hasMinor(String sysId){
		String query ="SELECT * from student_minor where sys_id = '" + sysId + "'";
		// Use systemID to write query for returning row from 
		//std_minor table
		String s = "";
		
		ResultSet rs = dbConnector.hasMinor(sysId);
		int i = 0;
		try {
			while(rs.next()){
				i++;
				s = s + rs.getString(5);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(i>0)
			return s;
		else 
			return s;
	}

	public void setMinorDesc (Course c, String origMinor){
		int CourseId = c.getCourseCode();

		//Use courseId to write a query to return row from minor course list
		//table
		ResultSet rs = dbConnector.setMinorDesc(CourseId);
		String desc = "";
		try {

			while(rs.next()){

				String s = rs.getString(1);
				int indexOfP = s.indexOf("P");
				int indexOfMinor = origMinor.indexOf("P");
				String check = null;
				if (indexOfMinor >= 0 ) {
					check = origMinor.substring(0,indexOfMinor);
				}
				desc =	s.substring(0, indexOfP);
				if(desc.equalsIgnoreCase(check)){
					desc += rs.getString(3).substring(0, 1);
					c.setElDescr(desc);
				}

				else {
					desc = null;
					c.setElDescr(desc);
				}

			}


		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		try {
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
