package myWhatsServer;

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
				Socket inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
			
		    sSoc.close();
		}
			
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

					String user = null;
					String passwd = null;
				
					try {
						user = (String)inStream.readObject();
						passwd = (String)inStream.readObject();
						System.out.println("thread: depois de receber a password e o user");
					
						//TODO ciclo de fazer actual shit
						int result = Persistence.getInstance().verifyUser(user, passwd);
						
						if(result != -1){ 
							// cliente foi autenticado ou criado
							System.out.println("cool story bro, u autentic");
							selectedOperation(inStream, outStream, result);
						}
						else 
							outStream.writeObject("Failed!!!!!!"); //TODO
						
						
						
						
					
					
					}catch (ClassNotFoundException | IOException e1) {
						e1.printStackTrace();
					}
					

					
					/*
					if (user.length() != 0){
						outStream.writeObject(new Boolean(true));
					}
					else {
						outStream.writeObject(new Boolean(false));
					}
					*/
					outStream.close();
					inStream.close();
	 			
					socket.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			private void selectedOperation(ObjectInputStream inStream, ObjectOutputStream outStream, int result) throws ClassNotFoundException, IOException {
				
				MessageFlags type = (MessageFlags)inStream.readObject();
				
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
						if(end.equals(MessageFlags.END_MESSAGE)) {
							String result = saveMessage(contact, text);
							outStream.writeObject(result);
						}
						else
							outStream.writeObject("Error");
						break;
						
					case F_MESSAGE:
						
						break;
						
					case R_MESSAGE:
						
						break;
						
					case A_MESSAGE:
						
						break;
						
					case D_MESSAGE:
						
						break;
				
				}
				
			}
			
			
			
		}
}
