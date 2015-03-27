package bits.arcd.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class GraduationRequirements {

	private EligibilitySheetQueries elSheet;

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
	
	
}

