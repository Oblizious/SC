package myWhatsServer;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String username;
	private String password;
	private List<Group> groups;
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.groups = new ArrayList<Group>();
	}

	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean addGroup(Group group) {
		return groups.add(group);
	}
}
