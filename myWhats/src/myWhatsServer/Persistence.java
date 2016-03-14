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
import java.util.ArrayList;
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
	
	/**
	 * 
	 */
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
					System.out.println("O ficheiro de utilizadores está corrupto.");
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
					System.out.println("O ficheiro de grupos está corrupto.");
					r.close();
					System.exit(0);
				}					
				groups.put(g.getName(), g);
			}			
			r.close();
			
		} catch (Exception e) {	e.printStackTrace();}
	}
	
	/**
	 * Funcao que cria um grupo a partir da sua reprsentacao em string
	 * @param s Representacao em string do grupo com o seguinte formato grupo;master:user1:user2:...
	 * @requires s != null
	 * @return o grupo criado ou null caso o formato nao esteja em conformidade
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
			User u = users.get(v2[i]);
			if(u != null) {
				g.addUser(u); // se o user nao tiver sido caregado para memoria antes eh ignorado
			}
		}
		
		return g;
	}
	
	/**
	 * Funcao que verifica se um utilizador com dado nome e uma dada password pode
	 * fazer login. Se esse um utilizador ainda nao estiver registado, eh registado.
	 * @param username Nome do utilizador 
	 * @param password Password do utilizador
	 * @requires username != null && password != null
	 * @return -1 - se o username e a password não coincidem
	 * 			0 - se o username e a password coincidem
	 * 			1 - se este username ainda não existe e foi criado com sucesso
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
	
	/**
	 * Escreve um utilizador no ficheiro de utilizadores
	 * @param u uilizador a ser escrito
	 * @requires u != null
	 */
	private synchronized void writeUserToFile(User u) {
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(usersFile,true));
			w.write(u.getUsername()+":"+u.getPassword()+"\n");
			w.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Escreve um grupo no ficheiro de grupos
	 * @param g grupo a ser escrito
	 * @requires g != null
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

	/**
	 * Função que guarda uma messagem trocada entre dois utilizadores ou um utilizador 
	 * e um grupo em ficheiro 
	 * @param username nome do utilizador que enviou a messagem
	 * @param contact nome do destinatário da messagem(utilizador ou grupo)
	 * @param message messagem de texto a ser guardada
	 * @requires username != null && contact != null && message != null
	 * @return true se a mensagem foi guardada com sucesso, caso contrario false
	 */
	public synchronized boolean saveMessage(String username, String contact, String message){
		Group group = groups.get(contact);
		boolean isGroup = (group != null);
		
		if(users.get(contact) == null && !isGroup) 
			return false;

		Date date = new Date();
		
		String filename = FILENAMEFORMAT.format(date).toString();
		
		if(!isGroup) {
			File file1 = new File("./Data/" + username + "/" + contact + "/" + filename);
			file1.getParentFile().mkdirs();
			File file2 = new File("./Data/" + contact + "/" + username + "/" + filename);
			file2.getParentFile().mkdirs();
		
			try {
				BufferedWriter w = new BufferedWriter(new FileWriter(file1));
				w.write("me: " + message + "\n");
				w.close();
			
				w  = new BufferedWriter(new FileWriter(file2));
				w.write(username + ": " + message + "\n");
				w.close();
			
			} catch (IOException e) {e.printStackTrace(); return false;}
		}
		else {
			if(group.userBelongsToGroup(username)) {
				File file = new File("./Data/" + contact + "/" + filename);
				file.getParentFile().mkdirs();
				try {
					BufferedWriter w = new BufferedWriter(new FileWriter(file));
					w.write(username + ": " + message + "\n");
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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
		Group group = groups.get(contact);
		boolean isGroup = (group != null);
		
		if(users.get(contact) == null && !isGroup) {
			file.delete();
			return false;	
		}
		
		if(isGroup && !group.userBelongsToGroup(username)) {
			return false;
		}
		
		
		File result;
		if(!isGroup)
			result = new File("Data/" + contact + "/" + username + "/" + username + "-)" + filename);
		else
			result = new File("Data/" + contact + "/" + username + "-)" + filename);
		
		result.getParentFile().mkdirs();	
			try {
			
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
			sb.append("Contact: " + d.getName());
			sb.append( "\n");
			getFileData(mostRecent, sb);
		}
		
		List<Group> groupList = new ArrayList<Group>(groups.values());
		for(Group g : groupList) {
			if(g.userBelongsToGroup(username)) {
				File groupDir = new File("Data/" + g.getName());
				File mostRecent = getMostRecentFile(groupDir );
				sb.append("Contact: " + g.getName());
				sb.append( "\n");
				getFileData(mostRecent, sb);
			}
		}
		return sb.toString();
	}
	
	public synchronized String getAllContactCommunications(String username, String contact) {
		Group group = groups.get(contact);
		boolean isGroup = (group != null);
		
		if(users.get(contact) == null && !isGroup) {
			return null;	
		}
		
		File dir;
		if(!isGroup)
			dir = new File("Data/" + username + "/" + contact);
		else
			dir = new File("Data/" + contact);
		
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
			
		});
		StringBuilder sb = new StringBuilder();
		for(File file : files) {
			getFileData(file, sb);
		}
		
		return sb.toString();
	}
	
	public synchronized File getContactFile(String username, String contact, String filename) {
		Group group = groups.get(contact);
		boolean isGroup = (group != null);
		
		if(users.get(contact) == null && !isGroup)
			return null;	
		
		File file;
		
		if(!isGroup)
			file = new File("Data/" + contact + "/" + username + "/" + username + "->" + filename);
		else
			file = new File("Data/" + contact + "/" + username + "->" + filename);
		
		if(!file.exists())
			return null;
		
		return file;
	}
	
	public void getFileData(File file, StringBuilder sb) {
		if(file.getName().lastIndexOf(".") != -1) {
			sb.append(file.getName().substring(0, file.getName().lastIndexOf("-)")) + ": ");
			sb.append(file.getName().substring(file.getName().lastIndexOf("-)") + 2,  file.getName().length()));
			sb.append( "\n");
		}
		
		else {
		
			try {
				BufferedReader br = new  BufferedReader (new FileReader(file));
				String s;
				while((s = br.readLine()) != null)
					sb.append(s + "\n");
			
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		sb.append(TIMESTAMPFORMAT.format(file.lastModified()));
		sb.append( "\n");
	}
	
	
	public synchronized boolean addToGroup(String username, String contact, String groupname) {	
		User u = users.get(contact);
		if(u == null) return false;
		
		Group g = groups.get(groupname);
		if(g == null){// grupo nao exite entao cria-se
			User leader = users.get(username);
			g = new Group(leader, groupname);
			g.addUser(leader);
			g.addUser(u);
			writeGroupToFile(g);
			File group = new File("Data/" + groupname + "/");
			group.mkdirs();
			groups.put(groupname, g);
			return true;
		}
		
		if(!g.userIsLeader(username)) return false;
		
		//se chegou aqui entao estah pronto a adiciona ao grupo
		g.addUser(u);
		return addToGroupFile(u, groupname);
	}
	
	private synchronized boolean addToGroupFile(User u, String groupname) {
		File temp = new File("Data/groupsTMP");
		try {
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2){
					w.close();
					r.close();
					return false; // ficheiro encontra-se corrumpido	
				}
				
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
		if(username.equals(contact)){
			deleteGroup(groupname);
			return true;
		}
		
		User u = users.get(contact);
		if(u == null) return false;
		
		Group g = groups.get(groupname);
		if(g == null)
			return false;		
		
		if(!g.userIsLeader(username)) return false;
		
		//se chegou aqui entao estah pronto a remover do grupo
		g.removeUser(u);
		return removeFromGroupFile(u, groupname);
	}
	
	private boolean removeFromGroupFile(User u, String groupname) {
		File temp = new File("Data/groupsTMP");
		try {
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2){
					w.close();
					r.close();
					return false; // ficheiro encontra-se corrumpido	
				}
				
				if(v[0].equals(groupname)){
					String [] v2 = v[1].split(":");
					StringBuilder sb = new StringBuilder(v[0]+";");
					
					for(int i = 0; i < v2.length; i++){
						if(!v2[i].equals(u.getUsername()))
							sb.append(v2[i]+":");						
					}
					
					sb.deleteCharAt(sb.length()-1);	// para remover o ultimo ':'				
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
			e.printStackTrace();
			return false;
		}		
	}

	/**
	 * Apaga um grupo tanto da persistencia como da memoria
	 * @param groupname o nome do grupo a apagar
	 * @return true se bem sucedido, false caso contrario
	 */
	public synchronized boolean deleteGroup(String groupname){
		try{	
			groups.remove(groups.get(groupname)); // apaga o grupo da memória
			
			File temp = new File("Data/groupsTMP");
			
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2){
					w.close();
					r.close();
					return false; // ficheiro encontra-se corrumpido
				}
				
				if(v[0].equals(groupname)) continue; // se eh a linha que representa o grupo a apagar
				
				w.write(s);
			}	
			
			w.close();
			r.close();		
			
			groupsFile.delete();
			
			File groupFile = new File ("Data/" + groupname);
			String files[] = groupFile.list();
			for(String filename : files) {
				new File(filename).delete();
			}
			groupFile.delete();
			return temp.renameTo(groupsFile);
		}catch(IOException e){
			e.printStackTrace();				
			return false;
		}		
	}
}