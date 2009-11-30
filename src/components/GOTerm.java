package components;

import java.util.LinkedList;

/**
 *
 * @author Jeroen Van Goey
 */
public class GOTerm {
    private String name;
    private String id;
    private LinkedList<String> isas = new LinkedList<String>();

//    private String is_a;

    public GOTerm(String id, String name, LinkedList<String> isas) {
        this.id = id;
        this.name = name;
        for(int i = 0 ; i < isas.size();i++) {
            this.isas.add(isas.get(i));

        }
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkedList<String> getIsas() {
        return isas;
    }

    public void setIsas(LinkedList<String> isas) {
        this.isas = isas;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GO term Details:");
        sb.append("\nName: " + getName());
        for(int i = 0 ; i < isas.size();i++) {
            sb.append("\nIs a: " + isas.get(i));
        }
	return sb.toString();

    }


}
