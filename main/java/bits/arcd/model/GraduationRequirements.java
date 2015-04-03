package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GraduationRequirements {

	private EligibilitySheetQueries elSheet;
	private String studentId;

	//COIP = Complete Or In Progress
	
	private int noOfOelsCOIP, noOfHuelsCOIP;
	private int unitsOfnoOfOelsCOIP, unitsOFnoOfHuelsCOIP;
	private int noOFHuelProjects,noOfOelProjects ;
	private int noOfDelsType1COIP;
	private int unitsOfnoOfDelsType1COIP;
	private int noOfDelsType2COIP;
	private int unitsOfnoOfDelsType2COIP;
	private int noOfDelType1Projects, noOfDelType2Projects;
	ArrayList<Course> incompleteNamedCourses = new ArrayList<Course>();
	private int noOfIncompleteNamedCourses;
	private int noOfNamedCoursesCOIP;
	private int unitsOFnoOfNamedCoursesCOIP;
	private int totalCoursesCOIP;
	private int totalUnitsCOIP;	
	
	private int noOfPS1;
	private int unitOfExtraEl;
	private int noOfExtraEl;
	private int noOfPS2;
	private int noOf16unitThesis;
	private int noOf9unitThesis;
	
	private boolean likelyToGraduate = false;
	private boolean graduated = false;
	private boolean stillEnrolled = false;	
	private String graduationStatus;	
	
	private ArrayList<String> reasons = new ArrayList<String>();	

	private int unitsOfHuelProjects;
	private int unitsOfDelType1Projects;
	private int unitsOfDelType2Projects;
	private int unitsOfOelProjects;

	public GraduationRequirements(String studentId) {
		super();
		this.studentId = studentId;
		this.elSheet = new EligibilitySheetQueries(studentId, 1131);
		loopThroughSemesters(elSheet);
		setGraduationFields();
//		debugger();	
	}

	public GraduationRequirements(EligibilitySheetQueries e) {
		super();
		this.elSheet = e;
		this.studentId = e.getStudentId();
		loopThroughSemesters(elSheet);		
		setGraduationFields();
//		debugger();		
	}

	private void debugger() {
		System.out.println(this.studentId);
		System.out.println("checkForDEL  =" + checkForDEL());
		System.out.println("checkForHUEL  =" + checkForHUEL());
		System.out.println("checkForNamedCourses  =" + checkForNamedCourses());
		System.out.println("checkForOEL  =" + checkForOEL());
		System.out.println("checkPSThesisConditions  =" + checkPSThesisConditions());
		System.out.println("checkTotalCoursework  =" + checkTotalCoursework());
		System.out.println("totalCoursesCOIP  ="+ this.totalCoursesCOIP);
		System.out.println("totalUnitsCOIP  =" + totalUnitsCOIP);
		System.out.println("incompleteNamedCourses  =" + this.incompleteNamedCourses);
		System.out.println("graduationStatus  =" + this.graduationStatus);
		System.out.println();
	}
	
	public EligibilitySheetQueries getELSheet() {
		return this.elSheet;
	}
	
	private void loopThroughSemesters(EligibilitySheetQueries elsheet){

		for(Semester s: elsheet.getChart().getSemsInChart()) {

			for(Course c : s.getAllCourses()){

				c.checkAndSetGradeValidAndGradeComplete();			

				if(c.isGradeComplete() || (c.isInProgress() != null && c.isInProgress().equalsIgnoreCase("Y"))) {
					
					// To check if student is still enrolled in courses
					if(c.isInProgress().equalsIgnoreCase("Y")) {
						this.stillEnrolled = true;
					}

					
					if(!(c.isPS2() || c.is16unitThesis() || c.is9unitThesis() || c.isSummerTermPS1())) {
						this.totalCoursesCOIP++;
						this.totalUnitsCOIP += c.getMaxUnits();						
					}					
					else if(c.isOelPS1() || c.isSummerTermPS1()) {
						this.noOfPS1++;
					}
					else if(c.isPS2()) {					
						this.noOfPS2++;						
					}					
					else if(c.is16unitThesis()) {
							this.noOf16unitThesis++;
						}							
					else if(c.is9unitThesis()) {
						this.noOf9unitThesis++;

						if(noOfPS1>0) {
							this.noOfExtraEl++;
							this.unitOfExtraEl += 1;
							noOfPS1 =0;
						}
						
						else {
							this.noOfExtraEl +=2;
							this.unitOfExtraEl +=6;
						}
					}

					if(c.isNamedCourse() || c.isOptional() || c.isSummerTermPS1()) {
						this.noOfNamedCoursesCOIP++;
						this.unitsOFnoOfNamedCoursesCOIP+=c.getMaxUnits();						
					}

					else if(c.isHuel()){
						this.noOfHuelsCOIP++;
						this.unitsOFnoOfHuelsCOIP+=c.getMaxUnits();
						if(c.getIsProjectTypeCourse()){
							this.noOFHuelProjects++;
							this.unitsOfHuelProjects+=c.getMaxUnits();
						}
					}

					else if(c.isDel()){

						if(c.getElDescr().equalsIgnoreCase(elsheet.getChart().getStream1() + "EL")) {
							this.noOfDelsType1COIP++;
							this.unitsOfnoOfDelsType1COIP+=c.getMaxUnits();
							if(c.getIsProjectTypeCourse()){
								this.noOfDelType1Projects++;
								this.unitsOfDelType1Projects+=c.getMaxUnits();
							}
						}

						else if(c.getElDescr().equalsIgnoreCase(elsheet.getChart().getStream2() + "EL")) {
							this.noOfDelsType2COIP++;
							this.unitsOfnoOfDelsType2COIP+=c.getMaxUnits();
							if(c.getIsProjectTypeCourse()){
								this.noOfDelType2Projects++;
								this.unitsOfDelType2Projects+=c.getMaxUnits();
							}
						}
					}			

					else if(c.isOel()){
						this.noOfOelsCOIP++;
						this.unitsOfnoOfOelsCOIP+=c.getMaxUnits();
						if(c.getIsProjectTypeCourse()){
							this.noOfOelProjects++;
							this.unitsOfOelProjects+=c.getMaxUnits();
						}
					}
				}

				else {
					//lists the named courses to be completed
					if(c.isNamedCourse() || c.isOptional() || c.isSummerTermPS1()) {
						
						if(c.getCatalog() != null && c.getCatalog().equalsIgnoreCase("F421T") 
								&& c.getSubject().equalsIgnoreCase("BITS") && s.hasPS2()) {
							// optional thesis. do no add to incomplete course
						}
						else {
							this.noOfIncompleteNamedCourses++;
							this.incompleteNamedCourses.add(c);
						}
					}					
				}
			}
		}
	}
	
	private void setGraduationFields() {	
		
		boolean namedCoursesCondition = checkForNamedCourses();
		boolean huelCondition = checkForHUEL();
		boolean delCondition = checkForDEL();
		boolean oelCondition = checkForOEL();
		boolean totalCourseworkCondition = checkTotalCoursework();
		boolean psThesisCondition = checkPSThesisConditions();
		boolean totalProjCoursesCondition = checkforTotalProjectCourses();
		
		if(namedCoursesCondition && huelCondition && delCondition && oelCondition 
				&& totalCourseworkCondition && psThesisCondition && totalProjCoursesCondition) {
			this.likelyToGraduate = true;
		}
		else {
			this.likelyToGraduate = false;
		}
		
		if(this.likelyToGraduate) {
			if(this.stillEnrolled) {
				this.graduationStatus = "L"; // L = Likely to graduate
			}
			else {
				this.graduationStatus = "G"; // G = Graduated or cleared 9.01 clause
			}
		}
		else {
			this.graduationStatus = "I"; // I = Incomplete ie. neither graduated nor likely to.
		}
	}	

	private boolean checkforTotalProjectCourses() {
		
		int totalNoOfProjectCourses = this.noOFHuelProjects + this.noOfDelType1Projects + this.noOfDelType2Projects
									+ this.noOfOelProjects;
		int totalUnitsOfProjectCourses = this.unitsOfHuelProjects + this.unitsOfDelType1Projects
										+ this.unitsOfDelType2Projects + this.unitsOfOelProjects;
		String rs = "";
		if(totalNoOfProjectCourses > 5) {
			
			rs += " Total Project Type Courses: " + totalNoOfProjectCourses + " > 5(max).";	
			reasons.add(rs);
		}
		
		if(totalUnitsOfProjectCourses > 15) {
			rs += " Total units of Project Type Courses: " + totalUnitsOfProjectCourses
					+ " > 15(max).";
			reasons.add(rs);
		}
		
		if(totalNoOfProjectCourses > 5 || totalUnitsOfProjectCourses > 15) {
			return false;			
		}
		else {
			return true;
		}
		
	}

	private boolean checkForNamedCourses() {
		if(noOfIncompleteNamedCourses == 0) {
			return true;
		}
		else {
			String rs = "Named courses not complete. "+ this.incompleteNamedCourses.size() 
						+ " incomplete named course(s).";
			reasons.add(rs);
			return false;			
		}

	}

	private boolean checkForHUEL(){
		if((this.noOfHuelsCOIP< 3) || (this.unitsOFnoOfHuelsCOIP<8) 
				||(this.noOFHuelProjects >1) || (this.unitsOfHuelProjects > 3)){
			
			String rs = "Humanities electives not complete. (" + this.noOfHuelsCOIP + "/3 courses, " 
					+ this.unitsOFnoOfHuelsCOIP + "/8 units) done.";
			if(this.noOFHuelProjects >1) {
				rs += " Number of HUEL Projects: " + this.noOFHuelProjects + "> 1";
			}
			else if(this.unitsOfHuelProjects > 3) {
				rs += " Units of HUEL Project(s): " + this.unitsOfHuelProjects + "> 3";
			}
			
			reasons.add(rs);
			return false;
		}
		else return true;
	}

	private boolean checkForDEL(){
		
		boolean delType1Satisfied=false, delType2Satisfied=false;
		
		String delStream1 = elSheet.getChart().getStream1();		
		int[] cu = getNumCoursesAndUnitsforDELs(delStream1);		
		int noOfDelType1Courses = cu[0];
		int noOfDelType1Units = cu[1];

		int noOfDelType2Courses=0, noOfDelType2Units=0;

		String delStream2 = elSheet.getChart().getStream2();
		// Checks if first or dual degree
		if(delStream2 != null && !delStream2.isEmpty()){
			noOfDelType2Courses = 0;
			noOfDelType2Units = 0;
		}
		else {			
			int[] cu2 = getNumCoursesAndUnitsforDELs(delStream1);		
			noOfDelType2Courses = cu2[0];
			noOfDelType2Units = cu2[1];
		}

		if((this.noOfDelsType1COIP< noOfDelType1Courses) || (this.unitsOfnoOfDelsType1COIP< noOfDelType1Units)
				|| (this.noOfDelType1Projects > 3) || (this.unitsOfDelType1Projects > 9)){	
			
			String rs = delStream1 + " Discipline electives not complete. (" + this.noOfDelsType1COIP + "/" 
					+ noOfDelType1Courses + " courses, " + this.unitsOfnoOfDelsType1COIP + "/"
					+ noOfDelType1Units + " units) done.";
			
			if(this.noOfDelType1Projects > 3) {
				rs += " Number of " + delStream1 +"EL Projects: " + this.noOfDelType1Projects + "> 3";
			}
			else if(this.unitsOfDelType1Projects > 9) {
				rs += " Units of " + delStream1 + "EL Projects: " + this.unitsOfDelType1Projects + "> 9";
			}
			
			reasons.add(rs);
			
			delType1Satisfied = false;
		}
		
		else {
			delType1Satisfied = true;
		}
		
		if((elSheet.getChart().getStream2() != null) && 
				((this.noOfDelsType2COIP< noOfDelType2Courses) || (this.unitsOfnoOfDelsType2COIP< noOfDelType2Units)
						|| (this.noOfDelType2Projects > 3) || (this.unitsOfDelType2Projects > 9) )){	
			
			String rs = delStream2 + " Discipline electives not complete. (" + this.noOfDelsType2COIP + "/" 
					+ noOfDelType2Courses + " courses, " + this.unitsOfnoOfDelsType2COIP + "/"
					+ noOfDelType2Units + " units) done.";
			
			if(this.noOfDelType2Projects > 3) {
				rs += " Number of " + delStream2 +"EL Projects: " + this.noOfDelType2Projects + "> 3";
			}
			else if(this.unitsOfDelType2Projects > 9) {
				rs += " Units of " + delStream2 + "EL Projects: " + this.unitsOfDelType2Projects + "> 9";
			}

			reasons.add(rs);
			delType2Satisfied = false;
		}

		else {		
			delType2Satisfied = true;
		}
		
		if(delType1Satisfied && delType2Satisfied) {
			return true;
		}
		else {
			return false;
		}
			
	}

	private boolean checkForOEL(){
		
		int noOfOelsReq=0, unitsOfOelsReq=0;
		
		if(elSheet.getChart().getStream2() == null) {
			noOfOelsReq = 5;
			unitsOfOelsReq = 15;
		}
		else {
			noOfOelsReq = 0;
			unitsOfOelsReq = 0;
		}
		
		noOfOelsReq += this.noOfExtraEl;
		unitsOfOelsReq += this.unitOfExtraEl;
		
		if( (this.noOfOelsCOIP< noOfOelsReq) || (this.unitsOfnoOfOelsCOIP<unitsOfOelsReq)
				|| (this.noOfOelProjects > 3) || (this.unitsOfOelProjects > 9)){
			
			String rs = "Open electives not complete. (" + this.noOfOelsCOIP + "/" 
					+ noOfOelsReq + " courses, " + this.unitsOfnoOfOelsCOIP + "/"
					+ unitsOfOelsReq + " units) done.";
			
			if(this.noOfOelProjects > 3) {
				rs += " Number of Open Elective Projects: " + this.noOfOelProjects + "> 3";
			}
			else if(this.unitsOfDelType2Projects > 9) {
				rs += " Units of Open Elective Projects: " + this.unitsOfOelProjects + "> 9";
			}

			reasons.add(rs);
			
			return false;
		}
		else {
			return true;
		}
	}

	private boolean checkTotalCoursework() {

		if(this.totalCoursesCOIP >= 40 && this.totalUnitsCOIP >= 126) {			
			return true;
		}
		else {
			
			String rs = "Total coursework not complete. (" + this.totalCoursesCOIP + "/40 courses, "
					+ this.totalUnitsCOIP + "/126 units) done.";
			
			reasons.add(rs);		
			return false;
		}
	}

	private boolean checkPSThesisConditions() {
		
		if(this.elSheet.getChart().getStream2() == null) {
			if(this.noOfPS2+this.noOf16unitThesis + this.noOf9unitThesis>0) {
				
				return true;
			}
			else {
				
				String rs = "PS/TS conditions not satisfied.";
				reasons.add(rs);
				
				return false;
			}
		}
		else {
			if(this.noOfPS2+this.noOf16unitThesis + this.noOf9unitThesis>1) {			
				return true;
			}
			else {
				
				String rs = "PS/TS conditions not satisfied.";
				reasons.add(rs);
				
				return false;
			}
		}
	}

	public int[] getNumCoursesAndUnitsforDELs(String elStream) {

		// Returns the num of required courses and units for a stream passed in as the parameter

		int[] cu = {-1, -1};

		
		// Condition check for NULL value to be added
		if(elStream != null && (elStream.equalsIgnoreCase("C6") || elStream.equalsIgnoreCase("A5") 
				|| elStream.equalsIgnoreCase("AB") || elStream.equalsIgnoreCase("A4")
				|| elStream.equalsIgnoreCase("A8") || elStream.equalsIgnoreCase("AA")
				|| elStream.equalsIgnoreCase("A3") || elStream.equalsIgnoreCase("A7")
				|| elStream.equalsIgnoreCase("B2") || elStream.equalsIgnoreCase("A2"))) {

			cu[0] = 4;
			cu[1] = 12;
			return cu;
		}

		if(elStream != null && (elStream.equalsIgnoreCase("B1")|| elStream.equalsIgnoreCase("B5")
				|| elStream.equalsIgnoreCase("B4")|| elStream.equalsIgnoreCase("A1")||
				elStream.equalsIgnoreCase("D2")|| elStream.equalsIgnoreCase("C7"))){
			cu[0] = 5;
			cu[1] = 15;
			return cu;
		}

		if(elStream != null && (elStream.equalsIgnoreCase("B3"))) {
			cu[0] = 6;
			cu[1] = 18;
			return cu;
		}

		if(elStream != null && (elStream.equalsIgnoreCase("C2"))) {
			cu[0] = 7;
			cu[1] = 21;
			return cu;
		}

		return cu;
	}
	
	// getters and setters
	
	public String getStudentId() {
		return studentId;
	}
	
	public int getNoOfOelsCOIP() {
		return noOfOelsCOIP;
	}


	public int getUnitsOfnoOfOelsCOIP() {
		return unitsOfnoOfOelsCOIP;
	}


	public int getUnitsOFnoOfHuelsCOIP() {
		return unitsOFnoOfHuelsCOIP;
	}


	public int getNoOFHuelProjects() {
		return noOFHuelProjects;
	}


	public int getNoOfDelsType1COIP() {
		return noOfDelsType1COIP;
	}


	public int getUnitsOfnoOfDelsType1COIP() {
		return unitsOfnoOfDelsType1COIP;
	}


	public int getNoOfDelsType2COIP() {
		return noOfDelsType2COIP;
	}


	public int getNoOfHuelsCOIP() {
		return noOfHuelsCOIP;
	}


	public int getUnitsOfnoOfDelsType2COIP() {
		return unitsOfnoOfDelsType2COIP;
	}


	public int getTotalCoursesCOIP() {
		return totalCoursesCOIP;
	}


	public int getTotalUnitsCOIP() {
		return totalUnitsCOIP;
	}
	
	public boolean isLikelyToGraduate() {
		return likelyToGraduate;
	}

	public boolean isGraduated() {
		return graduated;
	}
	
	public String getGraduationStatus() {
		return graduationStatus;
	}
	
	public ArrayList<String> getReasons() {
		return reasons;
	}

}
