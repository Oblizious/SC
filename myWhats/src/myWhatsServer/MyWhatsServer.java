package myWhatsServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Resources.MessageFlags;

/**
 * 
 * @author lapc1
 *
 */
public class MyWhatsServer {
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 1){
			System.out.println("Como correr : MyWhatsServer <porto>");
			System.exit(0);
		}
		
		int porto = Integer.parseInt(args[0]);
		
		System.out.println("servidor: main");
		MyWhatsServer server = new MyWhatsServer();
		server.startServer(porto);
	}
	
	/**
	 * 
	 * @param porto
	 */
	public void startServer (int porto){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(porto);
			
			while(true) {
				try {
					System.out.println("Waiting connection");
					Socket inSoc = sSoc.accept();
					System.out.println("New connection");
					ServerThread newServerThread = new ServerThread(inSoc);
					newServerThread.start();
				}
				catch (IOException e) {
					e.printStackTrace();
					sSoc.close();
				}
			}
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		
		
	}
	
	//Threads utilizadas para comunicao com os clientes
	/**
	 * 
	 * @author lapc1
	 *
	 */
	private	class ServerThread extends Thread {

			private Socket socket = null;

			/**
			 * 
			 * @param inSoc
			 */
			ServerThread(Socket inSoc) {
				socket = inSoc;
				System.out.println("thread do server para cada cliente");
			}
	 
			public void run(){
				try {
					ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			
					try {
							selectedOperation(inStream, outStream);
					}catch (ClassNotFoundException | IOException e1) {
						e1.printStackTrace();
					}
					outStream.close();
					inStream.close();
	 			
					socket.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			/**
			 * 
			 * @param inStream
			 * @param outStream
			 * @throws ClassNotFoundException
			 * @throws IOException
			 */
			private void selectedOperation(ObjectInputStream inStream, ObjectOutputStream outStream) throws ClassNotFoundException, IOException {
								
				String user = (String)inStream.readObject();
				String passwd = (String)inStream.readObject();
				
				int result = Persistence.getInstance().verifyUser(user, passwd);
			
				MessageFlags type = (MessageFlags)inStream.readObject();
				
				if(result == -1)
					outStream.writeObject("Failed!!");
				else {
					switch(type) {
				
						case END_MESSAGE:
							if(result == 0)
								outStream.writeObject("Logged");
							else if(result == 1)
								outStream.writeObject("User Created");
							break;
						
						case M_MESSAGE:
							String contact = (String) inStream.readObject();
							String text = (String) inStream.readObject();
							MessageFlags end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE))
								outStream.writeObject(saveMessage(user, contact, text));
							else
								outStream.writeObject("Error");
							break;
						
						case F_MESSAGE:
							contact = (String) inStream.readObject();
							String filename = (String) inStream.readObject();
							long fileSize = (long) inStream.readObject();
							long alreadyRead = 0;
							
							File file = new File(filename);
							FileOutputStream fileOutStream = new FileOutputStream(file);
							byte[] buffer = new byte[1024];
							
                            while(alreadyRead < fileSize) {
                                int size = inStream.read(buffer);
                                fileOutStream.write(buffer, 0, size);
                                alreadyRead += size;
                            }
                            
                            fileOutStream.close();
                           
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE))
								outStream.writeObject(saveFile(user, contact, file, filename));
							else
								outStream.writeObject("Error");
							break;
						
						case R0_MESSAGE:
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE))
								outStream.writeObject(getMostRecentCommunications(user));
							else
								outStream.writeObject("Error");
							break;
						
						case R1_MESSAGE:
							contact = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE))
								outStream.writeObject(getAllContactCommunications(user, contact));
							else
								outStream.writeObject("Error");
							break;
						
						case R2_MESSAGE:
							contact = (String) inStream.readObject();
							filename = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								File fileAnswer = getContactFile(user, contact, filename);
								
						    	byte[] buff = new byte[1024];
						    	long fileAnswerSize = fileAnswer.length();
					    		
					    		outStream.writeObject(fileAnswerSize);
						    	
						    	FileInputStream  fileInStream = new FileInputStream(fileAnswer);
					    		int readSize;
					    		
					            while( (readSize = fileInStream.read(buff, 0, 1024)) != -1) {
					            	outStream.write(buff,0,readSize);
					            }
					    		fileInStream.close();
							}
							else
								outStream.writeObject("Error");
							break;
						
						case A_MESSAGE:
							contact = (String) inStream.readObject();
							String group = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) 
								outStream.writeObject(addToGroup(user, contact, group));
							else
								outStream.writeObject("Error");
							break;
						
						case D_MESSAGE:
							contact = (String) inStream.readObject();
							group = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE))
								outStream.writeObject(removeFromGroup(user, contact, group));
							else
								outStream.writeObject("Error");
							break;
					}	
				}
			}
			
			/**
			 * 
			 * @param username
			 * @param contact
			 * @param message
			 * @return
			 */
			private String saveMessage(String username, String contact, String message) {
				boolean result = Persistence.getInstance().saveMessage(username, contact, message);
				if(result)
					return "Messagem guardada com sucesso!";
				else
					return "Erro!";
			}
			
			/**
			 * 
			 * @param username
			 * @param contact
			 * @param file
			 * @param filename
			 * @return
			 */
			private String saveFile(String username, String contact, File file, String filename) {
				boolean result = Persistence.getInstance().saveFile(username, contact, file,filename);
				if(result)
					return "Ficheiro guardado com sucesso!";
				else
					return "Erro!";
			}
			
			/**
			 * 
			 * @param username
			 * @return
			 */
			private String getMostRecentCommunications(String username) {
				return Persistence.getInstance().getMostRecentCommunications(username);
			}
			
			/**
			 * 
			 * @param username
			 * @param contact
			 * @return
			 */
			private String getAllContactCommunications(String username, String contact) {
				String result = Persistence.getInstance().getAllContactCommunications(username, contact);
				if(result == null)
					return "Erro!";
				else
					return result;
			}
			
			/**
			 * 
			 * @param username
			 * @param contact
			 * @param filename
			 * @return
			 */
			private File getContactFile(String username, String contact, String filename) {
				return Persistence.getInstance().getContactFile(username, contact, filename);
			}
			
			/**
			 * 
			 * @param username
			 * @param contact
			 * @param groupname
			 * @return
			 */
			private String addToGroup(String username, String contact, String groupname) {
				boolean result = Persistence.getInstance().addToGroup(username, contact, groupname);
				if(result)
					return "Utilizador adicionado ao grupo com sucesso";
				else
					return "Erro!";
			}
			
			/**
			 * 
			 * @param username
			 * @param contact
			 * @param groupname
			 * @return
			 */
			private String removeFromGroup(String username, String contact, String groupname) {
				boolean result = Persistence.getInstance().removeFromGroup(username, contact, groupname);
				if(result)
					return "Utilizador removido do grupo com sucesso";
				else
					return "Erro!";
			}
	}
	
}
