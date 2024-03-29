package bits.arcd.model;

public class Course {

	private int courseCode ; //this is the compcode
	private String subject; //eg CS
	private String catalog ; //eg F111
	private String description;//Computer Programming
	private String elDescr; // "HUEL", "A8EL", "B3EL", "EL"
	private int numGrade;
	
	private boolean OPSC;


	public Course(){

	}

	public void setOPSC(boolean opsc){
		this.OPSC = opsc;
	}

	public String getElDescr() {
		return elDescr;
	}

	public void setElDescr(String elDescr) {
		this.elDescr = elDescr;
	}

	private int minUnits, maxUnits;
	private String grade; //A, NC
	private int term; //1121
	private int classNo; //3515
	private String includeInGPA, earnCredit;
	private int line; //20, 30
	private String isRepeat, inProgress;

	private boolean doneInPrevSem;

	private boolean projectTypeCourse;

	private boolean isOptional, isSummerTermPS1;

	public boolean isGradeValid() {
		return gradeValid;
	}

	public boolean isGradeComplete() {
		return gradeComplete;
	}

	private boolean gradeValid, gradeComplete;

	private boolean isNamedCourse, isHuel, isDel, isOel, isUnaccountedCourse;

	
	
	public boolean isUnaccountedCourse() {
		return isUnaccountedCourse;
	}

	public void setUnaccountedCourse(boolean isUnaccountedCourse) {
		this.isUnaccountedCourse = isUnaccountedCourse;
	}

	public Course(int courseCode, String subject, String catalog,
			String description, int minUnits, int maxUnits) {
		super();
		this.courseCode = courseCode;
		this.subject = subject.trim();
		this.catalog = catalog.trim();
		this.description = description.trim();
		this.minUnits = minUnits;
		this.maxUnits = maxUnits;
	}

	public Course(int courseCode, String subject, String catalog,
			String description, int minUnits, int maxUnits, String grade,
			int term, int classNo, String includeInGPA, String earnCredit,
			int line, String inProgress, int prevTerm) {
		super();
		this.courseCode = courseCode;
		this.subject = subject.trim();
		this.catalog = catalog.trim();
		this.description = description.trim();
		this.minUnits = minUnits;
		this.maxUnits = maxUnits;

		this.grade = grade.trim();
		this.term = term;
		this.classNo = classNo;
		this.includeInGPA = includeInGPA.trim();
		this.earnCredit = earnCredit.trim();
		this.line = line;

		this.inProgress = inProgress.trim();
		setIsDoneInPrevTerm(prevTerm);
		setIsProjectTypeCourse();

	}


	public boolean isNamedCourse() {
		return isNamedCourse;
	}

	public void setIsNamedCourse(boolean isNamedCourse) {
		this.isNamedCourse = isNamedCourse;
	}

	public boolean isHuel() {
		return isHuel;
	}

	public void setIsHuel(boolean isHuel) {
		this.isHuel = isHuel;
	}

	public boolean isDel() {
		return isDel;
	}

	public void setIsDel(boolean isDel) {
		this.isDel = isDel;
	}

	public boolean isOel() {
		return isOel;
	}

	public void setIsOel(boolean isOel) {
		this.isOel = isOel;
	}

	@Override
	public String toString() {
		// catalog has many spaces

		if (this.getCourseCode() == 0 && this.description == null) {
			return "    ................................................            " + this.getElDescr() + "      ";
		}
		else {	

			//5
			String courseCodeString = "";
			if(courseCode != 0){
				courseCodeString = courseCodeString + courseCode;
			} 
			while (courseCodeString.length() < 5 ) {
				courseCodeString = courseCodeString + " ";
			}

			//5
			String courseSubjectString = "";
			if (subject != null)
				courseSubjectString = courseSubjectString + subject;

			while (courseSubjectString.length() <= 5 ) {
				courseSubjectString = courseSubjectString + " ";
			}

			//5
			String courseCatalogString = "";
			if (catalog != null) {
				courseCatalogString = courseCatalogString + catalog.trim() + "";
			}

			while (courseCatalogString.length() < 5 ) {
				courseCatalogString = courseCatalogString + " ";
			}

			//25
			String courseDescrString = "";
			if (description != null)
				courseDescrString = courseDescrString + description.trim() + "";
			while (courseDescrString.length() <= 25 ) {
				courseDescrString = courseDescrString + " ";
			}

			//3
			String UnitsString = "";
			if(UnitsString != null){
				UnitsString = UnitsString + maxUnits + "";
			}
			while (UnitsString.length() < 3 ) {
				UnitsString = UnitsString + " ";
			}

			//5
			String gradeString = "";

			if (grade != null) {
				gradeString = grade.trim();
				while(gradeString.length() < 5) {
					gradeString = gradeString + " ";
				}
			}

			else {
				while(gradeString.length() < 5) {
					gradeString = gradeString + " ";
				}
			}

			//5
			String ExtString = "";
			if (this.doneInPrevSem) {
				ExtString = ExtString + "%";
			}
			while (ExtString.length() < 5){
				ExtString = ExtString + " ";
			}

			//5
			String infoString = "";


			if (this.inProgress != null && this.inProgress.equals("Y"))
			{
				infoString = infoString + "||";
			}

			if (this.OPSC){
				infoString = infoString + "$";
			}

			if (grade != null && this.gradeValid && !this.gradeComplete )
				infoString = infoString + "*";			

			if ((this.isInProgress() != null) && ! this.isInProgress().equals("Y") && (this.projectTypeCourse || this.courseCode == 1591 ))
				infoString = infoString + "#";

			while (infoString.length() < 3){
				infoString = infoString + " ";
			}


			//5
			String TypeString = "";
			if (this.isDel) {
				TypeString = TypeString + this.getElDescr();
			}

			if (this.isOel) {
				TypeString = TypeString + this.getElDescr();

			}

			if (this.isHuel) {
				TypeString = TypeString + this.getElDescr();
			}

			if (this.isUnaccountedCourse) {
				TypeString = TypeString + this.getElDescr();
			}
			
			
			while (TypeString.length() < 10 ){
				TypeString = TypeString + " ";
			}



			String retval = infoString + " " + courseCodeString + " " +  courseSubjectString + " "
					+ courseCatalogString + " " + courseDescrString + " "
					+ UnitsString + " " + gradeString + ExtString + TypeString  ;


			return retval;
		}
	}


	public void checkAndSetGradeValidAndGradeComplete() {

		String[] completeGrades = {"A", "A-", "B", "B-", "C", "C-", "D", "E"};
		String[] incompleteGrades = {"I", "GA", "W", "RC", "RRA", "DP", "NC","TGA", "AC"};

		String countedGrade = this.grade;

		// set correct grade for repeat
		if(this.isRepeat() != null && this.isRepeat().equalsIgnoreCase("Y")) {
			countedGrade = this.grade.substring(this.grade.lastIndexOf("/") + 1);
		}

		for(int i=0; i < completeGrades.length; i++) {
			if(countedGrade != null && countedGrade.equalsIgnoreCase(completeGrades[i])) {
				this.gradeValid = true;
				this.gradeComplete = true;
				return;
			}
		}

		for(int i=0; i<incompleteGrades.length; i++) {
			if(countedGrade != null && countedGrade.equalsIgnoreCase(incompleteGrades[i])) {
				this.gradeValid = true;
				this.gradeComplete = false;
				return;
			}
		}

		this.gradeValid = false;
		this.gradeComplete = false;
	}

	//getters and setters

	public int getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(int courseCode) {
		this.courseCode = courseCode;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getMinUnits() {
		return minUnits;
	}

	public void setMinUnits(int minUnits) {
		this.minUnits = minUnits;
	}

	public int getMaxUnits() {
		return maxUnits;
	}

	public void setMaxUnits(int maxUnits) {
		this.maxUnits = maxUnits;
	}

	public String getGrade() {
		return grade;
	}

	public void setGrade(String grade) {
		this.grade = grade;
	}

	public int getTerm() {
		return term;
	}

	public void setTerm(int term) {
		this.term = term;
	}

	public int getClassNo() {
		return classNo;
	}

	public void setClassNo(int classNo) {
		this.classNo = classNo;
	}

	public String isIncludeInGPA() {
		return includeInGPA;
	}

	public void setIncludeInGPA(String includeInGPA) {
		this.includeInGPA = includeInGPA;
	}

	public String isEarnCredit() {
		return earnCredit;
	}

	public void setEarnCredit(String earnCredit) {
		this.earnCredit = earnCredit;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String isRepeat() {
		return isRepeat;
	}

	public void setRepeat(String isRepeat) {
		this.isRepeat = isRepeat;
	}

	public String isInProgress() {
		return inProgress;
	}

	public void setInProgress(String setInProgress) {
		this.inProgress = setInProgress;
	}

	public void setIsDoneInPrevTerm (int lastTerm){
		if (this.term== lastTerm){
			this.doneInPrevSem = true;
		}
		else
			this.doneInPrevSem =false;
	}

	public void setIsProjectTypeCourse (){
		//376/266/366/367/377/BITS F381/382/383/ xxx 491

		if( this.catalog.equalsIgnoreCase("F376") ||this.catalog.equalsIgnoreCase("F266") ||
				this.catalog.equalsIgnoreCase("F366") ||this.catalog.equalsIgnoreCase("F367") ||
				this.catalog.equalsIgnoreCase("F377") ||this.catalog.equalsIgnoreCase("F491") )
		{
			this.projectTypeCourse = true ;

		}
		else if (this.subject.equalsIgnoreCase("BITS") && ( this.catalog.equalsIgnoreCase("F381") ||this.catalog.equalsIgnoreCase("F382") ||
				this.catalog.equalsIgnoreCase("F383"))){

			this.projectTypeCourse = true ;

		}
		else this.projectTypeCourse = false;
	}

	public boolean getIsDoneInPrevSem() {
		return doneInPrevSem;
	}

	public boolean getIsProjectTypeCourse() {
		return projectTypeCourse;
	}

	public boolean isOptional(){
		return isOptional;
	}

	public void setIsOptional(boolean isOptional){
		this.isOptional = isOptional;
	}

	public boolean isPS2() {

		if (this.subject.equalsIgnoreCase("BITS")) {
			if (this.catalog.equalsIgnoreCase("F412")){
				return true;
			}
		}

		else {
			return false;
		}

		return false;

	}

	public boolean is16unitThesis() {

		if (this.subject.equalsIgnoreCase("BITS")) {
			if (this.catalog.equalsIgnoreCase("F421T")
					|| this.catalog.equalsIgnoreCase("F422T")
					|| this.catalog.equalsIgnoreCase("F422")
					|| this.catalog.equalsIgnoreCase("F421") 
					){
				return true;
			}
		}

		else {
			return false;
		}

		return false;

	}
	
	public boolean is9unitThesis() {

		if (this.subject.equalsIgnoreCase("BITS")) {
			if (this.catalog.equalsIgnoreCase("F423")|| this.catalog.equalsIgnoreCase("F423T")){
				return true;
			}
		}

		else {
			return false;
		}

		return false;

	}
	
	public int getNumGrade(){
		String countedGrade = new String();
		boolean isValidGrade = false;
		//if(this.isRepeat !=null && this.isRepeat.equalsIgnoreCase("Y")){
			countedGrade = grade;
			String[] grds = countedGrade.split("/");
			int size = grds.length;
			while(!isValidGrade && size>0){
				size--;
				if(grds[size]!=null &(grds[size].equalsIgnoreCase("A") || grds[size].equalsIgnoreCase("A-") || grds[size].equalsIgnoreCase("B")
						|| grds[size].equalsIgnoreCase("B-") || grds[size].equalsIgnoreCase("C") || grds[size].equalsIgnoreCase("C-") 
						|| grds[size].equalsIgnoreCase("D") || grds[size].equalsIgnoreCase("E") || grds[size].equalsIgnoreCase("NC"))){
					isValidGrade = true;
					countedGrade = grds[size];
				}
			}
//		}
//		else
//			countedGrade = grade;
		
		switch(countedGrade){
		case "A":
			return 10;
		case "A-":
			return 9;
		case "B":
			return 8;
		case "B-":
			return 7;
		case "C":
			return 6;
		case "C-":
			return 5;
		case "D":
			return 4;
		case "E":
			return 2;
		default:
			return 0;
		}

	}

	
	public static int getAccGrade(String switchGrade){
		switch(switchGrade){
		case "A":
			return 10;
		case "A-":
			return 9;
		case "B":
			return 8;
		case "B-":
			return 7;
		case "C":
			return 6;
		case "C-":
			return 5;
		case "D":
			return 4;
		case "E":
			return 2;
		case "NC":
			return 0;
		default:
			return -1;
		}	

	}

	public void setIsSummerTerm(boolean isPS1){
		this.isSummerTermPS1 = isPS1;
	}
	
	
	public boolean isSummerTermPS1() {
		
		return this.isSummerTermPS1;
	}
	
	public boolean isOelPS1() {
		if (!this.isSummerTermPS1 && this.catalog != null 
				&& this.catalog.equalsIgnoreCase("F221") 
				&& this.subject.equalsIgnoreCase("BITS"))
		return true;
	
		return false;
	}

}