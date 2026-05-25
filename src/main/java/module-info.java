module com.game.dice {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;

    opens com.game.dice to javafx.fxml;
    exports com.game.dice;
    exports com.game.dice.model;
    exports com.game.dice.engine;
    exports com.game.dice.ui;
}
