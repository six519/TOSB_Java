package com.ferdinandsilva.townofsalem;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.swt.widgets.Display;

public class TOSClient implements Runnable {
	
	class Friend {
		public boolean status = false;
		public String username = "";
		public String id = "";
		
		Friend(String username, boolean status) {
			this.status = status;
			this.username = username;
		}
		
		Friend(String username, boolean status, String id) {
			this.status = status;
			this.username = username;
			this.id = id;
		}
	}
	
	private TOSBot tosBot;
	private Thread thread;
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private boolean loggedIn = false;
	public boolean startProcess = false;
	private HashMap<String, Friend> friendsList = new HashMap<String, Friend>();

	private final AtomicBoolean running = new AtomicBoolean(false);
	private static final String TOS_HOST = "live4.tos.blankmediagames.com";
	private static final int TOS_PORT = 3600;
	private static final String TOS_BUILD = "11704";
	private static final String TOS_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAziIxzMIz7ZX4KG5317Sm\nVeCt9SYIe/+qL3hqP5NUX0i1iTmD7x9hFR8YoOHdAqdCJ3dxi3npkIsO6Eoz0l3e\nH7R99DX16vbnBCyvA3Hkb1B/0nBwOe6mCq73vBdRgfHU8TOF9KtUOx5CVqR50U7M\ntKqqc6M19OZXZuZSDlGLfiboY99YV2uH3dXysFhzexCZWpmA443eV5ismvj3Nyxv\nRk/4ushZV50vrDjYiInNEj4ICbTNXQULFs6Aahmt6qmibEC6bRl0S4TZRtzuk2a3\nTpinLJooDTt9s5BvRRh8DLFZWrkWojgrzS0sSNcNzPAXYFyTOYEovWWKW7TgUYfA\ndwIDAQAB\n-----END PUBLIC KEY-----";
	
	public TOSClient(TOSBot bot) {
		tosBot = bot;
	}
	
	private String encryptPassword(String password) {
		String ret = "";
		PublicKey publicKey = null;
		
		String publicKeyString = TOS_PUBLIC_KEY.replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");
		
			
		X509EncodedKeySpec keySpec;
		try {
			keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString.getBytes("UTF-8")));
			
			try {
				KeyFactory keyFactory = KeyFactory.getInstance("RSA");
				publicKey = keyFactory.generatePublic(keySpec);
				
				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				
				byte[] encryptedString = cipher.doFinal(password.getBytes());
				ret = new String(Base64.getEncoder().encode(encryptedString));
					
			} catch (NoSuchAlgorithmException e) {
			} catch (InvalidKeySpecException e) {
			} catch (NoSuchPaddingException e) {
			} catch (InvalidKeyException e) {
			} catch (IllegalBlockSizeException e) {
			} catch (BadPaddingException e) {
			}
			
		} catch (UnsupportedEncodingException e1) {
		}
		
		return ret;
	}
	
	public void start(String username, String password){
		tosBot.setDisplayText("Connecting to: <b><span style=\"color: green;\">" + TOS_HOST + ":" + TOS_PORT + "</span></b>...");

		try {
			socket = new Socket(TOS_HOST, TOS_PORT);
			output = new DataOutputStream(socket.getOutputStream());
			input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			
			String initMessage = Character.toString((char)2) + Character.toString((char)2) + Character.toString((char)2) + Character.toString((char)1) + TOS_BUILD + Character.toString((char)30) + username + Character.toString((char)30) + encryptPassword(password) + Character.toString((char)0);
			output.write(initMessage.getBytes());
			
			startProcess = false;
			thread = new Thread(this);
			thread.start();
		} catch (UnknownHostException e) {
			tosBot.setDisplayText("<b><span style=\"color: red;\">Unable to connect to server...</span></b>");
			stop();
		} catch (IOException e) {
			tosBot.setDisplayText("<b><span style=\"color: red;\">Unexpected I/O error occurred...</span></b>");
			stop();
		}
		
	}
	
	public boolean isRunning() {
		if(running.get()) {
			return true;
		}
		return false;
	}
	
	public void stop() {
		loggedIn = false;
		running.set(false);
	}
	
	public void setDisplayText(String msg) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				tosBot.setDisplayText(msg);
			}
		});	
	}
	
	public int hex2Decimal(String s) {
		String digits = "0123456789ABCDEF";
		s = s.toUpperCase();
		int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
		return val;
	}
	
	public void listFriends() {
		Map<String, Friend> map = friendsList;
		StringBuilder sb = new StringBuilder();
		
		setDisplayText("<u style=\"color:orange;\"><h3>Your friends are:</h3></u>");
		sb.append("<ul>");
		for(Map.Entry<String, Friend> entry : map.entrySet()) {
			String color = "";
			String stat = "";
			
			if(entry.getValue().status) {
				color = "teal";
				stat = "Online";
			} else {
				color = "tomato";
				stat = "Offline";
			}
			
			sb.append("<li><strong><span style=\"color:violet;\">" + entry.getValue().username + "</span></strong> with ID: <strong><span style=\"color:purple;\">" + entry.getKey() + "</span></strong> and status: <strong><span style=\"color:" + color + ";\">" + stat + "</span></strong></li>");
		}
		sb.append("</ul>");
		setDisplayText(sb.toString());
	}

	@Override
	public void run() {
		running.set(true);
		StringBuilder sb = new StringBuilder();
		int nullCount = 0;
		boolean gotFriendsList = false;
		ArrayList<Friend> friendsRequest = new ArrayList<Friend>();
		friendsList.clear();
		
		while(running.get()) {
			try {
				byte b = input.readByte();
				int intMessage = (int)b;
				//(int)b -30 instead of 226

				if (intMessage == -30) {
					if(!loggedIn) {
						setDisplayText("<b><span style=\"color: red;\">Invalid login...</span></b>");
					} else {
						setDisplayText("<b><span style=\"color: red;\">Disconnected from server...</span></b>");
					}
					stop();
				}
				
				if(intMessage == 1 && !loggedIn) {
					loggedIn = true;
					setDisplayText("<b><span style=\"color: green;\">You are connected to the server...</span></b>...");
				}
				
				if(loggedIn && intMessage != 0) {
					sb.append(Character.toString((char)intMessage));
				}
				
				if(loggedIn && intMessage == 0) {
					//System.out.println(sb.toString());
					nullCount += 1;
					
					if(nullCount > 12 && !gotFriendsList) {
						
						if(sb.toString().contains(Character.toString((char)hex2Decimal("01"))) || sb.toString().contains(Character.toString((char)hex2Decimal("03")))) {
							//friends list
							System.out.println(sb.toString());
							String[] splittedInfos = sb.toString().split(",");
							int countInfo = 0;
							String currentUser = "";
							String currentID = "";
							
							for(String splittedInfo : splittedInfos) {
								if (countInfo == 0) {
									currentUser = splittedInfo.replace("1*", "").replace(Character.toString((char)hex2Decimal("14")), "");
									countInfo += 1;
								} else if(countInfo == 1) {
									currentID = splittedInfo;
									countInfo += 1;
								} else {
									boolean stat = false;
									
									if(splittedInfo.equals(Character.toString((char)hex2Decimal("03")))) {
										stat = true;
									}
									
									friendsList.put(currentID, new Friend(currentUser, stat));
									countInfo = 0;
									currentID = "";
									currentUser = "";
								}
							}
							
							//show friends on the window
							listFriends();
							gotFriendsList = true;
							
						} else {
							//friends request
						}
						
					} else if(gotFriendsList) {
						
					}
					
					sb.setLength(0);
				}
				
			} catch (IOException e) {
				setDisplayText("<b><span style=\"color: red;\">An error occurred while reading data...</span></b>");
				stop();
			}
		}
	}	
	
}
