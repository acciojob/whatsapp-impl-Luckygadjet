package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.


    //private HashMap<Message, User> senderMap;

    private HashSet<String> userMobile;
    ////
    HashMap<String,String > userMap = new HashMap<>();
    private HashMap<Group, User> adminMap;
    HashMap<User,List<Message>> senderMessageMap = new HashMap<>();

    //HashMap<User,String> usergroupMap = new HashMap<>();

    List<Message> messages = new ArrayList<>();
    HashMap<Group,List<Message>> GrpMsgMap = new HashMap<>();
    HashMap<Group,List<User>> grpListMap = new HashMap<>();
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        //this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile))
        {
            throw new Exception("User already exists");
        }
        else {
            userMap.put(name,mobile);
            userMobile.add(mobile);
            senderMessageMap.put(new User(name,mobile),new ArrayList<>());
        }

        return "SUCCESS";

    }

    public Group createGroup(List<User> users) {

        //  If there are only 2 users, the group is a personal chat
        if(users.size() == 2){
            Group gp = new Group(users.get(1).getName(),2);
            grpListMap.put(gp,users);
            GrpMsgMap.put(gp,new ArrayList<>());
            return gp;
        }else {
            customGroupCount++;
            Group gp = new Group("Group " + customGroupCount,users.size());
            grpListMap.put(gp,users);
            GrpMsgMap.put(gp,new ArrayList<>());
            adminMap.put(gp,users.get(0));
            return gp;
        }

    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(!grpListMap.containsKey(group))
        {
           throw new Exception("Group does not exist");
        }
        if(!grpListMap.get(group).contains(sender))
        {
            throw new Exception("You are not allowed to send message");
        }

        List<Message> list = GrpMsgMap.get(group);
        list.add(message);
        GrpMsgMap.put(group,list);


        /*List<Message> l = senderMessageMap.get(sender);
        l.add(message);
        senderMessageMap.put(sender,l);
        messages.add(message);*/


        return GrpMsgMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(grpListMap.containsKey(group) == false)
        {
            throw new Exception("Group does not exist");
        }
        else if(adminMap.get(group) != approver)
        {
            throw new Exception("Approver does not have rights");
        }
        else if(!grpListMap.get(group).contains(user)){
            throw new Exception("User is not a participant");
        }
        else {
            adminMap.replace(group,approver);
        }

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        int ans = 0;
        boolean found = false;
        for(Group l : grpListMap.keySet())
        {
            if(grpListMap.get(l).contains(user) == true)
            {
               found = true;
               break;
            }
        }
        if(found == false)
        {
            throw new Exception("User not found");
        }
        boolean admin = false;
        for(Group g : adminMap.keySet())
        {
            if(adminMap.get(g).equals(user))
            {
                admin = true;
                break;
            }
        }

        if(admin == false)
        {
            throw new Exception("Cannot remove admin");
        }

        List<Message> list = senderMessageMap.get(user);  // Fetch all Messages that he sent
        for(Group grp : grpListMap.keySet())  // HashMap<Group,List<User>> grpListMap = new HashMap<>();
        {
            if(grpListMap.get(grp).contains(user))
            {
                List<Message> update = GrpMsgMap.get(grp);  // HashMap<Group,List<Message>> GrpMsgMap = new HashMap<>();
                for(Message m : list)
                {
                    if(update.contains(m))
                    {
                        update.remove(m);  // Removing Grp Messages that user sent
                    }
                }
                grp.setNumberOfParticipants(grp.getNumberOfParticipants()-1);
                GrpMsgMap.put(grp,update);
                grpListMap.get(grp).remove(user);
                ans +=grpListMap.get(grp).size();
                ans +=GrpMsgMap.get(grp).size();
                break;
            }
        }
        for(Message m : list)
        {
            if(messages.contains(m))
            {
                messages.remove(m);  // Removing from Messages also
            }
        }
        userMobile.remove(user.getMobile());   // Removed from Users
        senderMessageMap.remove(user);   // Removed User and his List of Messages from it

        ans +=messages.size();

        return ans;


    }

    public String findMessage(Date start, Date end, int k) throws Exception {

        List<Message> list = new ArrayList<>();
        for(Message m : messages)
        {
            if(m.getTimestamp().after(start) && m.getTimestamp().before(end))
            {
                list.add(m);
            }
        }
        if(list.size() < k)
        {
            throw new Exception("K is greater than the number of messages");
        }

        Collections.sort(list,(m1,m2)->m2.getTimestamp().compareTo(m1.getTimestamp()));
        return list.get(k-1).getContent();
    }

    public int createMessage(String content) {
        messageId++;
        Date timestamp = new Date();
        Message m = new Message();
        m.setTimestamp(timestamp);
        m.setId(messageId);
        m.setContent(content);
        messages.add(m);
        return messageId;

    }
}
