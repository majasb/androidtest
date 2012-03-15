package bratseth.maja.msgtransport.transport;

import java.io.Serializable;

public class Invocation implements Serializable {
    
    private final Class serviceType;
    private final String methodName;
    private final Class[] parameterClasses;
    private final Object[] parameters;

    public Invocation(Class serviceType, String methodName, Class[] parameterClasses, Object[] parameters) {
        this.serviceType = serviceType;
        this.methodName = methodName;
        this.parameterClasses = parameterClasses;
        this.parameters = parameters;
    }

    public Class getServiceType() {
        return serviceType;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterClasses() {
        return parameterClasses;
    }

    public Object[] getParameters() {
        return parameters;
    }

}
