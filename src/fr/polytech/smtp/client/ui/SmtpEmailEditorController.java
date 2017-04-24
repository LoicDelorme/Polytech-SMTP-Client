package fr.polytech.smtp.client.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import fr.polytech.smtp.client.SmtpClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * This class represents an SMTP email editor controller.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class SmtpEmailEditorController implements Initializable {

	/**
	 * The SMTP server hostname input text field.
	 */
	@FXML
	private TextField smtpServerHostnameInputTextField;

	/**
	 * The SMTP server port input text field.
	 */
	@FXML
	private TextField smtpServerPortInputTextField;

	/**
	 * The email FROM input text field.
	 */
	@FXML
	private TextField emailFromInputTextField;

	/**
	 * The email TO input text field.
	 */
	@FXML
	private TextField emailToInputTextField;

	/**
	 * The email SUBJECT input text field.
	 */
	@FXML
	private TextField emailSubjectInputTextField;

	/**
	 * The email DATE input text field.
	 */
	@FXML
	private TextField emailDateInputTextField;

	/**
	 * The email CONTENT input text area.
	 */
	@FXML
	private TextArea emailContentInputTextArea;

	/**
	 * The email send button.
	 */
	@FXML
	private Button emailSendButton;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.emailSendButton.disableProperty().bind(this.smtpServerHostnameInputTextField.textProperty().isEmpty().or(this.smtpServerPortInputTextField.textProperty().isEmpty()).or(this.emailFromInputTextField.textProperty().isEmpty()).or(this.emailToInputTextField.textProperty().isEmpty()).or(this.emailSubjectInputTextField.textProperty().isEmpty()).or(this.emailDateInputTextField.textProperty().isEmpty()).or(this.emailContentInputTextArea.textProperty().isEmpty()));
		this.emailSendButton.setOnAction(event -> onSendAction());
	}

	/**
	 * The on send action.
	 */
	private void onSendAction() {
		Alert alert = null;
		try {
			final String smtpServerHostname = this.smtpServerHostnameInputTextField.getText().trim();
			final int smtpServerPort = Integer.parseInt(this.smtpServerPortInputTextField.getText().trim());
			final Map<String, String> headers = generateHeaders();
			final String emailContent = this.emailContentInputTextArea.getText().trim();

			final String result = new SmtpClient(smtpServerHostname, smtpServerPort).sendEmail(headers, emailContent);

			alert = generateAlert(AlertType.INFORMATION, "Success", "Email sent!", result);
		} catch (Exception e) {
			alert = generateAlert(AlertType.ERROR, "Error", "An exception occured!", e.getMessage());
		}

		alert.showAndWait();
	}

	/**
	 * Generate the headers.
	 * 
	 * @return The generated headers.
	 */
	private Map<String, String> generateHeaders() {
		final Map<String, String> headers = new HashMap<String, String>();
		headers.put("From:", this.emailFromInputTextField.getText().trim());
		headers.put("To:", this.emailToInputTextField.getText().trim());
		headers.put("Subject:", this.emailSubjectInputTextField.getText().trim());
		headers.put("Date:", this.emailDateInputTextField.getText().trim());

		return headers;
	}

	/**
	 * Generate an alert.
	 * 
	 * @param alertType
	 *            The alert type.
	 * @param title
	 *            The title.
	 * @param headerText
	 *            The header text.
	 * @param contentText
	 *            The content text.
	 * @return The generated alert.
	 */
	private Alert generateAlert(AlertType alertType, String title, String headerText, String contentText) {
		final Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);

		return alert;
	}
}