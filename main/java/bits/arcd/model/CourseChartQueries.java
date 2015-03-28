package bits.arcd.model;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.sql.*;

import org.apache.commons.lang.StringUtils;

public class CourseChartQueries {

	private String requirementNo;
	private int requirementGroup;
	private String requirementDescription ;	
	private ArrayList<Semester> semsInChart = new ArrayList<Semester>();
	private String reqGroupCheck;

	private String stream1; //eg: A3, A8, B4, B3
	private String stream2; // eg: A4, A2		

	
	// Maximum year, sem --> Default set to one
	private int[] yrNsem = {1,1};

	public int[] getYrNsem() {
		return yrNsem;
	}
	
	public boolean checkIfHasOptional(){
		String query = "SELECT descr_4 FROM charts WHERE rqrmnt = '" +this.requirementNo+ "' AND descr_4 LIKE '%Opti%'";
		ResultSet r = dbConnector.queryExecutor(query, false);
		int i=0;
		try {
			while(r.next()){
				i++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(i>0){
			return true;
		}
		else return false;
	}

	public void setYrNsem(int[] yrNsem) {
		this.yrNsem = yrNsem;
	}

	private DBConnector dbConnector = null;	
	// db_op.batchCSVLoad("D:\\Dropbox");

	public CourseChartQueries(String requirementNo) {
		super();

		// This number will be used for checking whether a valid requirementNo was passed
		// or not. If it is equal to 0, do nothing, else proceed.......
		requirementGroup = 0;

		dbConnector = new DBConnector();
		this.requirementNo = requirementNo;

		reqGroupCheck = getRequirementGroup(requirementNo).trim();
		// Proceed after passing the following checks only!!
		if (reqGroupCheck != null && !(reqGroupCheck.equals("")))	{
			// Is a number check
			if (StringUtils.isNumeric(reqGroupCheck))	{
				requirementGroup = Integer.parseInt(getRequirementGroup(requirementNo));
				requirementDescription = getRequirementDescription(requirementNo);		
				this.setStreams(requirementNo);
				this.addSems();
			}
		}
		// Don't forget to close the connections!!!
		dbConnector.closeConnections();
	}

	
	private String returnCenteredString(String s){
		
		int total_spaces = 135 - s.length();
		
		for(int i = 0; i < total_spaces/2; i++){
			s = " " + s + " ";
		}
		
		return s.toUpperCase();
	}

	
	private int maxof(int a, int b) {
		
		if (a > b)
			return a;
		else 
			return b;
		
	}
	
	
	@Override
	public String toString() {
		
		String s = "";
		int i = 0;
		
		s = s + returnCenteredString("BITS Pilani") + "\n";
		
		s = s + returnCenteredString("CHART FROM 2011 ONWARDS")  + "\n";
		
		String details = "REQ. NO : " + this.requirementNo + "   REQ. GROUP : "  + this.requirementGroup 
				+ "   REQ. DESCRIPTION : " + this.requirementDescription;
		
		s = s + returnCenteredString(details) + "\n";
		
		
		s = s + "\nYEAR     COMP  COURSE NO  COURSE TITLE              GRADES                ";
		s = s + "COMP  COURSE NO  COURSE TITLE              GRADES                ";
		
		s = s + "-----------------------------------------"
				+ "-----------------------------------------"
				+ "---------------------------------------------------------\n";
//		for(Semester sem :this.getCh().getSemsInChart()){
		
		
		int rem = 1;
		for ( i = 0; i < this.getSemsInChart().size() - 1; i++) {
			
			if (this.getSemsInChart().get(i).isSummerTerm() != true) {

				if ( i % 2 == rem ) {
					continue;
				}
				
				else {
					
					Semester first = this.getSemsInChart().get(i);
					Semester second = this.getSemsInChart().get(i+1);
								
					ArrayList<String> sem1Courses = new ArrayList<String>();
					ArrayList<String> sem2Courses = new ArrayList<String>();
					
					
					
					// Add the Compulsory Courses string
//					System.out.println(first.getCompulsoryCourses().size());
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
					
					// For Optional Courses
					int t = 0;
					if (second.getOptionalCourse() != null) {
						t = 1;
					}
					
					for (int j = 0; j < t; j++){
						
						sem2Courses.add(second.getOptionalCourse().toString());
						
					}
					
					
					// Add the Hum Electives to be done
					
					for (int j = 0; j < first.getNoOfHUEL(); j++){
						String temp = "    ------------------------------------------------          HUEL ";
				
						sem1Courses.add(temp);
						
					}
					
					for (int j = 0; j < second.getNoOfHUEL(); j++){
						String temp = "    ------------------------------------------------          HUEL ";						
						sem2Courses.add(temp);
						
					}
					
					/**
					// Add the Disp Electives to be done
					for (int j = 0; j < first.getNoOfDEL(); j++){
						String temp = "   ------------------------------------------------        DEL    ";	
						sem1Courses.add(temp);
						
					}
					
					for (int j = 0; j < second.getNoOfDEL(); j++){
						String temp = "   ------------------------------------------------        DEL    ";
						
						sem2Courses.add(temp);
						
					}
					
					*/
					
					// Add the Open Electives to be done
					for (int j = 0; j < first.getNoOfOEL(); j++){
						String temp = "    ------------------------------------------------          EL   ";
						sem1Courses.add(temp);
						
					}
					
					for (int j = 0; j < second.getNoOfOEL(); j++){
						String temp = "    ------------------------------------------------          EL   ";
						sem2Courses.add(temp);
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
				
				Semester PS = this.getSemsInChart().get(i);
				s = s + "Summer" + PS.getPS().toString() + "\n";
				
				
				s = s + "-----------------------------------------"
						+ "-----------------------------------------"
						+ "---------------------------------------------------------\n";
				
				rem = 0;
			
			}
			
		}
		
	
	
//		return "Chart [Requirement No: " + this.requirementNo + ",\tRequirement Group: " 
//		+ this.requirementGroup + ",\tRequirement Description: "
//		+ this.requirementDescription +"]\n" + s;
		
		return s;
	}
	
	private void addSems() {		
		int yearNo = 1, semNo = 1;
		while(existsSem(requirementNo, yearNo, semNo)) {
			try {

				semsInChart.add(new Semester(yearNo, semNo, requirementNo, this));
				if(yearNo == 2 && semNo == 2) {
					addSummerTerm(requirementNo);
				}

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(semNo==1){ semNo++; yrNsem[0] = yearNo; yrNsem[1] = semNo;}
			else {
				semNo = 1;
				yearNo++;
				yrNsem[0] = yearNo; yrNsem[1] = semNo;
			}		
		}
	}

	private void addSummerTerm(String requirementNo) {

		String query = "select * from " + DBConnector.table_semCharts + "  t, "
				+ "courses c WHERE (t.course = c.course_id or t.course = '') and rqrmnt = " +
				requirementNo+ " and descr_3 like '%ummer%erm%'";

		ResultSet rs = dbConnector.queryExecutor(query, false);		
		//System.out.println(query);

		int i =0;		
		try {
			if (rs.next()){
				i++;
				if(i>0){

					Semester summerTerm = new Semester(0, 0, requirementNo, this);			
					Course practiceSchool = new Course(rs.getInt(22), rs.getString(23), rs.getString(24),
							rs.getString(25), rs.getInt(26), rs.getInt(26));					

					summerTerm.setPS(practiceSchool);
					semsInChart.add(summerTerm);
					summerTerm.setIsSummerTerm(true);
				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public ResultSet getCompulsoryCoursesForOneSem(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
		//System.out.println(req_grp);
		String sqlQuery = "SELECT * FROM "+DBConnector.table_semCharts
				+ " t, courses c WHERE (t.course = c.course_id or t.course = '') and rqrmnt = '"+req_num
				+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
				+"AND descr_4 not like '%lective%'"
				+"AND descr_4 not like '%Opti%'";// AND rq_group = "+req_grp+";";

		//System.out.println(sqlQuery);
		rs = dbConnector.queryExecutor(sqlQuery, false);
		return rs;
	}

	
	//+++++++++++++++++++++++++++++++++++++++++++++++	
	
//	public int getNoOfDEL (String req_num, int yearNo, int semNo) {
//		ResultSet rs = null; int retVal = 0;		
//		
//		String sqlQuery = "SELECT min_course FROM "+DBConnector.table_semCharts +
//				" WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
//				+"AND descr_4 like '%isp%%lective%' ";
//		
//		rs = dbConnector.queryExecutor(sqlQuery, false);
//		try {
//			while(rs.next()){
//				retVal = rs.getInt(1);
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return retVal;
//	}	

	
	public int getNoOfDELType1 (String req_num, int yearNo, int semNo) {
		ResultSet rs = null; int retVal = 0;
		
		
		String sqlQuery = "SELECT min_course FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
				+"AND descr_4 like '"+ stream1 +"%isp%%lective%' ";
		
		rs = dbConnector.queryExecutor(sqlQuery, false);
		try {
			while(rs.next()){
				retVal = rs.getInt(1);				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		return retVal;
	}
	
	public int getNoOfDELType2 (String req_num, int yearNo, int semNo) {
		ResultSet rs = null; int retVal = 0;		
		
		String sqlQuery = "SELECT min_course FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
				+"AND descr_4 like '"+ stream2 +"%isp%%lective%' ";
		
		rs = dbConnector.queryExecutor(sqlQuery, false);
		try {
			while(rs.next()){
				retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	
	public int getNoOfHUEL(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
		int num = 0;
		String sqlQuery = "SELECT min_course FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
				+"AND descr_4 like '%um%lective%' ";
		rs = dbConnector.queryExecutor(sqlQuery, false);

		try {
			while(rs.next()){
				num = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return num;
	}

	public int getNoOfDEL (String req_num, int yearNo, int semNo) {
		ResultSet rs = null; int retVal = 0;		
		String sqlQuery = "SELECT min_course FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
				+"AND descr_4 like '%isp%%lective%' ";//+"AND rq_group = "+req_grp+";";
		rs = dbConnector.queryExecutor(sqlQuery, false);
		try {
			while(rs.next()){
				retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return retVal;
	}

	public int getNoOfOEL(String req_num, int yearNo, int semNo) {
		ResultSet rs = null;
		int num = 0;
		String sqlQuery = "SELECT min_course FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%' "
				+"AND descr_4 like '%pen%lective%' ";//+"AND rq_group = "+req_grp+";";
		rs = dbConnector.queryExecutor(sqlQuery, false);

		try {
			while(rs.next()){
				num = rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return num;		
	}	

	public String getRequirementDescription(String req_num) {
		ResultSet rs = null;
		String req_descr = "";
		String sqlQuery = "SELECT descr FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = " + req_num +
				" LIMIT 1;";
		rs = dbConnector.queryExecutor(sqlQuery, false);

		try {
			while(rs.next()) {
				req_descr = rs.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return req_descr;			
	}

	public String getRequirementGroup(String req_num) {
		ResultSet rs = null;
		String req_gp = "";
		String sqlQuery = "SELECT rq_group FROM "+DBConnector.table_semCharts +
				" WHERE rqrmnt = " + req_num +
				" LIMIT 1;";
		rs = dbConnector.queryExecutor(sqlQuery, false);

		try {
			while(rs.next()) {
				req_gp = rs.getString(1);
				//System.out.println(req_gp);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return req_gp;			
	}

	public boolean existsSem (String req_num, int yearNo, int semNo)	{
		ResultSet rs = null; boolean retVal = true;
		// descr_3 string must be created from yearNo and semNo		
		String sqlQuery = "SELECT COUNT(*) FROM "+DBConnector.table_semCharts
				+ " WHERE rqrmnt = '"+req_num+"' and descr_3 like 'Year "+yearNo+" Sem "+semNo+"%';";
		rs = dbConnector.queryExecutor(sqlQuery, false);
		try {
			while(rs.next()) {
				if (rs.getInt(1)==0)	{
					retVal = false;
				}
				else {
					retVal = true;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retVal;
	}

	// Keeping this as a static method because it is dependent only only IPAddress, UserName, 
	// and Password which is statically loaded from the WindowLoader class
	/**
	 * Import a single CSV File at one go into the MySQL database
	 * Note: The method checks iftruncates to "TRUNCATE" i.e., clear 
	 * the table, before refilling it
	 * @param csv_path, iftruncates
	 */
	public static void importChartsData(String csv_path, String table_name, boolean iftruncates)
	{
		DBConnector tempConnector = new DBConnector();
		if (iftruncates)	{
			String truncateQuery = "TRUNCATE TABLE " + table_name;
			tempConnector.queryExecutor(truncateQuery, true);
		}
		String loadQuery = "LOAD DATA LOCAL INFILE '" + 
				csv_path + 
				"' INTO TABLE "+ table_name +" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
				" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
				"(career, rq_group, Eff_Date, status_1, descr, rqrmnt, eff_date_2, status_2,"
				+ "descr_2, line,descr_3, Line_Type, min_units, min_course, max_units, max_course,"
				+ "dtl_seq, dtl_type, crse_lst, descr_4, course);";

		tempConnector.queryExecutor(loadQuery, false);
		//System.out.println("Single operation completed ----------------------------");
	}

	// Keeping this as a static method because it is dependent only only IPAddress, UserName, 
	// and Password which is statically loaded from the WindowLoader class

	public static String batchCSVChartsLoad(String directoryPath)	{
		DBConnector tempConnector = new DBConnector();
		StringBuffer output = new StringBuffer();
		File directory = new File(directoryPath);

		if(directory.exists() && directory.isDirectory()) {
			// get all the files from a directory
			File[] fList = directory.listFiles();
			// clear the existing database first
			tempConnector.clearDatabase();
			// filter for files with .csv type
			for (File file : fList) {
				if (file.isFile() && file.getName().endsWith(".csv")) {
					try {
						//System.out.println(file.getCanonicalPath().replace("\\", "\\\\"));
						output.append(file.getCanonicalPath().replace("\\", "\\\\"));
						importChartsData(file.getAbsolutePath().replace("\\", "\\\\"), file.getName(), false);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
			}
			return output.append("\nAbove files got added to the database!").toString();
		}
		else {
			return output.append("\nDirectory is invalid!").toString();
		}
	}

	// Section of getters and setters
	public String getRequirementNo() {
		return requirementNo;
	}

	public void setRequirementNo(String requirementNo) {
		this.requirementNo = requirementNo;
	}

	public int getRequirementGroup() {
		return requirementGroup;
	}

	public void setRequirementGroup(int requirementGroup) {
		this.requirementGroup = requirementGroup;
	}

	public String getRequirementDescription() {
		return requirementDescription;
	}

	public void setRequirementDescription(String requirementDescription) {
		this.requirementDescription = requirementDescription;
	}

	public ArrayList<Semester> getSemsInChart() {
		return semsInChart;
	}
	
	private String getTableColumns(String table_name) {
		
		String s = "";

		if (table_name == dbConnector.table_semCharts)
			s= "career,	rq_group,Eff_Date,	status_1,descr,rqrmnt,eff_date_2,"
				+ "status_2,descr_2,line,descr_3,Line_Type, min_units,min_course,"
				+ "max_units,max_course,dtl_seq,dtl_type,crse_lst,descr_4,course";
		
		else if (table_name == dbConnector.table_course)
			return s;
		
		
		return s;
	}
	
	public String getStream1(){
		return this.stream1;
	}
	
	public String getStream2(){
		return this.stream2;
	}
	
	public void setStreams(String requirementNo) {
		String query = "Select DISTINCT descr_4 from charts" 
				+ " where descr_4 like '%isp%lective%' and rqrmnt = '" + requirementNo
				+ "' LIMIT 2";
		
		ResultSet rs = dbConnector.queryExecutor(query, false);
		
		int i=0;
		try {
			while(rs.next()) {
				if(i==0) {
					this.stream1 = rs.getString(1).substring(0, 2);
				}
				else {
					if(this.stream1.equalsIgnoreCase(rs.getString(1).substring(0, 2))){
						this.stream2 = "";
					}
					else {
						this.stream2 = rs.getString(1).substring(0, 2);
					}
				}
				i++;					
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// End of this class

}
