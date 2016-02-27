package myWhats;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class MyWhatsStub {
	
	private static Socket socket = null;
	private static ObjectInputStream in = null;
	private static ObjectOutputStream out = null;
	private static String result = null;
	
	public static String createUser(String localUser, String password, String serverAddress){
		startConnection(localUser, password, serverAddress);
		
		try {
			login(localUser, password);
			out.writeObject(Constants.END_MESSAGE);
			result = (String)in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		closeConnection(socket, in, out);
		return result;
    }
    
    public static String sendMessage(String localUser, String password, String serverAddress, String contact, String text) {
    	startConnection(localUser, password, serverAddress);
    
    	try {
    		login(localUser, password);
    		out.writeObject(Constants.M_MESSAGE);
    		out.writeObject(contact);
    		out.writeObject(text);
    		out.writeObject(Constants.END_MESSAGE);
    		result = (String)in.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, in, out);
		return result;
    }
    
    public static String sendFile(String localUser, String password, String serverAddress, String contact, String filename) {
    	startConnection(localUser, password, serverAddress);
        
    	try {
    		login(localUser, password);
    		out.writeObject(Constants.M_MESSAGE);
    		out.writeObject(contact);
    		out.writeObject(text);
    		out.writeObject(Constants.END_MESSAGE);
    		result = (String)in.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, in, out);
		return result;
    }
    
    public static boolean enviarMensagem(String nome, String password, String server, String contacto, String messagem) {


    }

    public static String comunicacoesMaisRecentes(String nome, String password, String server) {
    
    }

    public static String todasComunicacoes(String nome, String password, String server, String contacto) {
    
    }
    
    public static File buscaFicheiro(String nome, String password, String server, String contacto, String ficheiro) {
    
    }
    
    public static boolean adicionarAoGrupo(String nome, String password, String server, String utilizador, String grupo) {
    
   }
    
    public static boolean removerDoGrupo(String nome, String password, String server, String utilizador, String grupo){
    
   }

    private static Message sendReceive(Message message) {
    
    }
    
    private static Message Marshalling(int contentType, int operation, String name, String password, String server, ArrayList<Object> objs) {
    
    }

    
    private static void startConnection(String localUser, String password, String serverAddress) {
    	String[] aux = serverAddress.split(":");
    
		try {
			socket = new Socket(aux[0], Integer.parseInt(aux[1]));
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}

    }

    private static void login(String localUser, String password) throws IOException {
		out.writeObject(localUser);
		out.writeObject(password);
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
