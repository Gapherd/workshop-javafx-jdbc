package model.services;

import model.entities.Department;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Leandro Antonelli
 * Date: 29/03/2022
 */
public class DepartmentService {

    public List<Department> findAll(){
        List<Department> departments = new ArrayList<>();
        departments.add(new Department(1, "Books"));
        departments.add(new Department(2, "Computers"));
        departments.add(new Department(3, "Eletronics"));

        return departments;
    }

}
