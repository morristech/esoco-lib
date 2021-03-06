//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
// This file is a part of the 'esoco-lib' project.
// Copyright 2018 Elmar Sonnenschein, esoco GmbH, Flensburg, Germany
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//	  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package de.esoco.lib.comm.smtp;

import de.esoco.lib.comm.Endpoint;
import de.esoco.lib.comm.EndpointFunction;
import de.esoco.lib.comm.SmtpEndpoint;
import de.esoco.lib.logging.LogAspect;
import de.esoco.lib.logging.LogLevel;
import de.esoco.lib.logging.LogRecord;

import java.util.Collection;
import java.util.Date;

import org.obrel.core.ObjectRelations;

import static de.esoco.lib.comm.CommunicationRelationTypes.ENDPOINT_ADDRESS;
import static de.esoco.lib.comm.CommunicationRelationTypes.PASSWORD;
import static de.esoco.lib.comm.CommunicationRelationTypes.USER_NAME;
import static de.esoco.lib.comm.smtp.Email.email;


/********************************************************************
 * A log aspect that sends log messages as emails via an {@link SmtpEndpoint}.
 * By default the minimum log level (see {@link LogAspect#MIN_LOG_LEVEL}) will
 * be set to {@link LogLevel#ERROR} and loosening this constraint that should be
 * handled with caution because depending on the log volume that could put the
 * target mail server under heavy load.
 *
 * @author eso
 */
public class MailLogging extends LogAspect<Email>
{
	//~ Instance fields --------------------------------------------------------

	private Endpoint aMailServer;
	private Email    aEmailTemplate = email();

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Creates a new instance and sets the minimum log level to ERROR.
	 *
	 * @param sSmtpEndpointUrl The endpoint URL of the SMTP server
	 */
	public MailLogging(String sSmtpEndpointUrl)
	{
		set(MIN_LOG_LEVEL, LogLevel.ERROR);

		aMailServer = Endpoint.at(sSmtpEndpointUrl);
	}

	//~ Methods ----------------------------------------------------------------

	/***************************************
	 * Sets the sender of the logging emails.
	 *
	 * @param  sSender The email sender
	 *
	 * @return This instance for fluent invocation
	 */
	public MailLogging from(String sSender)
	{
		aEmailTemplate.from(sSender);

		return this;
	}

	/***************************************
	 * Sets the authentication credentials.
	 *
	 * @param  sUserName The user name for authentication with the mail server
	 * @param  sPassword The authentication password
	 *
	 * @return This instance for fluent invocation
	 */
	public MailLogging loginAs(String sUserName, String sPassword)
	{
		aMailServer.set(USER_NAME, sUserName);
		aMailServer.set(PASSWORD, sPassword);

		return this;
	}

	/***************************************
	 * Sets the receiver of the logging emails.
	 *
	 * @param  sReceiver The email receiver
	 *
	 * @return This instance for fluent invocation
	 */
	public MailLogging to(String sReceiver)
	{
		aEmailTemplate.to(sReceiver);

		return this;
	}

	/***************************************
	 * Adds the endpoint address to the string representation generated by the
	 * superclass.
	 *
	 * @return The string representation for this instance
	 */
	@Override
	public String toString()
	{
		return String.format("%s(%s)",
							 super.toString(),
							 aMailServer.get(ENDPOINT_ADDRESS));
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected Email createLogObject(LogRecord rLogRecord)
	{
		String sMessage = rLogRecord.format(get(MIN_STACK_LOG_LEVEL));
		Email  aEmail   = new Email();

		ObjectRelations.copyRelations(aEmailTemplate, aEmail, false);

		aEmail.subject(String.format("[%1$s]%2$tF %2$tT: %3$s",
									 rLogRecord.getLevel(),
									 new Date(rLogRecord.getTime()),
									 rLogRecord.getMessage()));
		aEmail.message(sMessage);

		return aEmail;
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected String getLogInitMessage()
	{
		return "Starting logging to mail server at " +
			   aMailServer.get(ENDPOINT_ADDRESS);
	}

	/***************************************
	 * {@inheritDoc}
	 */
	@Override
	protected void processLogObjects(Collection<Email> rLogEmails)
		throws Exception
	{
		EndpointFunction<Email, Void> fSendMail =
			SmtpEndpoint.sendMail().on(aMailServer);

		for (Email rEmail : rLogEmails)
		{
			fSendMail.send(rEmail);
		}
	}
}
