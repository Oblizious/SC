package myWhats;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Resources.MessageFlags;

public class MyWhatsStub {
	
	private static Socket socket = null;
	private static ObjectInputStream objInStream = null;
	private static ObjectOutputStream objOutStream = null;
	private static String result = null;
	
	public static String createUser(String localUser, String password, String serverAddress){
		startConnection(localUser, password, serverAddress);
		
		try {
			login(localUser, password);
			objOutStream.writeObject(MessageFlags.END_MESSAGE);
			result = (String)objInStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    public static String sendMessage(String localUser, String password, String serverAddress, String contact, String text) {
    	startConnection(localUser, password, serverAddress);
    
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.M_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(text);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    public static String sendFile(String localUser, String password, String serverAddress, String contact, String filename) {
    	startConnection(localUser, password, serverAddress);
    	File file = new File(filename);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.F_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(file);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    public static String getMostRecentCommunications(String localUser, String password, String serverAddress) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.R0_MESSAGE);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }

    public static String getAllContactCommunications(String localUser, String password, String serverAddress, String contact) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.R1_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    public static File getContactFile(String localUser, String password, String serverAddress, String contact, String filename) {
    	startConnection(localUser, password, serverAddress);
    	File file = null;
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.R2_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(filename);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		file = (File)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return file;
    }
    
    public static String addToGroup(String localUser, String password, String serverAddress, String contact, String group) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.A_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(group);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
   }
    
    public static String removeFromGroup(String localUser, String password, String serverAddress, String contact, String group){
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.D_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(group);
    		objOutStream.writeObject(MessageFlags.END_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
   }

    private static void startConnection(String localUser, String password, String serverAddress) {
    	String[] aux = serverAddress.split(":");
    
		try {
			socket = new Socket(aux[0], Integer.parseInt(aux[1]));
			objInStream = new ObjectInputStream(socket.getInputStream());
			objOutStream = new ObjectOutputStream(socket.getOutputStream());
			
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
    }

    private static void login(String localUser, String password) throws IOException {
		objOutStream.writeObject(localUser);
		objOutStream.writeObject(password);
    }
    
	private static void closeConnection(Socket socket, ObjectInputStream in, ObjectOutputStream out) {
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
