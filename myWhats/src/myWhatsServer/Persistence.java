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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Classe que garante a persistencia do servidor
 * @author Telmo Santos 44839, Luís Carvalho 44907
 *
 */
public class Persistence {
	private static final Persistence INSTANCE = new Persistence();
	private final DateFormat TIMESTAMPFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private final DateFormat FILENAMEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH mm ss SSS");
	private File usersFile;
	private Map<String,User> users;
	private File groupsFile;
	private Map<String, Group> groups;
	private Map<String, Long> timestamps;
	
	/**
	 * Construtor da classe Persistence
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
			
			timestamps = new HashMap<>();
			if(!loadAllTimestamps())
				System.exit(0);
			
			File tmp = new File("tmp"); //diretorio temporario que recebe ficheiros vindos do servidor
			tmp.mkdir();
			
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
	 * Obtem a unica instancia da classe Persistence, garantindo que é um singleton
	 * @return instancia de Persistence
	 */
	public static Persistence getInstance() {
        return INSTANCE;
    }	
	
	/**
	 * Cria um grupo a partir da sua reprsentacao em string
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
	 * Adiciona a representação de um timestamp ao mapa de timestamps
	 * @param s representação de um timestamp
	 * @requires s != null
	 */
	private synchronized void addTimestampToMap(String s) {
		String [] v = s.split(";");
		if(v.length == 2) 
			timestamps.put(v[0], Long.parseLong(v[1].trim()));
		
	}
	
	/**
	 * Verifica se um utilizador com dado nome e uma dada password pode
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
		BufferedWriter w = null;
		try {
			w = new BufferedWriter(new FileWriter(groupsFile,true));
			w.write(g.getName()+";");
			w.write(g.getLeader().getUsername());
			
			List<User> membros =  g.getMembers();
			for(User u : membros)
				w.write(":"+u.getUsername());
			w.write("\n");
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Guarda uma messagem trocada entre dois utilizadores ou um utilizador 
	 * e um grupo em ficheiro 
	 * @param username nome do utilizador que enviou a messagem
	 * @param contact nome do destinatário da messagem(utilizador ou grupo)
	 * @param message messagem de texto a ser guardada
	 * @requires username != null && contact != null && message != null
	 * @return true se a mensagem foi guardada com sucesso, caso contrario false
	 */
	public synchronized boolean saveMessage(String username, String contact, String message){
		if(username.equals(contact))
			return false;
		
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
				w.write(username + ": " + message + "\n");
				w.close();
			
				w  = new BufferedWriter(new FileWriter(file2));
				w.write(username + ": " + message + "\n");
				w.close();
			
				if(!addFileToTimestamps(file1, username) || 
				   !addFileToTimestamps(file2, contact))
					return false;	
				
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
					
					if(!addFileToTimestamps(file, contact))
						return false;
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * Cria um user a partir da sua reprsentacao em string
	 * @param s representação em string de um user com o formato nome:password
	 * @requires s != null
	 * @return u o user criado ou null caso o formato nao esteja em conformidade
	 */
	private synchronized User getUser(String s) {
		String [] info = s.split(":");
		if(info.length != 2) // se o formato esta errado
			return null;
		return new User(info[0], info[1]);
	}
		
	/**
	 * Permite guardar um dado ficheiro enviado por um utilizador para 
	 * um utilizador ou grupo
	 * @param username nome do utilizador que enviou o ficheiro
	 * @param contact nome do utilizador/grupo que recebe o ficheiro
	 * @param file ficheiro enviado
	 * @param filename nome do ficheiro enviado
	 * @requires username != null && contact != null && file != null && filename != null
	 * @return true se o ficheiro foi guardado com sucesso, false caso contrario
	 */
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
		
		if(!isGroup) {
			File file1 = new File("Data/" + username + "/" + contact + "/" + username + "-)" + filename);
			File file2 = new File("Data/" + contact + "/" + username + "/" + username + "-)" + filename);
			file1.getParentFile().mkdirs();
			file2.getParentFile().mkdirs();
			
			if(file1.exists() || file2.exists()) {
				file.delete();
				return false;
			}
			
			try {
				InputStream inStream = new FileInputStream(file);
				OutputStream outStream = new FileOutputStream(file1);
				byte[] buffer = new byte[1024];

				int length;
				while ((length = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, length);
				}

				inStream.close();
				outStream.close();
			
				if(!addFileToTimestamps(file1, username))
					return false;
			
				inStream = new FileInputStream(file);
				outStream = new FileOutputStream(file2);
			
				while ((length = inStream.read(buffer)) > 0) {
					outStream.write(buffer, 0, length);
				}

				inStream.close();
				outStream.close();
			
				if(!addFileToTimestamps(file2, contact))
					return false;
			
				file.delete();
				
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			String [] files = new File("Data/" + contact).list();
			for(String f : files) {
				if(f.contains(filename)) {
					return false;
				}
			}
			File result = new File("Data/" + contact + "/" + username + "-)" + filename);
			
			if(result.exists()) {
				file.delete();
				return false;
			}
		
			result.getParentFile().mkdirs();	
		
			try {
			
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
			
				if(!addFileToTimestamps(result, contact))
					return false;
			
				return true;

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Retorna o ficheiro mas recente num dado diretorio
	 * @param contactDir diretorio com ficheiros
	 * @requires contactDir != null
	 * @return o ficheiro mais recente ou null caso o directorio esteja vazio
	 */
	private synchronized File getMostRecentFile(File contactDir) {

		File[] files = contactDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if(file.getName().equals("timestamps"))
					return false;
				
				return file.isFile();
			}
			
		});
		
		long mostRecentTime = 0;
		File mostRecent = null;
		try {
			for(File file : files) {
				long aux = timestamps.get(file.getCanonicalPath());	
				if(aux > mostRecentTime) {
					mostRecent = file;
					mostRecentTime = aux;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mostRecent;
	}
	
	/**
	 * Obtem as comunicações mais recentes de todos os contactos e 
	 * grupos de um dado utilizador
	 * @param username nome do utilizador
	 * @requires username != null
	 * @return todas as comunicações mais recentes
	 */
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
			if(mostRecent != null) {
				sb.append("Contact: " + d.getName());
				sb.append( "\n");
				getFileData(mostRecent, sb, username);
			}
		}
		
		List<Group> groupList = new ArrayList<Group>(groups.values());
		for(Group g : groupList) {
			if(g.userBelongsToGroup(username)) {
				File groupDir = new File("Data/" + g.getName());
				File mostRecent = getMostRecentFile(groupDir);
				if(mostRecent != null) {
					sb.append("Contact: " + g.getName());
					sb.append( "\n");
					getFileData(mostRecent, sb, username);
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * Obtem todas os comunições entre um utilizador e um outro utilizador ou grupo 
	 * @param username nome do utilizador
	 * @param contact nome de um outro utilizador ou grupo
	 * @requires username != null && contact != null
	 * @return todas as comunicações entre um utilizador e outro utilizador ou grupo
	 */
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
		
		if(!dir.exists())
			return null;
		
		File[] files = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				if(file.getName().equals("timestamps"))
					return false;
				
				return file.isFile();
			}
			
		});
		StringBuilder sb = new StringBuilder();
		if(files.length == 0) {
			sb.append("Não ainda foram efectuadas comunicações.");
		}
		else {
			sb.append("Contact: " + contact + "\n");
			ArrayList<Entry<File, Long>> list = new ArrayList<>();
			for(File file : files) {
				try {
					list.add(new AbstractMap.SimpleEntry<File, Long>(file, timestamps.get(file.getCanonicalPath())));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Collections.sort(list, new Comparator<Entry<File, Long>>() {

				@Override
				public int compare(Entry<File, Long> arg0, Entry<File, Long> arg1) {
					return (arg0.getValue().compareTo(arg1.getValue()));
				}
			});
			
			for(Entry<File, Long> entry : list) {
				getFileData(entry.getKey(), sb, username);
			}
		}
		return sb.toString();
	}
	
	/**
	 * Obtem um ficheiro partilhado entre um utilizador e um outro utilizador/grupo
	 * @param username nome do utilizador
	 * @param contact nome do outro utilizador ou grupo
	 * @param filename nome do ficheiro
	 * @requires username != null && contact != null && filename != null
	 * @return o ficheiro ou null se o ficheiro não existir
	 */
	public synchronized File getContactFile(String username, String contact, String filename) {
		Group group = groups.get(contact);
		boolean isGroup = (group != null);
		
		if(users.get(contact) == null && !isGroup)
			return null;	
		
		File file = null;
		
		if(!isGroup)
			file = new File("Data/" + contact + "/" + username + "/" + username + "-)" + filename);
		else {
			String [] files = new File("Data/" + contact).list();
			for(String f : files) {
				if(f.contains(filename)) {
					file = new File("Data/" + contact + "/" + f);
					break;
				}
			}
		}
		if(file == null || !file.exists())
			return null;
		
		return file;
	}
	
	/**
	 * Escreve num StringBuilder o conteudo do ficheiro se este for de texto, 
	 * caso contrario escreve o seu nome
	 * @param file ficheiro do qual se vai ler o conteudo
	 * @param sb StringBuilder onde vai ser escrito o conteudo do ficheiro
	 * @requires file != null && sb != null
	 */
	public synchronized void getFileData(File file, StringBuilder sb, String username) {
		if(file.getName().lastIndexOf(".") != -1) {
			try {
				String name = file.getName().substring(0, file.getName().indexOf("-)"));
				if(name.equals(username))
					name = "me";
					
				sb.append(name + ": ");
				sb.append(file.getName().substring(file.getName().indexOf("-)") + 2,  file.getName().length()));
				sb.append( "\n");
				sb.append(TIMESTAMPFORMAT.format(timestamps.get(file.getCanonicalPath())));
				sb.append( "\n\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		else {
			try {
				BufferedReader br = new  BufferedReader (new FileReader(file));
				String s;
				while((s = br.readLine()) != null) {
					s = s.replace(username + ": ", "me: ");
					sb.append(s + "\n");	
				}
			
				br.close();
				sb.append(TIMESTAMPFORMAT.format(timestamps.get(file.getCanonicalPath())));
				sb.append("\n\n");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Adiciona um utilizador a um grupo e cria-o se não existir
	 * @param username nome do utilizador que pretende adicionar outro utilizador
	 * @param contact nome do utitilizador a ser adicionado
	 * @param groupname nome do grupo
	 * @requires username != null && contact != null && groupname != null
	 * @return true se o utilizador foi adicionado ao grupo com sucesso
	 * 		   false caso contrario
	 */
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
		//se o user ja estah no grupo entao nao adiciona de novo ...		
		if(g.getMembers().contains(u))
			return false; 
		
		g.addUser(u);
		return addToGroupFile(u, groupname);
	}
	
	/**
	 * Adiciona um utilizador adicionado a um grupo ao ficheiro de grupos
	 * @param u utilizador a ser adicionado ao ficheiro
	 * @param groupname nome do grupo
	 * @requires u != null && groupname != null
	 * @return true se foi adicionado ao ficheiro com sucesso
	 *         false caso contrario
	 */
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
	 * Remove um utilizador de um grupo, se o utilizador que 
	 * se remove for o lider com o grupo é apagado
	 * @param username nome do utilizador que pretende remover outro utilizador
	 * @param contact nome do utilizador a ser removido
	 * @param groupname nome do grupo
	 * @require username != null && contact != null && groupname != null
	 * @return true se removeu o utilizador do grupo, false caso contrario
	 */
	public synchronized boolean removeFromGroup(String username, String contact, String groupname) {
		if(username.equals(contact)){
			return deleteGroup(groupname);
		}
		
		User u = users.get(contact);
		if(u == null) return false;
		
		Group g = groups.get(groupname);
		if(g == null)
			return false;		
		
		if(!g.userIsLeader(username) || !g.userBelongsToGroup(contact)) return false;
		
		//se chegou aqui entao estah pronto a remover do grupo
		g.removeUser(u);
		return removeFromGroupFile(u, groupname);
	}
	
	/**
	 * Remove o utilizador do ficheiro de grupos
	 * @param u utilizador a ser removido do grupo
	 * @param groupname nome do grupo
	 * @requires u != null && groupname != null
	 * @return true se o utilizador foi removido com sucesso do ficheiro,
	 *         false caso contrario 
	 */
	private synchronized boolean removeFromGroupFile(User u, String groupname) {
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
	 * Apaga um grupo tanto da persistencia como da memoria, apagando todas mensagens e ficheiros
	 * @param groupname o nome do grupo a apagar
	 * @return true se bem sucedido, false caso contrario
	 */
	public synchronized boolean deleteGroup(String groupname){
		try{	
			groups.remove(groupname); // apaga o grupo da memória
			
			File temp = new File("Data/groupsTMP");
			
			BufferedReader r = new BufferedReader(new FileReader(groupsFile));
			BufferedWriter w = new BufferedWriter(new FileWriter(temp));
			
			String s;
			while((s = r.readLine()) != null){
				String [] v = s.split(";");
				
				if(v.length < 2){
					w.close();
					r.close();
					temp.delete();
					return false; // ficheiro encontra-se corrumpido
				}
				
				if(v[0].equals(groupname)) continue; // se eh a linha que representa o grupo a apagar
				
				w.write(s);
			}	
			
			w.close();
			r.close();	
			
			File groupFile = new File ("Data/" + groupname);
			if(!groupFile.exists()){
				temp.delete();
				return false;
			}
			File[] files = groupFile.listFiles();
			
			if(files.length == 0){
				groupFile.delete();
				groupsFile.delete();
				temp.renameTo(groupsFile);
				return true;
			}
			
			for(File file : files) {
				timestamps.remove(file.getCanonicalPath());
				file.delete();
			}
			groupFile.delete();
			groupsFile.delete();
			return temp.renameTo(groupsFile);
		}catch(IOException e){
			e.printStackTrace();				
			return false;
		}		
	}
	
	/**
	 * Adiciona o timestamp de um novo ficheiro ao mapa de timestamps e 
	 * ao ficheiro de timestamps 
	 * @param file novo ficheiro do qual se vai o timestamp
	 * @param username nome do utilizador
	 * @requires file != null && username != null
	 * @return true se o timestamp foi adicionado com sucesso
	 *         false caso contrario 
	 * @throws IOException
	 */
	private synchronized boolean addFileToTimestamps(File file, String username) throws IOException {
		String path = file.getCanonicalPath();
		long timestamp = file.lastModified();
		
		if(timestamps.containsKey(path))
			return false;
		
		timestamps.put(path, timestamp);
		File timeFile = new File("Data/" + username + "/timestamps");
		if(!timeFile.exists())
			timeFile.createNewFile();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(timeFile,true));
		bw.write(path + ";" + Long.toString(timestamp) + "\n");
		bw.close();
		
		return true;
	}
	
	/**
	 * Adiciona todos os timestamps em persistencia ao mapa de timestamps
	 * @return true se os timestamps foram adicionados com sucesso
	 *         false caso contrario
	 * @throws IOException
	 */
	private synchronized boolean loadAllTimestamps() throws IOException {
		File dir = new File("Data/");
		if(!dir.exists())
			return false;
		
		File[] list = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
			
		});
	
		File aux = null;
		
		for(File d : list) {
			aux = new File(d.getAbsolutePath() + "/timestamps");
			if(aux.exists() && aux.isFile()) {
				BufferedReader bw = new BufferedReader(new FileReader(aux));
				String s;
				while((s = bw.readLine()) != null) {
					addTimestampToMap(s);
				}
				bw.close();
			}
		}
		return true;
	}
}