package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GraduationRequirements {

	private EligibilitySheetQueries elSheet;
	private String StudentId;

	//COIP = Complete Or In Progress

	private int noOfOelsCOIP, noOfHuelsCOIP;
	private int unitsOfnoOfOelsCOIP, unitsOFnoOfHuelsCOIP;
	private int noOFHuelProjects,noOfOelProjects ;
	private int noOfDelsType1COIP;
	private int unitsOfnoOfDelsType1COIP;
	private int noOfDelsType2COIP;
	private int unitsOfnoOfDelsType2COIP;
	private int noOfDelType1Projects, noOfDelType2Projects;

	private boolean likelyToGraduate;
	public boolean isLikelyToGraduate() {
		return likelyToGraduate;
	}

	private boolean graduated;

	public boolean isGraduated() {
		return graduated;
	}

	ArrayList<Course> incompleteNamedCourses = new ArrayList<Course>();
	private int noOfIncompleteNamedCourses;
	private int noOfNamedCoursesCOIP;
	private int unitsOFnoOfNamedCoursesCOIP;
	private int totalCoursesCOIP;
	private int totalUnitsCOIP;
	private int noOfPSTSCOIP;

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


	public GraduationRequirements(String studentId) {
		super();
		this.elSheet = new EligibilitySheetQueries(studentId, 1131);
		loopThroughSemesters(elSheet);
		if(checkForNamedCourses() && checkForHUEL() && checkForDEL() && checkForOEL() 
				&& checkTotalCoursework() &&checkPSThesisConditions()) {
			this.likelyToGraduate = true;
		}		
	}
	
	
	public GraduationRequirements(EligibilitySheetQueries e) {
		super();
		loopThroughSemesters(e);
		this.elSheet = e;
		if(checkForNamedCourses() && checkForHUEL() && checkForDEL() && checkForOEL() 
				&& checkTotalCoursework() &&checkPSThesisConditions()) {
			this.likelyToGraduate = true;
		}		
	}

	public void loopThroughSemesters(EligibilitySheetQueries elsheet){

		for(Semester s: elsheet.getChart().getSemsInChart()) {

			for(Course c : s.getAllCourses()){

				c.checkAndSetGradeValidAndGradeComplete();			

				if(c.isGradeComplete() || (c.isInProgress() != null && c.isInProgress().equalsIgnoreCase("Y"))) {

					if(!(c.isPS2orThesis())) {
						this.totalCoursesCOIP++;
						this.totalUnitsCOIP += c.getMaxUnits();						
					}

					else if(c.isPS2orThesis()) {
						this.noOfPSTSCOIP++;
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
						this.noOfIncompleteNamedCourses++;
						this.incompleteNamedCourses.add(c);						
					}
				}
			}
		}
	}

	public boolean checkForNamedCourses() {
		if(noOfIncompleteNamedCourses == 0) {
			return true;
		}
		else {
			return true;
		}

	}

	public boolean checkForHUEL(){
		if((this.noOfHuelsCOIP< 3) || (this.noOFHuelProjects >1) 
				|| (this.unitsOFnoOfHuelsCOIP<8)){
			return false;
		}
		else return true;
	}

	public boolean checkForDEL(){

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

		if((this.noOfDelsType1COIP< noOfDelType1Courses) || (this.noOfDelType1Projects >3) 
				|| (this.unitsOfnoOfDelsType1COIP< noOfDelType1Units)){
			return false;
		}
		if((this.noOfDelsType2COIP< noOfDelType2Courses) || (this.noOfDelType2Projects >3) 
				|| (this.unitsOfnoOfDelsType2COIP< noOfDelType2Units)){
			return false;
		}

		else return true;
	}

	public boolean checkForOEL(){
		if(((this.noOfOelsCOIP< 5) || (this.noOfOelProjects >3) 
				|| (this.unitsOFnoOfHuelsCOIP<15)) && elSheet.getChart().getStream2() == null){
			return false;
		}
		else return true;
	}

	public boolean checkTotalCoursework() {

		if(this.totalCoursesCOIP > 40 && this.totalUnitsCOIP > 126) {
			return true;
		}
		else {
			return false;
		}
	}

	public boolean checkPSThesisConditions() {
		//Change?
		if(this.elSheet.getChart().getStream2().isEmpty()) {
			if(this.noOfPSTSCOIP>1) {			
				return true;
			}
			else
				return false;
		}
		else {
			if(this.noOfPSTSCOIP>2) {			
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
}