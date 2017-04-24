package fr.polytech.smtp.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class represents an SMTP client.
 *
 * @author DELORME Loïc
 * @since 1.0.0
 */
public class SmtpClient extends Thread {

	/**
	 * The logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(SmtpClient.class.getName());

	/**
	 * The end character pattern.
	 */
	public static final Pattern END_CHARACTER_PATTERN = Pattern.compile("354 Start Mail input; end with (.*)");

	/**
	 * The socket.
	 */
	private final Socket socket;

	/**
	 * The input stream.
	 */
	private final BufferedReader inputStream;

	/**
	 * The output stream.
	 */
	private final DataOutputStream outputStream;

	/**
	 * Create an SMTP client.
	 * 
	 * @param smtpServerHostname
	 *            The SMTP server hostname.
	 * @param smtpServerPort
	 *            The SMTP server port.
	 * @throws IOException
	 *             If an error occurs.
	 */
	public SmtpClient(String smtpServerHostname, int smtpServerPort) throws IOException {
		this.socket = new Socket(smtpServerHostname, smtpServerPort);
		this.inputStream = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.outputStream = new DataOutputStream(this.socket.getOutputStream());
	}

	/**
	 * Send an email.
	 * 
	 * @param headers
	 *            The headers.
	 * @param emailContent
	 *            The email content.
	 * @return The state of the send.
	 * @throws Exception
	 *             If an error occurs.
	 */
	public String sendEmail(Map<String, String> headers, String emailContent) throws Exception {
		final String from = headers.get("From:");
		final String[] recipients = headers.get("To:").split(";");

		String command = null;
		String commandResult = null;

		// Welcoming message.
		commandResult = readCommandResult();
		LOGGER.log(Level.INFO, commandResult);
		if (!commandResult.startsWith("220")) {
			throw new Exception(commandResult);
		}

		// Send emitter server domain name.
		command = String.format("EHLO %s", from.split("@")[1]);
		writeCommand(command);
		LOGGER.log(Level.INFO, command);

		// Waiting emitter server domain name ACK.
		commandResult = readCommandResult();
		LOGGER.log(Level.INFO, commandResult);

		// Send original emitter.
		command = String.format("MAIL FROM <%s>", from);
		writeCommand(command);
		LOGGER.log(Level.INFO, command);

		// Waiting for original emitter ACK.
		commandResult = readCommandResult();
		LOGGER.log(Level.INFO, commandResult);

		// Send recipients email addresses.
		final List<String> invalidEmailAddresses = new ArrayList<String>();
		for (String recipient : recipients) {
			command = String.format("RCPT TO: <%s>", recipient);
			writeCommand(command);
			LOGGER.log(Level.INFO, command);

			commandResult = readCommandResult();
			LOGGER.log(Level.INFO, commandResult);
			if (commandResult.startsWith("550")) {
				invalidEmailAddresses.add(recipient);
			}
		}

		// Check there is at least one valid recipient
		if (invalidEmailAddresses.size() == recipients.length) {
			// Reset mail drop request.
			command = "RSET";
			writeCommand(command);
			LOGGER.log(Level.INFO, command);

			commandResult = readCommandResult();
			LOGGER.log(Level.INFO, commandResult);

			// Exit process.
			command = "QUIT";
			writeCommand(command);
			LOGGER.log(Level.INFO, command);

			commandResult = readCommandResult();
			LOGGER.log(Level.INFO, commandResult);

			throw new Exception(String.format("All recipients are invalid (%s) -> process exited and mail drop request aborted!", invalidEmailAddresses.stream().collect(Collectors.joining(" ; "))));
		}

		// Request sending data.
		command = "DATA";
		writeCommand(command);
		LOGGER.log(Level.INFO, command);

		// Recover ending character.
		commandResult = readCommandResult();
		LOGGER.log(Level.INFO, commandResult);

		final Matcher matcher = END_CHARACTER_PATTERN.matcher(commandResult);
		if (!matcher.find()) {
			throw new Exception("No ending character was found!");
		}

		// Send data.
		final String endingCharacter = matcher.group(1);
		final String computedHeaders = headers.entrySet().stream().map(entry -> String.format("%s %s", entry.getKey(), entry.getValue())).collect(Collectors.joining("\r\n"));

		final StringBuilder content = new StringBuilder();
		content.append(computedHeaders);
		content.append("\r\n");
		content.append(".\r\n");
		content.append(emailContent);
		content.append("\r\n");
		content.append(endingCharacter);
		content.append("\r\n");

		command = content.toString();
		writeCommand(command);
		LOGGER.log(Level.INFO, command);

		// Waiting for data ACK.
		commandResult = readCommandResult();
		LOGGER.log(Level.INFO, commandResult);

		// Close connection.
		command = "QUIT";
		writeCommand(command);
		LOGGER.log(Level.INFO, command);

		// Waiting for closing ACK.
		commandResult = readCommandResult();
		LOGGER.log(Level.INFO, commandResult);

		if (!invalidEmailAddresses.isEmpty()) {
			return String.format("The email was successfully sent. However, %d recipient(s) was(ere) invalid (%s)", invalidEmailAddresses.size(), invalidEmailAddresses.stream().collect(Collectors.joining(" ; ")));
		}

		return "The email was successfully sent to all recipient(s).";
	}

	/**
	 * Write a command.
	 * 
	 * @param command
	 *            The command to write.
	 * @throws IOException
	 *             If an error occurs.
	 */
	private void writeCommand(String command) throws IOException {
		this.outputStream.writeBytes(command + "\r\n");
	}

	/**
	 * Read command result.
	 * 
	 * @return The read command result.
	 * @throws IOException
	 *             If an error occurs.
	 */
	private String readCommandResult() throws IOException {
		final StringBuilder commandResult = new StringBuilder();

		String data = null;
		while ((data = this.inputStream.readLine()) != null && !data.isEmpty()) {
			commandResult.append(data);
		}

		return commandResult.toString();
	}
}