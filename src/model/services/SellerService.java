package model.services;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Seller;

import java.util.List;

public class SellerService {

    private SellerDao sellerDao = DaoFactory.createSellerDao();

    public List<Seller> findAll(){
        return sellerDao.findAll();
    }

    public void saveOrUpdate(Seller dep){
        if(dep.getId() == null){
            sellerDao.insert(dep);
        } else {
            sellerDao.update(dep);
        }
    }

    public void remove(Seller dep){
        sellerDao.deleteById(dep.getId());
    }
}
