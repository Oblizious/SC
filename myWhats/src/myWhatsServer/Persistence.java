package myWhatsServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Persistence {
	private static final Persistence INSTANCE = new Persistence();
	private File usersFile;
	private Map<String,User> users;
	
	private Persistence() {
		try {
			usersFile = new File("/Data/users");
			users = new HashMap<>();
			
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
		} catch (Exception e) {	e.printStackTrace(); }
	}
	
	/*
	 * Retorna :
	 * -1 - se utilizador e password não coincidem
	 *  0 - Se utilizador existe e e a password coincide
	 *  1- Se utilizador não existe e foi criado com sucesso
	 */
	public int verifyUser(String username, String password){
		User u = users.get(username);
		if(u == null){
			u = new User(username, password);
			users.put(username, u);
			writeUserToFile(u);
			return 1;
		}		
		return u.getPassword().equals(password) ? 0 : -1;
	}
	
	/*
	 * Escreve o utilizador u no ficheiro de utilizadores
	 */
	private void writeUserToFile(User u) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(usersFile));
			w.write(u.getUsername()+":"+u.getPassword());
			w.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	/*
	 * Retorna true se bem sucedido, false caso ocorra erro
	 */
	public boolean saveMessage(String username, Calendar timestamp, String message){
		String filename = timestamp.toString();
		File f = new File("/Data/username/"+filename);
		
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			w.write(message+"\n");
			w.write(timestamp.toString());
			w.close();
		} catch (IOException e) {e.printStackTrace(); return false;}
		
		return true;
	}

	/*
	 * Retorna o utilizador representado pela string s sob o formato user:password
	 * Return null caso o formato nao esteja em conformidade
	 */
	private User getUser(String s) {
		String [] info = s.split(":");
		if(info.length != 2)
			return null;
		return new User(info[0], info[1]);
	}
	
	public static Persistence getInstance() {
        return INSTANCE;
    }	
}
