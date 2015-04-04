package bits.arcd.view;

//import static org.junit.Assert.*;

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
//import org.junit.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import bits.arcd.main.WindowLoader;
import bits.arcd.model.Course;
import bits.arcd.model.CourseChartQueries;
import bits.arcd.model.DBConnector;
import bits.arcd.model.EligibilitySheetQueries;

public class ELSheetController {
	// Reference to the main application.

	private WindowLoader mainApp; 


	@FXML public TextField idNum;
	@FXML public Button getElSheetButton;			
	@FXML public WebView browser;

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
	@FXML public CheckBox separateFilesELSheet;

	@FXML public Button stopButton;
	private boolean continueProc = true;

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

	@FXML
	private void initialize() {
		// Load the welcome page in WebView
		webEngine = browser.getEngine();
		FileSystem tempfs = FileSystems.getDefault();
		Path path = tempfs.getPath("src/main/resources/html_res/Welcome.html");
		webEngine.load("file:///"+path.toAbsolutePath().toString());

		destFolder.setText(getSettings("destFolderELSheet"));
		sourceIdNosCSV.setText(getSettings("sourceIdNosCSV"));
		refreshFolder.setText(getSettings("sourceCSVs"));

		// Button for ELSheet Generation
		getElSheetButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				if (idNum.getText() != null && ! idNum.getText().equals("")) {

					if (idNum.getText().length() == 12){

						threadSafeConsoleOutput("Processing, Please wait.........");
						loadWaitAnim();
						Thread temp = new Thread(){
							public void run(){
								loadSingleELSheet(idNum.getText());
							}
						};
						temp.start();

					}

					else if (idNum.getText().length() != 12 && idNum.getText().contains("%")) {

						threadSafeConsoleOutput("Processing, Please wait.........");
						loadWaitAnim();
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
					WindowLoader.showAlertDialog("Invalid IDNO", "Enter a proper IDNO");
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
					WindowLoader.showAlertDialog("Invalid Source File", "Please choose a valid source file!!");
					threadSafeConsoleOutput("Please choose a valid source file!!");
				}

			}
		});

		// Button for Batch ELSheet Generation
		generateButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				Thread thread = new Thread(){
					public void run(){
						batchProcessELSheets(sourceIdNosCSV.getText());
						WindowLoader.showAlertDialog("Process Completed", "The process has been completed.");
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
							setSettings("sourceCSVs", selectedFolder.getAbsolutePath());
						}
					});

				}
				else {
					WindowLoader.showAlertDialog("Invalid Source Folder", "Please choose a valid source folder!!");
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
					WindowLoader.showAlertDialog("Invalid Destination", "Please choose a valid destination folder!!");
					threadSafeConsoleOutput("Please choose a valid destination folder!!");
				}

			}
		});


		reloadButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				threadSafeConsoleOutput("Processing, Please wait.........");
				loadWaitAnim();
				Thread thread = new Thread(){
					public void run(){
						threadSafeConsoleOutput(CourseChartQueries.batchCSVChartsLoad(refreshFolder.getText()));
						loadHomeAnim();
					}
				};
				thread.start();


			}
		});

		// Terminate any currently executing loops
		stopButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (stopButton.getText().equalsIgnoreCase("Stop")){
					continueProc = false;
					stopButton.setText("Unlock the Processor");
				}
					
				else{
					stopButton.setText("Stop");
					continueProc = true;
				}
					
				
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
		final File f_Out = new File(destFolder.getText() + "\\EL_SHEETS_" + formattedDate + ".txt");

		threadSafeConsoleOutput("Processing, Please wait.........");


		if( f.isFile() && f2.isDirectory()) {


			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					WindowLoader.showExceptionDialog("File could not created", e);
					e.printStackTrace();
				}
			}
			Scanner sc = null;
			try {
				sc = new Scanner(f);
			} catch (FileNotFoundException e) {
				WindowLoader.showExceptionDialog("Source file not found", e);
				e.printStackTrace();
			}

			final ArrayList<String> idNos = new ArrayList<String>();

			String idNo = "";
			while(sc.hasNext()){
				idNo = sc.nextLine().replace(",", "");
				idNos.add(idNo);
			}
			batchProcessELSheetsHelper(idNos, f_Out);

		}
		else {
			threadSafeConsoleOutput("Invalid directory specified!");
		}
		threadSafeConsoleOutput("Finished!..........");

		loadHomeAnim();

	}




	private void batchProcessELSheetsHelper(ArrayList<String> idNos, File f){

		FileWriter fw = null;
		BufferedWriter bw = null;
		
		File f_separate = null;
		FileWriter fw_s = null;
		BufferedWriter bw_s = null;
		
		try {
			fw = new FileWriter(f.getAbsoluteFile(), true);
			bw = new BufferedWriter(fw);
		} catch (IOException e1) {

			WindowLoader.showExceptionDialog("The File Writer/ "
					+ "Buffer could not be created", e1);
			loadHomeAnim();
			e1.printStackTrace();
		}

		for(int j = 0; j < idNos.size(); j++) {
			if (continueProc)	{
				if (! inpSemNum.getText().equals("")){
					EligibilitySheetQueries e = new EligibilitySheetQueries(idNos.get(j), Integer.parseInt(inpSemNum.getText()));
					String s = e.toString();

					try {
						if (separateFilesELSheet.isSelected()) {
							f_separate = new File(destFolder.getText() + "\\" + e.getStudentId() + ".txt");
							if (!f_separate.exists()) {
								f_separate.createNewFile();
							}
							fw_s = new FileWriter(f_separate);
							bw_s = new BufferedWriter(fw_s);
							bw_s.write(s);
							bw_s.write("\f");
							bw_s.close();
							fw_s.close();
							threadSafeConsoleOutput("\n" + (new Date()).toString() 
									+ " : Wrote " + idNos.get(j).toString() + " to " + f_separate.getAbsolutePath() + "\n");
						}

						else {
							bw.write(s);
							bw.write("\f");
							
							threadSafeConsoleOutput("\n" + (new Date()).toString() 
									+ " : Wrote " + idNos.get(j).toString() + " to " + f.getAbsolutePath() + "\n");
						}
					} catch (IOException e1) {

						e1.printStackTrace();
					}

					threadSafeConsoleOutput("\n" + (new Date()).toString() 
							+ " : Wrote " + idNos.get(j).toString() + "\n");
				}
			}
		}


		try {
			bw.close();
		} catch (IOException e2) {
			WindowLoader.showExceptionDialog("BufferWriter could not closed", e2);
			loadHomeAnim();
			e2.printStackTrace();
		}

		threadSafeConsoleOutput("\n" + (new Date()).toString() 
				+ " : File(s) written inside : " + destFolder.getText() + "\n");
	}

	public void loadSingleELSheet(String idNum) {
		Platform.runLater(new Runnable() {
			public void run() {

			}
		});

		try {

			File file = new File(destFolder.getText() + "\\EL_SHEET_" + idNum + ".txt");

			if (!file.exists()) {
				file.createNewFile();
			}

			if (! inpSemNum.getText().equals("")){

				EligibilitySheetQueries c = new EligibilitySheetQueries(idNum, Integer.parseInt(inpSemNum.getText()));
				String s = c.toString();
				CourseChartQueries ch =c.getChart();
				if(ch.getStream2()==null ) {
					if(c.getStudentName().indexOf("PS")>0) {
						if(ch.getSemsInChart().size()<9)
						WindowLoader.showAlertDialog("Chart has lesser semesters than expected", 
								"Students does not have 8+1(PS) sems");
					}
					else {	if(ch.getSemsInChart().size()<8)
						WindowLoader.showAlertDialog("Chart has lesser semesters than expected", 
								"Students does not have 8 sems");
					}
					
				}
				else {	if(ch.getSemsInChart().size()<10)
					WindowLoader.showAlertDialog("Chart has lesser semesters than expected", 
							"Students does not have 10 sems");
				}
				

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
			WindowLoader.showExceptionDialog("Error Occured while generating the EL Sheet", e);
			loadHomeAnim();
			e.printStackTrace();
		}
	}

	public void loadHomeAnim()	{
		Platform.runLater(new Runnable() {
			public void run() {

				FileSystem tempfs = FileSystems.getDefault();
				Path path = tempfs.getPath("src/main/resources/html_res/Welcome.html");
				webEngine.load("file:///"+path.toAbsolutePath().toString());
			}
		});
	}

	public void loadWaitAnim()	{
		Platform.runLater(new Runnable() {
			public void run() {
				FileSystem tempfs = FileSystems.getDefault();
				Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
				webEngine.load("file:///"+path.toAbsolutePath().toString());
			}
		});
	}



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

		DBConnector db = DBConnector.getInstance();

		String query = "SELECT * FROM STUDENTS WHERE campus_id LIKE '" + inputString + "'";

		ResultSet rs = db.queryExecutor(query, false);

		String idNo = "";

		ArrayList<String> idNos = new ArrayList<String>();

		try {
			while ( rs.next() ) {
				idNo = rs.getString(2);
				idNos.add(idNo);
			}

			rs.close();

		} catch (SQLException e) {
			WindowLoader.showExceptionDialog("SQL Execption", e);
			e.printStackTrace();
		}



		File f2 = new File(destFolder.getText());

		Date d = new Date();
		String formattedDate = convertToProperString(d.toString());
		File f_Out = new File(destFolder.getText() + "\\EL_SHEETS_" + formattedDate + ".txt");

		if(f2.isDirectory()) {
			batchProcessELSheetsHelper(idNos, f_Out);
		}
		
		WindowLoader.showAlertDialog("Export Completed", "Process of Export successfully completed");
		loadHomeAnim();

	}
}
