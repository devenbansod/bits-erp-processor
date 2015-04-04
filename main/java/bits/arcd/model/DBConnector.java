package bits.arcd.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import bits.arcd.main.WindowLoader;
import bits.arcd.view.SemChartController;

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
	private PreparedStatement psGetNoOfOELsHigherDegree;
	private PreparedStatement psGetHigherDegreeELs;
	

	private DBConnector()	{
		this.db_host = SemChartController.getSettings("hostIp"); this.user_nm = SemChartController.getSettings("mysqlUser"); 
		this.passwd = SemChartController.getSettings("mysqlPassword");
		this.db_name = SemChartController.getSettings("databaseName");
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Properties p = new Properties();
			connection = DriverManager.getConnection("jdbc:mysql://" + db_host + "/" + db_name + "?cachePrepStmts=true", user_nm, passwd);

		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while connecting to the "
					+ "database. Please check whether your database server is running or not!!", e);
			e.printStackTrace();
		} catch (InstantiationException e) {
			WindowLoader.showExceptionDialog("The java program could not find the appropriate JAR for the class"
					+ " 'com.mysql.jdbc.Driver'", e);
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			WindowLoader.showExceptionDialog("Illegal Access", e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			WindowLoader.showExceptionDialog("Could not find the class", e);
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
		readyGetNoOfOELsHigherDegree();


		// For EligibilitySheetQueries
		readyUpdateCGPACupUnits();
		readyAddPS1toSem();
		readyAddUnaccountedCourses();
		readyCheckRepeatAndSetFlag();
		readyGetAdmissionTerm();
		readyGetELsType();
		readyGetNoOfOELsHigherDegree();
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
		readyGetHigherDegreeELs();
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
			WindowLoader.showExceptionDialog("Error while closing the connection", e);
			e.printStackTrace();
		}
	}

	/**
	 * Provides a method for opening connections
	 */
	public void reopenConnections()	{
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + db_host + "/" + db_name, user_nm, passwd);
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while getting the MySQL Connection", e);
			e.printStackTrace();
		}
	}

	public void clearDatabase()	{
		String clearTable = "TRUNCATE TABLE " + table_semCharts;
		queryExecutor(clearTable, true);
	}

	/**
	 * 
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
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : " + query + "\n", e);
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
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyAddSummerTerm \n", e);
			e.printStackTrace();
		}
	}

	private void readyCheckIsOptional(){
		try {
			this.psCheckIsOptional = this.connection.prepareStatement("SELECT descr_4 FROM charts "
					+ "WHERE rqrmnt = ? AND descr_4 LIKE '%Opti%'");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyCheckIsOptional \n", e);
			e.printStackTrace();
		}
	}

	private void readyGetCompulsoryCoursesForOneSem(){
		try {
			this.psGetCompulsoryCourses = this.connection.prepareStatement("SELECT * FROM "+DBConnector.table_semCharts
					+ " t, courses c WHERE (t.course = c.course_id or t.course = '') and rqrmnt = ?"
					+ " and descr_3 like ? "
					+"AND descr_3 not like '%lective%'"
					+"AND descr_4 not like '%Opti%'");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetCompulsoryCourses \n", e);
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
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetNoOfDELsType1 \n", e);
			e.printStackTrace();
		}
	}
		
	
	private void readyGetNoOfOELsHigherDegree(){
		try {

			this.psGetNoOfOELsHigherDegree = this.connection.prepareStatement("SELECT min_course FROM "
					+ ""+DBConnector.table_semCharts +
					" WHERE rqrmnt = ? and descr_3 like ? "
					+"AND descr_3 like ?");

		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetNoOfOELSHigherDegree \n", e);
			e.printStackTrace();
		}
	}

	private void readyExistsSem(){
		try {

			this.psExistsSem = this.connection.prepareStatement("SELECT COUNT(*) FROM "+
					DBConnector.table_semCharts + " WHERE rqrmnt = ? "
					+ "and descr_3 like ?");

		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyExistsSem \n", e);
			e.printStackTrace();
		}
	}



	private void readyGetRequirementDescription(){
		try {

			this.psGetRequirementDescription = this.connection.prepareStatement("SELECT descr FROM "+DBConnector.table_semCharts +
					" WHERE rqrmnt = ? LIMIT 1");

		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetRequirementDescription \n", e);
			e.printStackTrace();
		}
	}


	private void readyGetRequirementGroup(){
		try {

			this.psGetRequirementGroup = this.connection.prepareStatement("SELECT rq_group FROM "+DBConnector.table_semCharts +
					" WHERE rqrmnt = ? LIMIT 1");

		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetRequirementGroup \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : addSummerTerm \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : checkOptional \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getNoOfDELsType1 \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getNoOfDELs \n", e);
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

			this.psGetNoOfOELsHigherDegree.setString(1, req_num);
			this.psGetNoOfOELsHigherDegree.setString(2, "Year "+ yearNo +" Sem " + semNo + "%");
			this.psGetNoOfOELsHigherDegree.setString(3, "%lective%");

			
			if (Integer.parseInt(req_num) > 1557)
				rs = this.psGetNoOfDELsType1.executeQuery();
			else
				rs = this.psGetNoOfOELsHigherDegree.executeQuery();
				
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while executing the Query : getNoOfOELs \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getNoOfHUELsType \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getRequirementDescription \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getRequirementGroup \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : ExistsSem \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getCompulsoryCoursesForOneSem \n", e);
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
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyUpdateCGPACupUnits \n", e);
			e.printStackTrace();
		}

	}


	private void readySetStudentNameFromDatabase(){

		try {
			this.psSetStudentNameFromDatabase = connection.prepareStatement("Select student_name from students "
					+ "where campus_id = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readySetStudentNameFromDatabase \n", e);
			e.printStackTrace();
		}

	}

	private void readyGetSemTerm(){

		try {
			this.psGetSemTerm = connection.prepareStatement("SELECT sem_description FROM terms "
					+ "WHERE semester = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetSemTerm \n", e);
			e.printStackTrace();
		}

	}

	private void readyCheckRepeatAndSetFlag(){

		try {
			this.psCheckRepeatAndSetFlag = connection.prepareStatement("SELECT * FROM student_enrollment WHERE sys_id = ?"
					+ " AND course_id = ? AND units_taken > 0");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyCheckRepeatAndSetFlag \n", e);
			e.printStackTrace();
		}

	}

	private void readyUpdateCompulsoryCourses(){

		try {
			this.psUpdateCompulsoryCourses = connection.prepareStatement("Select * from req_course_map "
					+ "where sys_id = ? and sem_course_decription like ? and course_id = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyUpdateCompulsoryCourses \n", e);
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
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyAddUnaccountedCourses \n", e);
			e.printStackTrace();
		}

	}

	private void readyGetELsType(){

		try {
			this.psGetELsType = connection.prepareStatement("Select * from req_course_map "
					+ "where descr_2 like ? and sys_id = ?"
					+ " and sem_course_decription like ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetELsType \n", e);
			e.printStackTrace();
		}

	}
	
	private void readyGetHigherDegreeELs(){

		try {
			this.psGetHigherDegreeELs = connection.prepareStatement("Select * from req_course_map "
					+ "where descr_2 like '%lective%' and sys_id = ?"
					+ " and sem_course_decription like ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetELsType \n", e);
			e.printStackTrace();
		}

	}

	private void readyOptionalCourses(){
		try {
			this.psGetOpti = connection.prepareStatement("Select * from req_course_map "
					+ "where descr_2 like '%POM%POE%Opti%' and sys_id = ? "
					+ "and sem_course_decription like ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyOptionalCourses \n", e);
			e.printStackTrace();
		}
	}


	private void readyAddPS1toSem(){
		try {
			this.psAddPS1toSem = connection.prepareStatement("Select * from req_course_map "
					+ "where sys_id = ? and sem_course_decription like '%ummer%erm%'");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyAddPS1toSem \n", e);
			e.printStackTrace();
		}
	}

	private void readySetCGPA(){
		try {
			this.psSetCGPA = connection.prepareStatement("SELECT CGPA FROM student_terms"
					+ " where sys_id = ? "
					+ "and semester = (select max(semester) from student_terms)");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readySetCGPA \n", e);
			e.printStackTrace();
		}
	}


	private void readySetRequirementNo(){
		try {
			this.psSetRequirementNo = connection.prepareStatement("SELECT rqrmnt FROM std_req_mapping"
					+ " WHERE sys_id = (SELECT sys_id FROM students WHERE campus_id = ?)");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readySetRequirementNo \n", e);
			e.printStackTrace();
		}
	}


	private void readySetSystemId(){
		try {
			this.psSetSystemId = connection.prepareStatement("SELECT sys_id FROM students "
					+ "WHERE campus_id = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readySetSystemId \n", e);
			e.printStackTrace();
		}
	}


	private void readySetPrevTerm(){
		try {
			this.psSetPrevTerm = connection.prepareStatement("SELECT MAX(term_taken) from req_course_map "
					+ "where sys_id = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readySetPrevTerm \n", e);
			e.printStackTrace();
		}
	}

	private void readyGetAdmissionTerm(){
		try {
			this.psGetAdmissionTerm = connection.prepareStatement("SELECT DISTINCT sem_description from terms where semester = ("
					+ "SELECT min(semester) from student_enrollment where sys_id = ? ) LIMIT 1");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetAdmissionTerm \n", e);
			e.printStackTrace();
		}
	}


	private void readyGetPrintingTerm(){
		try {
			this.psGetPrintingTerm = connection.prepareStatement("SELECT DISTINCT sem_description "
					+ "from terms where semester = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyGetPrintingTerm \n", e);
			e.printStackTrace();
		}
	}

	private void readyHasMinor(){
		try {
			this.psHasMinor = connection.prepareStatement("SELECT * from student_minor "
					+ "where sys_id = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readyHasMinor \n", e);
			e.printStackTrace();
		}
	}

	private void readySetMinorDesc(){
		try {
			this.psSetMinorDesc = connection.prepareStatement("SELECT * FROM minor_course_list "
					+ "where course_id = ?");
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while pre-compiling the Query : readySetMinorDesc \n", e);
			e.printStackTrace();
		}
	}




	// Public methods to be called from EligibilitySheetQueries

	public ResultSet updateCgpaCupAndUnits( String systemId ){
		ResultSet rs = null;

		try {
			psUpdateCGPACupUnits.setString(1, systemId);
			rs = psUpdateCGPACupUnits.executeQuery();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while executing the Query : readyUpdateCompulsoryCourses \n", e);
			e.printStackTrace();
		}

		return rs;
	}


	public ResultSet setStudentNameFromDatabase( String systemId ){
		ResultSet rs = null;

		try {
			psSetStudentNameFromDatabase.setString(1, systemId);
			rs = psSetStudentNameFromDatabase.executeQuery();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while executing the Query : setStudentNameFromDatabase \n", e);
			e.printStackTrace();
		}

		return rs;
	}


	public ResultSet getSemTerm( int term ){
		ResultSet rs = null;

		try {
			psGetSemTerm.setInt(1, term);
			rs = psGetSemTerm.executeQuery();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while executing the Query : getSemTerm \n", e);
			e.printStackTrace();
		}

		return rs;
	}

	public ResultSet checkAndSetRepeatFlag( String sysid, int courseId){
		ResultSet rs = null;

		try {
			psCheckRepeatAndSetFlag.setString(1, sysid);
			psCheckRepeatAndSetFlag.setInt(2, courseId);
			rs = psCheckRepeatAndSetFlag.executeQuery();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while executing the Query : checkAndSetRepeatFlag \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : updateCompulsoryCourses \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : addUnaccountedCourses \n", e);
			e.printStackTrace();
		}

		return rs;
	}

	public ResultSet addELTypes( String desc, String sysid, String req_num, int yearNo, int semNo){
		ResultSet rs = null;

		try {
			psGetELsType.setString(1, desc);
			psGetELsType.setString(2, sysid);
			psGetELsType.setString(3, "Year " + yearNo + " Sem " + semNo + "%");
			
			psGetHigherDegreeELs.setString(1, sysid);
			psGetHigherDegreeELs.setString(2, "Year " + yearNo + " Sem " + semNo + "%");
			
			if (Integer.parseInt(req_num) < 1558)
				rs = psGetHigherDegreeELs.executeQuery();
			else
				rs = psGetELsType.executeQuery();
		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("Error while executing the Query : addELTypes \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : OptionalCourses \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : addPS1toSem \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : setCGPA \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : setRequirementNo \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : setSystemId \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : setPrevTerm \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getAdmissionTerm \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : getPrintingTerm \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : hasMinor \n", e);
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
			WindowLoader.showExceptionDialog("Error while executing the Query : setMinorDesc \n", e);
			e.printStackTrace();
		}
		return rs;
	}


}