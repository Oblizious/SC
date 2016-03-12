package myWhatsServer;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String username;
	private String password;
	private List<String> groups;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.groups = new ArrayList<String>();
	}

	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean addGroup(String group) {
		return groups.add(group);
	}
	
	public boolean removeGroup(String group) {
		return groups.remove(group);
	}
	
	public boolean userBelongsToGroup(String group) {
		return groups.contains(group);
	}
	
}
