package myWhatsServer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
					}catch (ClassNotFoundException e1) {
						e1.printStackTrace();
					}
					
					//TODO ciclo de fazer actual shit
					if(Persistence.getInstance().verifyUser(user, passwd) != -1){ 
						// cliente foi autenticado ou criado
						System.out.println("cool story bro, u autentic");						
					}
					
					if (user.length() != 0){
						outStream.writeObject(new Boolean(true));
					}
					else {
						outStream.writeObject(new Boolean(false));
					}

					outStream.close();
					inStream.close();
	 			
					socket.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
}
