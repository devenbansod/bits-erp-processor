package bits.arcd.model;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.sql.*;

import org.apache.commons.lang.StringUtils;

import bits.arcd.main.WindowLoader;

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
		ResultSet r = dbConnector.checkOptional(this.requirementNo);
		int i=0;
		try {
			while(r.next()){
				i++;
			}
			r.close();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
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

	public DBConnector dbConnector = null;	

	public CourseChartQueries(String requirementNo) {
		super();

		// This number will be used for checking whether a valid requirementNo was passed
		// or not. If it is equal to 0, do nothing, else proceed.......
		requirementGroup = 0;

		dbConnector = DBConnector.getInstance();
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

	}


	private String returnCenteredString(String s){

		int total_spaces = 149 - s.length();

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


		s = s + "\nYEAR     CODE  COURSE NO  COURSE TITLE              UNITS                         ";
		s = s + "CODE  COURSE NO  COURSE TITLE              UNITS                \n";

		s = s + "-----------------------------------------"
				+ "-----------------------------------------"
				+ "---------------------------------------------------------------\n";

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
						String temp = "    ................................................            HUEL      ";

						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfHUEL(); j++){
						String temp = "    ................................................            HUEL       ";						
						sem2Courses.add(temp);

					}


					// Add the Open Electives to be done
					for (int j = 0; j < first.getNoOfOEL(); j++){
						String temp = "    ................................................            EL        ";
						sem1Courses.add(temp);

					}

					for (int j = 0; j < second.getNoOfOEL(); j++){
						String temp = "    ................................................            EL        ";
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
							temp = temp + "      " + "                                                                          ";
						}


						if (j < sem2Courses.size()) {
							temp = temp + sem2Courses.get(j);
						}
						else {
							temp = temp + "                                                                           ";
						}

						temp = temp + "\n";

						s = s + temp;

					}

					s = s + "-----------------------------------------"
							+ "-----------------------------------------"
							+ "---------------------------------------------------------------\n";
				}
			}

			else {

				Semester PS = this.getSemsInChart().get(i);
				s = s + "Summer" + PS.getPS().toString() + "\n";


				s = s + "-----------------------------------------"
						+ "-----------------------------------------"
						+ "---------------------------------------------------------------\n";

				rem = 0;

			}

		}


		return s;
	}

	private void addSems() {		
		int yearNo = 1, semNo = 1;
		while(existsSem(requirementNo, yearNo, semNo)) {

			try {
				semsInChart.add(new Semester(yearNo, semNo, requirementNo, this));
			} catch (SQLException e) {
				WindowLoader.showExceptionDialog("Semester could not be added", e);
				e.printStackTrace();
			}
			if(yearNo == 2 && semNo == 2) {
				addSummerTerm(requirementNo);
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

		ResultSet rs = dbConnector.addSummerTerm(requirementNo);		

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
			rs.close();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Summer Term could not be added", e);
			e.printStackTrace();
		}

	}

	public int getNoOfDELType1 (String req_num, int yearNo, int semNo) {
		ResultSet rs = null; int retVal = 0;

		rs = dbConnector.getNoOfDELsType1(req_num, yearNo, semNo, this.stream1);
		try {
			while(rs.next()){
				retVal = rs.getInt(1);				
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
			e.printStackTrace();
		}		

		return retVal;
	}

	public int getNoOfDELType2 (String req_num, int yearNo, int semNo) {
		ResultSet rs = null; int retVal = 0;		

		rs = dbConnector.getNoOfDELsType1(req_num, yearNo, semNo, this.stream2);
		try {
			while(rs.next()){
				retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
			e.printStackTrace();
		}

		return retVal;
	}


	public int getNoOfHUEL(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
		int num = 0;

		rs = dbConnector.getNoOfHUELsType(req_num, yearNo, semNo);

		try {
			while(rs.next()){
				num = rs.getInt(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
			e.printStackTrace();
		}		
		return num;
	}

	public int getNoOfDEL (String req_num, int yearNo, int semNo) {
		ResultSet rs = null; int retVal = 0;		

		rs = dbConnector.getNoOfDELs(req_num, yearNo, semNo);
		try {
			while(rs.next()){
				retVal = rs.getInt(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
			e.printStackTrace();
		}

		return retVal;
	}

	public int getNoOfOEL(String req_num, int yearNo, int semNo) {
		ResultSet rs = null;
		int num = 0;

		rs = dbConnector.getNoOfOELs(req_num, yearNo, semNo);

		try {
			while(rs.next()){
				num = rs.getInt(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
			e.printStackTrace();
		}		
		return num;		
	}	

	public String getRequirementDescription(String req_num) {
		ResultSet rs = null;
		String req_descr = "";
		rs = dbConnector.getRequirementDescription(req_num);

		try {
			while(rs.next()) {
				req_descr = rs.getString(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
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
		rs = dbConnector.getRequirementGroup(req_num);

		try {
			while(rs.next()) {
				req_gp = rs.getString(1);
			}
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Query errored", e);
			e.printStackTrace();
		}		
		return req_gp;			
	}

	public boolean existsSem (String req_num, int yearNo, int semNo)	{
		ResultSet rs = null; boolean retVal = true;
		// descr_3 string must be created from yearNo and semNo		

		rs = dbConnector.existsSem(req_num, yearNo, semNo);
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
			WindowLoader.showExceptionDialog("SQL Query errored", e);
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
	public static void importChartsData(String csv_path, String filename, boolean iftruncates)
	{
		DBConnector tempConnector = DBConnector.getInstance();
		String table_name = getTableName(filename);


		if (iftruncates)	{
			String truncateQuery = "TRUNCATE TABLE " + table_name;
			tempConnector.queryExecutor(truncateQuery, true);
		}


		String loadQuery = "LOAD DATA LOCAL INFILE '" + 
				csv_path + 
				"' INTO TABLE "+ table_name +" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
				" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +"(" + getColumns(table_name) + ");";

		tempConnector.queryExecutor(loadQuery, false);

	}

	// Keeping this as a static method because it is dependent only only IPAddress, UserName, 
	// and Password which is statically loaded from the WindowLoader class

	public static String batchCSVChartsLoad(String directoryPath)	{
		DBConnector tempConnector = DBConnector.getInstance();
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
					importChartsData(file.getAbsolutePath().replace("\\", "\\\\"), file.getName(), true);
					output.append("\n" + file.getName() + " was added. \n");
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
			WindowLoader.showExceptionDialog("SQL Query errored. Could not set the Streams (eg. A1)", e);
			e.printStackTrace();
		}

	}


	private static String getTableName(String filename){
		String retVal = "";

		if (filename.equalsIgnoreCase("charts.csv"))
			retVal = "charts";
		else if (filename.equalsIgnoreCase("courses.csv"))
			retVal = "courses";
		else if (filename.equalsIgnoreCase("reqcourse_map.csv"))
			retVal = "req_course_map";
		else if (filename.equalsIgnoreCase("std.csv"))
			retVal = "students";
		else if (filename.equalsIgnoreCase("std_enrl.csv"))
			retVal = "student_enrollment";
		else if (filename.equalsIgnoreCase("std_programs.csv"))
			retVal = "student_programs";
		else if (filename.equalsIgnoreCase("terms.csv"))
			retVal = "terms";
		else if (filename.equalsIgnoreCase("std_terms.csv"))
			retVal = "student_terms";
		else if (filename.equalsIgnoreCase("std_req_mapping.csv"))
			retVal = "std_req_mapping";
		else if (filename.equalsIgnoreCase("minor_codes.csv"))
			retVal = "minor_codes";
		else if (filename.equalsIgnoreCase("minor_course_list.csv"))
			retVal = "minor_course_list";
		else if (filename.equalsIgnoreCase("minor_requirements.csv"))
			retVal = "minor_requirements";
		else if (filename.equalsIgnoreCase("std_minor.csv"))
			retVal = "student_minor";

		else {
			System.out.println(filename + " file was never used");
			WindowLoader.showAlertDialog("Invalid File(s)",filename + " file was never used");
		}
		return retVal;
	}


	private static String getColumns(String tablename){

		String retVal = "";

		if (tablename.equalsIgnoreCase("charts"))
			retVal = "career,rq_group,Eff_Date,	status_1,"
					+ "descr,rqrmnt,eff_date_2,status_2,descr_2,"
					+ "line,descr_3,Line_Type,	min_units,min_course,"
					+ "max_units,max_course,dtl_seq, dtl_type,crse_lst,"
					+ "	descr_4,course";
		else if (tablename.equalsIgnoreCase("courses"))
			retVal = "course_id,subject,catalog,"
					+ "course_descr,min_units,eq_course,"
					+ "grading,component";

		else if (tablename.equalsIgnoreCase("req_course_map"))
			retVal = "sys_id,campus_id, semester,term_taken,"
					+ "class_number,earn_credit,include_GPA,"
					+ "course_id,subject,catalog,course_descr,"
					+ "grade,units,	rq_group,rqrmnt,line,"
					+ "sem_course_decription,crse_lst,"
					+ "descr_2,	in_prog,report_date";

		else if (tablename.equalsIgnoreCase("students"))
			retVal = "sys_id, campus_id,student_name,sex";

		else if (tablename.equalsIgnoreCase("student_enrollment"))
			retVal = "sys_id,semester,class_number,course_id,subject,"
					+ "catalog,course_descr,section,component,repeater,"
					+ "	repeat_reason,units_taken,grade,earn_credit,"
					+ "	include_GPA,units_att,grade_points";

		else if (tablename.equalsIgnoreCase("student_programs"))
			retVal = "sys_id,campus_id,career,academic_program,"
					+ "status,career_number,academic_plan,descr,"
					+ "plan_type,admit_term,req_term";

		else if (tablename.equalsIgnoreCase("terms"))
			retVal = "career,semester,sem_description ,"
					+ "sem_short_description,begin_date,end_date";

		else if (tablename.equalsIgnoreCase("student_terms"))
			retVal = "sys_id,semester,project_level,start_level,end_level,"
					+ "take_prgrs,pass_prgs,take_GPA,pass_GPA,"
					+ "take_no_GPA,pass_no_GPA,inpr_GPA,inpr_no_GPA,"
					+ "grade_points,total,SGPA,	CGPA";

		else if (tablename.equalsIgnoreCase("std_req_mapping"))
			retVal = "sys_id,report_date,rq_group,rqrmnt";

		else if (tablename.equalsIgnoreCase("minor_codes"))
			retVal = "minor_code,program";

		else if (tablename.equalsIgnoreCase("minor_course_list"))
			retVal = "minor_code,course_id,	type";

		else if (tablename.equalsIgnoreCase("minor_requirements"))
			retVal = "minor_code,type,min_course,max_course";

		else if (tablename.equalsIgnoreCase("student_minor"))
			retVal = "serial_no,sys_id,	campus_id,"
					+ "	student_name,minor_code";

		else
			System.out.println("The columns related to table " + tablename + " were not found");

		return retVal;


	}

	// End of this class

}
