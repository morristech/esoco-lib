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
package de.esoco.lib.net;

import de.esoco.lib.io.StreamUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/********************************************************************
 * Utility class containing static network helper methods.
 *
 * @author eso
 */
public class NetUtil
{
	//~ Enums ------------------------------------------------------------------

	/********************************************************************
	 * An enumeration of socket connection types.
	 */
	public enum SocketType { PLAIN, SSL, SELF_SIGNED_SSL }

	//~ Static fields/initializers ---------------------------------------------

	private static final String TUNNELING_CHARSET = "ASCII7";

	private static final String JAVA_VERSION =
		"Java/" + System.getProperty("java.version");

	/** A user agent string like the string used by internal Java classes. */
	public static final String JAVA_USER_AGENT =
		System.getProperty("http.agent") == null
		? JAVA_VERSION : System.getProperty("http.agent") + " " + JAVA_VERSION;

	/** Constant for the default wake-on-LAN port */
	public static final int WAKEONLAN_DEFAULT_PORT = 9;

	/** The standard encoding for URL elements (UTF-8). */
	public static String URL_ENCODING = StandardCharsets.UTF_8.name();

	/**
	 * A constant for the \r\n string that is used as a separator in HTTP
	 * requests.
	 */
	public static final String CRLF = "\r\n";

	//~ Constructors -----------------------------------------------------------

	/***************************************
	 * Private, only static use.
	 */
	private NetUtil()
	{
	}

	//~ Static methods ---------------------------------------------------------

	/***************************************
	 * Appends a path element to an URL string and adds a separating '/' if
	 * necessary.
	 *
	 * @param  sBaseUrl rUrlBuilder The string build containing the base URL
	 * @param  sPath    The URL path to append
	 *
	 * @return The resulting URL string
	 */
	public static String appendUrlPath(String sBaseUrl, String sPath)
	{
		return appendUrlPath(new StringBuilder(sBaseUrl), sPath).toString();
	}

	/***************************************
	 * Appends a path element to an URL string builder and adds a separating '/'
	 * if necessary.
	 *
	 * @param  rUrlBuilder The string build containing the base URL
	 * @param  sPath       The URL path to append
	 *
	 * @return The input URL builder to allow concatenation
	 */
	public static StringBuilder appendUrlPath(
		StringBuilder rUrlBuilder,
		String		  sPath)
	{
		if (sPath != null && sPath.length() > 0)
		{
			if (rUrlBuilder.charAt(rUrlBuilder.length() - 1) != '/')
			{
				if (sPath.charAt(0) != '/')
				{
					rUrlBuilder.append('/');
				}
			}
			else if (sPath.charAt(0) == '/')
			{
				sPath = sPath.substring(1);
			}

			rUrlBuilder.append(sPath);
		}

		return rUrlBuilder;
	}

	/***************************************
	 * Creates a new socket for the connection to a certain host and port. This
	 * method takes into account any system properties for a connection proxy.
	 *
	 * @param  sHost       The host to connect the socket to
	 * @param  nPort       The port to connect the socket to
	 * @param  eSocketType The type of socket connection to create
	 *
	 * @return A new socket that is connected to the given host and port
	 *
	 * @throws IOException If creating the socket fails
	 */
	public static Socket createSocket(String	 sHost,
									  int		 nPort,
									  SocketType eSocketType) throws IOException
	{
		boolean bSSL	   = (eSocketType != SocketType.PLAIN);
		String  sProxyHost =
			System.getProperty(bSSL ? "https.proxyHost" : "http.proxyHost");

		Socket aSocket;
		int    nProxyPort = 0;

		if (sProxyHost != null)
		{
			nProxyPort =
				Integer.parseInt(System.getProperty(bSSL ? "https.proxyPort"
														 : "http.proxyPort"));

			String sNonProxyHosts = System.getProperty("http.nonProxyHosts");

			if (sNonProxyHosts != null)
			{
				sNonProxyHosts = sNonProxyHosts.replaceAll("\\.", "\\.");
				sNonProxyHosts = sNonProxyHosts.replaceAll("\\*", ".*");

				if (Pattern.matches(sNonProxyHosts, sHost))
				{
					sProxyHost = null;
					nProxyPort = 0;
				}
			}
		}

		if (bSSL)
		{
			SSLSocketFactory rFactory;

			if (eSocketType == SocketType.SSL)
			{
				rFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			}
			else
			{
				rFactory = getTrustingSocketFactory();
			}

			if (sProxyHost != null)
			{
				aSocket =
					createTunnelingSslSocket(rFactory,
											 sHost,
											 nPort,
											 sProxyHost,
											 nProxyPort);
			}
			else
			{
				aSocket = rFactory.createSocket(sHost, nPort);
			}
		}
		else
		{
			if (sProxyHost != null)
			{
				Proxy aProxy =
					new Proxy(Proxy.Type.HTTP,
							  new InetSocketAddress(sProxyHost, nProxyPort));

				aSocket = new Socket(aProxy);

				aSocket.connect(new InetSocketAddress(sHost, nPort));
			}
			else
			{
				aSocket = SocketFactory.getDefault().createSocket(sHost, nPort);
			}
		}

		return aSocket;
	}

	/***************************************
	 * Creates a new SSL {@link Socket} that tunnels it's communication through
	 * a proxy.
	 *
	 * @param  rFactory   The factory to create the socket with
	 * @param  sHost      The host to connect the socket to
	 * @param  nPort      The port to connect the socket to
	 * @param  sProxyHost The host of the proxy to tunnel through
	 * @param  nProxyPort The port of the proxy to tunnel through
	 *
	 * @return The new tunneling SSL socket, initialized and connected to the
	 *         given host and port
	 *
	 * @throws IOException If creating or initializing the socket fails
	 */
	public static Socket createTunnelingSslSocket(SSLSocketFactory rFactory,
												  String		   sHost,
												  int			   nPort,
												  String		   sProxyHost,
												  int			   nProxyPort)
		throws IOException
	{
		Socket aTunnelSocket = new Socket(sProxyHost, nProxyPort);

		initTunneling(aTunnelSocket, sHost, nPort);

		SSLSocket aSocket =
			(SSLSocket) rFactory.createSocket(aTunnelSocket,
											  sHost,
											  nPort,
											  true);

		aSocket.startHandshake();

		return aSocket;
	}

	/***************************************
	 * Enables HTTP basic authentication for a certain {@link URLConnection}.
	 *
	 * @param rUrlConnection The URL connection
	 * @param sUserName      The user name to perform the authentication with
	 * @param sPassword      The password to perform the authentication with
	 */
	public static void enableHttpBasicAuth(URLConnection rUrlConnection,
										   String		 sUserName,
										   String		 sPassword)
	{
		String sAuth = sUserName + ":" + sPassword;

		sAuth = Base64.getEncoder().encodeToString(sAuth.getBytes());

		rUrlConnection.setRequestProperty("Authorization", "Basic " + sAuth);
	}

	/***************************************
	 * Encodes a string so that it can be used as an element in an HTTP URL by
	 * applying the method {@link URLEncoder#encode(String, String)} with the
	 * recommended default encoding UTF-8.
	 *
	 * @param  sElement sName The name of the URL parameter
	 *
	 * @return A string containing the encoded parameter assignment
	 */
	public static String encodeUrlElement(String sElement)
	{
		try
		{
			return URLEncoder.encode(sElement, URL_ENCODING);
		}
		catch (UnsupportedEncodingException e)
		{
			// UTF-8 needs to be available for URL encoding
			throw new IllegalStateException(e);
		}
	}

	/***************************************
	 * Encodes the name and value of an HTTP URL parameter by applying the
	 * method {@link #encodeUrlElement(String)} to each and concatenating them
	 * with '='.
	 *
	 * @param  sName  The name of the URL parameter
	 * @param  sValue The value of the URL parameter
	 *
	 * @return A string containing the encoded parameter assignment
	 */
	public static String encodeUrlParameter(String sName, String sValue)
	{
		return encodeUrlElement(sName) + "=" + encodeUrlElement(sValue);
	}

	/***************************************
	 * Creates a concatenated string of multiple HTTP URL parameters that have
	 * been encoded with {@link #encodeUrlParameter(String, String)}. The
	 * concatenation character is '&amp;', the encoding UTF-8.
	 *
	 * @param  rParams A mapping from HTTP URL parameter names to values
	 *
	 * @return The encoded parameters (may be empty but will never be NULL)
	 */
	public static String encodeUrlParameters(Map<String, String> rParams)
	{
		StringBuilder aParams = new StringBuilder();

		for (Entry<String, String> rParam : rParams.entrySet())
		{
			aParams.append(encodeUrlParameter(rParam.getKey(),
											  rParam.getValue()));
			aParams.append('&');
		}

		int nLength = aParams.length();

		if (nLength > 0)
		{
			aParams.setLength(nLength - 1);
		}

		return aParams.toString();
	}

	/***************************************
	 * Returns a SSL socket factory that trusts self-signed certificates.
	 * Attention: this should only be used in test scenarios, not for production
	 * code!
	 *
	 * @return The trusting socket factory
	 */
	public static final SSLSocketFactory getTrustingSocketFactory()
	{
		SSLSocketFactory aSslSocketFactory;

		try
		{
			TrustManager[] aTrustManagers =
				new TrustManager[] { new SelfSignedCertificateTrustManager() };

			SSLContext aSslContext = SSLContext.getInstance("SSL");

			aSslContext.init(null, aTrustManagers, new SecureRandom());

			aSslSocketFactory = aSslContext.getSocketFactory();
		}
		catch (Exception e)
		{
			throw new SecurityException(e);
		}

		return aSslSocketFactory;
	}

	/***************************************
	 * Initializes the tunneling of communication through a proxy.
	 *
	 * @param  rProxySocket The socket that is connected to the tunneling proxy
	 * @param  sHost        The host to connect the tunnel to
	 * @param  nPort        The port to connect the tunnel to
	 *
	 * @throws IOException If the initialization fails
	 */
	@SuppressWarnings("boxing")
	public static void initTunneling(Socket rProxySocket,
									 String sHost,
									 int    nPort) throws IOException
	{
		String sRequest =
			String.format("CONNECT %s:%d HTTP/1.0\nUser-Agent: " +
						  JAVA_USER_AGENT + "\r\n\r\n",
						  sHost,
						  nPort);

		OutputStream rOutputStream = rProxySocket.getOutputStream();
		byte[]		 aRequestBytes = sRequest.getBytes(TUNNELING_CHARSET);
		String		 sReply		   = "";

		rOutputStream.write(aRequestBytes);
		rOutputStream.flush();

		ByteArrayOutputStream aOutput = new ByteArrayOutputStream();
		byte[]				  aReply  = null;

		if (StreamUtil.readUntil(rProxySocket.getInputStream(),
								 aOutput,
								 "\r\n\r\n".getBytes(TUNNELING_CHARSET),
								 512))
		{
			aReply = aOutput.toByteArray();
		}

		if (aReply != null)
		{
			sReply = new String(aReply, TUNNELING_CHARSET);
		}

		if (!sReply.startsWith("HTTP/1.0 200"))
		{
			throw new IOException(String.format("Cannot tunnel through %s:%d. " +
												"Proxy response: %s",
												sHost,
												nPort,
												sReply));
		}
	}

	/***************************************
	 * Sends a Wake-On-LAN packet to a particular MAC address over a certain
	 * broadcast IP address on the default port.
	 *
	 * @see #wakeUp(MACAddress, InetAddress, int)
	 */
	public static void wakeUp(MACAddress rMACAddress, InetAddress rBroadcastIP)
		throws IOException
	{
		wakeUp(rMACAddress, rBroadcastIP, WAKEONLAN_DEFAULT_PORT);
	}

	/***************************************
	 * Sends a Wake-On-LAN packet to a particular MAC address over a certain
	 * broadcast IP address and port. The broadcast IP is typcially an address
	 * that ends with 255 in the same subnet where the machine to be waked up
	 * will appear, like 192.168.0.255. It is not the IP of the target machine!
	 * This is because that machine is probably inactive and therefore doesn't
	 * have an IP address until it has been waked up.
	 *
	 * @param  rMACAddress  The MAC address of the target network adapter
	 * @param  rBroadcastIP The broadcast IP number to send the packet to
	 * @param  nPort        The port to send the broadcast packet to
	 *
	 * @throws IOException If the network access fails
	 */

	public static void wakeUp(MACAddress  rMACAddress,
							  InetAddress rBroadcastIP,
							  int		  nPort) throws IOException
	{
		DatagramSocket aSocket   = new DatagramSocket();
		byte[]		   aMACBytes = rMACAddress.getBytes();
		byte[]		   aDatagram = new byte[17 * 6];

		for (int i = 0; i < 6; i++)
		{
			aDatagram[i] = (byte) 0xff;
		}

		for (int i = 6; i < aDatagram.length; i += 6)
		{
			System.arraycopy(aMACBytes, 0, aDatagram, i, 6);
		}

		DatagramPacket aPacket =
			new DatagramPacket(aDatagram,
							   aDatagram.length,
							   rBroadcastIP,
							   nPort);

		aSocket.send(aPacket);
		aSocket.close();
	}

	//~ Inner Classes ----------------------------------------------------------

	/********************************************************************
	 * An implementation of {@link TrustManager} that accepts self-signed
	 * certificates.
	 */
	public static class SelfSignedCertificateTrustManager
		implements X509TrustManager
	{
		//~ Methods ------------------------------------------------------------

		/***************************************
		 * @see X509TrustManager#checkClientTrusted(X509Certificate[], String)
		 */
		@Override
		public void checkClientTrusted(X509Certificate[] rArg0, String rArg1)
			throws CertificateException
		{
			// perform no checks to accept any certificate
		}

		/***************************************
		 * @see X509TrustManager#checkServerTrusted(X509Certificate[], String)
		 */
		@Override
		public void checkServerTrusted(X509Certificate[] rArg0, String rArg1)
			throws CertificateException
		{
			// perform no checks to accept any certificate
		}

		/***************************************
		 * @see X509TrustManager#getAcceptedIssuers()
		 */
		@Override
		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}
	}
}
