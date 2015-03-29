package bits.arcd.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.Scanner;
import java.util.prefs.Preferences;

import org.apache.commons.lang.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import bits.arcd.main.WindowLoader;
import bits.arcd.model.Course;
import bits.arcd.model.CourseChartQueries;
import bits.arcd.model.DBConnector;
import bits.arcd.model.EligibilitySheetQueries;

public class ELSheetController {
	// Reference to the main application.

	private WindowLoader mainApp; 
	//private ExtraTablesOperator elOper;

	@FXML public TextField idNum;
	@FXML public Button getElSheetButton;			
	@FXML public WebView browser;

	// Year and Sem -- Must be verified for every button event
	@FXML public TextField inpSemNum;

	// CSV Updates -- Please don't disturb the order
	@FXML public TextField refreshFolder;
	@FXML public Button refreshBrowseButton;
	@FXML public Button reloadButton;

	// Used for displaying messages
	@FXML public TextArea consoleOutput;

	// Batch Generation
	@FXML public TextField sourceIdNosCSV;	
	@FXML public TextField destFolder;
	@FXML public Button browseSourceFileButton;
	@FXML public Button browseDestFolderButton;
	@FXML public Button generateButton;

	private WebEngine webEngine;

	EligibilitySheetQueries ElSheetQueries;

	final String userhome = System.getProperty("user.home");

	public ELSheetController()	{

	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(WindowLoader mainApp) {
		this.mainApp = mainApp;
	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded. Called after constructor
	 */
	@SuppressWarnings({ "restriction", "restriction", "restriction" })
	@FXML
	private void initialize() {
		// Load the welcome page in WebView
		webEngine = browser.getEngine();
		FileSystem tempfs = FileSystems.getDefault();
		Path path = tempfs.getPath("src/main/resources/html_res/Welcome.html");
		webEngine.load("file:///"+path.toAbsolutePath().toString());

		destFolder.setText(getSettings("destFolderELSheet"));
		sourceIdNosCSV.setText(getSettings("sourceIdNosCSV"));


		// Button for ELSheet Generation
		getElSheetButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				if (idNum.getText() != null && ! idNum.getText().equals("")) {

					if (idNum.getText().length() == 12){

						threadSafeConsoleOutput("Processing, Please wait.........");
						Platform.runLater(new Runnable() {
							public void run() {
								FileSystem tempfs = FileSystems.getDefault();
								Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
								webEngine.load("file:///"+path.toAbsolutePath().toString());
							}
						});

						Thread temp = new Thread(){
							public void run(){
								loadSingleELSheet(idNum.getText());
							}
						};
						temp.start();

					}

					else if (idNum.getText().length() != 12 && idNum.getText().contains("%")) {
						
						threadSafeConsoleOutput("Processing, Please wait.........");
						Platform.runLater(new Runnable() {
							public void run() {
								FileSystem tempfs = FileSystems.getDefault();
								Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
								webEngine.load("file:///"+path.toAbsolutePath().toString());
							}
						});

						Thread temp = new Thread(){
							public void run(){
								loadELSheetsLike(idNum.getText());
							}
						};
						
						temp.start();

					}
				}

				else {
					threadSafeConsoleOutput("Enter a Proper IDNO!");
				}
			}
		});





		browseSourceFileButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				FileChooser chooser = new FileChooser();
				chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"),
						new ExtensionFilter("txt Files", "*.txt"));

				chooser.setTitle("Select the CSV File with Charts.csv");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				

				File selectedFile = chooser.showOpenDialog(new Stage());

				if (selectedFile != null)	{
					sourceIdNosCSV.setText(selectedFile.getAbsolutePath());
					setSettings("sourceIdNosCSV", selectedFile.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutput("Please choose a valid source folder!!");
				}

			}
		});

		// Button for Batch ELSheet Generation
		generateButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				Thread thread = new Thread(){
					public void run(){
						batchProcessELSheets(sourceIdNosCSV.getText());
						threadSafeConsoleOutput("Processing Done!");		
					}
				};
				thread.start();
			}
		});

		// for CSV Updates
		refreshBrowseButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser chooser = new DirectoryChooser();

				chooser.setTitle("Select the Folder with all the CSV Files");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				


				final File selectedFolder = chooser.showDialog(new Stage());

				if (selectedFolder != null)	{
					Platform.runLater(new Runnable() {
						public void run() {
							refreshFolder.setText(selectedFolder.getAbsolutePath());
						}
					});

				}
				else {
					threadSafeConsoleOutput("Please choose a valid source folder!!");
				}
			}
		});

		// Directory chooser for batch generation
		browseDestFolderButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser chooser = new DirectoryChooser();
				chooser.setTitle("Destination Folder for Output file");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);

				File selectedDirectory = chooser.showDialog(new Stage());
				if (selectedDirectory != null)	{
					destFolder.setText(selectedDirectory.getAbsolutePath());
					setSettings("destFolderELSheet", selectedDirectory.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutput("Please choose a valid destination folder!!");
				}

			}
		});


		reloadButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				threadSafeConsoleOutput("Processing, Please wait.........");
				Platform.runLater(new Runnable() {
					public void run() {
						FileSystem tempfs = FileSystems.getDefault();
						Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
						webEngine.load("file:///"+path.toAbsolutePath().toString());
					}
				});
				Thread thread = new Thread(){
					public void run(){


						threadSafeConsoleOutput(CourseChartQueries.batchCSVChartsLoad(refreshFolder.getText()));
						putWelcomeHTML();
					}
				};
				thread.start();


			}
		});

	}

	public void threadSafeConsoleOutput(final String output)	{
		Platform.runLater(new Runnable() {
			public void run() {
				consoleOutput.setText(consoleOutput.getText() + "" + output);
				consoleOutput.end();
			}
		});
	}


	private String convertToProperString(String s) {	
		s = s.replace(" ", "_");
		s = s.replace("-", "_");
		s = s.replace(":", "_");
		return s;
	}

	public void batchProcessELSheets(String filePath) {
		File f = new File(filePath);
		File f2 = new File(destFolder.getText());

		Date d = new Date();
		String formattedDate = convertToProperString(d.toString());
		File f_Out = new File(destFolder.getText() + "\\EL_SHEETS_" + formattedDate + ".txt");

		if( f.isFile() && f2.isDirectory()) {


			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Scanner sc = null;
			try {
				sc = new Scanner(f);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ArrayList<String> idNos = new ArrayList<String>();

			String idNo = "";
			while(sc.hasNext()){
				idNo = sc.nextLine().replace(",", "");
				System.out.println(idNo);
				idNos.add(idNo);
			}



			batchProcessELSheetsHelper(idNos, f_Out);
		}
		else {
			threadSafeConsoleOutput("Invalid directory specified!");
		}
		threadSafeConsoleOutput("Finished!..........");

		putWelcomeHTML();

	}

	public void putWelcomeHTML(){
		Platform.runLater(new Runnable() {
			public void run() {

				FileSystem tempfs = FileSystems.getDefault();
				Path path = tempfs.getPath("src/main/resources/html_res/Welcome.html");
				webEngine.load("file:///"+path.toAbsolutePath().toString());
			}
		});
	}


	private void batchProcessELSheetsHelper(ArrayList<String> idNos, File f){

		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(f.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//		System.out.println(idNos.size());
		for(int j = 0; j < idNos.size(); j++) {

			if (! inpSemNum.getText().equals("")){
				EligibilitySheetQueries e = new EligibilitySheetQueries(idNos.get(j), Integer.parseInt(inpSemNum.getText()));
				String s = e.toString();


				try {
					bw.write(s);
					bw.write("\f");
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}


				threadSafeConsoleOutput("\n" + (new Date()).toString() 
						+ " : Wrote " + idNos.get(j).toString() + "\n");
			}

			threadSafeConsoleOutput("\n\n" + (new Date()).toString() + " : Exported the Chart Data into " 
					+ f.getAbsolutePath() + "\n");

		}
		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		threadSafeConsoleOutput("\n" + (new Date()).toString() 
				+ " : File written inside : " + destFolder.getText() + "\n");
	}

	public void loadSingleELSheet(String idNum) {
		try {

			File file = new File(destFolder.getText() + "\\EL_SHEET_" + idNum + ".txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			if (! inpSemNum.getText().equals("")){

				EligibilitySheetQueries c = new EligibilitySheetQueries(idNum, Integer.parseInt(inpSemNum.getText()));
				String s = c.toString();

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(s);
				bw.close();

				threadSafeConsoleOutput("\n\n" + (new Date()).toString() + " : Finished Processing !\n");

				threadSafeConsoleOutput("\n\n" + (new Date()).toString() + " : Exported the EL Sheet Data into " 
						+ file.getAbsolutePath() + "\n");


				final String contentURL = new URL("file:///" +  file.getAbsolutePath()).toExternalForm();

				Platform.runLater(new Runnable() {
					public void run() {
						webEngine.load(contentURL);	
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/**
			if (processDocument(idNum, userhome))	{
				String inputfilepath = userhome + "\\" + idNum + ".docx";
				WordprocessingMLPackage wordMLPackage;
				wordMLPackage = Docx4J.load(new java.io.File(inputfilepath));
				HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
				htmlSettings.setImageDirPath(inputfilepath + "_files");
				htmlSettings.setImageTargetUri(inputfilepath.substring(inputfilepath.lastIndexOf("/")+1)
						+ "_files");
				htmlSettings.setWmlPackage(wordMLPackage);
				OutputStream os;
				os = new FileOutputStream(inputfilepath + ".html");
				Docx4jProperties.setProperty("docx4j.Convert.Out.HTML.OutputMethodXML", true);
				//Don't care what type of exporter you use
				//	Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_NONE);
				//Prefer the exporter, that uses a xsl transformation
				Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL);
				//Prefer the exporter, that doesn't use a xsl transformation (= uses a visitor)
				// Docx4J.toHTML(htmlSettings, os, Docx4J.FLAG_EXPORT_PREFER_NONXSL);
				os.close();
				String contentURL = new URL("file:///" + inputfilepath + ".html").toExternalForm();
				threadSafeConsoleOutput("Finished!");
				// Puts a queue to the JavaFX main application thread
				Platform.runLater(new Runnable() {
					public void run() {
						webEngine.load(contentURL);	
					}
				});
			}
	 */


	/**
	public boolean processDocument(String idNum, String destDir){

		threadSafeConsoleOutput("Processing, Please wait.........");
		Platform.runLater(new Runnable() {
			public void run() {
				FileSystem tempfs = FileSystems.getDefault();
				Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
				webEngine.load("file:///"+path.toAbsolutePath().toString());
			}
		});

		idNum = idNum.trim();

		ElSheetQueries = new EligibilitySheetQueries(idNum);

		boolean retVal = false;

		// Checking EL Sheet it was completely made
		if (ElSheetQueries.getSystemId() != null) {
			WordProcessor w1 ;
			CourseChartQueries chartQueries = ElSheetQueries.getCh();
			if (chartQueries.getYrNsem()[0] == 4) {
				System.out.println("4 Year Chart was selected!");
				w1= new WordProcessor("src/main/java/bits/arcd/wordProcess/4YearEL.docx");
			}
			else {
				System.out.println("5 Year Chart was selected!");
				w1= new WordProcessor("src/main/java/bits/arcd/wordProcess/5YearEL.docx");
			}


			// ----------------------------------------------------TOP PLACEHOLDERS PART START-------------
			String[] placeholdersTop = 
					new String[] {"INSTI_NM", "CHART_DESC", "DATE_CUR"};
			Map<String,String> replTop1 = new HashMap<String, String>();
			Map<String, String>	replTop2 = new HashMap<String, String>();

			// row 1 -- Please keep in mind there is row 2 to fill also, 
			//So keep some content for row 2 also
			replTop1.put("INSTI_NM", "BITS PILANI"); 
			// Write other things here such as ID Number and all
			replTop1.put("CHART_DESC", "EL SHEET FOR: 2011AXXXXX"); 
			// Write all the crap here regarding EL Sheet and all
			replTop1.put("DATE_CUR", new SimpleDateFormat("dd/MM/yyyy").format(new Date()).toString()); 
			// Can add extra things below date

			// row 2
			replTop2.put("INSTI_NM", "CHART CODE: "+chartQueries.getRequirementDescription().toUpperCase()); 
			// replace this with appropriate content
			replTop2.put("CHART_DESC", "REQ GRP: "+chartQueries.getRequirementGroup()
					+"   REG NO: "+chartQueries.getRequirementNo()); 
			// replace this with appropriate content
			replTop2.put("DATE_CUR", ""); 
			// replace this with appropriate content

			// Write it to the word document
			w1.replaceTable(placeholdersTop, Arrays.asList(replTop1,replTop2), w1.doc1);

			// ----------------------------------------------------TOP PLACEHOLDERS PART END-------------

			// Loop through all the sems (and years)
			for (int i=0,  year = 0; i<chartQueries.getSemsInChart().size(); i++)	{




				// There is no else condition for this loop. Hence this loop only inputs
				// left hand-side semesters. Wherever, mergeTwoSems is called, it is called for both
				// i and i+1 to account for both the left and the right semesters
				if (i%2==0)	{
					year++;	// Increase year value on every even semester completion
					System.out.println("Year: "+ year + "Sem: 1,2");
					// Appropriate placeholder for the left part -- don't include Year && B2 in this
					// to maintain symmetric nature with right hand part
					// Year && B2 would be added later in the mergeTwoSems method (in the leftPlaceholder)
					String[] placeholderLeft = {"PP"+year+"1", "CD"+year+"1","CRS"+year+"1",
							"N"+year+"1", "CT"+year+"1", "U"+year+"1", "G"+year+"1", "EX"+year+"1"};

					// Appropriate placeholder for the right part
					String[] placeholderRight = {"PP"+year+"2", "CD"+year+"2","CRS"+year+"2","N"+year+"2", 
							"CT"+year+"2", "U"+year+"2", "G"+year+"2", "EX"+year+"2"};

					// Overall placeholder -- Year column + placeholder left + B2 + placeholder right
					String[] overallPlaceholder = {"YR" + year, "PP"+year+"1", "CD"+year+"1","CRS"+year+"1",
							"N"+year+"1", "CT"+year+"1", "U"+year+"1", "G"+year+"1", "EX"+year+"1", "B2", 
							"PP"+year+"2", "CD"+year+"2","CRS"+year+"2","N"+year+"2", "CT"+year+"2", "U"+year+"2", 
							"G"+year+"2", "EX"+year+"2"};

					// It means both the sems exist
					// ?? How --> Because as told earlier, we enter the loop only for 
					// i%2==0 i.e, left hand-side semesters only. (i+1) guarantees the existence of the 
					// right hand-side semester also
					if ((i+1)<chartQueries.getSemsInChart().size())	{
						w1.replaceTable(overallPlaceholder, 
								equalizeTwoSemsIntoYear(""+year, semesterBuilder(""+i, placeholderLeft), 
										semesterBuilder(""+(i+1), placeholderRight), placeholderLeft, placeholderRight,
										overallPlaceholder), 
										w1.doc1);
					}

					// Last leg of the loop, the right hand-side semester does not exist
					else {
						// Pass empty ArrayList for the right side
						w1.replaceTable(overallPlaceholder, 
								equalizeTwoSemsIntoYear(""+year, semesterBuilder(""+i, placeholderLeft), 
										new ArrayList<Map<String, String>>(), placeholderLeft, placeholderRight,
										overallPlaceholder), 
										w1.doc1);
					}
				}
			}

			// Handle summer term separately --------------------------------------------

			String[] placeholderLeft = {"PP"+"S"+"1", "CD"+"S"+"1","CRS"+"S"+"1",
					"N"+"S"+"1", "CT"+"S"+"1", "U"+"S"+"1", "G"+"S"+"1", "EX"+"S"+"1"};

			String[] placeholderRight = {"PP"+"S"+"2", "CD"+"S"+"2","CRS"+"S"+"2","N"+"S"+"2", 
					"CT"+"S"+"2", "U"+"S"+"2", "G"+"S"+"2", "EX"+"S"+"2"};

			String[] overallPlaceholder = {"YR" + "S", "PP"+"S"+"1", "CD"+"S"+"1","CRS"+"S"+"1",
					"N"+"S"+"1", "CT"+"S"+"1", "U"+"S"+"1", "G"+"S"+"1", "EX"+"S"+"1", "B2", 
					"PP"+"S"+"2", "CD"+"S"+"2","CRS"+"S"+"2","N"+"S"+"2", "CT"+"S"+"2", "U"+"S"+"2", 
					"G"+"S"+"2", "EX"+"S"+"2"};


			w1.replaceTable(overallPlaceholder, 
					equalizeTwoSemsIntoYear("S", semesterBuilder("S", placeholderLeft), 
							new ArrayList<Map<String, String>>(), placeholderLeft, placeholderRight, overallPlaceholder), 
							w1.doc1);

			// Summer term finished! ----------------------------------------------------

			w1.writeDocxToStream(w1.doc1, destDir+"\\"+idNum+".docx");
			// By default set for false
			retVal = true;
		}

		// This reqNum is either not valid or does not exist in database
		else {
			threadSafeConsoleOutput("The input ID: "+idNum+" is either not valid or does "
					+ "not exist in the database!");
			// ----------------------Remove the "processing..."  Animation --------------------
			Platform.runLater(new Runnable() {
				public void run() {
					FileSystem tempfs = FileSystems.getDefault();
					Path path = tempfs.getPath("src/main/resources/html_res/Welcome.html");
					webEngine.load("file:///"+path.toAbsolutePath().toString());
				}
			});
			// ----------------------Remove the "processing..."  Animation --------------------
		}
		// By default set for false
		return retVal;
	}

	public List<Map<String, String>> semesterBuilder(String sem_index, String[] placeholder)	{
		List<Map<String, String>> allRowsPerSem = new ArrayList<Map<String, String>>();	

		CourseChartQueries chartQueries = ElSheetQueries.getCh();

		// Check whether summer term or not !! Summer terms are handled separately
		if (!sem_index.equals("S"))	{
			int semIndexNum = Integer.parseInt(sem_index);
			ArrayList<Course> sem_temp = chartQueries.getSemsInChart().get(semIndexNum).getCompulsoryCourses();

			for (int j=0; j<sem_temp.size(); j++)	{
				Course temp = sem_temp.get(j);
				Map<String,String> columnValues = new HashMap<String, String>();
				// placeholder[0] denotes the PIPE column
				if (temp.isInProgress() == "Y"){
					columnValues.put(placeholder[0], "||");	
				} 	else {	columnValues.put(placeholder[0], "");}

				if (temp.getIsDoneInPrevSem()){
					columnValues.put(placeholder[0], "%");	
				} else { columnValues.put(placeholder[0], "");}

				// placeholder[1], [2], [3], [4], [5] denotes the code, course subject, no(catalog), title, units
				columnValues.put(placeholder[1], ""+ temp.getCourseCode());
				columnValues.put(placeholder[2], ""+ temp.getSubject());
				columnValues.put(placeholder[3], ""+temp.getCatalog());
				columnValues.put(placeholder[4], ""+temp.getDescription());
				columnValues.put(placeholder[5], ""+temp.getMaxUnits());
				if (temp.getGrade() != null) {
					columnValues.put(placeholder[6], ""+temp.getGrade());
				} else { columnValues.put(placeholder[6], ""); }

				// placeholder[7] denotes the HEL, OEL, DEL descriptions
				columnValues.put(placeholder[7], "");

				//System.out.println(temp.toString());


				allRowsPerSem.add(columnValues);
			}

			// Humanities
			int noOFHUEL = chartQueries.getSemsInChart().get(semIndexNum).getNoOfHUEL();
			for (int j=0; j<noOFHUEL; j++)	{
				Map<String,String> columnValues = new HashMap<String, String>();
				Course temp = sem_temp.get(j);
				// placeholder[0] denotes the PIPE column
				if (temp.isInProgress() == "Y"){
					columnValues.put(placeholder[0], "||");	
				} 	else {	columnValues.put(placeholder[0], "");}

				if (temp.getIsDoneInPrevSem()){
					columnValues.put(placeholder[0], "%");	
				} else { columnValues.put(placeholder[0], "");}

				// placeholder[1], [2], [3], [4], [5] denotes the code, course subject, no(catalog), title, units
				columnValues.put(placeholder[1], ""+ temp.getCourseCode());
				columnValues.put(placeholder[2], ""+ temp.getSubject());
				columnValues.put(placeholder[3], ""+temp.getCatalog());
				columnValues.put(placeholder[4], ""+temp.getDescription());
				columnValues.put(placeholder[5], ""+temp.getMaxUnits());
				if (temp.getGrade() != null) {
					columnValues.put(placeholder[6], ""+temp.getGrade());
				}
				else {
					columnValues.put(placeholder[6], "");
				}

				// placeholder[7] denotes the HEL, OEL, DEL descriptions
				columnValues.put(placeholder[7], "HEL");
				allRowsPerSem.add(columnValues);
			}

			// Discipline
			int noOFDEL = chartQueries.getSemsInChart().get(semIndexNum).getNoOfDEL();
			for (int j=0; j<noOFDEL; j++)	{
				Course temp = sem_temp.get(j);
				Map<String,String> columnValues = new HashMap<String, String>();
				// placeholder[0] denotes the PIPE column
				if (temp.isInProgress() == "Y"){
					columnValues.put(placeholder[0], "||");	
				} 	else {	columnValues.put(placeholder[0], "");}

				if (temp.getIsDoneInPrevSem()){
					columnValues.put(placeholder[0], "%");	
				} else { columnValues.put(placeholder[0], "");}

				// placeholder[1], [2], [3], [4], [5] denotes the code, course subject, no(catalog), title, units
				columnValues.put(placeholder[1], ""+ temp.getCourseCode());
				columnValues.put(placeholder[2], ""+ temp.getSubject());
				columnValues.put(placeholder[3], ""+temp.getCatalog());
				columnValues.put(placeholder[4], ""+temp.getDescription());
				columnValues.put(placeholder[5], ""+temp.getMaxUnits());
				if (temp.getGrade() != null) {
					columnValues.put(placeholder[6], ""+temp.getGrade());
				}

				else {
					columnValues.put(placeholder[6], "");
				}
				// placeholder[7] denotes the HEL, OEL, DEL descriptions
				columnValues.put(placeholder[7], "DEL");
				allRowsPerSem.add(columnValues);
			}

			// Open Electives
			int noOFOEL = chartQueries.getSemsInChart().get(semIndexNum).getNoOfOEL();
			for (int j=0; j<noOFOEL; j++)	{
				Course temp = sem_temp.get(j);
				Map<String,String> columnValues = new HashMap<String, String>();
				// placeholder[0] denotes the PIPE column
				if (temp.isInProgress() == "Y"){
					columnValues.put(placeholder[0], "||");	
				} 	else {	columnValues.put(placeholder[0], "");}

				if (temp.getIsDoneInPrevSem()){
					columnValues.put(placeholder[0], "%");	
				} else { columnValues.put(placeholder[0], "");}

				// placeholder[1], [2], [3], [4], [5] denotes the code, course subject, no(catalog), title, units
				columnValues.put(placeholder[1], ""+ temp.getCourseCode());
				columnValues.put(placeholder[2], ""+ temp.getSubject());
				columnValues.put(placeholder[3], ""+temp.getCatalog());
				columnValues.put(placeholder[4], ""+temp.getDescription());
				columnValues.put(placeholder[5], ""+temp.getMaxUnits());
				if (temp.getGrade() != null) {
					columnValues.put(placeholder[6], ""+temp.getGrade());
				}

				else {
					columnValues.put(placeholder[6], "");
				}
				// placeholder[7] denotes the HEL, OEL, DEL descriptions
				columnValues.put(placeholder[7], "OEL");
				allRowsPerSem.add(columnValues);			
			}
			// If ends here (summer term check!)
		}

		// ------------------------------------------------------------------
		// Its a summer term semester
		else {
			Map<String,String> columnValues = new HashMap<String, String>();
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WRITE A METHOD TO GET PIPE OF PRACTICE SCHOOL !!!!!!!
			columnValues.put(placeholder[0], "");
			columnValues.put(placeholder[1], "1591");
			columnValues.put(placeholder[2], "BITS");
			columnValues.put(placeholder[3], "F221");
			columnValues.put(placeholder[4], "PRACTICE SCHOOL I");
			columnValues.put(placeholder[5], "5");
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! WRITE A METHOD TO GET GRADE OF PRACTICE SCHOOL !!!!!!!
			columnValues.put(placeholder[6], "A");
			columnValues.put(placeholder[7], "");
			allRowsPerSem.add(columnValues);
		}
		//System.out.println(allRowsPerSem.size());
		return allRowsPerSem;
	}


	public List<Map<String, String>> equalizeTwoSemsIntoYear (String year, 
			List<Map<String, String>> listLeft, List<Map<String, String>> listRight, 
			String[] placeholderLeft, String[] placeholderRight, String[] overallPlaceholder)	{

		// A complete ArrayList for a particular year
		List<Map<String, String>> listYear = new ArrayList<Map<String, String>>();

		// Empty Hash-Maps to handle empty ArrayList inputs or unequal sizes
		// They act as empty fillers !!
		Map<String, String> emptyMapLeft = new HashMap<String, String>();
		Map<String, String> emptyMapRight = new HashMap<String, String>();


		emptyMapLeft.put(placeholderLeft[0], "");
		emptyMapLeft.put(placeholderLeft[1], "");
		emptyMapLeft.put(placeholderLeft[2], "");
		emptyMapLeft.put(placeholderLeft[3], "");
		emptyMapRight.put(placeholderLeft[4], "");
		emptyMapLeft.put(placeholderLeft[5], "");
		emptyMapRight.put(placeholderLeft[6], "");
		emptyMapLeft.put(placeholderLeft[7], "");


		emptyMapRight.put(placeholderRight[0], "");
		emptyMapRight.put(placeholderRight[1], "");
		emptyMapRight.put(placeholderRight[2], "");
		emptyMapRight.put(placeholderRight[3], "");
		emptyMapRight.put(placeholderRight[4], "");
		emptyMapRight.put(placeholderRight[5], "");
		emptyMapRight.put(placeholderRight[6], "");
		emptyMapRight.put(placeholderRight[7], "");

		// Check if either of the list is empty
		// If found, insert an empty map to them
		if (listLeft.size()==0){listLeft.add(emptyMapLeft);} 
		if (listRight.size()==0){listRight.add(emptyMapRight);}

		boolean isSummer = false;
		if (year.equals("S"))	{
			isSummer = true;
		}

		// ----------------------- LEFT GREATER THAN OR EQUAL TO RIGHT -------------------------------//
		if (listLeft.size()>=listRight.size())	{
			for (int i=0; i<listLeft.size(); i++)	{

				//----------------------INITIAL PREPARATION START----------------------------------------
				//----------------------SAME FOR BOTH PARTS, IRRESPECTIVE OF MAP SIZES-------------------
				// Why?? Because, the year information is always added to the left placeholder
				// irrespective of which side is smaller

				if (i==0)	{
					// Put year Number if not a summer term
					if (!isSummer)	{
						listLeft.get(i).put(overallPlaceholder[0],""+toRoman(Integer.parseInt(year)));
					}

					// else put Summer
					else {
						listLeft.get(i).put(overallPlaceholder[0],"SUMMER");
					}
				}
				//----------------------INITIAL PREPARATION END----------------------------------------

				// MAIN merging code, MIRROR OF OPPOSTIE SIZE RELATION (see the else part of this) -------
				if (i>=listRight.size())	{
					// i!=0 is just an addition check for a very special condition, listRight.size() = 0
					// and i=0
					if(i!=0)	{
						// Take actual list from the left and combine it with an empty Column Map for
						// the right
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), emptyMapRight);
						temp.put(overallPlaceholder[0], "");
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}

					else {
						// i==0 condition, already done above this if-else loop !!
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine it with an empty Column Map for
						// the right
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), emptyMapRight);
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}

				}

				// Left has not yet exceeded the Right
				// Hence Fill actual content from both
				else{
					if(i!=0)	{
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), listRight.get(i));
						temp.put(overallPlaceholder[0], "");
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}
					else {
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine it with the Right Side Column Map
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), listRight.get(i));
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}
				}
				// -----------------------------------------------------------------------
			}
		}

		// ----------------------- RIGHT GREATER THAN LEFT -------------------------------//
		// EQUAL TO CONDITION ALREADY CONSIDERED IN ABOVE
		else {
			for (int i=0; i<listRight.size(); i++)	{
				//----------------------INITIAL PREPARATION START----------------------------------------
				//----------------------SAME FOR BOTH PARTS, IRRESPECTIVE OF MAP SIZES-------------------
				// Why?? Because, the year information is always added to the left placeholder
				// irrespective of which side is smaller
				if (i==0)	{
					// Put year Number is not a summer term
					if (!isSummer)	{
						listLeft.get(i).put(overallPlaceholder[0],""+toRoman(Integer.parseInt(year)));
					}

					// else put Summer
					else {
						listLeft.get(i).put(overallPlaceholder[0],"SUMMER");
					}
				}
				//----------------------INITIAL PREPARATION END----------------------------------------

				// MAIN merging code, MIRROR OF OPPOSTIE SIZE RELATION (see the if part of this) -------
				if (i>=listLeft.size())	{
					if (i!=0)	{
						// Take empty Column Map for the left and combine it with actual value from
						// the right
						Map<String, String> temp = mergeHashMaps(emptyMapLeft, listRight.get(i));
						temp.put(overallPlaceholder[0], "");
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}
					else {
						// i==0 condition, already done above this if-else loop !!
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine it with an empty Column Map for
						// the left
						Map<String, String> temp = mergeHashMaps(emptyMapLeft, listRight.get(i));
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}
				}

				// Right has not yet exceeded the Left
				// Hence Fill actual content from both
				else{
					if (i!=0)	{
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), listRight.get(i));
						temp.put(overallPlaceholder[0], "");
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}
					else {
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), listRight.get(i));
						// B2 filler
						temp.put(overallPlaceholder[9], " ");
						listYear.add(temp);
					}
				}
				// -----------------------------------------------------------------------
			}
		}
		return listYear;
	}


	public Map<String,String> mergeHashMaps (Map<String,String> s1, Map<String,String> s2)	{
		Map<String,String> m3 = new HashMap<String,String>();
		for (Map.Entry<String, String> entry : s1.entrySet())
		{
			m3.put(entry.getKey(),entry.getValue());
		}
		for (Map.Entry<String, String> entry : s2.entrySet())
		{
			m3.put(entry.getKey(),entry.getValue());
		}
		return m3;
	}

	 **/

	public String toRoman(int inp)	{
		String ret = null;
		if (inp==1)	{
			ret = "I";
		}
		else if (inp==2)	{
			ret = "II";
		}
		else if (inp==3)	{
			ret = "III";
		}
		else if (inp==4)	{
			ret = "IV";
		}
		else if (inp==5)	{
			ret = "V";
		}
		else if (inp==6)	{
			ret = "VI";
		}
		else if (inp==7)	{
			ret = "VII";
		}
		else if (inp==8)	{
			ret = "VIII";
		}
		return ret;
	}

	public boolean strictNumCheck(String inp) {
		inp = inp.trim();
		if (inp != null && !(inp.equals("")))	{
			// Is a number check
			if (StringUtils.isNumeric(inp))	{
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}

	private void setSettings(String key, String newValue){
		Preferences prefs = Preferences.userNodeForPackage(WindowLoader.class);

		prefs.put(key, newValue);

	}

	private String getSettings(String key) {
		Preferences prefs = Preferences.userNodeForPackage(WindowLoader.class);

		String value = prefs.get(key, null);

		return value;
	}


	private void loadELSheetsLike(String inputString) {

		DBConnector db = new DBConnector();

		String query = "SELECT * FROM STUDENTS WHERE campus_id LIKE '" + inputString + "'";
		
		ResultSet rs = db.queryExecutor(query, false);

		String idNo = "";

		ArrayList<String> idNos = new ArrayList<String>();

		try {
			while ( rs.next() ) {
				idNo = rs.getString(2);
				idNos.add(idNo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		File f2 = new File(destFolder.getText());

		Date d = new Date();
		String formattedDate = convertToProperString(d.toString());
		File f_Out = new File(destFolder.getText() + "\\EL_SHEETS_" + formattedDate + ".txt");

		if(f2.isDirectory()) {
			batchProcessELSheetsHelper(idNos, f_Out);
		}
		
		putWelcomeHTML();

	}
}