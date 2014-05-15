/**
 * 
 */
package net.ligreto.executor;

import java.io.File;
import java.util.Properties;

import net.ligreto.ResultStatus;
import net.ligreto.exceptions.LigretoException;
import net.ligreto.parser.nodes.EmailNode;
import net.ligreto.parser.nodes.LigretoNode;
import net.ligreto.util.MiscUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Executes the email send operation on the specified report node.
 * 
 * @author Julius Stroffek
 *
 */
public class EmailExecutor extends Executor {

	/** The logger instance for the class. */
	private static Log log = LogFactory.getLog(EmailExecutor.class);
	
	/**
	 * Default constructor.
	 */
	public EmailExecutor() {
	}

	@Override
	public ResultStatus execute() throws LigretoException {
		return null;
	}

	/**
	 * Sends the email according the specified email settings.
	 * 
	 * @param email parsed email node
	 * @param result report result
	 * @param reportFile generated report file to be attached
	 * @throws LigretoException 
	 * @throws MessagingException 
	 * @throws AddressException 
	 */
	public void execute(EmailNode email, ResultStatus result, File reportFile) throws LigretoException {
		boolean send = false;
		switch (email.getSendCondition()) {
		case always:
			send = true;
			break;
		case accepted:
			send = result.isAccepted();
			break;
		case rejected:
			send = !result.isAccepted();
			break;
		case empty:
			send = result.getTotalRowCount() == 0;
			break;
		case nonempty:
			send = result.getTotalRowCount() > 0;
			break;
		}
		if (!send) {
			log.info(String.format("Skipping sending the email message '%2$s' to '%1$s'.", email.getTo(), email.getSubject()));
			return;
		}

		log.info(String.format("Sending the email message '%2$s' to '%1$s'.", email.getTo(), email.getSubject()));
		try {
			Session smtpSession = createSmtpSession(email.getLigretoNode());
			MimeMessage message = new MimeMessage(smtpSession);
			String emailFrom = email.getLigretoNode().substituteParams(email.getFrom());
			if (MiscUtils.isEmpty(emailFrom)) {
				emailFrom = email.getLigretoNode().getLigretoParameters().getSmtpEmailFrom();
			}
			message.setFrom(new InternetAddress(emailFrom));
			
			// Setup all the TO recipients
			String toString = email.getLigretoNode().substituteParams(email.getTo());
			if (MiscUtils.isNotEmpty(toString)) {
				String toAddresses[] = toString.split(";");
				for (String toAddress: toAddresses) {
					message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
				}
			}
			
			// Setup all the CC recipients
			String ccString = email.getLigretoNode().substituteParams(email.getCc());
			if (MiscUtils.isNotEmpty(ccString)) {
				String ccAddresses[] = ccString.split(";");
				for (String ccAddress: ccAddresses) {
					message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccAddress));
				}
			}
			
			// Setup all the BCC recipients
			String bccString = email.getLigretoNode().substituteParams(email.getBcc());
			if (MiscUtils.isNotEmpty(bccString)) {
				String bccAddresses[] = bccString.split(";");
				for (String bccAddress: bccAddresses) {
					message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccAddress));
				}
			}
			
			// Set the message content
			message.setSubject(email.getLigretoNode().substituteParams(email.getSubject()), "utf-8");

			// Setup the message with text
			Multipart multipart = new MimeMultipart();
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(email.getLigretoNode().substituteParams(email.getBody()), "text/html; charset=utf-8");
			multipart.addBodyPart(messageBodyPart);
			
			// Add the attachment
			if (email.isAttach()) {
				messageBodyPart = new MimeBodyPart();
		        DataSource source = new FileDataSource(reportFile.getAbsolutePath());
		        messageBodyPart.setDataHandler(new DataHandler(source));
		        messageBodyPart.setFileName(reportFile.getName());
		        multipart.addBodyPart(messageBodyPart);
		        message.setContent(multipart);
			}
			
			// Sent the email out
			Transport.send(message);
		} catch (Exception e) {
			throw new LigretoException("Failed to send email to: " + email.getTo(), e);
		}
	}
	
	/**
	 * Creates the SMTP session based on the settings present in the ligreto node.
	 * 
	 * @param ligretoNode
	 * @return created SMTP session
	 */
	private Session createSmtpSession(LigretoNode ligretoNode) {
		final String username = ligretoNode.substituteParams(ligretoNode.getLigretoParameters().getSmtpUser());
		final String password = ligretoNode.substituteParams(ligretoNode.getLigretoParameters().getSmtpPasswd());
		String smtpPort = ligretoNode.substituteParams(ligretoNode.getLigretoParameters().getSmtpPort());
		String smtpSsl = ligretoNode.substituteParams(ligretoNode.getLigretoParameters().getSmtpSsl());
		
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		if (MiscUtils.isNotEmpty(ligretoNode.getLigretoParameters().getSmtpSsl())) {
			if ("tls".equalsIgnoreCase(smtpSsl)) {
				if (MiscUtils.isEmpty(smtpPort)) {
					smtpPort = "587";
				}
				props.put("mail.smtp.starttls.enable", "true");
			} else if ("ssl".equalsIgnoreCase(smtpSsl)) {
				if (MiscUtils.isEmpty(smtpPort)) {
					smtpPort = "465";
				}
				props.put("mail.smtp.socketFactory.port", smtpPort);
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			} else if ("false".equalsIgnoreCase(smtpSsl)) {
				// This is fine, we will just do nothing
			} else if (MiscUtils.isNotEmpty(smtpSsl)) {
				throw new IllegalArgumentException("Wrong value for SSL settings in ligreto parameters: " + smtpSsl);
			}

		}
		if (MiscUtils.isEmpty(smtpPort)) {
			smtpPort = "25";
		}
		props.put("mail.smtp.host", ligretoNode.substituteParams(ligretoNode.getLigretoParameters().getSmtpHost()));
		props.put("mail.smtp.port", smtpPort);

		Session session = null;
		
		if (MiscUtils.isNotEmpty(username)) {
			session = Session.getInstance(props,
					new javax.mail.Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username,
									password);
						}
					});
		} else {
			session = Session.getInstance(props);
		}

		return session;
	}
}
