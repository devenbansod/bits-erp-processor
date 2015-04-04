package bits.arcd.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import bits.arcd.main.WindowLoader;
import bits.arcd.model.AcadCounselBoard;
import bits.arcd.model.CourseChartQueries;
import bits.arcd.model.DBConnector;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

import javax.xml.soap.Text;


public class SemChartController {

	// Reference to the main application.
	@SuppressWarnings("unused")
	private WindowLoader mainApp; 
	@FXML public TextField reqNum;
	@FXML public Button getChart; 

	@FXML public TextField sourceFile;
	@FXML public TextField destFolder;
	@FXML public Button sourceBrowse;
	@FXML public Button destBrowse;
	@FXML public CheckBox separateFiles;

	@FXML public Button generateAndSave;


	@FXML public TextField srcFileRefresh;
	@FXML public Button sourceFileRefreshButton;
	@FXML public Button updateAll;
	@FXML public WebView browser;
	@FXML public TextArea consoleOutput;


	@FXML public Button stopButton;
	private boolean continueProc = true;

	
	@FXML public Button saveDBSettings;
	@FXML public TextField hostIp;
	@FXML public TextField mysqlUser;
	@FXML public TextField mysqlPassword;
	@FXML public TextField databaseName;
	
	
	
	final String userhome = System.getProperty("user.home");
	private WebEngine webEngine;

	// Changed to requirementGroup ------------ USE THIS NEW TEXTFIELD TO GENERATE CHARTS FOR 
	// ---------------------	A PARTICULAR GROUP  --------------------------------

	@FXML public TextField reqGroup;

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

		
		hostIp.setText(getSettings("hostIp"));
		mysqlUser.setText(getSettings("mysqlUser"));
		mysqlPassword.setText(getSettings("mysqlPassword"));
		databaseName.setText(getSettings("databaseName"));
		

		// Implement listeners for various buttons
		getChart.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {


				if (reqNum.getText() != null && !reqNum.getText().equals("") 
						&& strictNumCheck(reqNum.getText().trim()) && reqGroup.getText().equals(""))	{

					threadSafeConsoleOutput("Processing, Please wait.........");
					loadWaitAnim();

					Thread temp = new Thread (){
						@Override
						public void run() {
							try {
								loadSingleChart(reqNum.getText());
							} catch (Exception e) {
								WindowLoader.showExceptionDialog("Error occured while processing the Chart", e);
								e.printStackTrace();
							}
						}
					};
					temp.start();
				}

				else if (reqGroup.getText() != null && ! reqGroup.getText().equals("")
						&& strictNumCheck(reqGroup.getText().trim()) && reqNum.getText().equals("")) {
					threadSafeConsoleOutput("Processing, Please wait.........");
					loadWaitAnim();

					Thread temp = new Thread (){
						@Override
						public void run() {
							try {
								ArrayList<String> reqNums = new ArrayList<String>();

								String query = "Select DISTINCT rqrmnt from charts where rq_group = '" + reqGroup.getText() + "'";
								ResultSet rs = DBConnector.getInstance().queryExecutor(query, false);
								while (rs.next()) {
									reqNums.add(rs.getString(1));
								}

								File file = new File(destFolder.getText() + "\\Output_Charts_" + reqGroup.getText() + ".txt");

								if (!file.exists()) {
									file.createNewFile();
								}

								batchProcessChartsHelper(reqNums, file);
							} catch (Exception e) {
								WindowLoader.showExceptionDialog("Error occured while processing the Chart", e);
								e.printStackTrace();
							}
						}
					};
					temp.start();


				}
				else {
					WindowLoader.showAlertDialog("Invalid Requirement Number", "Please input a valid requirment number");
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
					WindowLoader.showAlertDialog("Invalid Source File", "Please select a valid source file");
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
					WindowLoader.showAlertDialog("Invalid Destination Folder", "Please select a valid Destination Folder");
					threadSafeConsoleOutput("Please choose a valid Destination folder!!");
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
					WindowLoader.showAlertDialog("Invalid Source Folder", "Please select a valid source folder");
					threadSafeConsoleOutput("Please choose a valid source folder!!");
				}
			}
		});


		updateAll.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {

				threadSafeConsoleOutput("Processing, Please wait.........");
				loadWaitAnim();
				Thread thread = new Thread(){
					public void run(){
						threadSafeConsoleOutput(CourseChartQueries.batchCSVChartsLoad(srcFileRefresh.getText()));
						loadHomeAnim();
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
				loadWaitAnim();
				Thread thread = new Thread(){
					public void run(){

						if (! sourceFile.getText().equals("")){
							batchProcessChartsFromFile(sourceFile.getText());
							WindowLoader.showAlertDialog("Processing done", "The Export of Charts Data was completed");
							threadSafeConsoleOutput("Processing Done!\n");
						}

						/**
						else if(! reqNumValues.getText().equals("")) {
							String[] reqNums = reqNumValues.getText().split(",");

							Date d = new Date();
							String formattedDate = convertToProperString(d.toString());
							File f_Out = new File(destFolder.getText() + ""
									+ "\\Output_Charts_" + formattedDate + ".txt");
							batchProcessChartsHelper(new ArrayList<String>(Arrays.asList(reqNums)), f_Out);
						}**/

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
		
		saveDBSettings.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (! hostIp.getText().equalsIgnoreCase("") 
						&& ! mysqlUser.getText().equals("") 
						&& ! mysqlPassword.getText().equals("")
						&& ! databaseName.getText().equals("")) {
					
					setSettings("hostIp", hostIp.getText());
					setSettings("mysqlUser", mysqlUser.getText());
					setSettings("mysqlPassword", mysqlPassword.getText());
					setSettings("databaseName", databaseName.getText());
					
					WindowLoader.showAlertDialog("Settings saved", "The Database settings are saved. "
							+ "Please restart the Software to let it take effects");
					
				}
				
				else {
					WindowLoader.showAlertDialog("Invalid Values", "Please enter valid values");
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

	public void loadWaitAnim()	{
		Platform.runLater(new Runnable() {
			public void run() {
				FileSystem tempfs = FileSystems.getDefault();
				Path path = tempfs.getPath("src/main/resources/html_res/Wait.html");
				webEngine.load("file:///"+path.toAbsolutePath().toString());
			}
		});
	}

	public void loadHomeAnim(){
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

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);


				String queryForEffDate = "SELECT DISTINCT eff_date_2 from charts where rqrmnt = '" + reqNum + "'";
				
				DBConnector db = DBConnector.getInstance();
				
				ResultSet rs = db.queryExecutor(queryForEffDate, false);
				
				while (rs.next()){
					
					CourseChartQueries c = new CourseChartQueries(reqNum, rs.getString(1));
					
					String s = c.toString();

					bw.write(s);

				}

				rs.close();
				
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
				WindowLoader.showExceptionDialog("Error occured", e);
				e.printStackTrace();
			}
		}
		else {
			throw new Exception("Please input a proper Integer!");
			//			threadSafeConsoleOutput("The input number: " + reqNum + " is either invalid numeric!");
		}
	}

	public void batchProcessChartsFromFile (String SourceFile)	{

		File f = new File(SourceFile);
		File f2 = new File(destFolder.getText());

		Date d = new Date();
		String formattedDate = convertToProperString(d.toString());
		File f_Out = new File(destFolder.getText() + "\\Output_Charts_" + formattedDate + ".txt");

		if( f2.isDirectory()) {


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
			WindowLoader.showAlertDialog("There was some problem", "Please rechech and try again");
			threadSafeConsoleOutput("Invalid directory specified!");
		}
		threadSafeConsoleOutput("Finished!..........");

		loadHomeAnim();
	}


	private void batchProcessChartsHelper(ArrayList<String> reqNos, File f){

		FileWriter fw = null;
		BufferedWriter bw = null;
		
		File f_separate = null;
		FileWriter fw_s = null;
		BufferedWriter bw_s = null;
		
		try {
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for(int j = 0; j < reqNos.size(); j++) {

			if (continueProc)	{
				CourseChartQueries c = null;
				try {
					c = new CourseChartQueries(reqNos.get(j));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String s = c.toString();

				try {

					if (separateFiles.isSelected()) {
						f_separate = new File(destFolder.getText() + "\\" + c.getRequirementNo() + ".txt");
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
								+ " : Wrote " + reqNos.get(j).toString() + " to " + f_separate.getAbsolutePath() + "\n");
					}

					else {
						bw.write(s);
						bw.write("\f");
						
						threadSafeConsoleOutput("\n" + (new Date()).toString() 
								+ " : Wrote " + reqNos.get(j).toString() + " to " + f.getAbsolutePath() + "\n");
					}
				} catch (IOException e) {

					e.printStackTrace();
				}


			}

		}

		try {
			bw.close();
		} catch (IOException e) {
			WindowLoader.showExceptionDialog("BufferWriter could not be closed", e);
			e.printStackTrace();
		}
		WindowLoader.showAlertDialog("Process finished", "The Export process is completed");
		threadSafeConsoleOutput("\n" + (new Date()).toString() 
				+ " : File written inside : " + destFolder.getText() + "\n");
		loadHomeAnim();
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

	public static String getSettings(String key) {
		Preferences prefs = Preferences.userNodeForPackage(WindowLoader.class);

		String value = prefs.get(key, null);

		return value;
	}
}
