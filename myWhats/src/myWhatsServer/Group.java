package myWhatsServer;

import java.util.ArrayList;
import java.util.List;

public class Group {
	
	private User leader;
	private List<User> users;
	private String name;
	
	public Group (User leader, String name) {
		this.leader = leader;
		this.name = name;
		this.users = new ArrayList<User>();
	}
	
	public boolean addUser(User user) {
		return users.add(user);
	}
	
	public User getLeader() {
		return leader;
	}
	
	public String getName() {
		return name;
	}
	
	public List<User> getMembers(){
		return users;
	}

}
