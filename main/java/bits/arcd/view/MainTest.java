package bits.arcd.view;



import bits.arcd.main.WindowLoader;
import bits.arcd.model.CourseChartQueries;
import bits.arcd.model.EligibilitySheetQueries;
import bits.arcd.model.Semester;

public class MainTest {
	public static void main(String[] args){
		String k = "D:/Dropbox/ERP Volunteers/Data/sample_data/";
		//ELChartOperator e0 = new ELChartOperator("jdbc:mysql://"+SemChartController.IPAddress+"/","ghazi","erp321");
		//e0.importCSVs(true, k+"std.csv", k+"std_programs.csv", k+"reqcourse_map.csv", k+"std_enrl.csv", k+"courses.csv", k+"terms.csv", k+"std_terms.csv", k+"std_req_mapping.csv");
		EligibilitySheetQueries e1 = new EligibilitySheetQueries("2012A8PS187P", 1131);
		//System.out.println(e1.getSystemId("2012B3A7316P"));
		//CourseChartQueries c = e1.getChart();
		//System.out.println(c);
		System.out.println(e1.getCalcultedCUP()+" : "+e1.getCalculatedUnits());
		System.out.println(e1.getAccumulatedCUP()+" : "+ e1.getAccumulatedUnits());
		//c.
//		Semester s1 = e1.getChart().getSemsInChart().get(4);
		//s1.getCompulsoryCourses().get(0).getGrade();
		System.out.println(e1.toString());
	}
}	
