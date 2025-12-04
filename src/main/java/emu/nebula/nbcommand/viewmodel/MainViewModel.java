package emu.nebula.nbcommand.viewmodel;

import emu.nebula.nbcommand.service.I18nManager;
import emu.nebula.nbcommand.model.Command;
import emu.nebula.nbcommand.repository.ConfigRepository;
import emu.nebula.nbcommand.service.command.CommandExecutor;
import emu.nebula.nbcommand.service.command.CommandRegistry;
import emu.nebula.nbcommand.service.TypedDataManager;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Main view model for the application.
 * Handles the business logic and state for the main view.
 */
public class MainViewModel {
    private static final Logger logger = LoggerFactory.getLogger(MainViewModel.class);
    
    private final I18nManager i18n = I18nManager.getInstance();

    // Properties for UI binding
    private final StringProperty serverAddress = new SimpleStringProperty();
    private final StringProperty authToken = new SimpleStringProperty();
    private final StringProperty maskedAuthToken = new SimpleStringProperty();
    private final StringProperty commandPreview = new SimpleStringProperty();
    private final StringProperty selectedCommandName = new SimpleStringProperty();
    private final StringProperty commandDetails = new SimpleStringProperty();
    private final StringProperty history = new SimpleStringProperty();
    private final StringProperty uid = new SimpleStringProperty();
    private final ObjectProperty<Command> selectedCommand = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<Command>> commands = new SimpleObjectProperty<>();

    private final ConfigRepository configRepository;
    private final CommandExecutor commandExecutor;
    private final TypedDataManager typedDataManager;

    public MainViewModel() {
        this.configRepository = new ConfigRepository();
        this.typedDataManager = new TypedDataManager();
        this.commandExecutor = new CommandExecutor(
                configRepository.getServerAddress(),
                configRepository.getAuthToken()
        );

        // Load initial configuration
        loadConfiguration();
        
        // Initialize typed data
        typedDataManager.loadItemData();
    }

    /**
     * Load configuration from repository
     */
    private void loadConfiguration() {
        serverAddress.set(configRepository.getServerAddress());
        authToken.set(configRepository.getAuthToken());
        maskedAuthToken.set(configRepository.maskToken(configRepository.getAuthToken()));
    }

    /**
     * Save configuration to repository
     */
    public void saveConfiguration() {
        boolean saved = configRepository.saveConfig(serverAddress.get(), authToken.get());
        if (saved) {
            // Update the command executor with new configuration
            commandExecutor.updateConfiguration(serverAddress.get(), authToken.get());
            addToHistory(i18n.getString("message.config_saved") + ": " + serverAddress.get());
            logger.info("Configuration saved: {}", serverAddress.get());
        } else {
            addToHistory(i18n.getString("message.config_save_failed"));
            logger.error("Failed to save configuration");
        }
        maskedAuthToken.set(configRepository.maskToken(authToken.get()));
    }

    /**
     * Execute selected command
     */
    public void executeCommand(String command) {
        if (command == null || command.isEmpty()) {
            addToHistory(i18n.getString("message.execute_command_failed"));
            logger.warn("Attempted to execute command but none selected");
            return;
        }
        commandExecutor.executeCommand(uid.get(), command, this::addToHistory);
    }

    /**
     * Execute custom command
     */
    public void executeCustomCommand(String customCommand) {
        if (customCommand == null || customCommand.isEmpty()) {
            addToHistory(i18n.getString("message.execute_custom_command_failed"));
            logger.warn("Attempted to execute custom command but it was empty");
            return;
        }
        commandExecutor.executeCommand(uid.get(), customCommand, this::addToHistory);
    }

    /**
     * Load commands by category
     */
    public void loadCommandsByCategory(Map<String, ObservableList<Command>> commandsByCategory) {
        // Create all commands organized by category
        Map<String, List<Command>> allCommands = CommandRegistry.createAllCommands();
        
        // Convert List to ObservableList for each category
        for (Map.Entry<String, List<Command>> entry : allCommands.entrySet()) {
            String category = entry.getKey();
            List<Command> commandList = entry.getValue();
            ObservableList<Command> observableCommandList = FXCollections.observableArrayList(commandList);
            commandsByCategory.put(category, observableCommandList);
        }
    }

    /**
     * Add message to history
     */
    public void addToHistory(String message) {
        String currentHistory = history.get();
        if (currentHistory == null || currentHistory.isEmpty()) {
            history.set(message);
        } else {
            history.set(currentHistory + "\n" + message);
        }
    }

    // Property getters for UI binding
    public StringProperty serverAddressProperty() {
        return serverAddress;
    }

    public StringProperty authTokenProperty() {
        return authToken;
    }

    public StringProperty maskedAuthTokenProperty() {
        return maskedAuthToken;
    }

    public StringProperty commandPreviewProperty() {
        return commandPreview;
    }

    public StringProperty selectedCommandNameProperty() {
        return selectedCommandName;
    }

    public StringProperty commandDetailsProperty() {
        return commandDetails;
    }

    public StringProperty historyProperty() {
        return history;
    }

    public StringProperty uidProperty() {
        return uid;
    }

    public ObjectProperty<Command> selectedCommandProperty() {
        return selectedCommand;
    }

    public ObjectProperty<ObservableList<Command>> commandsProperty() {
        return commands;
    }

    // Getters for direct access
    public String getServerAddress() {
        return serverAddress.get();
    }

    public String getAuthToken() {
        return authToken.get();
    }

    public String getMaskedAuthToken() {
        return maskedAuthToken.get();
    }

    public String getCommandPreview() {
        return commandPreview.get();
    }

    public Command getSelectedCommand() {
        return selectedCommand.get();
    }

    public ObservableList<Command> getCommands() {
        return commands.get();
    }

    public String getHistory() {
        return history.get();
    }

    // Setters
    public void setServerAddress(String serverAddress) {
        this.serverAddress.set(serverAddress);
    }

    public void setAuthToken(String authToken) {
        this.authToken.set(authToken);
    }

    public void setSelectedCommand(Command selectedCommand) {
        this.selectedCommand.set(selectedCommand);
        if (selectedCommand != null) {
            this.selectedCommandName.set(selectedCommand.name());
            this.commandDetails.set(i18n.getString("command.label") + ": " + selectedCommand.name() + "\n\n" + 
                                  i18n.getString("command.description") + ": " + selectedCommand.description() + "\n\n" +
                                  selectedCommand.fullDescription());
        } else {
            this.selectedCommandName.set("");
            this.commandDetails.set("");
        }
    }

    public void setCommands(ObservableList<Command> commands) {
        this.commands.set(commands);
    }

    public void setCommandPreview(String commandPreview) {
        this.commandPreview.set(commandPreview);
    }

    // Getters for services
    public TypedDataManager getTypedDataManager() {
        return typedDataManager;
    }

    public CommandExecutor getCommandExecutor() {
        return commandExecutor;
    }
}