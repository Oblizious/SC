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
	
	public boolean userIsLeader(String username) {
		return leader.getUsername().equals(username);
	}
	
	public boolean removeUser(User user) {
		return users.remove(user);
	}
	
	public boolean userBelongsToGroup(String name) {
		for(User user : users) {
			if(user.getUsername().equals(name))
				return true;
		}
		return false;
	}

}
