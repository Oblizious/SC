package myWhats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Resources.MessageFlags;

/**
 * Classe que efetua a ligação entre o cliente e o servidor e 
 * permite troca de dados de dados entre eles
 * @author Telmo Santos 44839, Luís Carvalho 44907
 *
 */
public class MyWhatsStub {
	
	private static Socket socket = null;
	private static ObjectInputStream objInStream = null;
	private static ObjectOutputStream objOutStream = null;
	private static String result = null;
	
	/**
	 * Cria um utilizador 
	 * @param localUser nome do utilizador 
	 * @param password palavra-passe do utilizadaor
	 * @param serverAddress endereço e porto do servidor
	 * @requires localUser != null && password != null && serverAddress != null
	 * @return o texto de resposta vindo do servidor
	 */
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
    
	/**
	 * Envia mensagem a um utilizador ou grupo 
	 * @param localUser nome do utilizador que envia a mensagem
	 * @param password palavra-passe do utilizador
	 * @param serverAddress endereço e porto do servidor
	 * @param contact nome do utilizador que recebe a mensagem
	 * @param text texto da mensagem a ser enviado
	 * @requires localUser != null && password != null && 
	 *           serverAddress != null && contact != null &&
	 *           text != null 
	 * @return o texto de resposta vindo do servidor
	 */
    public static String sendMessage(String localUser, String password, String serverAddress, String contact, String text) {
    	startConnection(localUser, password, serverAddress);
    
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.M_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(text);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    /**
     * Envia um ficheiro a um utilizador ou grupo
     * @param localUser nome do utilizador que envia o ficheiro
     * @param password palavra-passe do utilizador
     * @param serverAddress endereço e porto do servidor
     * @param contact nome do utilizador que recebe o ficheiro
     * @param filename nome do ficheiro a enviar
     * @requires localUser != null && password != null &&
     *           serverAddress != null && contact != null &&
     *           filename != null
     * @return o texto de resposta vindo do servidor
     */
    public static String sendFile(String localUser, String password, String serverAddress, String contact, String filename) {
    	startConnection(localUser, password, serverAddress);
    	
    	File file = new File("Client/" + filename);
    	FileInputStream fileInStream;
    	byte[] buff = new byte[1024];
    	long fileSize = file.length();
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.F_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(filename);
    		objOutStream.writeObject(fileSize);
    		
    		fileInStream = new FileInputStream(file);
    		int readSize;
    		
            while( (readSize = fileInStream.read(buff, 0, 1024)) != -1) {
                objOutStream.write(buff,0,readSize);
            }
    		fileInStream.close();
    		
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    /**
     * Obtem as comunicações mais recentes de/para o utilizador   
     * @param localUser nome do utilizador 
     * @param password palavra-passe do utilizador
     * @param serverAddress endereço e porto do servidor
     * @requires localuser != null && password != null &&
     *           serverAddress != null
     * @return as comunicações mais recentes
     */
    public static String getMostRecentCommunications(String localUser, String password, String serverAddress) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.R0_MESSAGE);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }

    /**
     * Obtem todas a comunicações feitas entre um utilizador e 
     * um outro utilizador ou grupo
     * @param localUser nome do utilizador que pretende obter a comunicações
     * @param password palavra-passe do utilizador
     * @param serverAddress endereço e porto do servidor
     * @param contact utilizador ou grupo do qual se pretende a comunicações
     * @requires localUser != null && password != null &&
     *           serverAddress != null && contact != null
     * @return todas as comunicações com um dado utilizador ou grupo
     */
    public static String getAllContactCommunications(String localUser, String password, String serverAddress, String contact) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.R1_MESSAGE);
    		objOutStream.writeObject(contact);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    /**
     * Obtem um ficheiro partilhado entre um utilizador e 
     * um outro utilizador ou grupo
     * @param localUser nome do utilizador que pretende obter o ficheiro
     * @param password palavra-passe do utilizador 
     * @param serverAddress endereço e porta do servidor
     * @param contact utilizador ou grupo que tem o ficheiro
     * @param filename nome do ficheiro
     * @return o ficheiro e o texto de resposta vindo do servidor
     */
    public static String getContactFile(String localUser, String password, String serverAddress, String contact, String filename) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.R2_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(filename);
    		
    		result = (String) objInStream.readObject();
    		
    		if(!result.contains("Erro!")) {
    			long fileSize = (long) objInStream.readObject();
    			long alreadyRead = 0;
			
    			File file = new File(filename);
    			FileOutputStream fileOutStream = new FileOutputStream(file);
    			byte[] buffer = new byte[1024];
			
    			while(alreadyRead < fileSize) {
    				int size = objInStream.read(buffer);
    				fileOutStream.write(buffer, 0, size);
    				alreadyRead += size;
    			}
    			fileOutStream.close();
    		}
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
    }
    
    /**
     * Adiciona um utilizador a um grupo, se o grupo não existir é criado
     * @param localUser nome do utilizador que quer adicionar outro 
     *                  utilizador ao grupo
     * @param password palavra-passe do utilizador
     * @param serverAddress endereço e porto do servidor
     * @param contact nome do utilizador a ser adicionado
     * @param group nome do grupo ao qual se vai adicionar o utilizador
     * @requires localUser != null && password != null &&
     *           serverAddress != null && contact != null &&
     *           group != null
     * @return o texto de resposta vindo do servidor
     */
    public static String addToGroup(String localUser, String password, String serverAddress, String contact, String group) {
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.A_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(group);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
   }
    
    /**
     * Remove um utilizador de um grupo, se o utilizador a ser removido for 
     * ele próprio o grupo é eliminado
     * @param localUser nome do utilizador que quer remover um outro do grupo
     * @param password palavra-passe do utilizador
     * @param serverAddress endereço e porto do servidor
     * @param contact nome do utilizador a ser removido do grupo
     * @param group grupo do qual se vai remover o utilizador
     * @requires localUser != null && password != null &&
     *           serverAddress != null && contact != null &&
     *           group != null
     * @return o texto de resposta vindo do servidor
     */
    public static String removeFromGroup(String localUser, String password, String serverAddress, String contact, String group){
    	startConnection(localUser, password, serverAddress);
    	
    	try {
    		login(localUser, password);
    		objOutStream.writeObject(MessageFlags.D_MESSAGE);
    		objOutStream.writeObject(contact);
    		objOutStream.writeObject(group);
    		result = (String)objInStream.readObject();
    	} catch(IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
		closeConnection(socket, objInStream, objOutStream);
		return result;
   }

    /**
     * Inicia a socket para comunicar o servidor assim como os 
     * streams de input e output
     * @param localUser nome do utilizador
     * @param password palavra-passe do utilizador
     * @param serverAddress endereço e porto do servidor
     * @requires localUser != null && password != null &&
     *           serverAddress != null
     */
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

    /**
     * Envia para o servidor o nome de utilizador e a sua palavra-passe
     * @param localUser nome do utilizador
     * @param password  palavra-passe do utilizador
     * @throws IOException 
     * @requires localUser != null && password != null &&
     *           Socket de ligação e stream de output ligadas ao servidor
     */
    private static void login(String localUser, String password) throws IOException {
		objOutStream.writeObject(localUser);
		objOutStream.writeObject(password);
    }
    
    /**
     * Termima a conexão com o servidor fechando a socket e as streams 
     * @param socket socket de conexão 
     * @param in stream de input da socket
     * @param out stream de output da socket
     * @requires socket != null && in != null && out != null
     */
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
