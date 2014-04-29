/**
 * 
 */
package net.ligreto.parser.nodes;

import net.ligreto.exceptions.InvalidValueException;
import net.ligreto.util.MiscUtils;

/**
 * @author julo
 *
 */
public class EmailNode extends Node {

	public static enum SendCondition {
		always,
		success,
		failure
	}
	
	private String from;
	private String to;
	private String cc;
	private String bcc;
	private String subject;
	private String body;
	private boolean attach = true;
	private SendCondition sendCondition = SendCondition.always;
	
	/**
	 * @param aLigretoNode
	 */
	public EmailNode(LigretoNode aLigretoNode) {
		super(aLigretoNode);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return bcc;
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public boolean isAttach() {
		return attach;
	}

	public void setAttach(String attach) throws InvalidValueException {
		this.attach = MiscUtils.parseBoolean(attach);
	}

	public SendCondition getSendCondition() {
		return sendCondition;
	}

	public void setSendCondition(String sendCondition) {
		try {
			this.sendCondition = Enum.valueOf(SendCondition.class, sendCondition);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Wrong value specified in 'when' attribute: " + sendCondition);
		}
	}
}
