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
	
	private static DBConnector d = new DBConnector();
	
	private Connection connection;

	private String db_host, user_nm, passwd;

	private PreparedStatement psAddSummerTerm = null;
	private PreparedStatement psGetCompulsoryCourses = null;
	private PreparedStatement psCheckIsOptional = null;
	private PreparedStatement psGetNoOfDELsType1 = null;
	private PreparedStatement psGetRequirementDescription = null;
	private PreparedStatement psGetRequirementGroup = null;
	private PreparedStatement psExistsSem = null;
	private PreparedStatement psUpdateCGPACupUnits;
	private PreparedStatement psSetStudentNameFromDatabase;
	private PreparedStatement psGetSemTerm;
	private PreparedStatement psCheckRepeatAndSetFlag;
	private PreparedStatement psUpdateCompulsoryCourses;
	private PreparedStatement psAddUnaccountedCourses;
	private PreparedStatement psGetELsType;
	private PreparedStatement psGetOpti;
	private PreparedStatement psAddPS1toSem;
	private PreparedStatement psSetCGPA;
	private PreparedStatement psSetRequirementNo;
	private PreparedStatement psSetSystemId;
	private PreparedStatement psSetPrevTerm;
	private PreparedStatement psGetAdmissionTerm;
	private PreparedStatement psGetPrintingTerm;
	private PreparedStatement psHasMinor;
	private PreparedStatement psSetMinorDesc;
	
	
	private DBConnector()	{
		this.db_host = WindowLoader.IPAddress; this.user_nm = WindowLoader.usernm; 
		this.passwd = WindowLoader.passwd;
		try {
			connection = DriverManager.getConnection(db_host+db_name, user_nm, passwd);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// For CourseChartQueries
		readyAddSummerTerm();
		readyGetCompulsoryCoursesForOneSem();
		readyCheckIsOptional();
		readyGetNoOfDELsType1();
		readyGetRequirementDescription();
		readyGetRequirementGroup();
		readyExistsSem();
		
		
		// For EligibilitySheetQueries
		readyUpdateCGPACupUnits();
		readyAddPS1toSem();
		readyAddUnaccountedCourses();
		readyCheckRepeatAndSetFlag();
		readyGetAdmissionTerm();
		readyGetELsType();
		readyGetPrintingTerm();
		readyGetSemTerm();
		readyHasMinor();
		readyOptionalCourses();
		readySetCGPA();
		readySetMinorDesc();
		readyUpdateCompulsoryCourses();
		readyUpdateCGPACupUnits();
		readySetSystemId();
		readySetStudentNameFromDatabase();
		readySetRequirementNo();
		readySetPrevTerm();
	}
	
	
	public static DBConnector getInstance() {
		return d;
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
	 * 
	 * DEPRECIATED
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

	
	// Methods to ready the public methods used in CourseChartQueuries
	private void readyAddSummerTerm(){
		try {
			this.psAddSummerTerm = this.connection.prepareStatement("select * from " + table_semCharts + "  t, "
					+ "courses c WHERE (t.course = c.course_id or t.course = '') and rqrmnt = ?"
					+ " and descr_3 like '%ummer%erm%'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readyCheckIsOptional(){
		try {
			this.psCheckIsOptional = this.connection.prepareStatement("SELECT descr_4 FROM charts "
					+ "WHERE rqrmnt = ? AND descr_4 LIKE '%Opti%'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readyGetCompulsoryCoursesForOneSem(){
		try {
			this.psGetCompulsoryCourses = this.connection.prepareStatement("SELECT * FROM "+DBConnector.table_semCharts
					+ " t, courses c WHERE (t.course = c.course_id or t.course = '') and rqrmnt = ?"
					+ " and descr_3 like ? "
					+"AND descr_4 not like '%lective%'"
					+"AND descr_4 not like '%Opti%'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readyGetNoOfDELsType1(){
		try {
			
			this.psGetNoOfDELsType1 = this.connection.prepareStatement("SELECT min_course FROM "
					+ ""+DBConnector.table_semCharts +
					" WHERE rqrmnt = ? and descr_3 like ? "
					+"AND descr_4 like ?");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readyExistsSem(){
		try {
			
			this.psExistsSem = this.connection.prepareStatement("SELECT COUNT(*) FROM "+
					DBConnector.table_semCharts + " WHERE rqrmnt = ? "
					+ "and descr_3 like ?");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	private void readyGetRequirementDescription(){
		try {
			
			this.psGetRequirementDescription = this.connection.prepareStatement("SELECT descr FROM "+DBConnector.table_semCharts +
					" WHERE rqrmnt = ? LIMIT 1");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void readyGetRequirementGroup(){
		try {
			
			this.psGetRequirementGroup = this.connection.prepareStatement("SELECT rq_group FROM "+DBConnector.table_semCharts +
					" WHERE rqrmnt = ? LIMIT 1");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

		
	
	
	
	// Methods to be used from CourseChartQueries
	public ResultSet addSummerTerm(String requirementNo) {
		
		ResultSet rs = null;
		try {
			
			this.psAddSummerTerm.setString(1, requirementNo);
			
			rs = this.psAddSummerTerm.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	
	public ResultSet checkOptional(String requirementNo) {
		
		ResultSet rs = null;
		try {
			this.psCheckIsOptional.setString(1, requirementNo);
			
			rs = this.psCheckIsOptional.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}

	
	
	public ResultSet getNoOfDELsType1(String req_num, int yearNo, int semNo, String stream1)	{
		ResultSet rs = null;
		
		try {
			this.psGetNoOfDELsType1.setString(1, req_num);
			this.psGetNoOfDELsType1.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			this.psGetNoOfDELsType1.setString(3, stream1 + "%isp%lective%");
			
			rs = this.psGetNoOfDELsType1.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet getNoOfDELs(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
		try {
			this.psGetNoOfDELsType1.setString(1, req_num);
			this.psGetNoOfDELsType1.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			this.psGetNoOfDELsType1.setString(3, "%isp%lective%");
			
			rs = this.psGetNoOfDELsType1.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet getNoOfOELs(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
		try {
			this.psGetNoOfDELsType1.setString(1, req_num);
			this.psGetNoOfDELsType1.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			this.psGetNoOfDELsType1.setString(3, "%pen%lective%");
			
			rs = this.psGetNoOfDELsType1.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	
	public ResultSet getNoOfHUELsType(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
	
		try {
			this.psGetNoOfDELsType1.setString(1, req_num);
			this.psGetNoOfDELsType1.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			this.psGetNoOfDELsType1.setString(3, "%um%lective%");
			
			rs = this.psGetNoOfDELsType1.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet getRequirementDescription(String req_num)	{
		ResultSet rs = null;
	
		try {
			this.psGetRequirementDescription.setString(1, req_num);
			
			rs = this.psGetRequirementDescription.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet getRequirementGroup(String req_num)	{
		ResultSet rs = null;
	
		try {
			this.psGetRequirementGroup.setString(1, req_num);
			
			rs = this.psGetRequirementGroup.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet existsSem(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
	
		try {
			this.psExistsSem.setString(1, req_num);
			this.psExistsSem.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			
			rs = this.psExistsSem.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	
	public ResultSet getCompulsoryCoursesForOneSem(String req_num, int yearNo, int semNo)	{
		ResultSet rs = null;
		//System.out.println(sqlQuery);
		try {
			this.psGetCompulsoryCourses.setString(1, req_num);
			this.psGetCompulsoryCourses.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			rs = this.psGetCompulsoryCourses.executeQuery();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	
	
	// Methods to ready public methods from EligibilitySheetQueries
	private void readyUpdateCGPACupUnits(){
		
		try {
			this.psUpdateCGPACupUnits = connection.prepareStatement("SELECT grade_points, "
					+ "total FROM student_terms WHERE sys_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void readySetStudentNameFromDatabase(){
		
		try {
			this.psSetStudentNameFromDatabase = connection.prepareStatement("Select student_name from students "
					+ "where campus_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readyGetSemTerm(){
		
		try {
			this.psGetSemTerm = connection.prepareStatement("SELECT sem_description FROM terms "
					+ "WHERE semester = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readyCheckRepeatAndSetFlag(){
		
		try {
			this.psCheckRepeatAndSetFlag = connection.prepareStatement("SELECT * FROM student_enrollment WHERE sys_id = ?"
					+ " AND course_id = ? AND units_taken > 0");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readyUpdateCompulsoryCourses(){
		
		try {
			this.psUpdateCompulsoryCourses = connection.prepareStatement("Select * from req_course_map "
					+ "where sys_id = ? and sem_course_decription like ? and course_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readyAddUnaccountedCourses(){
		
		try {
			this.psAddUnaccountedCourses = connection.prepareStatement("select distinct course_id, subject,"
					+ " catalog, course_descr, units_taken, grade"
					+ " from student_enrollment where sys_id = ? and units_taken > 0"
					+ " and course_id not in (select course_id from req_course_map where"
					+ " sys_id = ?)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readyGetELsType(){
		
		try {
			this.psGetELsType = connection.prepareStatement("Select * from req_course_map "
					+ "where descr_2 like ? and sys_id = ?"
					+ " and sem_course_decription like ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void readyOptionalCourses(){
		try {
			this.psGetOpti = connection.prepareStatement("Select * from req_course_map "
					+ "where descr_2 like '%POM%POE%Opti%' and sys_id = ? "
					+ "and sem_course_decription like ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void readyAddPS1toSem(){
		try {
			this.psAddPS1toSem = connection.prepareStatement("Select * from req_course_map "
					+ "where sys_id = ? and sem_course_decription like '%ummer%erm%'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readySetCGPA(){
		try {
			this.psSetCGPA = connection.prepareStatement("SELECT CGPA FROM student_terms"
				+ " where sys_id = ? "
				+ "and semester = (select max(semester) from student_terms)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void readySetRequirementNo(){
		try {
			this.psSetRequirementNo = connection.prepareStatement("SELECT rqrmnt FROM std_req_mapping"
				+ " WHERE sys_id = (SELECT sys_id FROM students WHERE campus_id = ?)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void readySetSystemId(){
		try {
			this.psSetSystemId = connection.prepareStatement("SELECT sys_id FROM students "
					+ "WHERE campus_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void readySetPrevTerm(){
		try {
			this.psSetPrevTerm = connection.prepareStatement("SELECT MAX(term_taken) from req_course_map "
					+ "where sys_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readyGetAdmissionTerm(){
		try {
			this.psGetAdmissionTerm = connection.prepareStatement("SELECT DISTINCT sem_description from terms where semester = ("
				+ "SELECT min(semester) from student_enrollment where sys_id = ? ) LIMIT 1");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void readyGetPrintingTerm(){
		try {
			this.psGetPrintingTerm = connection.prepareStatement("SELECT DISTINCT sem_description "
					+ "from terms where semester = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readyHasMinor(){
		try {
			this.psHasMinor = connection.prepareStatement("SELECT * from student_minor "
					+ "where sys_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void readySetMinorDesc(){
		try {
			this.psSetMinorDesc = connection.prepareStatement("SELECT * FROM minor_course_list "
					+ "where course_id = ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
	// Public methods to be called from EligibilitySheetQueries
	
	public ResultSet updateCgpaCupAndUnits( String systemId ){
		ResultSet rs = null;
		
		try {
			psUpdateCGPACupUnits.setString(1, systemId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	
	public ResultSet setStudentNameFromDatabase( String systemId ){
		ResultSet rs = null;
		
		try {
			psSetStudentNameFromDatabase.setString(1, systemId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	
	public ResultSet getSemTerm( int term ){
		ResultSet rs = null;
		
		try {
			psGetSemTerm.setInt(1, term);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public ResultSet checkAndSetRepeatFlag( String sysid, int courseId){
		ResultSet rs = null;
		
		try {
			psCheckRepeatAndSetFlag.setString(1, sysid);
			psCheckRepeatAndSetFlag.setInt(2, courseId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public ResultSet updateCompulsoryCourses( String sysid, int yearNo, int semNo, int courseId){
		ResultSet rs = null;
		
		try {
			psUpdateCompulsoryCourses.setString(1, sysid);
			if (yearNo != 1)
				psUpdateCompulsoryCourses.setString(2, "%ear " + yearNo + "%em " + semNo + "%");
			else 
				psUpdateCompulsoryCourses.setString(2, "%ear " + yearNo + "%");
			psUpdateCompulsoryCourses.setInt(3, courseId);
			rs = psUpdateCompulsoryCourses.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public ResultSet addUnaccountedCourses( String sysid ){
		ResultSet rs = null;
		
		try {
			psAddUnaccountedCourses.setString(1, sysid);
			psAddUnaccountedCourses.setString(2, sysid);
			rs = psAddUnaccountedCourses.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	public ResultSet addELTypes( String desc, String sysid, int yearNo, int semNo){
		ResultSet rs = null;
		
		try {
			psGetELsType.setString(1, desc);
			psGetELsType.setString(2, sysid);
			psGetELsType.setString(3, "Year " + yearNo + " Sem " + semNo + "%");
			rs = psGetELsType.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	

	public ResultSet OptionalCourses( String sysid, int yearNo, int semNo){
		ResultSet rs = null;
		
		try {
			psGetOpti.setString(1, sysid);
			psGetOpti.setString(2, "Year " + yearNo + " Sem " + semNo + "%");
			rs = psGetOpti.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	
	public ResultSet addPS1toSem( String sysid ){
		ResultSet rs = null;
		
		try {
			psAddPS1toSem.setString(1, sysid);
			rs = psAddPS1toSem.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rs;
	}
	
	

	public ResultSet setCGPA( String sysid ){
		ResultSet rs = null;
		
		try {
			psSetCGPA.setString(1, sysid);
			rs = psSetCGPA.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet setRequirementNo( String sysid ){
		ResultSet rs = null;
		
		try {
			psSetRequirementNo.setString(1, sysid);
			rs = psSetRequirementNo.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet setSystemId( String studentId ){
		ResultSet rs = null;
		
		try {
			psSetSystemId.setString(1, studentId);
			rs = psSetSystemId.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet setPrevTerm( String systemid ){
		ResultSet rs = null;
		
		try {
			psSetPrevTerm.setString(1, systemid);
			rs = psSetPrevTerm.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	
	public ResultSet getAdmissionTerm( String systemid ){
		ResultSet rs = null;
		
		try {
			psGetAdmissionTerm.setString(1, systemid);
			rs = psGetAdmissionTerm.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet getPrintingTerm( int term ){
		ResultSet rs = null;
		
		try {
			psGetPrintingTerm.setInt(1, term);
			rs = psGetPrintingTerm.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet hasMinor( String systemId ){
		ResultSet rs = null;
		
		try {
			psHasMinor.setString(1, systemId);
			rs = psHasMinor.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	public ResultSet setMinorDesc( int coursecode ){
		ResultSet rs = null;
		
		try {
			psSetMinorDesc.setInt(1, coursecode);
			rs = psSetMinorDesc.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	}
	
	
	
	

	
	
	
	
	
	
	
	
	
	// NOT NEEDED METHODS AND INNER CLASS
	
/*
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
	/*
		public void closeConnections()	{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
/*
		/**
		 * Provides a method for opening connections
		 */
	/*
	public void reopenConnections()	{
			try {
				connection = DriverManager.getConnection(db_host+db_name, user_nm, passwd);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
/*
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

		/*public ResultSet queryExecutor(String query, boolean isdatamanipulated)	{
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
		 *
		 */
		/*
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

 **/
	
	
}