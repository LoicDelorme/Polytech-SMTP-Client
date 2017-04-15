package fr.polytech.smtp.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class represents the launcher of the application.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class Launcher extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/polytech/smtp/client/ui/SmtpEmailEditor.fxml"));
		primaryStage.setScene(new Scene(loader.load()));
		primaryStage.show();
	}

	/**
	 * The entry of the application.
	 * 
	 * @param args
	 *            The arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}