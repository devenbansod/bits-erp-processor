package bits.arcd.main;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import bits.arcd.view.SemChartController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class WindowLoader extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	public static String IPAddress = "jdbc:mysql://"+"localhost:8888"+"/";
	public static String usernm = "ghazi";
	public static String passwd = "erp321";

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("ARCD ERP Data Processor");
		intializeRootLayout();
	}

	/**
	 * Initializes the root layout.
	 */
	public void intializeRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(WindowLoader.class.getResource("../view/ErpMain.fxml"));
			rootLayout = (AnchorPane) loader.load();
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout, 1366, 700);
			primaryStage.setScene(scene);

			// Give the controller(s) access to the main app.
			SemChartController sem_chart = loader.getController();
			sem_chart.setMainApp(this);
			// Give the controller(s) access to the main app.
			//ELSheetController el_sheet = loader.getController();
			//el_sheet.setMainApp(this);

			// Show the window
			primaryStage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void showAlertDialog (String messageHeading, String friendlyMessage)	{
		Platform.runLater(new Runnable() {
			public void run() {
				Alert alert = new Alert(AlertType.WARNING);
				alert.setTitle("Warning!");
				alert.setHeaderText(messageHeading);
				alert.setContentText(friendlyMessage);
				alert.showAndWait();
			}
		});
	}
	
	public static void showExceptionDialog	(String friendlyMessage, Exception e)	{
		Platform.runLater(new Runnable() {
			public void run() {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("An Exception Occured");
				alert.setHeaderText("The process encountered an exception");
				alert.setContentText(friendlyMessage);

				// Create expandable Exception.
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String exceptionText = sw.toString();

				Label label = new Label("Stack Trace of the Exception: ");

				TextArea textArea = new TextArea(exceptionText);
				textArea.setEditable(false);
				textArea.setWrapText(true);

				textArea.setMaxWidth(Double.MAX_VALUE);
				textArea.setMaxHeight(Double.MAX_VALUE);
				GridPane.setVgrow(textArea, Priority.ALWAYS);
				GridPane.setHgrow(textArea, Priority.ALWAYS);

				GridPane expContent = new GridPane();
				expContent.setMaxWidth(Double.MAX_VALUE);
				expContent.add(label, 0, 0);
				expContent.add(textArea, 0, 1);

				// Set expandable Exception into the dialog pane.
				alert.getDialogPane().setExpandableContent(expContent);
				alert.showAndWait();
			}
		});
	}

	/**
	 * Returns the main stage.
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
