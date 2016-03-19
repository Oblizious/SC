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
 * Classe servidor que efetua a ligação entre o servidor e o cleinte e 
 * permite troca de dados de dados entre eles
 * @author Telmo Santos 44839, Luís Carvalho 44907
 *
 */
public class MyWhatsServer {
	
	/**
	 * Executa do servidor MyWhats
	 * @param args numero do porto de ligação
	 */
	public static void main(String[] args) {
		if(args.length < 1){
			System.out.println("Como correr : MyWhatsServer <porto>");
			System.exit(0);
		}
		
		int porto = Integer.parseInt(args[0]);
		
		System.out.println("MyWhatsServer");
		MyWhatsServer server = new MyWhatsServer();
		server.startServer(porto);
	}
	
	/**
	 * Inicia o servidor e espera por ligações de clientes
	 * @param porto numero do porto de ligação
	 * @requires porto > 0 
	 */
	public void startServer (int porto){
		ServerSocket sSoc = null;
        
		try {
			sSoc = new ServerSocket(porto);
			
			while(true) {
				try {
					System.out.println("Waiting connections");
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

	/**
	 * Classe de thread que permite a comunicação ente o cliente e o servidor 
	 * @author Telmo Santos 44839, Luís Carvalho 44907
	 *
	 */
	private	class ServerThread extends Thread {

			private Socket socket = null;

			/**
			 * Constructor da thread que é executada para cada ligação ao servidor
			 * @param inSoc socket de comunicação entre o servidor e o cliente
			 * @requires inSoc != null
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
			 * Recebe os dados vindos do cliente, determina quais as operações a serem executadas sobre o dados  
			 * e envia uma resposta ao cliente 
			 * @param inStream stream de recepção de dados vindos do cliente 
			 * @param outStream stream de envio de dados para o cliente
			 * @throws ClassNotFoundException
			 * @throws IOException
			 * @requires inSteam != null && outStream != null
			 */
			private void selectedOperation(ObjectInputStream inStream, ObjectOutputStream outStream) throws ClassNotFoundException, IOException {
								
				String user = (String)inStream.readObject();
				String passwd = (String)inStream.readObject();
				
				int result = Persistence.getInstance().verifyUser(user, passwd);
			
				MessageFlags type = (MessageFlags)inStream.readObject();
				
				if(result == -1)
					outStream.writeObject("Erro!");
				else {
					switch(type) {
				
						case END_MESSAGE:
							if(result == 0)
								outStream.writeObject("Logged");
							else if(result == 1)
								outStream.writeObject("Utilizador criado");
							break;
						
						case M_MESSAGE:
							String contact = (String) inStream.readObject();
							String text = (String) inStream.readObject();
							outStream.writeObject(saveMessage(user, contact, text));
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
                           
							outStream.writeObject(saveFile(user, contact, file, filename));
							break;
						
						case R0_MESSAGE:
							outStream.writeObject(getMostRecentCommunications(user));
							break;
						
						case R1_MESSAGE:
							contact = (String) inStream.readObject();
							outStream.writeObject(getAllContactCommunications(user, contact));
							break;
						
						case R2_MESSAGE:
							contact = (String) inStream.readObject();
							filename = (String) inStream.readObject();
							Object answer = getContactFile(user, contact, filename);
							if(answer instanceof File) {
								
								File fileAnswer = (File) answer;
								outStream.writeObject("Ficheiro " + filename + " recebido.");
								
								
								byte[] buff = new byte[1024];
						    	long fileAnswerSize = fileAnswer.length();
					    		
					    		outStream.writeObject(fileAnswerSize);
						    	
						    	FileInputStream  fileInStream = new FileInputStream(fileAnswer);
					    		int readSize;
					    		
					        	while((readSize = fileInStream.read(buff, 0, 1024)) != -1) {
					        		outStream.write(buff,0,readSize);
					        	}
					    		fileInStream.close();
					    		
							} else if(answer instanceof String)
								outStream.writeObject((String) answer);
							
							break;
						
						case A_MESSAGE:
							contact = (String) inStream.readObject();
							String group = (String) inStream.readObject();
							outStream.writeObject(addToGroup(user, contact, group));
							break;
						
						case D_MESSAGE:
							contact = (String) inStream.readObject();
							group = (String) inStream.readObject();
							outStream.writeObject(removeFromGroup(user, contact, group));
							break;
					}	
				}
			}
			
			/**
			 * Acede à persistencia para guardar um mensagem trocada entre um 
			 * utilizador e um outro utilizador ou grupo
			 * @param username nome do utilizador que envia a mensagem
			 * @param contact nome do utilizador que recebe a messagem
			 * @param message texto da mensagem
			 * @requires username != null && contact != null &&
			 *           message != null
			 * @return texto de resposta
			 */
			private String saveMessage(String username, String contact, String message) {
				boolean result = Persistence.getInstance().saveMessage(username, contact, message);
				if(result)
					return "Messagem guardada com sucesso!";
				else
					return "Erro!";
			}
			
			/**
			 * Acede à persistencia para guardar um ficheiro enviado de utilizador para um 
			 * utilizador ou grupo
			 * @param username nome do utilizador que envia o ficheiro
			 * @param contact nome do utilizador que recebe o ficehiro
			 * @param file ficheiro a ser enviado 
			 * @param filename nome do ficheiro
			 * @requires username != null && contact != null && 
			 *           file != null && filename != null
			 * @return texto de resposta
			 */
			private String saveFile(String username, String contact, File file, String filename) {
				boolean result = Persistence.getInstance().saveFile(username, contact, file, filename);
				if(result)
					return "Ficheiro guardado com sucesso!";
				else
					return "Erro!";
			}
			
			/**
			 * Acede à persistencia para obtem as comunicações mais recentes com cada dos contactos
			 * @param username nome do utilizador
			 * @requires result != null
			 * @return as comunicações mais recentes ou erro
			 */
			private String getMostRecentCommunications(String username) {
				String result =  Persistence.getInstance().getMostRecentCommunications(username);
				if(result == null)
					return "Erro!";
				else {
					if(result.equals(""))
						return "Ainda não fez qualquer tipo de comunicação.\n";
					else
						return result;
				}
			}
			
			/**
			 * Acede à persistencia para obter todas as cominucações com um dado utilizador
			 * @param username nome do utilizador
			 * @param contact nome do utilizador com o qual se comunicou
			 * @return todas as comunicações com um dado utilizador ou erro
			 */
			private String getAllContactCommunications(String username, String contact) {
				String result = Persistence.getInstance().getAllContactCommunications(username, contact);
				if(result == null)
					return "Erro!";
				else 
					return result;
			}
			
			/**
			 * Acede à persistencia para obtem um dado ficheiro
			 * @param username nome do utilizador
			 * @param contact nome do utitilizador que tem o ficheiro
			 * @param filename nome do ficheiro
			 * @requires username != null && contact != null &&
			 *           filename != null
			 * @return o ficheiro pretendido se existir,
			 *         senão retorna uma messagem de erro
			 */
			private Object getContactFile(String username, String contact, String filename) {
				File result = Persistence.getInstance().getContactFile(username, contact, filename);
				if(result == null)
					return "Erro!";
				else
					return result;
			}
			
			/**
			 * Acede à persistencia para adicionar um utilizador a um grupo
			 * e devolve uma string de resposta
			 * @param username nome de utlizador
			 * @param contact nome do utilizdor a ser adicionado ao grupo
			 * @param groupname nome do grupo
			 * @requires username != null && contact != null &&
			 *           groupname != null
			 * @return texto de resposta
			 */
			private String addToGroup(String username, String contact, String groupname) {
				boolean result = Persistence.getInstance().addToGroup(username, contact, groupname);
				if(result)
					return "Utilizador adicionado ao grupo com sucesso!";
				else
					return "Erro!";
			}
			
			/**
			 * Acede à persistencia para remover um utilizador de um grupo 
			 * e devolve uma string de resposta
			 * @param username nome do utilizdor
			 * @param contact nome do utilizador a ser removido do grupo
			 * @param groupname nome do grupo
			 * @requires usernname != null && contact != null && 
			 *           groupname != null
			 * @return texto de resposta
			 */
			private String removeFromGroup(String username, String contact, String groupname) {
				boolean result = Persistence.getInstance().removeFromGroup(username, contact, groupname);
				if(result)
					return "Utilizador removido do grupo com sucesso!";
				else
					return "Erro!";
			}
	}
	
}
