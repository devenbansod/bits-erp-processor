package bits.arcd.model;

import java.util.ArrayList;
import java.sql.*;

public class Semester {

	//String semName; //like Civil-Y1S1

	private int yearNo, semNo;	
	@SuppressWarnings("unused")
	private String requirementNo, requirementGroup;

	private ArrayList<Course> compulsoryCourses = new ArrayList<Course>();
	private ArrayList<Course> humanitiesCourses = new ArrayList<Course>();
	private ArrayList<Course> delCourses = new ArrayList<Course>();
	private ArrayList<Course> openElCourses = new ArrayList<Course>();
	private ArrayList<Course> allCourses = new ArrayList<Course>();

	private Course optionalCourse;
	private Course practiceSchoolOne ;

	private int noOfHUEL ;//no of huels in the sem
	private int noOfDEL;// no of discipline el	
	private int noOfDELType1; // A3, A7, B3
	private int noOfDELType2; // B3 , A2, A4
	private int noOfOEL ;// no of open electives in the sem	

	public boolean hasOptional; //checks if semester has optional course (POE/POM)
	public boolean isSummerTerm; // checks if semester is summer term

	public Semester(int yearNo, int semNo, String requirementNo, CourseChartQueries chartOps) throws SQLException {
		super();		

		this.yearNo = yearNo;
		this.semNo = semNo;
		this.requirementNo = requirementNo;

		ResultSet rs =  chartOps.dbConnector.getCompulsoryCoursesForOneSem(requirementNo, yearNo, semNo);		

		int i = 0;
		
		
		
		while(rs.next()) {		
			Course c = new Course(rs.getInt(22), rs.getString(23), rs.getString(24),
					rs.getString(25), rs.getInt(26), rs.getInt(26));
			c.setIsNamedCourse(true);
			this.compulsoryCourses.add(c);
			i++;
		}			
		
		this.noOfHUEL = chartOps.getNoOfHUEL(requirementNo, yearNo, semNo);

		this.noOfDELType1 = chartOps.getNoOfDELType1(requirementNo, yearNo, semNo);
		int temp1 = this.noOfDELType1;
		while(temp1>0) {
			Course c = new Course();
			c.setElDescr(chartOps.getStream1()+"EL");
			this.addDisciplineElectives(c);
			temp1--;
		}

		this.noOfDELType2 = chartOps.getNoOfDELType2(requirementNo, yearNo, semNo);
		int temp2 = this.noOfDELType2;
		while(temp2>0) {
			Course c = new Course();
			c.setElDescr(chartOps.getStream2()+"EL");
			this.addDisciplineElectives(c);
			temp2--;
		}

		this.noOfDEL = this.noOfDELType1 + this.noOfDELType2;

		this.noOfOEL = chartOps.getNoOfOEL(requirementNo, yearNo, semNo);

		// Adding optional course properties for chart
		if(this.yearNo==2 && this.semNo==2){
			this.hasOptional = chartOps.checkIfHasOptional();
			if (this.hasOptional)
			setOptionalCourse(new Course(1024, "MGTS", "F211", "PRINCIPLES OF MANAGEMENT", 3, 3));				
		}			
		else 
			this.hasOptional = false;

		//it will send a query to teh database to find the number of HUELS,DELS and open electives and 
		// initiate the respective values here
		// look for couses in the database that have the same semester name 
		//as that of semName and return info of the course.(courseID,subject,etc). Using this info a new course
		// will be created and added to the arraylist. This query will run in loop till all the courses that have the 
		// same semName written beside it in the database have been added to the semester object
	}


	public void addHumanitiesElectives (Course c) {
		this.humanitiesCourses.add(c);
	}

	public void addDisciplineElectives (Course c) {
		this.delCourses.add(c);
	}

	public void addOpenElectives (Course c) {	
		this.openElCourses.add(c);
	}

	public void addAllCourses(ArrayList<Course> compulsory,ArrayList<Course>
	HUEL,ArrayList<Course> DEL,ArrayList<Course> OEL,Course optional, Course PS) {
		this.allCourses.addAll(compulsory);
		this.allCourses.addAll(HUEL);
		this.allCourses.addAll(DEL);
		this.allCourses.addAll(OEL);		
		if(this.hasOptional) this.allCourses.add(optional);		
		if(this.isSummerTerm) this.allCourses.add(PS);		
	}

	@Override
	public String toString() {
		String s = "";

		// Print compulsory courses
		for(Course c : compulsoryCourses){
			s +=  c.toString();
		}

		// Print optional course if any
		if(this.hasOptional)
			s += optionalCourse.toString();

		// Print hum courses
		for(Course c : humanitiesCourses){
			s +=  c.toString();
		}		

		for (int i=0 ; i < noOfHUEL; i++){
			s += "----- \tHUEL\n";
		}

		//Print del courses		
		for(Course c : delCourses){
			s +=  c.toString();
		}		

		for (int i=0 ; i < noOfDEL; i++){
			s += "----- \tDEL\n";
		}

		// Print open electives		
		for(Course c : openElCourses){
			s +=  c.toString();
		}		

		for (int i=0 ; i < noOfOEL; i++){
			s += "----- \tOEL\n";
		}

		// Print practice school if summer term

		if(this.isSummerTerm) {
			s += this.practiceSchoolOne.toString();
		}

		String title = "\n\nSemester [yearNo" + this.yearNo + "\tsemNo" + this.semNo + "]\n\n";

		if(this.isSummerTerm){
			title = "\n\nSummer Term\n\n";
		}

		return (title + s);
	}
	
	// getters and setters
	public ArrayList<Course> getAllCourses() {
		return this.allCourses;
	}

	public int getYearNo() {
		return yearNo;
	}

	public void setYearNo(int yearNo) {
		this.yearNo = yearNo;
	}

	public int getSemNo() {
		return semNo;
	}

	public void setSemNo(int semNo) {
		this.semNo = semNo;
	}

	public String getRequirementNo() {
		return requirementNo;
	}

	public Course getPS() {
		return practiceSchoolOne;
	}

	public void setRequirementNo(String requirementNo) {
		this.requirementNo = requirementNo;
	}

	public ArrayList<Course> getCompulsoryCourses() {
		return compulsoryCourses;
	}	

	public void setCompulsoryCourses(ArrayList<Course> namedCourses) {
		this.compulsoryCourses = namedCourses;
	}

	public ArrayList<Course> getHumanitiesCourses() {
		return humanitiesCourses;
	}

	public void setHumanitiesCourses(ArrayList<Course> humanitiesCourses) {
		this.humanitiesCourses = humanitiesCourses;
	}

	public ArrayList<Course> getDelCourses() {
		return delCourses;
	}

	public void setDelCourses(ArrayList<Course> delCourses) {
		this.delCourses = delCourses;
	}

	public ArrayList<Course> getOpenElCourses() {
		return openElCourses;
	}

	public void setOpenElCourses(ArrayList<Course> openElCourses) {
		this.openElCourses = openElCourses;
	}

	public int getNumOfDelCompleted() {
		return this.delCourses.size();
	}

	public int getNumOfHuelCompleted() {
		return this.humanitiesCourses.size();
	}

	public int getNumOfOellCompleted() {
		return this.openElCourses.size();
	}

	public Course getOptionalCourse() {
		return optionalCourse;
	}

	public void setOptionalCourse(Course optionalCourse) {
		this.optionalCourse = optionalCourse;
	}

	public int getNoOfHUEL() {
		return noOfHUEL;
	}

	public void setNoOfHUEL(int noOfHUEL) {
		this.noOfHUEL = noOfHUEL;
	}

	public int getNoOfDEL() {
		return noOfDEL;
	}

	public void setNoOfDEL(int noOfDEL) {
		this.noOfDEL = noOfDEL;
	}

	public int getNoOfOEL() {
		return noOfOEL;
	}

	public void setNoOfOEL(int noOfOEL) {
		this.noOfOEL = noOfOEL;
	}

	public boolean isSummerTerm() {
		return isSummerTerm;
	}

	public void setIsSummerTerm(boolean isSummerTerm) {
		this.isSummerTerm = isSummerTerm;
	}

	public void setPS(Course praticeSchoolOne){
		this.practiceSchoolOne = praticeSchoolOne;
	}

}
