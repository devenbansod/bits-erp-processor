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

	public GraduationRequirements(String studentId) {
		super();
		this.studentId = studentId;
		this.elSheet = new EligibilitySheetQueries(studentId, 1131);
		loopThroughSemesters(elSheet);
		setGraduationFields();
	}
	
	public GraduationRequirements(EligibilitySheetQueries e) {
		super();
		this.elSheet = e;
		this.studentId = e.getStudentId();
		loopThroughSemesters(elSheet);		
		setGraduationFields();
	}

	private void loopThroughSemesters(EligibilitySheetQueries elsheet){

		for(Semester s: elsheet.getChart().getSemsInChart()) {

			for(Course c : s.getAllCourses()){

				c.checkAndSetGradeValidAndGradeComplete();			

				if(c.isGradeComplete() || (c.isInProgress() != null && c.isInProgress().equalsIgnoreCase("Y"))) {
					
					if(c.isInProgress().equalsIgnoreCase("Y")) {
						this.stillEnrolled = true;
					}
					
					if(!(c.isPS2() || c.is16unitThesis() || c.is9unitThesis() || c.isPS1())) {
						this.totalCoursesCOIP++;
						this.totalUnitsCOIP += c.getMaxUnits();						
					}
					
					else if(c.isPS1()) {
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

					if(c.isNamedCourse() || c.isOptional() || c.isPS1()) {				

						if(c.isGradeComplete() || c.isInProgress().equalsIgnoreCase("Y")){
							this.noOfNamedCoursesCOIP++;
							this.unitsOFnoOfNamedCoursesCOIP+=c.getMaxUnits();
						}
					}

					else if(c.isHuel()){
						this.noOfHuelsCOIP++;
						this.unitsOFnoOfHuelsCOIP+=c.getMaxUnits();
						if(c.getIsProjectTypeCourse()){
							this.noOFHuelProjects++;
						}
					}

					else if(c.isDel()){

						if(c.getElDescr().equalsIgnoreCase(elsheet.getChart().getStream1() + "EL")) {
							this.noOfDelsType1COIP++;
							this.unitsOfnoOfDelsType1COIP+=c.getMaxUnits();
							if(c.getIsProjectTypeCourse()){
								this.noOfDelType1Projects++;
							}
						}

						if(c.getElDescr().equalsIgnoreCase(elsheet.getChart().getStream2() + "EL")) {
							this.noOfDelsType2COIP++;
							this.unitsOfnoOfDelsType2COIP+=c.getMaxUnits();
							if(c.getIsProjectTypeCourse()){
								this.noOfDelType2Projects++;
							}
						}
					}			

					else if(c.isOel()){
						this.noOfOelsCOIP++;
						this.unitsOfnoOfOelsCOIP+=c.getMaxUnits();
						if(c.getIsProjectTypeCourse()){
							this.noOfOelProjects++;
						}
					}
				}

				else {
					
					//lists the named courses to be completed
					if(c.isNamedCourse() || c.isOptional() || c.isPS1()) {						
						if(!(c.getSubject().equalsIgnoreCase("BITS") 
								&& c.getCatalog().equalsIgnoreCase("F421T"))) {								
							this.noOfIncompleteNamedCourses++;
							this.incompleteNamedCourses.add(c);		
						}									
					}					
				}
			}
		}
	}
	
	private void setGraduationFields() {		
		
		if(checkForNamedCourses() && checkForHUEL() && checkForDEL() && checkForOEL() 
				&& checkTotalCoursework() && checkPSThesisConditions()) {
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

	private boolean checkForNamedCourses() {
		if(noOfIncompleteNamedCourses == 0) {
			return true;
		}
		else {
			return false;
		}

	}

	private boolean checkForHUEL(){
		if((this.noOfHuelsCOIP< 3) || (this.noOFHuelProjects >1) 
				|| (this.unitsOFnoOfHuelsCOIP<8)){
			return false;
		}
		else return true;
	}

	private boolean checkForDEL(){

		String delStream1 = elSheet.getChart().getStream1();		
		int[] cu = getNumCoursesAndUnitsforDELs(delStream1);		
		int noOfDelType1Courses = cu[0];
		int noOfDelType1Units = cu[1];

		int noOfDelType2Courses=0, noOfDelType2Units=0;

		String delStream2 = elSheet.getChart().getStream2();
		// Checks if first or dual degree
		if(delStream2 != null && delStream2.isEmpty()){
			noOfDelType2Courses = 0;
			noOfDelType2Units = 0;
		}
		else {			
			int[] cu2 = getNumCoursesAndUnitsforDELs(delStream1);		
			noOfDelType2Courses = cu2[0];
			noOfDelType2Units = cu2[1];
		}

		if((this.noOfDelsType1COIP< noOfDelType1Courses) || (this.unitsOfnoOfDelsType1COIP< noOfDelType1Units)){			
			return false;
		}
		if((elSheet.getChart().getStream2() != null) && ((this.noOfDelsType2COIP< noOfDelType2Courses)
				|| (this.unitsOfnoOfDelsType2COIP< noOfDelType2Units))){			
			return false;
		}

		else {		
			return true;
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
		
		if( (this.noOfOelsCOIP< noOfOelsReq) || (this.unitsOfnoOfOelsCOIP<unitsOfOelsReq)){
			return false;
		}
		else {
			return true;
		}
	}

	private boolean checkTotalCoursework() {

		if(this.totalCoursesCOIP > 40 && this.totalUnitsCOIP > 126) {
			return true;
		}
		else {
			return false;
		}
	}

	private boolean checkPSThesisConditions() {
		
		if(this.elSheet.getChart().getStream2() == null) {
			if(this.noOfPS2+this.noOf16unitThesis + this.noOf9unitThesis>0) {
				
				return true;
			}
			else
				return false;
		}
		else {
			if(this.noOfPS2+this.noOf16unitThesis + this.noOf9unitThesis>1) {			
				return true;
			}
			else
				return false;
		}
	}

	public int[] getNumCoursesAndUnitsforDELs(String elStream) {

		// Returns the num of required courses and units for a stream passed in as the parameter

		int[] cu = {-1, -1};

		if(elStream.equalsIgnoreCase("C6") || elStream.equalsIgnoreCase("A5") 
				|| elStream.equalsIgnoreCase("AB") || elStream.equalsIgnoreCase("A4")
				|| elStream.equalsIgnoreCase("A8") || elStream.equalsIgnoreCase("AA")
				|| elStream.equalsIgnoreCase("A3") || elStream.equalsIgnoreCase("A7")
				|| elStream.equalsIgnoreCase("B2") || elStream.equalsIgnoreCase("A2")) {

			cu[0] = 4;
			cu[1] = 12;
			return cu;
		}

		if(elStream.equalsIgnoreCase("B1")|| elStream.equalsIgnoreCase("B5")
				|| elStream.equalsIgnoreCase("B4")|| elStream.equalsIgnoreCase("A1")||
				elStream.equalsIgnoreCase("D2")|| elStream.equalsIgnoreCase("C7")){
			cu[0] = 5;
			cu[1] = 15;
			return cu;
		}

		if(elStream.equalsIgnoreCase("B3")) {
			cu[0] = 6;
			cu[1] = 18;
			return cu;
		}

		if(elStream.equalsIgnoreCase("C2")) {
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

}