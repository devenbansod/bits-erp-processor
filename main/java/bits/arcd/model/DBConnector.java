package bits.arcd.model;

import java.sql.*;
import java.util.ArrayList;

import bits.arcd.main.WindowLoader;

public class DBConnector
{
	// Provide a global database name as well as table to work with
	public static String db_name = "erp_temp";
	public static String table_semCharts = "charts";
	public static String table_course = "course";
	public static String table_req_course_map = "req_course_map";
	public static String table_std_req_mapping = "std_req_mapping";
	public static String table_student_enrollment = "student_enrollment";
	public static String table_student_terms = "student_terms";
	public static String table_students = "students";
	public static String table_terms = "terms";
	

	private Connection connection;

	private String db_host, user_nm, passwd;

	public DBConnector()	{
		this.db_host = WindowLoader.IPAddress; this.user_nm = WindowLoader.usernm; 
		this.passwd = WindowLoader.passwd;
		try {
			connection = DriverManager.getConnection(db_host+db_name, user_nm, passwd);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Provides a method for closing connections
	 */
	public void closeConnections()	{
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Provides a method for opening connections
	 */
	public void reopenConnections()	{
		try {
			connection = DriverManager.getConnection(db_host+db_name, user_nm, passwd);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearDatabase()	{
		String clearTable = "TRUNCATE TABLE " + table_semCharts;
		queryExecutor(clearTable, true);
	}

	/**
	 * Provides a safe way to execute queries. Minimizes SQL Injection risks
	 * The parameter "isdatamanipulated" must be set to true when changing database data
	 * Statement.query method is used for data manipulation queries
	 * Statement.executeQuery method is used for non-data manipulation queries
	 */
	public ResultSet queryExecutor(String query, boolean isdatamanipulated)	{
		ResultSet result = null;
		try {
			Statement stmt = connection.createStatement();
			if (isdatamanipulated)	{
				stmt.execute(query);
			}
			else {
				result = stmt.executeQuery(query);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	

	// SEPARATE INNER CLASS FOR HANDLING EXTRA TABLES
	// Used for handling the extra tables data --> Use this to fetch table names, etc. etc.
	public static class ExtraTablesOperator
	{
		// Provide a global database name as well as table to work with
		public static String db_name = "erp_temp";

		// List of tables excluding the EL Chart
		private int noOfExtraTables = 8;
		public static String table_students = "students";
		public static String table_student_programs = "student_programs";
		public static String table_req_course_map = "req_course_map";
		public static String table_student_enrollment = "student_enrollment";
		public static String table_courses = "courses";
		public static String table_terms = "terms";
		public static String table_student_terms = "student_terms";
		public static String table_std_req_mapping = "std_req_mapping";
		// List of tables
		public void refreshTableList()	{
			tables = new ArrayList<String>();
			tables.add(table_students);		tables.add(table_student_programs);	
			tables.add(table_req_course_map);		tables.add(table_student_enrollment);	
			tables.add(table_courses);		tables.add(table_terms);	
			tables.add(table_student_terms);		tables.add(table_std_req_mapping);		
		}

		private ArrayList<String> tables;

		private Connection connection;

		private String db_host, user_nm, passwd;

		private String idNum;

		public String getIdNum() {
			return idNum;
		}

		public void setIdNum(String idNum) {
			this.idNum = idNum;
		}

		public ExtraTablesOperator()	{
			this.db_host = WindowLoader.IPAddress; this.user_nm = WindowLoader.usernm; 
			this.passwd = WindowLoader.passwd;
			try {
				connection = DriverManager.getConnection(db_host+db_name, user_nm, passwd);
				refreshTableList();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Provides a method for closing connections
		 */
		public void closeConnections()	{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		/**
		 * Provides a method for opening connections
		 */
		public void reopenConnections()	{
			try {
				connection = DriverManager.getConnection(db_host+db_name, user_nm, passwd);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void clearAllTables()	{
			for (int i=0; i<noOfExtraTables; i++)	{
				String clearTable = "TRUNCATE TABLE " + tables.get(i);
				queryExecutor(clearTable, true);
				System.out.println(tables.get(i)+" table cleared!");
			}
		}

		/**
		 * Provides a safe way to execute queries. Minimizes SQL Injection risks
		 * The parameter "isdatamanipulated" must be set to true when changing database data
		 * Statement.query method is used for data manipulation queries
		 * Statement.executeQuery method is used for non-data manipulation queries
		 */
		public ResultSet queryExecutor(String query, boolean isdatamanipulated)	{
			ResultSet result = null;
			try {
				Statement stmt = connection.createStatement();
				if (isdatamanipulated)	{
					stmt.execute(query);
				}
				else {
					result = stmt.executeQuery(query);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

		/**
		 * Import a single CSV File at one go into the MySQL database
		 * Note: The method checks iftruncates to "TRUNCATE" i.e., clear 
		 * the entire database, before refilling it
		 * @param csv_path, iftruncates
		 */
		public void importCSVs(boolean iftruncates, 
				String stdCSV,	String std_programsCSV,
				String reqcourse_mapCSV,	String std_enrlCSV, 
				String coursesCSV,	String termsCSV,
				String std_termsCSV,	String std_req_mappingCSV
				)
		{
			if (iftruncates)	{
				clearAllTables();
			}
			String loadQuery = "LOAD DATA LOCAL INFILE '" +  stdCSV + 
					"' INTO TABLE "+table_students+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(sys_id, campus_id, student_name, sex);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  std_programsCSV + 
					"' INTO TABLE "+table_student_programs+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(sys_id, campus_id, career, academic_program, status, career_number, "
					+ "academic_plan, descr, plan_type, admit_term,	req_term);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  reqcourse_mapCSV + 
					"' INTO TABLE "+table_req_course_map+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(sys_id, campus_id, semester, term_taken, class_number, "
					+ "earn_credit, include_GPA, course_id, subject, catalog, "
					+ "course_descr, grade, units, rq_group, rqrmnt, line, "
					+ "sem_course_decription, crse_lst, descr_2, in_prog, report_date);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  std_enrlCSV + 
					"' INTO TABLE "+table_student_enrollment+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(sys_id, semester, class_number, course_id, subject, catalog, "
					+ "course_descr, section, component, repeater, repeat_reason, "
					+ "units_taken, grade, earn_credit, include_GPA, units_att, grade_points);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  coursesCSV + 
					"' INTO TABLE "+table_courses+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(course_id, subject, catalog, course_descr, min_units, eq_course, grading, component);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  termsCSV + 
					"' INTO TABLE "+table_terms+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(career, semester, sem_description, sem_short_description, begin_date, end_date);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  std_termsCSV + 
					"' INTO TABLE "+table_student_terms+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(sys_id, semester, project_level, start_level, end_level, take_prgrs, "
					+ "pass_prgs, take_GPA, pass_GPA, take_no_GPA, pass_no_GPA, inpr_GPA, "
					+ "inpr_no_GPA, grade_points, total, SGPA, CGPA);";
			queryExecutor(loadQuery, false);

			loadQuery = "LOAD DATA LOCAL INFILE '" +  std_req_mappingCSV + 
					"' INTO TABLE "+table_std_req_mapping+" FIELDS TERMINATED BY ','" + "ENCLOSED BY '\"'" +
					" LINES TERMINATED BY '\r\n' " + "IGNORE 1 ROWS" +
					"(sys_id, report_date, rq_group, rqrmnt);";
			queryExecutor(loadQuery, false);

			System.out.println("All tables reloaded ----------------------------");
		}
	}
}