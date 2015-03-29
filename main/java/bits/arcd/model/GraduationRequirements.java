package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GraduationRequirements {

	private EligibilitySheetQueries elSheet;
	private String StudentId;
	private int noOfOELSCompleteOrInProgress,
	noOfHUELScompleteOrInProgress;
	private int unitsOfnoOfOELSCompleteOrInProgress,unitsOFnoOfHUELScompleteOrInProgress;
	private int noOFHUELProjects,noOfELProjects ;
	private int noOfDelsType1CompleteOrInProgress;
	private int unitsOfnoOfDelsType1CompleteOrInProgress;
	private int noOfDelsType2CompleteOrInProgress;
	private int unitsOfnoOfDelsType2CompleteOrInProgress;
	private int noOfDelType1Projects;
	private int noOfDelType2Projects;
	
	ArrayList<Course> incompleteNamedCourses = new ArrayList<Course>();
	private int noOfIncompleteNamedCourses;

	public GraduationRequirements(String studentId) {		

		super();
		this.elSheet = new EligibilitySheetQueries(studentId, 1131);
		checkIfLikelyToGraduate(elSheet);	


	}

	private void checkIfLikelyToGraduate(EligibilitySheetQueries elSheet) {

		boolean areNamedCoursesDone = checkIfNamedCoursesDone(elSheet);
		boolean areHuelsDone = checkIfHuelsDone(elSheet);
		boolean areDelsDone = checkIfDelsDone(elSheet);

	}

	private boolean checkIfNamedCoursesDone(EligibilitySheetQueries elSheet) {
		CourseChartQueries chart = elSheet.getChart();
		ArrayList<Semester> sems = chart.getSemsInChart();

		for(Semester sem : sems){			
			for(Course c: sem.getCompulsoryCourses()) {				
				c.checkAndSetGradeValidAndGradeComplete();
				if(!(c.isGradeComplete() || (c.isInProgress().equalsIgnoreCase("Y")))){
					return false;
				}													
			}			
		}

		return true;		
	}

	private boolean checkIfHuelsDone(EligibilitySheetQueries elSheet) {
		CourseChartQueries chart = elSheet.getChart();
		ArrayList<Semester> sems = chart.getSemsInChart();

		int huelsCounted = 0;
		int unitsHuelsCounted = 0;

		for(Semester sem : sems){						
			for(Course c: sem.getHumanitiesCourses()) {
				c.checkAndSetGradeValidAndGradeComplete();
				if(c.isGradeComplete() || (c.isInProgress().equalsIgnoreCase("Y"))){
					huelsCounted++;
					unitsHuelsCounted += c.getMaxUnits();
				}
			}

		}

		if(huelsCounted < 3 || unitsHuelsCounted<8) {
			return false;
		}
		else if(huelsCounted>=3 && unitsHuelsCounted>=8) {
			return true;			
		}

		return false;
	}

	private boolean checkIfDelsDone(EligibilitySheetQueries elSheet) {
		CourseChartQueries chart = elSheet.getChart();
		ArrayList<Semester> sems = chart.getSemsInChart();

		int delsCounted = 0;
		int unitsHuelsCounted = 0;

		for(Semester sem : sems){						
			for(Course c: sem.getHumanitiesCourses()) {
				c.checkAndSetGradeValidAndGradeComplete();
				if(c.isGradeComplete() || (c.isInProgress().equalsIgnoreCase("Y"))){
					delsCounted++;
					unitsHuelsCounted += c.getMaxUnits();
				}
			}

		}

		//		if(huelsCounted < 3 || unitsHuelsCounted<8) {
		//			return false;
		//		}
		//		else if(huelsCounted>=3 && unitsHuelsCounted>=8) {
		//			return true;			
		//		}

		return false;
	}

	//	public int getNoOfCompletedorIsInProgress(EligibilitySheetQueries elsheet){
	//		int count = 0;
	//		
	//		
	//		int countOfsems = this.elSheet.getChart().getSemsInChart().size();
	//		for(int i=0;i<countOfsems;i++){
	//			
	//			Semester s= elSheet.getChart().getSemsInChart().get(i);
	//			for(Course c : s.getAllCourses()){
	//				
	//				c.checkAndSetGradeValidAndGradeComplete();
	//				if(c.isGradeComplete() ||c.isInProgress().equalsIgnoreCase("Y")){
	//					count++;
	//					if(c.isDel()){
	//						this.noOfDelsCompleteOrInProgress++;
	//						this.unitsOfnoOfDelsCompleteOrInProgress+=c.getMaxUnits();
	//						if(c.getIsProjectTypeCourse()){
	//							this.noOfDelProjects++;
	//						}
	//					}
	//					
	//					if(c.isHuel()){
	//						this.noOfHUELScompleteOrInProgress++;
	//						this.unitsOFnoOfHUELScompleteOrInProgress+=c.getMaxUnits();
	//						if(c.getIsProjectTypeCourse()){
	//							this.noOFHUELProjects++;
	//						}
	//					}
	//					if(c.isOel()){
	//						this.noOfOELSCompleteOrInProgress++;
	//						this.unitsOfnoOfOELSCompleteOrInProgress+=c.getMaxUnits();
	//						if(c.getIsProjectTypeCourse()){
	//							this.noOfELProjects++;
	//						}
	//					}
	//				}
	//			}

	public void getNoOfCompletedorIsInProgress(EligibilitySheetQueries elsheet){
			
		int countOfsems = this.elSheet.getChart().getSemsInChart().size();
		for(int i=0;i<countOfsems;i++){

			Semester s= elSheet.getChart().getSemsInChart().get(i);
			for(Course c : s.getAllCourses()){
				c.checkAndSetGradeValidAndGradeComplete();
				if(c.isNamedCourse()) {					
					
					if(!(c.isGradeComplete() ||c.isInProgress().equalsIgnoreCase("Y"))){
						this.noOfIncompleteNamedCourses++;
						this.incompleteNamedCourses.add(c);
					}
				}
				
				if(c.isDel()){
					
					if(c.getElDescr().equalsIgnoreCase(elsheet.getChart().getStream1())) {
						this.noOfDelsType1CompleteOrInProgress++;
						this.unitsOfnoOfDelsType1CompleteOrInProgress+=c.getMaxUnits();
						if(c.getIsProjectTypeCourse()){
							this.noOfDelType1Projects++;
						}
					}
					
					if(c.getElDescr().equalsIgnoreCase(elsheet.getChart().getStream2())) {
						this.noOfDelsType2CompleteOrInProgress++;
						this.unitsOfnoOfDelsType2CompleteOrInProgress+=c.getMaxUnits();
						if(c.getIsProjectTypeCourse()){
							this.noOfDelType2Projects++;
						}
					}
				}

				if(c.isHuel()){
					this.noOfHUELScompleteOrInProgress++;
					this.unitsOFnoOfHUELScompleteOrInProgress+=c.getMaxUnits();
					if(c.getIsProjectTypeCourse()){
						this.noOFHUELProjects++;
					}
				}
				if(c.isOel()){
					this.noOfOELSCompleteOrInProgress++;
					this.unitsOfnoOfOELSCompleteOrInProgress+=c.getMaxUnits();
					if(c.getIsProjectTypeCourse()){
						this.noOfELProjects++;
					}
				}
			}
		}




	}
	
	private int[] getNumCoursesAndUnits(String elStream) {
		
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

	public boolean checkForHUEL(){
		if((this.noOfHUELScompleteOrInProgress< 3) || (this.noOFHUELProjects >1) 
				|| (this.unitsOFnoOfHUELScompleteOrInProgress<8)){
			return false;
		}
		else return true;
	}
	
	public boolean checkForDEL(){
		
		String delStream1 = elSheet.getChart().getStream1();
		int[] cu = getNumCoursesAndUnits(delStream1);		
		int noOfDelType1Courses = cu[0];
		int noOfDelType1Units = cu[1];
		
		int noOfDelType2Courses=0, noOfDelType2Units=0;
		
		String delStream2 = elSheet.getChart().getStream2();
		if(delStream2.isEmpty()){
			noOfDelType2Courses = 0;
			noOfDelType2Units = 0;
		}
		else {
			int[] cu2 = getNumCoursesAndUnits(delStream1);		
			noOfDelType2Courses = cu2[0];
			noOfDelType2Units = cu2[1];
		}
		
		if((this.noOfDelsType1CompleteOrInProgress< noOfDelType1Courses) || (this.noOfDelType1Projects >3) 
				|| (this.unitsOfnoOfDelsType1CompleteOrInProgress< noOfDelType1Units)){
			return false;
		}
		if((this.noOfDelsType2CompleteOrInProgress< noOfDelType2Courses) || (this.noOfDelType2Projects >3) 
				|| (this.unitsOfnoOfDelsType2CompleteOrInProgress< noOfDelType2Units)){
			return false;
		}
		
		else return true;
	}

public int getNoOfTotalCourses(EligibilitySheetQueries elsheet){
	int count = 0;
	int countOfsems = elSheet.getChart().getSemsInChart().size();
	for(int i=0;i<countOfsems;i++){
		Semester s= elSheet.getChart().getSemsInChart().get(i);
		count += s.getNumOfDelCompleted()+s.getNoOfDEL()+s.getNoOfHUEL()+s.getNumOfHuelCompleted()+s.getNoOfOEL()+s.getNumOfOellCompleted()
				+s.getCompulsoryCourses().size();
		if(s.hasOptional || s.isSummerTerm)
			count++;
	}
	return count;
}

public boolean checkForOEL(){
	if((this.noOfOELSCompleteOrInProgress< 5) || (this.noOfELProjects >3) 
			|| (this.unitsOFnoOfHUELScompleteOrInProgress<15)){
		return false;
	}
	else return true;
}
}