package myWhatsServer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import Resources.MessageFlags;

public class MyWhatsServer {
	
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
				}
			}
			//??????????????????????????
			//sSoc.close();
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		
		
	}
	
	//Threads utilizadas para comunicao com os clientes
	private	class ServerThread extends Thread {

			private Socket socket = null;

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
			
			private void selectedOperation(ObjectInputStream inStream, ObjectOutputStream outStream) throws ClassNotFoundException, IOException {
				
				String contact;
				MessageFlags end;
				String group;
				String answer;
				String filename;
				
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
							contact = (String) inStream.readObject();
							String text = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								answer = saveMessage(user, contact, text);
								outStream.writeObject(answer);
							}
							else
								outStream.writeObject("Error");
							break;
						
						case F_MESSAGE:
							contact = (String) inStream.readObject();
							filename = (String) inStream.readObject();
							long fileSize = (long) inStream.readObject();
							long alreadyRead = 0;
							
							File file = new File(filename);
							FileOutputStream fileOutStream = new FileOutputStream(file);
							byte[] buffer = new byte[1024];
							
							System.out.println(fileSize);
							
                            while(alreadyRead < fileSize) {
                                int size = inStream.read(buffer);
                                System.out.println(size);
                                fileOutStream.write(buffer, 0, size);
                                alreadyRead += size;
                            }
                            
                            fileOutStream.close();
                            
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								answer = saveFile(contact, file, filename);
								outStream.writeObject(answer);
							}
							else
								outStream.writeObject("Error");
							break;
						
						case R0_MESSAGE:
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								answer = getMostRecentCommunications(user);
								outStream.writeObject(answer);
							}
							else
								outStream.writeObject("Error");
							break;
						
						case R1_MESSAGE:
							contact = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								answer = getAllContactCommunications(user, contact);
								outStream.writeObject(answer);
							}
							else
								outStream.writeObject("Error");
							break;
						
						case R2_MESSAGE:
							contact = (String) inStream.readObject();
							filename = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								File fileAnswer = getContactFile(user, contact, filename);
								outStream.writeObject(fileAnswer);
							}
							else
								outStream.writeObject("Error");
							break;
						
						case A_MESSAGE:
							contact = (String) inStream.readObject();
							group = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								answer = addToGroup(user, contact, group);
								outStream.writeObject(answer);
							}
							else
								outStream.writeObject("Error");
						
							break;
						
						case D_MESSAGE:
							contact = (String) inStream.readObject();
							group = (String) inStream.readObject();
							end = (MessageFlags) inStream.readObject();
							if(end.equals(MessageFlags.END_MESSAGE)) {
								answer = removeFromGroup(user, contact, group);
								outStream.writeObject(result);
							}
							else
								outStream.writeObject("Error");
							break;
				
					}	
				}
			}
			
			private String saveMessage(String username, String contact, String message) {
				boolean result = Persistence.getInstance().saveMessage(username, contact, message);
				if(result)
					return "Messagem guardada com sucesso!";
				else
					return "Erro!";
			}
			
			private String saveFile(String contact, File file, String filename) {
				boolean result = Persistence.getInstance().saveFile(contact, file,filename);
				if(result)
					return "Ficheiro guardado com sucesso!";
				else
					return "Erro!";
			}
			
			private String getMostRecentCommunications(String user) {
				return null;
			}
			
			private String getAllContactCommunications(String user, String contact) {
				return null;
			}
			
			private File getContactFile(String user, String contact, String filename) {
				return null;
			}
			
			private String addToGroup(String user, String contact, String group) {
				return null;
			}
			
			private String removeFromGroup(String user, String contact, String group) {
				return null;
			}
			
			
			
		}
}
