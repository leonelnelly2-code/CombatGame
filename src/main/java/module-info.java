module com.example.combatgame01 {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.almasb.fxgl.all;
    requires annotations;

    opens com.example.combatgame01 to javafx.fxml;
    exports com.example.combatgame01;
}