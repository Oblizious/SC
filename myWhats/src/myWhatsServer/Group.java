package myWhatsServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa grupo que tem um nome e é constituido por um lider e 
 * por varios utilizadores
 * @author 44839 Telmo Santos , 44907 Luís Carvalho 
 */
public class Group {
	
	private User leader;
	private List<User> users;
	private String name;
	
	/**
	 * Construtor da classe Group
	 * @param leader utilizador que vai ser o lider do grupo
	 * @param name nome do grupo
	 * @requires leader != null && name != null
	 */
	public Group (User leader, String name) {
		this.leader = leader;
		this.name = name;
		this.users = new ArrayList<User>();
	}
	
	/**
	 * Adiciona um utilizador ao grupo
	 * @param user utilizador a ser adicionado
	 * @requires user != null
	 * @return true se o utilizador foi adicionado com sucesso,
	 *         false caso contrario
	 */
	public boolean addUser(User user) {
		return users.add(user);
	}
	
	/**
	 * Retorna o lider do grupo
	 * @return lider do grupo
	 */
	public User getLeader() {
		return leader;
	}
	
	/**
	 * Retorna o nome do grupo
	 * @return nome do grupo
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Retorna a lista de utilizadores do grupo
	 * @return lista de utilizadores do grupo
	 */
	public List<User> getMembers(){
		return users;
	}
	
	/**
	 * Verifica se um utilizador é lider do grupo
	 * @param username nome do utilizador
	 * @requires username != null
	 * @return true se o utilizador é o lider do grupo,
	 *         false caso contrario
	 */
	public boolean userIsLeader(String username) {
		return leader.getUsername().equals(username);
	}
	
	/**
	 * Remove um utilizador do grupo
	 * @param user utilizador a ser removido
	 * @requires user != null
	 * @return true se o utilizador foi removido com sucesso
	 *         false caso contrario
	 */
	public boolean removeUser(User user) {
		return users.remove(user);
	}
	
	/**
	 * Verifica se um utilizador pertence ao grupo
	 * @param name nome do utilizador
	 * @requires name != null
	 * @return true ser o utilizador pertence ao grupo
	 *         false caso contrario
	 */
	public boolean userBelongsToGroup(String name) {
		for(User user : users) {
			if(user.getUsername().equals(name))
				return true;
		}
		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("--Group--\n");
		sb.append("Name: " + name + "\n");
		sb.append("Leader: \n");
		sb.append(leader.toString());
		sb.append("Users: \n");
		for(User u : users) {
			sb.append(u.toString());
		}
		sb.append("\n");
		return sb.toString();
	}
}
