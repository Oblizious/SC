package myWhatsServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Persistence {
	private static final Persistence INSTANCE = new Persistence();
	private File usersFile;
	private Map<String,User> users;
	private DateFormat timestampFormat;
	private DateFormat filenameFormat;
	private Date date;
	
	private Persistence() {		
		try {
			usersFile = new File("Data/users");
			usersFile.getParentFile().mkdirs();//cria caminho
			usersFile.createNewFile();//cria ficheiro se este nao existe
			users = new HashMap<>();
			timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			filenameFormat = new SimpleDateFormat("yyyy-MM-dd HH mm ss SSS");
			date = new Date();
			
			BufferedReader r = new BufferedReader(new FileReader(usersFile));
			
			String s;
			while((s = r.readLine()) != null){
				User u = getUser(s);
				if(u == null){
					System.out.println("O ficheiro de utilizadores está corrupto.");
					r.close();
					System.exit(0);
				}					
				users.put(u.getUsername(), u);
			}			
			r.close();
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	/*
	 * Retorna :
	 * -1 - se utilizador e password não coincidem
	 *  0 - Se utilizador existe e e a password coincide
	 *  1- Se utilizador não existe e foi criado com sucesso
	 */
	public synchronized int verifyUser(String username, String password){
		User u = users.get(username);
		if(u == null){
			u = new User(username, password);
			users.put(username, u);
			writeUserToFile(u);
			
			//criar o diretorio associado ao utilizador
			File f = new File("Data/"+u.getUsername()+"/default");
			f.getParentFile().mkdirs();
			
			return 1;
		}		
		return u.getPassword().equals(password) ? 0 : -1;
	}
	
	/*
	 * Escreve o utilizador u no ficheiro de utilizadores
	 */
	private synchronized void writeUserToFile(User u) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(usersFile,true));
			w.write(u.getUsername()+":"+u.getPassword()+"\n");
			w.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	/*
	 * Retorna true se bem sucedido, false caso ocorra erro
	 */
	public synchronized boolean saveMessage(String username, String contact, String message){
		if(users.get(contact) == null) 
			return false;
		
		String timestamp = timestampFormat.format(date).toString();
		String filename = filenameFormat.format(date).toString();
		
		File file1 = new File("./Data/" + username + "/" + contact + "/" + filename);
		file1.getParentFile().mkdirs();
		File file2 = new File("./Data/" + contact + "/" + username + "/" + filename);
		file2.getParentFile().mkdirs();
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(file1));
			w.write("Contact: " + contact + "\n");
			w.write("me: " + message + "\n");
			w.write(timestamp.toString());
			w.close();
			
			w  = new BufferedWriter(new FileWriter(file2));
			w.write("Contact: " + username + "\n");
			w.write(username + ": " + message + "\n");
			w.write(timestamp.toString());
			w.close();
			
		} catch (IOException e) {e.printStackTrace(); return false;}
		
		return true;
	}

	/*
	 * Retorna o utilizador representado pela string s sob o formato user:password
	 * Return null caso o formato nao esteja em conformidade
	 */
	private synchronized User getUser(String s) {
		String [] info = s.split(":");
		if(info.length != 2)
			return null;
		return new User(info[0], info[1]);
	}
	
	public static Persistence getInstance() {
        return INSTANCE;
    }	
	
	public synchronized boolean saveFile(String username, String contact, File file, String filename){
		if(users.get(contact) == null) {
			file.delete();
			return false;	
		}
		
		try {
			
			File result = new File("Data/" + contact + "/" + username + "/" + filename);
			result.getParentFile().mkdirs();	
			
			InputStream inStream = new FileInputStream(file);
			OutputStream outStream = new FileOutputStream(result);

			byte[] buffer = new byte[1024];

			int length;
			while ((length = inStream.read(buffer)) > 0) {

				outStream.write(buffer, 0, length);

			}

			inStream.close();
			outStream.close();
			
			file.delete();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
}