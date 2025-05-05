package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.GUI.Model.OrderNumberModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

public class QAController extends BaseController{

    @FXML private ImageView qaReturnButton;
    @FXML private TableView<OrderNumberModel> qaOrderTable;
    @FXML private TableColumn<OrderNumberModel, String> qaOrderColumn;
    @FXML private TextField qaSearch;

    private ObservableList<OrderNumberModel> allOrders;

    @FXML
    private void initialize() {
        // Configure the order table column
        qaOrderColumn.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));

        // Add mock data to table
        loadMockOrders();

        // Set up the search functionality
        setupOrderSearch();
    }

    @FXML
    private void handleLogout() {
        screenManager.setScreen("login");
    }

    private void loadMockOrders() {
        allOrders = FXCollections.observableArrayList(
                new OrderNumberModel("015-05012-111-1"),
                new OrderNumberModel("018-12019-123-3"),
                new OrderNumberModel("014-02028-182-5"),
                new OrderNumberModel("016-01001-123-7"),
                new OrderNumberModel("015-11027-199-8")
        );
        qaOrderTable.setItems(allOrders);
    }

    private void setupOrderSearch() {
        // Create a filtered list wrapping the observable list
        FilteredList<OrderNumberModel> filteredOrders = new FilteredList<>(allOrders, p -> true);

        // Add listener to the search field
        qaSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredOrders.setPredicate(order -> {
                // If search field is empty, display all orders
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare order number with filter text
                String lowerCaseFilter = newValue.toLowerCase();

                if (order.getOrderNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Filter matches order number
                }
                return false; // Does not match
            });
        });

        // Wrap the filtered list in a sorted list
        SortedList<OrderNumberModel> sortedOrders = new SortedList<>(filteredOrders);

        // Bind the sorted list comparator to the TableView comparator
        sortedOrders.comparatorProperty().bind(qaOrderTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        qaOrderTable.setItems(sortedOrders);
    }


}
