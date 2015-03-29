package bits.arcd.main;

import java.io.IOException;

import bits.arcd.view.SemChartController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class WindowLoader extends Application {

	private Stage primaryStage;
	private AnchorPane rootLayout;
	public static String IPAddress = "jdbc:mysql://"+"localhost:3306"+"/";
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
