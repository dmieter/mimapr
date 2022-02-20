
package project.engine.data;

/**
 *
 * @author emelyanov
 */
public class Resource {
    public int id; 
    private String name;
    
    public Resource(int id, String name){
        this.id = id;
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
