package linq4j.expressions;

/**
 * Created by marrony on 6/15/17.
 */
public class MethodInfo {
    private final String owner;
    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] types;

    public MethodInfo(String owner, String name, Class<?> returnType, Class<?>... types) {
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.types = types;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getTypes() {
        return types;
    }
}
