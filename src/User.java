public class User {
    String id;
    UserTier tier;
    public User(String id, UserTier tier){
        this.id = id;
        this.tier = tier;
    }

    public String getId(){
        return this.id;
    }
    public UserTier getTier(){
        return this.tier;
    }
}
