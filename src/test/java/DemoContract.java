import java.util.Map;

public class DemoContract {

    private Map map;

    public DemoContract(Map map) {
        this.map = map;
    }

    public void insertUser(String id, String name){
        this.map.put(id, name);
    }

    public void updateUser(String id, String name){
        this.map.put(id, name);
    }

    public void delUser(String id){
        this.map.remove(id);
    }
}
