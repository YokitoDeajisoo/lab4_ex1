package org.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Main extends Application {
    private final DatabaseManager db = new DatabaseManager();
    private final TableView<Patient> table = new TableView<>();
    private final ObservableList<Patient> patientData = FXCollections.observableArrayList();

    private final TextField nameField = new TextField();
    private final TextField birthDateField = new TextField();
    private final TextField weightField = new TextField();
    private final CheckBox declarationCheck = new CheckBox("Підписана декларація");
    private final TextField diagnosisField = new TextField();

    private final ComboBox<String> sortFieldCombo = new ComboBox<>();
    private final CheckBox sortAscCheck = new CheckBox("За зростанням");

    private final ComboBox<String> searchFieldCombo = new ComboBox<>();
    private final TextField searchInput = new TextField();

    @Override
    public void start(Stage primaryStage) {
        showHelpDialog(); // Обов'язкова довідка при запуску

        primaryStage.setTitle("Практичне заняття №4 — Пацієнти сімейного лікаря (Варіант 1)");

        setupTable();


        // Панель введення/редагування даних
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(8);
        formGrid.setPadding(new Insets(10));
        formGrid.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 5;");

        nameField.setPromptText("Прізвище Ім'я По батькові");
        birthDateField.setPromptText("РРРР-ММ-ДД (напр. 1995-08-24)");
        weightField.setPromptText("Вага в кг (напр. 72.5)");
        diagnosisField.setPromptText("Діагноз / примітка");

        formGrid.add(new Label("ПІБ пацієнта:"), 0, 0);
        formGrid.add(nameField, 1, 0);
        formGrid.add(new Label("Дата народження:"), 2, 0);
        formGrid.add(birthDateField, 3, 0);

        formGrid.add(new Label("Вага (кг):"), 0, 1);
        formGrid.add(weightField, 1, 1);
        formGrid.add(new Label("Діагноз:"), 2, 1);
        formGrid.add(diagnosisField, 3, 1);
        formGrid.add(declarationCheck, 1, 2);

        // Кнопки CRUD
        Button addBtn = new Button("Додати запис");
        Button updateBtn = new Button("Редагувати вибраний");
        Button deleteBtn = new Button("Видалити вибраний");
        Button clearBtn = new Button("Очистити поля");
        Button helpBtn = new Button("Довідка");

        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        updateBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        HBox crudBox = new HBox(10, addBtn, updateBtn, deleteBtn, clearBtn, helpBtn);
        crudBox.setPadding(new Insets(5, 0, 5, 0));

        // Панель сортування та пошуку
        sortFieldCombo.getItems().addAll("ID", "ПІБ", "Дата народження", "Вага", "Діагноз");
        sortFieldCombo.setValue("ID");
        sortAscCheck.setSelected(true);
        Button sortBtn = new Button("Впорядкувати");

        searchFieldCombo.getItems().addAll("ПІБ", "Діагноз", "Дата народження");
        searchFieldCombo.setValue("ПІБ");
        searchInput.setPromptText("Текст для пошуку...");
        Button searchBtn = new Button("Знайти");
        Button resetBtn = new Button("Показати всіх");

        HBox filterBox = new HBox(10,
                new Label("Сортувати за:"), sortFieldCombo, sortAscCheck, sortBtn,
                new Separator(),
                new Label("Пошук за:"), searchFieldCombo, searchInput, searchBtn, resetBtn
        );
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dddddd;");

        // Обробники подій
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameField.setText(newSel.getFullName());
                birthDateField.setText(newSel.getBirthDate());
                weightField.setText(String.valueOf(newSel.getWeight()));
                declarationCheck.setSelected(newSel.isHasDeclaration());
                diagnosisField.setText(newSel.getDiagnosis());
            }
        });

        addBtn.setOnAction(e -> handleAdd());
        updateBtn.setOnAction(e -> handleUpdate());
        deleteBtn.setOnAction(e -> handleDelete());
        clearBtn.setOnAction(e -> clearForm());
        helpBtn.setOnAction(e -> showHelpDialog());
        sortBtn.setOnAction(e -> loadPatients());
        searchBtn.setOnAction(e -> handleSearch());
        resetBtn.setOnAction(e -> {
            searchInput.clear();
            loadPatients();
        });

        VBox root = new VBox(10, filterBox, table, formGrid, crudBox);
        root.setPadding(new Insets(12));

        primaryStage.setScene(new Scene(root, 980, 620));
        primaryStage.show();
    }

    private void setupTable() {
        TableColumn<Patient, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<Patient, String> nameCol = new TableColumn<>("ПІБ пацієнта");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(240);

        TableColumn<Patient, String> birthCol = new TableColumn<>("Дата народження");
        birthCol.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        birthCol.setPrefWidth(130);

        TableColumn<Patient, Double> weightCol = new TableColumn<>("Вага (кг)");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));
        weightCol.setPrefWidth(90);

        TableColumn<Patient, String> declCol = new TableColumn<>("Декларація");
        declCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isHasDeclaration() ? "Так" : "Ні"));
        declCol.setPrefWidth(95);

        TableColumn<Patient, String> diagCol = new TableColumn<>("Діагноз");
        diagCol.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        diagCol.setPrefWidth(330);

        table.getColumns().addAll(idCol, nameCol, birthCol, weightCol, declCol, diagCol);
        table.setItems(patientData);
    }

    private Patient validateAndBuildPatient(int id) throws InvalidPatientDataException {
        String name = nameField.getText().trim();
        if (name.isEmpty() || name.matches(".*\\d.*")) {
            throw new InvalidPatientDataException("ПІБ не може бути порожнім або містити цифри!");
        }

        String dateStr = birthDateField.getText().trim();
        try {
            LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidPatientDataException("Некоректна дата народження! Використовуйте формат РРРР-ММ-ДД (наприклад, 1990-05-15).");
        }

        double weight;
        try {
            weight = Double.parseDouble(weightField.getText().trim().replace(",", "."));
            if (weight <= 1.0 || weight > 350.0) {
                throw new InvalidPatientDataException("Вага повинна бути в межах від 1 до 350 кг!");
            }
        } catch (NumberFormatException e) {
            throw new InvalidPatientDataException("Поле 'Вага' повинно містити коректне число!");
        }

        String diagnosis = diagnosisField.getText().trim();
        if (diagnosis.isEmpty()) {
            throw new InvalidPatientDataException("Поле 'Діагноз' не може бути порожнім!");
        }

        return new Patient(id, name, dateStr, weight, declarationCheck.isSelected(), diagnosis);
    }

    private void handleAdd() {
        try {
            Patient newPatient = validateAndBuildPatient(0);
            db.addPatient(newPatient);
            loadPatients();
            clearForm();
            showInfo("Успіх", "Нового пацієнта успішно додано до бази даних!");
        } catch (InvalidPatientDataException | SQLException ex) {
            showError("Помилка додавання", ex.getMessage());
        }
    }

    private void handleUpdate() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Увага", "Спочатку виберіть рядок пацієнта у таблиці для редагування!");
            return;
        }
        try {
            Patient updated = validateAndBuildPatient(selected.getId());
            db.updatePatient(updated);
            loadPatients();
            showInfo("Успіх", "Дані пацієнта ID=" + selected.getId() + " оновлено!");
        } catch (InvalidPatientDataException | SQLException ex) {
            showError("Помилка редагування", ex.getMessage());
        }
    }

    private void handleDelete() {
        Patient selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Увага", "Спочатку виберіть пацієнта у таблиці для видалення!");
            return;
        }
        try {
            db.deletePatient(selected.getId());
            loadPatients();
            clearForm();
            showInfo("Видалено", "Запис пацієнта '" + selected.getFullName() + "' видалено з БД.");
        } catch (SQLException ex) {
            showError("Помилка бази даних", ex.getMessage());
        }
    }

    private void loadPatients() {
        String col = mapFieldToColumn(sortFieldCombo.getValue());
        try {
            patientData.setAll(db.getAllPatients(col, sortAscCheck.isSelected()));
        } catch (SQLException e) {
            showError("Помилка завантаження даних", e.getMessage());
        }
    }

    private void handleSearch() {
        String keyword = searchInput.getText().trim();
        if (keyword.isEmpty()) {
            loadPatients();
            return;
        }
        String col = mapFieldToColumn(searchFieldCombo.getValue());
        try {
            patientData.setAll(db.searchPatients(col, keyword));
        } catch (SQLException e) {
            showError("Помилка пошуку", e.getMessage());
        }
    }

    private String mapFieldToColumn(String uiName) {
        if (uiName == null) return "id";
        switch (uiName) {
            case "ПІБ": return "full_name";
            case "Дата народження": return "birth_date";
            case "Вага": return "weight";
            case "Діагноз": return "diagnosis";
            default: return "id";
        }
    }

    private void clearForm() {
        nameField.clear();
        birthDateField.clear();
        weightField.clear();
        declarationCheck.setSelected(false);
        diagnosisField.clear();
        table.getSelectionModel().clearSelection();
    }

    private void showHelpDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Довідка — Практичне заняття №4");
        alert.setHeaderText("Система обліку «Пацієнти сімейного лікаря» (JDBC + SQLite)");
        alert.setContentText(
                "Призначення програми:\n" +
                        "Управління базою даних пацієнтів сімейного лікаря за допомогою технології JDBC.\n\n" +
                        "Команди управління:\n" +
                        "• Додати запис — заповніть поля знизу та натисніть зелену кнопку.\n" +
                        "• Редагувати — клацніть на пацієнта у таблиці, змініть дані та натисніть синю кнопку.\n" +
                        "• Видалити — виберіть запис у таблиці та натисніть червону кнопку.\n" +
                        "• Впорядкувати — оберіть поле та напрямок сортування у верхній панелі.\n" +
                        "• Знайти — введіть фрагмент тексту та натисніть 'Знайти'."
        );
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Помилка виконання операції");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}