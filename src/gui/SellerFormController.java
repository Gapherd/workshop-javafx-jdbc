package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.SellerService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class SellerFormController implements Initializable {

    private Seller seller;
    private SellerService sellerService;

    private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtEmail;
    @FXML
    private DatePicker dpBirthDate;
    @FXML
    private TextField txtBaseSalary;
    @FXML
    private Label labelErrorName;
    @FXML
    private Label labelErrorEmail;
    @FXML
    private Label labelErrorBirthDate;
    @FXML
    private Label labelErrorBaseSalary;
    @FXML
    private Button btSave;
    @FXML
    private Button btCancel;

    public void setSeller(Seller seller){
        this.seller = seller;
    }
    public void setSellerService(SellerService sellerService){ this.sellerService = sellerService; }

    public void subscribeDataChangeListener(DataChangeListener listener){
        dataChangeListeners.add(listener);
    }

    @FXML
    public void onBtSaveAction(ActionEvent event){
        if(seller == null){
            throw new IllegalStateException("Seller was null");
        }
        if(sellerService == null){
            throw new IllegalStateException("DeparmentService was null");
        }
        try {
            seller = getFormData();
            sellerService.saveOrUpdate(seller);
            notifyDataChangeListeners();
            Utils.currentStage(event).close();
        } catch (DbException e){
            Alerts.showAlert("Error saving object", null, e.getMessage(), Alert.AlertType.ERROR);
        } catch (ValidationException e){
            setErrorMessages(e.getErrors());
        }
    }

    private void notifyDataChangeListeners() {
        for(DataChangeListener listener : dataChangeListeners){
            listener.onDataChanged();
        }
    }

    private Seller getFormData() {
        Seller sel = new Seller();
        ValidationException exception = new ValidationException("Validation error");

        sel.setId(Utils.tryParseToInt(txtId.getText()));
        if(txtName.getText() == null || txtName.getText().trim().equals("")){
            exception.addError("name", "Field can't be empty");
        }
        sel.setName(txtName.getText());

        if(exception.getErrors().size() > 0){
            throw exception;
        }
        return sel;
    }

    @FXML
    public void onBtCancelAction(ActionEvent event){
        Utils.currentStage(event).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initializeNodes();
    }

    private void initializeNodes(){
        Constraints.setTextFieldInteger(txtId);
        Constraints.setTextFieldMaxLength(txtName, 70);
        Constraints.setTextFieldDouble(txtBaseSalary);
        Constraints.setTextFieldMaxLength(txtEmail, 60);
        Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
    }

    public void updateFormData(){
        if(seller == null){
            throw new IllegalStateException("Seller was null");
        }
        txtId.setText(seller.getId() == null ? "" : String.valueOf(seller.getId()));
        txtName.setText(seller.getName());
        Locale.setDefault(Locale.US);
        txtBaseSalary.setText(seller.getBaseSalary() == null ? "" : String.format("%.2f", seller.getBaseSalary()));
        txtEmail.setText(seller.getEmail());
        if(seller.getBirthDate() != null){
            dpBirthDate.setValue(LocalDate.from(LocalDateTime.ofInstant(seller.getBirthDate().toInstant(), ZoneId.systemDefault())));
        }
    }

    private void setErrorMessages(Map<String, String> errors){
        if(errors.containsKey("name")){
            labelErrorName.setText(errors.get("name"));
        }
    }
}
