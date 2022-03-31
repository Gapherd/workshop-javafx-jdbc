package model.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Leandro Antonelli
 * Date: 31/03/2022
 */
public class ValidationException extends RuntimeException {

    private Map<String, String> errors = new HashMap<>();

    public ValidationException(String msg){
        super(msg);
    }

    public Map<String, String> getErrors(){
        return errors;
    }

    public void addError(String fieldName, String errorMessage){
        getErrors().put(fieldName, errorMessage);
    }

}
