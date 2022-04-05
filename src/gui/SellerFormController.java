package gui;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class SellerFormController implements Initializable {

    private Seller seller;
    private SellerService sellerService;
    private DepartmentService departmentService;

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
    private ComboBox<Department> comboBoxDepartment;
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

    private ObservableList<Department> obsList;

    public void setSeller(Seller seller){
        this.seller = seller;
    }
    public void setServices(SellerService sellerService, DepartmentService departmentService){
        this.sellerService = sellerService;
        this.departmentService = departmentService;
    }

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

        //Name checking
        if(txtName.getText() == null || txtName.getText().trim().equals("")){
            exception.addError("name", "Field can't be empty");
        }
        sel.setName(txtName.getText());

        //Email checking
        if(txtEmail.getText() == null || txtEmail.getText().trim().equals("")){
            exception.addError("email", "Field can't be empty");
        }
        sel.setEmail(txtEmail.getText());

        //Birthdate checking
        if(dpBirthDate.getValue() != null){
            Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
            sel.setBirthDate(Date.from(instant));
        } else {
            exception.addError("birthDate", "Field can't be empty");
        }

        //BaseSalary checking
        if(txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")){
            exception.addError("baseSalary", "Field can't be empty");
        }
        sel.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));

        sel.setDepartment(comboBoxDepartment.getValue());

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

        initializeComboBoxDepartment();
    }

    public void updateFormData(){
        if(seller == null){
            throw new IllegalStateException("Seller was null");
        }
        txtId.setText(seller.getId() == null ? "" : String.valueOf(seller.getId()));
        txtName.setText(seller.getName());
        Locale.setDefault(Locale.US);
        txtBaseSalary.setText(seller.getBaseSalary() == null ? "0.00" : String.format("%.2f", seller.getBaseSalary()));
        txtEmail.setText(seller.getEmail());
        if(seller.getBirthDate() != null){
            dpBirthDate.setValue(LocalDate.from(LocalDateTime.ofInstant(seller.getBirthDate().toInstant(), ZoneId.systemDefault())));
        }

        if(seller.getBirthDate() != null){
            comboBoxDepartment.setValue(seller.getDepartment());
        } else {
            comboBoxDepartment.getSelectionModel().selectFirst();
        }
    }

    public void loadAssociatedObjects(){
        if(departmentService == null){
            throw new IllegalStateException("DeparmentService was null");
        }
        List<Department> list = departmentService.findAll();
        obsList = FXCollections.observableArrayList(list);
        comboBoxDepartment.setItems(obsList);
    }

    private void setErrorMessages(Map<String, String> errors){
        labelErrorName.setText(errors.containsKey("name") ? errors.get("name") : "");
        labelErrorEmail.setText(errors.containsKey("email") ? errors.get("email") : "");
        labelErrorBaseSalary.setText(errors.containsKey("baseSalary") ? errors.get("baseSalary") : "");
        labelErrorBirthDate.setText(errors.containsKey("birthDate") ? errors.get("birthDate") : "");
    }

    private void initializeComboBoxDepartment() {
        Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
            @Override
            protected void updateItem(Department item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        };
        comboBoxDepartment.setCellFactory(factory);
        comboBoxDepartment.setButtonCell(factory.call(null));
    }

}
