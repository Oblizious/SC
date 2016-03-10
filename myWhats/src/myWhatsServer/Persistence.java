package myWhatsServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
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
import java.util.List;
import java.util.Map;

public class Persistence {
	private static final Persistence INSTANCE = new Persistence();
	private final DateFormat TIMESTAMPFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final DateFormat FILENAMEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH mm ss SSS");
	private File usersFile;
	private Map<String,User> users;
	private File groupsFile;
	private Map<String, Group> groups;
	
	private Persistence() {		
		try {
			usersFile = new File("Data/users");
			usersFile.getParentFile().mkdirs();//cria caminho
			usersFile.createNewFile();//cria ficheiro se este nao existe
			users = new HashMap<>();
			
			groupsFile = new File("Data/groups");
			groupsFile.getParentFile().mkdirs();//cria caminho
			groupsFile.createNewFile();//cria ficheiro se este nao existe
			groups = new HashMap<>();
			
			BufferedReader r = new BufferedReader(new FileReader(usersFile));			
			String s;
			while((s = r.readLine()) != null){
				User u = getUser(s);
				if(u == null){
					System.out.println("O ficheiro de utilizadores est� corrupto.");
					r.close();
					System.exit(0);
				}					
				users.put(u.getUsername(), u);
			}			
			r.close();
			
			r = new BufferedReader(new FileReader(groupsFile));		
			while((s = r.readLine()) != null){
				Group g = getGroup(s);
				if(g == null){
					System.out.println("O ficheiro de grupos est� corrupto.");
					r.close();
					System.exit(0);
				}					
				groups.put(g.getName(), g);
			}			
			r.close();
			
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	/*
	 * Retorna o grupo representado pela string s sob o formato grupo;master:user1:user2:user3:...
	 * Return null caso o formato nao esteja em conformidade
	 */
	private synchronized Group getGroup(String s) {
		String [] v = s.split(";"); // separa o nome do grupo dos elementos
		if(v.length != 2) // se o formato esta errado
			return null;	
		
		String [] v2 = v[1].split(":"); // lista dos elementos v2[0]= master
		if(v2.length == 0) // se nao ha mestre escrito para o grupo
			return null;
		
		User m = users.get(v2[0]);
		Group g = new Group(m, v[0]);
		
		for(int i = 1; i < v2.length; i++){
			User u = getUser(v2[i]);
			g.addUser(u);
		}
		
		return g;
	}

	/*
	 * Retorna :
	 * -1 - se utilizador e password n�o coincidem
	 *  0 - Se utilizador existe e e a password coincide
	 *  1- Se utilizador n�o existe e foi criado com sucesso
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
	 * Escreve o grupo g no ficheiro de grupos
	 */
	private synchronized void writeGroupToFile(Group g) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(groupsFile,true));
			w.write(g.getName()+";");
			w.write(g.getLeader().getUsername());
			
			List<User> membros =  g.getMembers();
			for(User u : membros)
				w.write(":"+u.getUsername());
			w.write("\n");
			w.close();
		} catch (IOException e) {e.printStackTrace();}
	}

	/*
	 * Retorna true se bem sucedido, false caso ocorra erro
	 */
	public synchronized boolean saveMessage(String username, String contact, String message){
		boolean isGroup = (groups.get(contact) == null);
		
		if(users.get(contact) == null && !isGroup) 
			return false;
		
	
		
		Date date = new Date();
		
		String timestamp = TIMESTAMPFORMAT.format(date).toString();
		String filename = FILENAMEFORMAT.format(date).toString();
		
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
		if(info.length != 2) // se o formato esta errado
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
	
	private synchronized File getMostRecentFile(File contactDir) {

		File[] files = contactDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
			
		});
		
		long modifiedTime = 0;
		File mostRecent = null;
		
		for(File file : files) {
			if(file.lastModified() > modifiedTime) {
				mostRecent = file;
				modifiedTime = file.lastModified();
			}
		}
		
		return mostRecent;
	}
	
	public synchronized String getMostRecentCommunications(String username) {
		
		File dir = new File("Data/" + username);
		
		File[] dirs = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File d) {
				return d.isDirectory();
			}
			
		});
		StringBuilder sb = new StringBuilder();
		for(File d : dirs) {
			File mostRecent = getMostRecentFile(d);
			
			try {
				BufferedReader br = new  BufferedReader (new FileReader(mostRecent));
				String s;
				while((s = br.readLine()) != null)
					sb.append(s + "\n");
				
				sb.append("\n");
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	public synchronized String getAllContactCommunications(String username, String contact) {
		if(users.get(contact) == null) {
			return null;	
		}
		
		File dir = new File("Data/" + username + "/" + contact);
		
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
			
		});
		StringBuilder sb = new StringBuilder();
		for(File file : files) {
			try {
				BufferedReader br = new  BufferedReader (new FileReader(file));
				String s;
				while((s = br.readLine()) != null)
					sb.append(s + "\n");
				
				sb.append("\n");
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sb.toString();
	}
	
	public synchronized File getContactFile(String username, String contact, String filename) {
		if(users.get(contact) == null)
			return null;
		
		File file = new File("Data/" + contact + "/" + username + "/" + filename);
		if(!file.exists())
			return null;
		
		return file;
	}
	
	public synchronized boolean addToGroup(String username, String contact, String groupname) {	
		User u = users.get(contact);
		if(u == null) return false;
		
		Group g = groups.get(groupname);
		if(g == null){// grupo nao exite entao cria-se
			g = new Group(users.get(username), groupname);
			g.addUser(u);
			writeGroupToFile(g);
			File group = new File("Data/" + groupname + "/");
			group.mkdirs();
			return true;
		}
		
		if(!g.userIsLeader(username)) return false;
		
		//se chegou aqui entao estah pronto a adiciona ao grupo
		g.addUser(u);
		return addToGroupFile(u, groupname);
	}
	
	private synchronized boolean addToGroupFile(User u, String groupname) { //TODO problema de leaks...
		File temp = new File("Data/groupsTMP");
		try {
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2)
					return false; // ficheiro encontra-se corrumpido	
				
				if(v[0].equals(groupname)){
					w.write(s+":"+u.getUsername()+"\n");
				}else{		
					w.write(s+"\n");
				}
			}	
			
			w.close();
			r.close();		
			
			groupsFile.delete();
			return temp.renameTo(groupsFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
	}

	/**
	 * 
	 * @param username
	 * @param contact
	 * @param groupname
	 * @return true se apagou o grupo com sucesso ou este nao existia, false caso contrario
	 */
	public synchronized boolean removeFromGroup(String username, String contact, String groupname) {
		if(username == contact){
			deleteGroup(groupname);
			return true;
		}
		
		User u = users.get(contact);
		if(u == null) return false;
		
		Group g = groups.get(groupname);
		if(g == null)
			return false;		
		
		if(!g.userIsLeader(username)) return false;
		
		//se chegou aqui entao estah pronto a adiciona ao grupo
		g.removeUser(u);
		return removeFromGroupFile(u, groupname);
	}
	
	private boolean removeFromGroupFile(User u, String groupname) { //TODO problema de leaks...
		File temp = new File("Data/groupsTMP");
		try {
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2)
					return false; // ficheiro encontra-se corrumpido	
				
				if(v[0].equals(groupname)){
					String [] v2 = v[1].split(":");
					StringBuilder sb = new StringBuilder(v[0]+";");
					
					for(int i = 0; i < v2.length; i++){
						if(!v2[i].equals(u.getUsername()) && i == v2.length - 2) // para nao meter : no fim da linha do grupo
							sb.append(v2[i]);
						else if(!v2[i].equals(u.getUsername()))
							sb.append(v2[i]+":");
					}					
					
					w.write(sb.toString()+"\n");
				}else{		
					w.write(s+"\n");
				}
			}	
			
			w.close();
			r.close();		
			
			groupsFile.delete();
			return temp.renameTo(groupsFile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}		
	}

	/**
	 * Apaga um grupo tanto da persistencia como da memoria
	 * @param groupname o nome do grupo a apagar
	 * @return true se bem sucedido, false caso contrario
	 */
	public synchronized boolean deleteGroup(String groupname){ //TODO problema de leaks...
		try{	
			groups.remove(groups.get(groupname)); // apaga o grupo da mem�ria
			
			File temp = new File("Data/groupsTMP");
			
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2)
					return false; // ficheiro encontra-se corrumpido			
				
				if(v[0].equals(groupname)) continue; // se eh a linha que representa o grupo a apagar
				
				w.write(s);
			}	
			
			w.close();
			r.close();		
			
			groupsFile.delete();
			return temp.renameTo(groupsFile);
		}catch(IOException e){
			e.printStackTrace();				
			return false;
		}		
	}
}