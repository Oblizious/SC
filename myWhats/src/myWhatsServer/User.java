package myWhatsServer;

/**
 * Classe que representa um utilizador que possui um nome e uma palavra-passe
 * @author 44839 Telmo Santos , 44907 Luís Carvalho 
 */
public class User {
	private String username;
	private String password;
	
	/**
	 * Construtor da classe User
	 * @param username nome do utilizador
	 * @param password palavra-passe do utilizador
	 * @requires username != null && password != null	
	 */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Retorna o nome do utilizador
	 * @return nome do utilizador
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Retorna a palavra-passe do utilizador
	 * @return palavra-passe do utilizador
	 */
	public String getPassword() {
		return password;
	}
	
	public String toString() {
		return "--User--\n Username: " + username + "\n Password: "  + password + "\n\n";
	}
}
