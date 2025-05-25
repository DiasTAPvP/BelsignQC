package com.belman.belsignqc.GUI.Controller;

import com.belman.belsignqc.GUI.Model.OrderModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

public class QAController extends BaseController{

    @FXML private ImageView qaReturnButton;
    @FXML private TableView<OrderModel> qaOrderTable;
    @FXML private TableColumn<OrderModel, String> qaOrderColumn;
    @FXML private TextField qaSearch;

    private ObservableList<OrderModel> allOrders;

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
                new OrderModel("015-05012-111-1"),
                new OrderModel("018-12019-123-3"),
                new OrderModel("014-02028-182-5"),
                new OrderModel("016-01001-123-7"),
                new OrderModel("015-11027-199-8")
        );
        qaOrderTable.setItems(allOrders);
    }

    private void setupOrderSearch() {
        // Create a filtered list wrapping the observable list
        FilteredList<OrderModel> filteredOrders = new FilteredList<>(allOrders, p -> true);

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
        SortedList<OrderModel> sortedOrders = new SortedList<>(filteredOrders);

        // Bind the sorted list comparator to the TableView comparator
        sortedOrders.comparatorProperty().bind(qaOrderTable.comparatorProperty());

        // Add sorted (and filtered) data to the table
        qaOrderTable.setItems(sortedOrders);
    }


}
