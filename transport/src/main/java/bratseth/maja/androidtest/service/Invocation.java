package bratseth.maja.androidtest.service;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class Invocation implements Serializable {
    
    private final String serviceType;
    private final String methodName;
    private final String[] parameterClasses;
    private final Object[] parameters;

    public Invocation(String serviceType, String methodName, String[] parameterClasses, Object[] parameters) {
        this.serviceType = serviceType;
        this.methodName = methodName;
        this.parameterClasses = parameterClasses;
        this.parameters = parameters;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParameterClasses() {
        return parameterClasses;
    }

    public Object[] getParameters() {
        return parameters;
    }

}
