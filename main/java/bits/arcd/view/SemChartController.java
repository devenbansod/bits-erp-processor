package bits.arcd.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.docx4j.Docx4J;
import org.docx4j.Docx4jProperties;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import com.sun.javafx.tk.FileChooserType;

import bits.arcd.main.WindowLoader;
import bits.arcd.model.AcadCounselBoard;
import bits.arcd.model.CourseChartQueries;
import bits.arcd.model.Course;
import bits.arcd.model.DBConnector;
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

import java.util.Properties;
import java.util.prefs.Preferences;

public class SemChartController {
	//Chart c = new Chart("1561","jdbc:mysql://172.17.27.43:8888/","ghazi","erp321");
	//System.out.println(c.toString());
	// Reference to the main application.
	private WindowLoader mainApp; 
	@FXML public TextField reqNum;
	@FXML public Button getChart; 

	@FXML public TextField sourceFile;
	@FXML public TextField destFolder;
	@FXML public Button sourceBrowse;
	@FXML public Button destBrowse;
	@FXML public TextField reqNumValues;
	@FXML public Button generateAndSave;


	@FXML public TextField srcFileRefresh;
	@FXML public Button sourceFileRefreshButton;
	@FXML public Button updateAll;
	@FXML public WebView browser;
	@FXML public TextArea consoleOutput;


	@FXML public TextField inpSemNumRep;
	@FXML public TextField IdNoFilter;
	@FXML public TextField destFolderReports;
	@FXML public Button browseDestFolderRepButton;
	@FXML public Button generateACBButton;
	@FXML public Button generateBLButton;
	@FXML public Button generateGRButton;
	@FXML public Button generateLikelyGRButton;
	@FXML public TextArea consoleOutputRep;
	


	private CourseChartQueries chartQueries;
	final String userhome = System.getProperty("user.home");
	private WebEngine webEngine;

	public SemChartController()	{

	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded. Called after constructor
	 */
	@FXML
	private void initialize() {
		// Load the welcome page in WebView
		webEngine = browser.getEngine();
		FileSystem tempfs = FileSystems.getDefault();
		Path path = tempfs.getPath("src/main/resources/html_res/Welcome.html");
		webEngine.load("file:///"+path.toAbsolutePath().toString());
		sourceFile.setText(getSettings("sourceFileCSV"));
		destFolder.setText(getSettings("destFolder"));
		srcFileRefresh.setText(getSettings("sourceCSVs"));
		destFolderReports.setText(getSettings("destFolderRep"));
		// Thread safe loading
		/**Platform.runLater(new Runnable() {
			public void run() {
				webEngine.load("C:\\Users\\Ghazi\\1561.docx.html");	
			}
		});**/

		// Implement listeners for various buttons
		getChart.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (reqNum.getText() != null && !reqNum.getText().equals("") 
						&& strictNumCheck(reqNum.getText().trim()))	{
					//System.out.println(reqNum.getText());
					//mainApp.showSemChart(reqNum.getText());
					threadSafeConsoleOutput("Processing, Please wait.........");
					Platform.runLater(new Runnable() {
						public void run() {
							FileSystem tempfs = FileSystems.getDefault();
							Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
							webEngine.load("file:///"+path.toAbsolutePath().toString());
						}
					});

					Thread temp = new Thread (){
						@Override
						public void run() {
							try {
								loadSingleChart(reqNum.getText());
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					};
					temp.start();
				}
				else {
					threadSafeConsoleOutput("Please put a valid requirement number!!");
				}
			}
		});

		sourceBrowse.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				//				DirectoryChooser chooser = new DirectoryChooser();
				FileChooser chooser = new FileChooser();

				chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"),
						new ExtensionFilter("txt Files", "*.txt"));

				chooser.setTitle("Select the CSV File with Req Nos");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				


				File selectedFile = chooser.showOpenDialog(new Stage());

				if (selectedFile != null)	{
					sourceFile.setText(selectedFile.getAbsolutePath());
					setSettings("sourceFileCSV", selectedFile.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutput("Please choose a valid CSV/ Txt File!!");
				}
			}
		});


		destBrowse.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser chooser = new DirectoryChooser();
				//FileChooser chooser = new FileChooser();

				chooser.setTitle("Select the Folder for Output");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				


				File selectedFile = chooser.showDialog(new Stage());

				if (selectedFile != null)	{
					destFolder.setText(selectedFile.getAbsolutePath());
					setSettings("destFolder", selectedFile.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutput("Please choose a valid source folder!!");
				}
			}
		});


		sourceFileRefreshButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser chooser = new DirectoryChooser();
				//				FileChooser chooser = new FileChooser();

				chooser.setTitle("Select the folder with all the CSV Files");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				

				File selectedFolder = chooser.showDialog(new Stage());

				if (selectedFolder != null)	{
					srcFileRefresh.setText(selectedFolder.getAbsolutePath());				
					setSettings("sourceCSVs", selectedFolder.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutput("Please choose a valid source folder!!");
				}
			}
		});


		updateAll.setOnAction(new EventHandler<ActionEvent>() {
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


						threadSafeConsoleOutput(CourseChartQueries.batchCSVChartsLoad(srcFileRefresh.getText()));
						putWelcomeHTML();
					}
				};
				thread.start();


			}
		});

		generateAndSave.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				// Launch it in a separate thread, so that it doesn't hang the main
				// window!

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

						if (! sourceFile.getText().equals("")){
							batchProcessCharts(sourceFile.getText());
							threadSafeConsoleOutput("Processing Done!");
						}
						else if(! reqNumValues.getText().equals("")) {
							String[] reqNums = reqNumValues.getText().split(",");

							Date d = new Date();
							String formattedDate = convertToProperString(d.toString());
							File f_Out = new File(destFolder.getText() + ""
									+ "\\Output_Charts_" + formattedDate + ".txt");
							batchProcessChartsHelper(new ArrayList<String>(Arrays.asList(reqNums)), f_Out);
						}
					}
				};
				thread.start();

			}
		});



		// For Reports Tab

		browseDestFolderRepButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser chooser = new DirectoryChooser();

				chooser.setTitle("Select the Folder for Output");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				


				File selectedFile = chooser.showDialog(new Stage());

				if (selectedFile != null)	{
					destFolderReports.setText(selectedFile.getAbsolutePath());
					setSettings("destFolderRep", selectedFile.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutput("Please choose a valid folder!!");
				}
			}
		});


		generateACBButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				threadSafeConsoleOutputRep("Processing .... Please wait .....\nGenerating ACB Report\n");

				Thread thread = new Thread(){
					public void run(){				
						DBConnector db = DBConnector.getInstance();
						String query = "SeLECT * FROM students where campus_id like '" + IdNoFilter.getText() + "'";
						Date d = new Date();

						String formattedDate = convertToProperString(d.toString());

						final File f_Out = new File(destFolder.getText() + "\\ACB_" + formattedDate + ".txt");

						FileWriter fw = null;
						BufferedWriter bw = null;

						try {
							fw = new FileWriter(f_Out.getAbsoluteFile(), true);
							bw = new BufferedWriter(fw);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						ResultSet rs = db.queryExecutor(query, false);

						try {
							while(rs.next()){
								AcadCounselBoard a = new AcadCounselBoard(rs.getString(2), 
										Integer.parseInt(inpSemNumRep.getText()));
								if (a.getIsAcb()) {
								
									bw.write(a.printACB());
									threadSafeConsoleOutputRep("\n" + (new Date()).toString() 
											+ " : Wrote " + rs.getString(2) + "\n");
								}
								else {
									threadSafeConsoleOutputRep("\n..");
								}
							}

							bw.close();
							rs.close();
							fw.close();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						threadSafeConsoleOutputRep("Exported the ACB Report to : \n" + f_Out.getAbsolutePath());
					}
					
					
				};
				thread.start();

			}
		});

		
		generateBLButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				threadSafeConsoleOutputRep("Processing .... Please wait .....\nGenerating BL Report\n");

				Thread thread = new Thread(){
					public void run(){				
						DBConnector db = DBConnector.getInstance();
						String query = "SeLECT * FROM students where campus_id like '" + IdNoFilter.getText() + "'";

						Date d = new Date();

						String formattedDate = convertToProperString(d.toString());

						final File f_Out = new File(destFolder.getText() + "\\BL_" + formattedDate + ".txt");

						FileWriter fw = null;
						BufferedWriter bw = null;

						try {
							fw = new FileWriter(f_Out.getAbsoluteFile(), true);
							bw = new BufferedWriter(fw);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						ResultSet rs = db.queryExecutor(query, false);

						try {
							while(rs.next()){
								AcadCounselBoard a = new AcadCounselBoard(rs.getString(2), 
										Integer.parseInt(inpSemNumRep.getText()));
								if (a.isBackLog()) {
								
									bw.write(a.printBackLog());
									threadSafeConsoleOutputRep("\n" + (new Date()).toString() 
											+ " : Wrote " + rs.getString(2) + "\n");
								}
								else {
									threadSafeConsoleOutputRep("\n..");
								}
							}

							bw.close();
							rs.close();
							fw.close();
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						threadSafeConsoleOutputRep("Exported the BL Report to : \n" + f_Out.getAbsolutePath());
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

	public void threadSafeConsoleOutputRep(final String output)	{
		Platform.runLater(new Runnable() {
			public void run() {
				consoleOutputRep.setText(consoleOutputRep.getText() + "" + output);
				consoleOutputRep.end();
			}
		});
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

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param mainApp
	 */
	public void setMainApp(WindowLoader mainApp) {
		this.mainApp = mainApp;
	}

	private String convertToProperString(String s) {

		s = s.replace(" ", "_");
		s = s.replace("-", "_");
		s = s.replace(":", "_");
		return s;
	}


	public void loadSingleChart(String reqNum)	throws Exception {
		if (strictNumCheck(reqNum))	{
			try {
				// Do the remaining operations if and only if this check returns true!!
				// Otherwise you will get File IO Exception, because the Word document will
				// not be created

				File file = new File(destFolder.getText() + "\\Output_Charts_" + reqNum + ".txt");

				if (!file.exists()) {
					file.createNewFile();
				}


				CourseChartQueries c = new CourseChartQueries(reqNum);
				String s = c.toString();

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(s);
				bw.close();

				threadSafeConsoleOutput("\n\n" + (new Date()).toString() + " : Finished Processing !\n");

				threadSafeConsoleOutput("\n\n" + (new Date()).toString() + " : Exported the Chart Data into " 
						+ file.getAbsolutePath() + ".txt\n");


				final String contentURL = new URL("file:///" +  file.getAbsolutePath()).toExternalForm();

				Platform.runLater(new Runnable() {
					public void run() {
						webEngine.load(contentURL);	
					}
				});
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			throw new Exception("Please input a proper Integer!");
			//			threadSafeConsoleOutput("The input number: " + reqNum + " is either invalid numeric!");
		}
	}

	public void batchProcessCharts (String SourceFile)	{

		File f = new File(SourceFile);
		File f2 = new File(destFolder.getText());

		Date d = new Date();
		String formattedDate = convertToProperString(d.toString());
		File f_Out = new File(destFolder.getText() + "\\Output_Charts_" + formattedDate + ".txt");

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

			ArrayList<String> reqNos = new ArrayList<String>();

			String reqNo = "";
			while(sc.hasNext()){
				reqNo = sc.nextLine().replace(",", "");
				reqNos.add(reqNo);
			}



			batchProcessChartsHelper(reqNos, f_Out);
		}
		else {
			threadSafeConsoleOutput("Invalid directory specified!");
		}
		threadSafeConsoleOutput("Finished!..........");

		putWelcomeHTML();
	}


	private void batchProcessChartsHelper(ArrayList<String> reqNos, File f){

		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(f.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(int j = 0; j < reqNos.size(); j++) {

			CourseChartQueries c = null;
			try {
				c = new CourseChartQueries(reqNos.get(j));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			String s = c.toString();


			try {
				bw.write(s);
				bw.write("\f");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			threadSafeConsoleOutput("\n" + (new Date()).toString() 
					+ " : Wrote " + reqNos.get(j).toString() + "\n");



		}

		threadSafeConsoleOutput("\n\n" + (new Date()).toString() + " : Exported the Chart Data into " 
				+ f.getAbsolutePath() + "\n");

		try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		threadSafeConsoleOutput("\n" + (new Date()).toString() 
				+ " : File written inside : " + destFolder.getText() + "\n");
	}



	/** NOT NEEDED 
	private boolean processDocument(String reqNum, String destDir) {
		// ---------------------- Load Animation --------------------------------
		threadSafeConsoleOutput("Processing, Please wait.........");
		Platform.runLater(new Runnable() {
			public void run() {
				FileSystem tempfs = FileSystems.getDefault();
				Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
				webEngine.load("file:///"+path.toAbsolutePath().toString());
			}
		});

		// ---------------------- Load Animation --------------------------------
		reqNum = reqNum.trim();
		chartQueries = new CourseChartQueries(reqNum);
		// To give return statement on whether processing occurred or not!
		boolean retVal = false;

		// Check if a complete Chart Object was made or not!
		if (chartQueries.getRequirementGroup()!=0)	{
			// Make a word processor instance

			WordProcessor w1 ;
			// Select the appropriate word file
			if (chartQueries.getYrNsem()[0]==5)	{
				System.out.println("4 Year Chart was selected!");
				w1= new WordProcessor("src/main/java/bits/arcd/wordProcess/4Year.docx");
			}
			else {
				System.out.println("5 Year Chart was selected!");
				System.out.println(chartQueries.getYrNsem()[0]);
				w1= new WordProcessor("src/main/java/bits/arcd/wordProcess/5Year.docx");
			}

			// First make changes to the topmost part of the sample document
			// ----------------------------------------------------TOP PLACEHOLDERS PART START-------------
			String[] placeholdersTop = 
					new String[] {"INSTI_NM", "CHART_DESC", "DATE_CUR"};
			Map<String,String> replTop1 = new HashMap<String, String>();
			Map<String, String>	replTop2 = new HashMap<String, String>();

			// row 1
			replTop1.put("INSTI_NM", "BITS PILANI");
			replTop1.put("CHART_DESC", "BASE CHART FOR: 2011 ONWARDS");
			replTop1.put("DATE_CUR", new SimpleDateFormat("dd/MM/yyyy").format(new Date()).toString());
			// row 2
			replTop2.put("INSTI_NM", "CHART CODE: "+chartQueries.getRequirementDescription().toUpperCase());
			replTop2.put("CHART_DESC", "REQ GRP: "+chartQueries.getRequirementGroup()+"   REG NO: "+chartQueries.getRequirementNo());
			replTop2.put("DATE_CUR", "");
			w1.replaceTable(placeholdersTop, Arrays.asList(replTop1,replTop2), w1.doc1);

			// ----------------------------------------------------TOP PLACEHOLDERS PART END-------------

			// Loop through all the sems (and years)
			for (int i=0,  year = 0; i<chartQueries.getSemsInChart().size(); i++)	{

				// There is no else condition for this loop. Hence this loop only inputs
				// left hand-side semesters. Wherever, mergeTwoSems is called, it is called for both
				// i and i+1 to account for both the left and the right semesters
				if (i%2==0)	{
					year++;
					System.out.println("year: "+year+" sem: "+1+","+2);
					//System.out.println(year);

					// Appropriate placeholder for the left part -- don't include Year in this
					// to maintain symmetric nature with right hand part
					// Year would be added later in the mergeTwoSems method (in the leftPlaceholder)

					String[] placeholderLeft = {"COD"+year+"1", "CONUM"+year+"1",
							"COTIT"+year+"1", "UNI"+year+"1"};

					// Appropriate placeholder for the right part
					String[] placeholderRight = {"COD"+year+"2", "CONUM"+year+"2", 
							"COTIT"+year+"2", "UNI"+year+"2"};

					// Overall placeholder -- Year column + placeholder left + placeholder right
					String[] overallPlaceholder = {"YR"+year, "COD"+year+"1", "CONUM"+year+"1",
							"COTIT"+year+"1", "UNI"+year+"1", 
							"COD"+year+"2", "CONUM"+year+"2", "COTIT"+year+"2", "UNI"+year+"2"};

					// It means both the sems exist
					// ?? How --> Because as told earlier, we enter the loop only for 
					// i%2==0 i.e, left hand-side semesters only. (i+1) guarantees the existence of the 
					// right hand-side semester also
					if ((i+1)<chartQueries.getSemsInChart().size())	{
						w1.replaceTable(overallPlaceholder, 
								equalizeTwoSemsIntoYear(""+year, overallPlaceholder[0], semesterBuilder(""+i, placeholderLeft), semesterBuilder(""+(i+1), placeholderRight), placeholderLeft, placeholderRight), 
								w1.doc1);
					}

					// Last leg of the loop, the right hand-side semester does not exist
					else {
						// Pass empty ArrayList for the right side
						w1.replaceTable(overallPlaceholder, 
								equalizeTwoSemsIntoYear(""+year, overallPlaceholder[0], semesterBuilder(""+i, placeholderLeft), new ArrayList<Map<String, String>>(), placeholderLeft, placeholderRight), 
								w1.doc1);
					}
				}
			}

			// Handle summer term separately --------------------------------------------

			String[] placeholderLeft = {"COD"+"S"+"1", "CONUM"+"S"+"1",
					"COTIT"+"S"+"1", "UNI"+"S"+"1"};
			String[] placeholderRight = {"COD"+"S"+"2", "CONUM"+"S"+"2", 
					"COTIT"+"S"+"2", "UNI"+"S"+"2"};
			String[] overallPlaceholder = {"YR"+"S", "COD"+"S"+"1", "CONUM"+"S"+"1",
					"COTIT"+"S"+"1", "UNI"+"S"+"1", 
					"COD"+"S"+"2", "CONUM"+"S"+"2", "COTIT"+"S"+"2", "UNI"+"S"+"2"};
			w1.replaceTable(overallPlaceholder, 
					equalizeTwoSemsIntoYear("S", overallPlaceholder[0], semesterBuilder("S", placeholderLeft), new ArrayList<Map<String, String>>(), placeholderLeft, placeholderRight), 
					w1.doc1);

			// Summer term finished! ----------------------------------------------------

			w1.writeDocxToStream(w1.doc1, destDir+"\\"+reqNum+".docx");
			// By default set for false
			retVal = true;
		}

		// This reqNum is either not valid or does not exist in database
		else {
			threadSafeConsoleOutput("The input number: "+reqNum+" is either not valid or does "
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

	 */

	/** UNWANTED CODE 

	public List<Map<String, String>> semesterBuilder(String sem_index, String[] placeholder)	{
		List<Map<String, String>> allRowsPerSem = new ArrayList<Map<String, String>>();	

		// Check whether summer term or not !! Summer terms are handled separately
		if (!sem_index.equals("S"))	{
			int semIndexNum = Integer.parseInt(sem_index);
			ArrayList<Course> sem_temp = chartQueries.getSemsInChart().get(semIndexNum).getCompulsoryCourses();

			for (int j=0; j<sem_temp.size(); j++)	{
				Course temp = sem_temp.get(j);
				Map<String,String> columnValues = new HashMap<String, String>();
				columnValues.put(placeholder[0], ""+temp.getCourseCode());
				columnValues.put(placeholder[1], ""+temp.getSubject()+" "+temp.getCatalog().trim());
				columnValues.put(placeholder[2], ""+temp.getDescription());
				columnValues.put(placeholder[3], ""+temp.getMaxUnits());
				allRowsPerSem.add(columnValues);
			}

			// Humanities
			int noOFHUEL = chartQueries.getSemsInChart().get(semIndexNum).getNoOfHUEL();
			for (int j=0; j<noOFHUEL; j++)	{
				Map<String,String> columnValues = new HashMap<String, String>();
				columnValues.put(placeholder[0], "----");
				columnValues.put(placeholder[1], "----");
				columnValues.put(placeholder[2], "------HEL------");
				columnValues.put(placeholder[3], "--");
				allRowsPerSem.add(columnValues);
			}

			// Discipline
			int noOFDEL = chartQueries.getSemsInChart().get(semIndexNum).getNoOfDEL();
			for (int j=0; j<noOFDEL; j++)	{
				Map<String,String> columnValues = new HashMap<String, String>();
				columnValues.put(placeholder[0], "----");
				columnValues.put(placeholder[1], "----");
				columnValues.put(placeholder[2], "------DEL------");
				columnValues.put(placeholder[3], "--");
				allRowsPerSem.add(columnValues);
			}

			// Open
			int noOFOEL = chartQueries.getSemsInChart().get(semIndexNum).getNoOfOEL();
			for (int j=0; j<noOFOEL; j++)	{
				Map<String,String> columnValues = new HashMap<String, String>();
				columnValues.put(placeholder[0], "----");
				columnValues.put(placeholder[1], "----");
				columnValues.put(placeholder[2], "------EL------");
				columnValues.put(placeholder[3], "--");
				allRowsPerSem.add(columnValues);
			}
			// If ends here (summer term check!)
		}

		// ------------------------------------------------------------------
		// Its a summer term semester
		else {
			Map<String,String> columnValues = new HashMap<String, String>();
			columnValues.put(placeholder[0], "1591");
			columnValues.put(placeholder[1], "BITS"+" F221");
			columnValues.put(placeholder[2], "PRACTICE SCHOOL I");
			columnValues.put(placeholder[3], "5");
			allRowsPerSem.add(columnValues);
		}
		//System.out.println(allRowsPerSem.size());
		return allRowsPerSem;
	}

	public List<Map<String, String>> equalizeTwoSemsIntoYear (String year, String yearPlaceHolder, 
			List<Map<String, String>> listLeft, List<Map<String, String>> listRight, 
			String[] placeholderLeft, String[] placeholderRight)	{

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

		emptyMapRight.put(placeholderRight[0], "");
		emptyMapRight.put(placeholderRight[1], "");
		emptyMapRight.put(placeholderRight[2], "");
		emptyMapRight.put(placeholderRight[3], "");

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
						listLeft.get(i).put(yearPlaceHolder,""+toRoman(Integer.parseInt(year)));
					}

					// else put Summer
					else {
						listLeft.get(i).put(yearPlaceHolder,"SUMMER");
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
						temp.put(yearPlaceHolder, "");
						listYear.add(temp);
					}
					else {
						// i==0 condition, already done above this if-else loop !!
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine it with an empty Column Map for
						// the right
						listYear.add(mergeHashMaps(listLeft.get(i), emptyMapRight));
					}

				}

				// Left has not yet exceeded the Right
				// Hence Fill actual content from both
				else{
					if(i!=0)	{
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), listRight.get(i));
						temp.put(yearPlaceHolder, "");
						listYear.add(temp);
					}
					else {
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine it with the Right Side Column Map
						listYear.add(mergeHashMaps(listLeft.get(i), listRight.get(i)));
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
						listLeft.get(i).put(yearPlaceHolder,""+toRoman(Integer.parseInt(year)));
					}

					// else put Summer
					else {
						listLeft.get(i).put(yearPlaceHolder,"SUMMER");
					}
				}
				//----------------------INITIAL PREPARATION END----------------------------------------

				// MAIN merging code, MIRROR OF OPPOSTIE SIZE RELATION (see the if part of this) -------
				if (i>=listLeft.size())	{
					if (i!=0)	{
						// Take empty Column Map for the left and combine it with actual value from
						// the right
						Map<String, String> temp = mergeHashMaps(emptyMapLeft, listRight.get(i));
						temp.put(yearPlaceHolder, "");
						listYear.add(temp);
					}
					else {
						// i==0 condition, already done above this if-else loop !!
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine it with an empty Column Map for
						// the left
						listYear.add(mergeHashMaps(emptyMapLeft, listRight.get(i)));
					}
				}

				// Right has not yet exceeded the Left
				// Hence Fill actual content from both
				else{
					if (i!=0)	{
						Map<String, String> temp = mergeHashMaps(listLeft.get(i), listRight.get(i));
						temp.put(yearPlaceHolder, "");
						listYear.add(temp);
					}
					else {
						// i==0 condition, already done above this if-else loop !!
						// This part is already taken care of (aka, the necessary part of adding the 
						// appropriate Roman year) above, so just combine left and right Column maps
						listYear.add(mergeHashMaps(listLeft.get(i), listRight.get(i)));
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

	 */

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
}
