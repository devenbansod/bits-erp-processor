package bits.arcd.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.prefs.Preferences;

import bits.arcd.main.WindowLoader;
import bits.arcd.model.AcadCounselBoard;
import bits.arcd.model.DBConnector;
import bits.arcd.model.GraduationRequirements;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class StudentReqsController {
	@FXML public TextField inpSemNum;
	@FXML public TextField IdNoFilter;

	@FXML public TextField destFolder;
	@FXML public Button browseDestFolderButton;
	@FXML public Button generateACBButton;
	@FXML public Button generateBLButton;
	@FXML public Button generateGRButton;
	@FXML public Button generateLikelyGRButton;
	@FXML public TextArea consoleOutput;

	@FXML public Button stopButton;
	private boolean continueProc = true;

	final String userhome = System.getProperty("user.home");
	//private WebEngine webEngine;



	public StudentReqsController()	{

		

	}

	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded. Called after constructor
	 */

	@FXML
	private void initialize() {
		destFolder.setText(getSettings("destFolderRep"));
		browseDestFolderButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				DirectoryChooser chooser = new DirectoryChooser();

				chooser.setTitle("Select the Folder for Output");
				File defaultDirectory = new File("C://");
				chooser.setInitialDirectory(defaultDirectory);				


				File selectedFile = chooser.showDialog(new Stage());

				if (selectedFile != null)	{
					destFolder.setText(selectedFile.getAbsolutePath());
					setSettings("destFolderRep", selectedFile.getAbsolutePath());
				}
				else {
					threadSafeConsoleOutputRep("Please choose a valid folder!!");
				}
			}
		});


		generateACBButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (IdNoFilter.getText().equals(""))
					IdNoFilter.setText("%");
				
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
							while(rs.next() && continueProc){
								AcadCounselBoard a = new AcadCounselBoard(rs.getString(2), 
										Integer.parseInt(inpSemNum.getText()));
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
				if (IdNoFilter.getText().equals(""))
					IdNoFilter.setText("%");
				
				threadSafeConsoleOutputRep("\nProcessing .... Please wait .....\nGenerating BL Report\n");
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
							while(rs.next() && continueProc){
								AcadCounselBoard a = new AcadCounselBoard(rs.getString(2), 
										Integer.parseInt(inpSemNum.getText()));
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
		
		

		generateGRButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (IdNoFilter.getText().equals(""))
					IdNoFilter.setText("%");
				
				threadSafeConsoleOutputRep("\nProcessing .... Please wait .....\nGenerating 'Graduated Students' Report\n");
				Thread thread = new Thread(){
					public void run(){				
						DBConnector db = DBConnector.getInstance();
						if (IdNoFilter.getText() == "")
							IdNoFilter.setText("%");
						
						String query = "SeLECT * FROM students where campus_id like '" + IdNoFilter.getText() + "'";
						Date d = new Date();
						String formattedDate = convertToProperString(d.toString());
						final File f_Out = new File(destFolder.getText() + "\\GR_" + formattedDate + ".csv");
						final File f_Out_not = new File(destFolder.getText() + "\\NOT_Likely_GR_" + formattedDate + ".csv");
						FileWriter fw = null;
						BufferedWriter bw = null;
						
						FileWriter fw_Not = null;
						BufferedWriter bw_not = null;
						try {
							fw = new FileWriter(f_Out.getAbsoluteFile(), true);
							bw = new BufferedWriter(fw);
							fw_Not= new FileWriter(f_Out_not.getAbsoluteFile(), true);
							bw_not = new BufferedWriter(fw_Not);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ResultSet rs = db.queryExecutor(query, false);
						try {
							while(rs.next() && continueProc){
								GraduationRequirements g = new GraduationRequirements(rs.getString(2));
								if (g.getGraduationStatus().equalsIgnoreCase("G")) {

									bw.write(g.getELSheet().getSystemId() + ", " + 
											g.getELSheet().getStudentId() + ", " + 
											g.getELSheet().getStudentName() + ",\n");
									threadSafeConsoleOutputRep("\n" + (new Date()).toString() 
											+ " : Wrote " + rs.getString(2) + "\n");
								}
								else if (g.getGraduationStatus().equalsIgnoreCase("I") 
										&& g.getGraduationStatus().equalsIgnoreCase("L")){
									bw_not.write(g.getELSheet().getSystemId() + ", " + 
											g.getELSheet().getStudentId() + ", " + 
											g.getELSheet().getStudentName() + ",\n");

									threadSafeConsoleOutputRep("\n..");
								}
							}
							bw.close();
							bw_not.close();
							rs.close();
							fw.close();
							fw_Not.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						threadSafeConsoleOutputRep("Exported the GR Report to : \n" + f_Out.getAbsolutePath());
					}
				};
				thread.start();
			}
		});
		
		
		generateLikelyGRButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (IdNoFilter.getText().equals(""))
					IdNoFilter.setText("%");
				
				threadSafeConsoleOutputRep("Processing .... Please wait .....\nGenerating 'Likely Graduate' Report\n");
				Thread thread = new Thread(){
					public void run(){				
						DBConnector db = DBConnector.getInstance();
						
						if (IdNoFilter.getText() == "")
							IdNoFilter.setText("%");
						
						String query = "SeLECT * FROM students where campus_id like '" + IdNoFilter.getText() + "'";
						Date d = new Date();
						String formattedDate = convertToProperString(d.toString());
						final File f_Out = new File(destFolder.getText() + "\\Likely_GR_" + formattedDate + ".csv");
						final File f_Out_not = new File(destFolder.getText() + "\\NOT_Likely_GR_" + formattedDate + ".csv");
						FileWriter fw = null;
						FileWriter fw_Not = null;
						BufferedWriter bw = null;
						BufferedWriter bw_not = null;
						try {
							fw = new FileWriter(f_Out.getAbsoluteFile(), true);
							bw = new BufferedWriter(fw);
							fw_Not= new FileWriter(f_Out_not.getAbsoluteFile(), true);
							bw_not = new BufferedWriter(fw_Not);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						ResultSet rs = db.queryExecutor(query, false);
						try {
							while(rs.next() && continueProc){
								GraduationRequirements g = new GraduationRequirements(rs.getString(2));
								if (g.getGraduationStatus().equalsIgnoreCase("L")) {

									bw.write(g.getELSheet().getSystemId() + ", " + 
											g.getELSheet().getStudentId() + ", " + 
											g.getELSheet().getStudentName() + ",\n");
									threadSafeConsoleOutputRep("\n" + (new Date()).toString() 
											+ " : Wrote " + rs.getString(2) + "\n");
								}
								else if (g.getGraduationStatus().equalsIgnoreCase("I")){
									bw_not.write(g.getELSheet().getSystemId() + ", " + 
											g.getELSheet().getStudentId() + ", " + 
											g.getELSheet().getStudentName() + ",\n");

									threadSafeConsoleOutputRep("\n..");
								}
								else {
									threadSafeConsoleOutputRep("\n..");
								}
							}
							bw.close();
							bw_not.close();
							rs.close();
							fw.close();
							fw_Not.close();
						} catch (SQLException e1) {
							
							e1.printStackTrace();
						} catch (IOException e1) {

							e1.printStackTrace();
						}
						threadSafeConsoleOutputRep("Exported the GR Report to : \n" + f_Out.getAbsolutePath());
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

	private String convertToProperString(String s) {
		s = s.replace(" ", "_");
		s = s.replace("-", "_");
		s = s.replace(":", "_");
		return s;
	}

	private String getSettings(String key) {
		Preferences prefs = Preferences.userNodeForPackage(WindowLoader.class);
		String value = prefs.get(key, null);
		return value;
	}

	public void threadSafeConsoleOutputRep(final String output)	{
		Platform.runLater(new Runnable() {
			public void run() {
				consoleOutput.setText(consoleOutput.getText() + "" + output);
				consoleOutput.end();
			}
		});
	}
	



	private void setSettings(String key, String newValue){
		Preferences prefs = Preferences.userNodeForPackage(WindowLoader.class);
		prefs.put(key, newValue);

	}

}
