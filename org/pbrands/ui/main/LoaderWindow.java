/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.pbrands.ui.DebugConsole
 *  org.pbrands.util.PanicUtil
 */
package org.pbrands.ui.main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import lombok.Generated;
import org.pbrands.hid.HIDDeviceType;
import org.pbrands.hid.uber.RazerDriverHook;
import org.pbrands.model.Product;
import org.pbrands.model.ProductType;
import org.pbrands.model.ServerInfo;
import org.pbrands.model.StartupParams;
import org.pbrands.model.UserType;
import org.pbrands.netty.NettyClient;
import org.pbrands.netty.handler.NettyClientHandler;
import org.pbrands.ui.CustomTitleBar;
import org.pbrands.ui.DebugConsole;
import org.pbrands.ui.LicenseInputPanel;
import org.pbrands.ui.ProgramSelectionPanel;
import org.pbrands.ui.ProgressWindow;
import org.pbrands.ui.components.ParticleBackgroundPanel;
import org.pbrands.ui.latest.IntegratedSettingsPanel;
import org.pbrands.util.BootstrapLoader;
import org.pbrands.util.ConnectionConstants;
import org.pbrands.util.HWID;
import org.pbrands.util.LoaderUtil;
import org.pbrands.util.MacUtil;
import org.pbrands.util.PanicUtil;
import org.pbrands.util.RegistryUtil;
import org.pbrands.util.ServerRegistry;

public class LoaderWindow
extends JFrame {
    public static LoaderWindow instance;
    private BootstrapLoader bootstrapLoader;
    private static final String LOCK_FILE = "app.lock";
    private static FileLock lock;
    private static FileChannel channel;
    private List<Product> availableProducts;
    private ServerRegistry serverRegistry;
    private final CustomTitleBar customTitleBar;
    private final JPanel mainPanel;
    private final LicenseInputPanel licenseInputPanel;
    private ProgramSelectionPanel programSelectionPanel;
    private String authToken;
    private String username;
    private long subscriptionExpiry;
    private byte[] pfpBytes;
    private final Gson gson = new Gson();
    private final ProgressWindow progressWindow;
    private UserType userType;
    private NettyClient nettyClient;
    private JLabel botStatusLabel;
    private JPanel loadingContainer;
    private Timer loadingDotsTimer;
    private int dotCount = 0;

    public NettyClient getNettyClient() {
        return this.nettyClient;
    }

    public LoaderWindow(ProgressWindow progressWindow) {
        this.progressWindow = progressWindow;
        instance = this;
        if (!this.isSingleInstance()) {
            JOptionPane.showMessageDialog(null, "Aplikacja jest ju\u017c uruchomiona.", "Instancja Aplikacji", 2);
            System.exit(0);
        }
        this.setTitle("LifeBot Loader");
        this.setSize(900, 600);
        this.setUndecorated(true);
        this.setBackground(Color.decode("#1E1E1E"));
        this.setDefaultCloseOperation(0);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        ImageIcon icon = new ImageIcon(this.getClass().getResource("/images/favicon.png"));
        Image originalImage = icon.getImage();
        List<Image> icons = Arrays.asList(originalImage.getScaledInstance(16, 16, 4), originalImage.getScaledInstance(24, 24, 4), originalImage.getScaledInstance(32, 32, 4), originalImage.getScaledInstance(48, 48, 4), originalImage.getScaledInstance(64, 64, 4), originalImage.getScaledInstance(128, 128, 4), originalImage.getScaledInstance(256, 256, 4));
        this.setIconImages(icons);
        this.getRootPane().setBorder(new LineBorder(Color.decode("#3E3E42"), 1));
        this.mainPanel = new ParticleBackgroundPanel();
        this.mainPanel.setLayout(new BorderLayout());
        this.add((Component)this.mainPanel, "Center");
        ImageIcon logoIcon = new ImageIcon(this.getClass().getResource("/images/logo.png"));
        this.customTitleBar = new CustomTitleBar(this, logoIcon, () -> {
            if (this.bootstrapLoader != null) {
                this.bootstrapLoader.terminateExternalProcess();
            }
            System.exit(0);
        });
        this.customTitleBar.setDragListener(() -> ((ParticleBackgroundPanel)this.mainPanel).setAnimationPaused(true), () -> ((ParticleBackgroundPanel)this.mainPanel).setAnimationPaused(false));
        this.mainPanel.add((Component)this.customTitleBar, "North");
        this.licenseInputPanel = new LicenseInputPanel(e -> this.authenticateLicense());
        this.mainPanel.add((Component)this.licenseInputPanel, "Center");
        this.availableProducts = new ArrayList<Product>();
        this.addWindowListener(new WindowAdapter(){

            @Override
            public void windowClosing(WindowEvent e) {
                LoaderWindow.this.bootstrapLoader.terminateExternalProcess();
                LoaderWindow.this.dispose();
                System.exit(0);
            }
        });
        this.attemptAutoLogin();
    }

    private void attemptAutoLogin() {
        String storedToken = RegistryUtil.getAuthToken();
        if (storedToken != null && !storedToken.isEmpty()) {
            this.licenseInputPanel.showLoading();
            new Thread(() -> {
                try {
                    System.out.println("[DEBUG] Attempting auto-login with stored token...");
                    if (this.nettyClient == null || !this.nettyClient.isConnected()) {
                        this.nettyClient = new NettyClient(ConnectionConstants.getInstance().getServerHost(), ConnectionConstants.getInstance().getServerPort(), storedToken, HWID.generate(), ProductType.LOADER);
                        CompletableFuture authFuture = this.nettyClient.authenticateWithToken();
                        this.nettyClient.start().sync();
                        System.out.println("[DEBUG] Connected. Waiting for AUTH response...");
                        NettyClientHandler.LoginResult result = (NettyClientHandler.LoginResult)authFuture.get();
                        System.out.println("[DEBUG] Auto-login response received. Success: " + result.success());
                        if (result.success()) {
                            this.authToken = result.token();
                            RegistryUtil.setAuthToken(this.authToken);
                            String role = result.role();
                            String serverUsername = result.username();
                            long subscriptionExpiry = result.subscriptionExpiry();
                            byte[] pfpBytes = result.profilePicture();
                            System.out.println("[DEBUG] Auto-login successful. Role: " + role);
                            for (UserType type : UserType.values()) {
                                if (!type.getApiRoleName().equals(role)) continue;
                                this.userType = type;
                                break;
                            }
                            if (this.userType == null) {
                                this.userType = UserType.STANDARD;
                            }
                            if (this.userType == UserType.ADMIN) {
                                SwingUtilities.invokeLater(DebugConsole::showConsole);
                            }
                            try {
                                System.out.println("[DEBUG] Requesting DLL...");
                                String checksum = LoaderUtil.getLoaderChecksum();
                                byte[] dllBytes = (byte[])this.nettyClient.requestLoaderDLL(checksum).get();
                                System.out.println("[DEBUG] DLL received, size: " + dllBytes.length);
                                SwingUtilities.invokeLater(() -> {
                                    System.out.println("[DEBUG] Loading loader util...");
                                    if (dllBytes.length > 0) {
                                        LoaderUtil.load(dllBytes);
                                    } else {
                                        LoaderUtil.load();
                                    }
                                    System.out.println("[DEBUG] Proceeding to fetch products...");
                                    this.proceedToFetchProducts(this.authToken, serverUsername, subscriptionExpiry, pfpBytes);
                                });
                            }
                            catch (Exception e) {
                                System.out.println("[DEBUG] Error fetching DLL: " + e.getMessage());
                                SwingUtilities.invokeLater(() -> this.licenseInputPanel.showError("B\u0142\u0105d pobierania DLL: " + e.getMessage()));
                            }
                        } else {
                            System.out.println("[DEBUG] Auto-login failed. Error: " + result.error());
                            RegistryUtil.setAuthToken("");
                            SwingUtilities.invokeLater(() -> this.licenseInputPanel.hideLoading());
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("[DEBUG] Exception during auto-login: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> this.licenseInputPanel.hideLoading());
                }
            }).start();
        }
    }

    private void authenticateLicense() {
        String username = this.licenseInputPanel.getUsername();
        String password = this.licenseInputPanel.getPassword();
        if (!this.licenseInputPanel.areCredentialsValid()) {
            this.licenseInputPanel.showError("Prosz\u0119 wprowadzi\u0107 nazw\u0119 u\u017cytkownika i has\u0142o.");
            return;
        }
        this.authenticateUser(username, password);
    }

    private void authenticateUser(String username, String password) {
        this.licenseInputPanel.showLoading();
        new Thread(() -> {
            try {
                System.out.println("[DEBUG] Starting authentication for user: " + username);
                if (this.nettyClient == null || !this.nettyClient.isConnected()) {
                    System.out.println("[DEBUG] NettyClient is null or disconnected. Connecting...");
                    if (this.nettyClient != null) {
                        this.nettyClient.disconnect();
                    }
                    this.nettyClient = new NettyClient(ConnectionConstants.getInstance().getServerHost(), ConnectionConstants.getInstance().getServerPort(), null, HWID.generate(), ProductType.LOADER);
                    this.nettyClient.start().sync();
                    System.out.println("[DEBUG] NettyClient connected successfully.");
                } else {
                    System.out.println("[DEBUG] NettyClient is already connected.");
                }
                System.out.println("[DEBUG] Sending login request...");
                NettyClientHandler.LoginResult result = (NettyClientHandler.LoginResult)this.nettyClient.login(username, password).get();
                System.out.println("[DEBUG] Login response received. Success: " + result.success());
                if (result.success()) {
                    this.authToken = result.token();
                    this.nettyClient.setToken(this.authToken);
                    RegistryUtil.setAuthToken(this.authToken);
                    String role = result.role();
                    String serverUsername = result.username();
                    long subscriptionExpiry = result.subscriptionExpiry();
                    byte[] pfpBytes = result.profilePicture();
                    System.out.println("[DEBUG] Login successful. Role: " + role);
                    for (UserType type : UserType.values()) {
                        if (!type.getApiRoleName().equals(role)) continue;
                        this.userType = type;
                        break;
                    }
                    if (this.userType == null) {
                        this.userType = UserType.STANDARD;
                    }
                    if (this.userType == UserType.ADMIN) {
                        SwingUtilities.invokeLater(DebugConsole::showConsole);
                    }
                    try {
                        System.out.println("[DEBUG] Requesting DLL...");
                        String checksum = LoaderUtil.getLoaderChecksum();
                        byte[] dllBytes = (byte[])this.nettyClient.requestLoaderDLL(checksum).get();
                        System.out.println("[DEBUG] DLL received, size: " + dllBytes.length);
                        SwingUtilities.invokeLater(() -> {
                            System.out.println("[DEBUG] Loading loader util...");
                            if (dllBytes.length > 0) {
                                LoaderUtil.load(dllBytes);
                            } else {
                                LoaderUtil.load();
                            }
                            System.out.println("[DEBUG] Proceeding to fetch products...");
                            this.proceedToFetchProducts(this.authToken, serverUsername, subscriptionExpiry, pfpBytes);
                        });
                    }
                    catch (Exception e) {
                        System.out.println("[DEBUG] Error fetching DLL: " + e.getMessage());
                        SwingUtilities.invokeLater(() -> this.licenseInputPanel.showError("B\u0142\u0105d pobierania DLL: " + e.getMessage()));
                    }
                } else {
                    System.out.println("[DEBUG] Login failed. Error: " + result.error());
                    SwingUtilities.invokeLater(() -> this.licenseInputPanel.showError(result.error()));
                }
            }
            catch (Exception e) {
                System.out.println("[DEBUG] Exception during authentication: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> this.licenseInputPanel.showError("B\u0142\u0105d po\u0142\u0105czenia: " + e.getMessage()));
            }
        }).start();
    }

    private void proceedToFetchProducts(String token, String username, long subscriptionExpiry, byte[] pfpBytes) {
        this.authToken = token;
        this.username = username;
        this.subscriptionExpiry = subscriptionExpiry;
        this.pfpBytes = pfpBytes;
        this.fetchProductsFromServer(token);
    }

    private void fetchProductsFromServer(String token) {
        SwingUtilities.invokeLater(this.licenseInputPanel::showLoading);
        this.serverRegistry = new ServerRegistry();
        this.serverRegistry.getServers().add(new ServerInfo(1, "Main Server", ConnectionConstants.getInstance().getServerHost(), ConnectionConstants.getInstance().getServerPort()));
        if (!MacUtil.isMacOs()) {
            System.out.println("[DEBUG] Requesting Uber DLL Key...");
            byte[] decodedKey = Base64.getDecoder().decode(this.nettyClient.requestDLLUberKey().get());
            System.out.println("[DEBUG] Uber DLL Key received.");
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");
            System.out.println("[DEBUG] Requesting Uber DLL...");
            byte[] bytes = this.nettyClient.requestDLLUber().get();
            System.out.println("[DEBUG] Uber DLL received. Initializing RazerDriverHook...");
            new RazerDriverHook(bytes, secretKey).close();
            System.out.println("[DEBUG] RazerDriverHook initialized and closed.");
        }
        new Thread(() -> {
            try {
                System.out.println("[DEBUG] Fetching products list...");
                String json = (String)this.nettyClient.fetchProducts().get();
                System.out.println("[DEBUG] Products list received: " + json);
                List serverProducts = (List)this.gson.fromJson(json, new TypeToken<List<ServerProductDTO>>(this){}.getType());
                this.availableProducts = new ArrayList<Product>();
                for (ServerProductDTO dto : serverProducts) {
                    Product product = new Product(dto.getId(), dto.getName(), dto.getDownloadUrl(), dto.getFileName(), dto.getFolderName(), dto.getDescription(), dto.getSubscriptionValidity(), dto.getLastUpdate(), dto.isAuthorized(), dto.isHidRequired(), dto.getCurrentChecksum(), dto.isBeta());
                    this.availableProducts.add(product);
                }
                SwingUtilities.invokeLater(() -> {
                    this.licenseInputPanel.hideLoading();
                    this.proceedToProgramSelectionAfterFetch();
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                this.handleError("Nie uda\u0142o si\u0119 pobra\u0107 produkt\u00f3w.");
            }
        }).start();
    }

    private void handleError(String message) {
        SwingUtilities.invokeLater(() -> this.licenseInputPanel.showError(message));
    }

    public void hideLoadingSpinner() {
        SwingUtilities.invokeLater(() -> {
            if (this.loadingContainer != null) {
                this.loadingContainer.setVisible(false);
            }
            if (this.loadingDotsTimer != null) {
                this.loadingDotsTimer.stop();
            }
            if (this.botStatusLabel != null) {
                this.botStatusLabel.setText("Program uruchomiony w tle");
            }
        });
    }

    public void setBotStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            if (this.botStatusLabel != null) {
                this.botStatusLabel.setText(status);
            }
        });
    }

    private void showBotLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 30, 20, 30));
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, 1));
        centerPanel.setOpaque(false);
        this.botStatusLabel = new JLabel("\u0141adowanie");
        this.botStatusLabel.setFont(new Font("Segoe UI", 1, 14));
        this.botStatusLabel.setForeground(new Color(200, 200, 200));
        this.botStatusLabel.setAlignmentX(0.5f);
        centerPanel.add(this.botStatusLabel);
        this.dotCount = 0;
        this.loadingDotsTimer = new Timer(400, e -> {
            this.dotCount = (this.dotCount + 1) % 4;
            String dots = ".".repeat(this.dotCount);
            this.botStatusLabel.setText("\u0141adowanie" + dots);
        });
        this.loadingDotsTimer.start();
        panel.add((Component)centerPanel, "Center");
        JPanel bottomPanel = new JPanel(new FlowLayout(1, 15, 0));
        bottomPanel.setOpaque(false);
        JButton panicButton = new JButton("Jestem sprawdzany");
        panicButton.setBackground(new Color(220, 53, 69));
        panicButton.setForeground(Color.WHITE);
        panicButton.setFont(new Font("Segoe UI", 1, 12));
        panicButton.setPreferredSize(new Dimension(160, 36));
        panicButton.setFocusPainted(false);
        panicButton.addActionListener(e -> PanicUtil.handlePanicAction());
        JButton quitButton = new JButton("Wy\u0142\u0105cz");
        quitButton.setBackground(new Color(108, 117, 125));
        quitButton.setForeground(Color.WHITE);
        quitButton.setFont(new Font("Segoe UI", 0, 12));
        quitButton.setPreferredSize(new Dimension(100, 36));
        quitButton.setFocusPainted(false);
        quitButton.addActionListener(e -> {
            if (this.loadingDotsTimer != null) {
                this.loadingDotsTimer.stop();
            }
            this.bootstrapLoader.terminateExternalProcess();
            System.exit(0);
        });
        bottomPanel.add(panicButton);
        bottomPanel.add(quitButton);
        panel.add((Component)bottomPanel, "South");
        this.setSize(340, 180);
        this.setLocationRelativeTo(null);
        this.setFocusable(false);
        this.setFocusableWindowState(true);
        this.setOpacity(0.95f);
        this.mainPanel.removeAll();
        this.mainPanel.add((Component)this.customTitleBar, "North");
        this.mainPanel.add((Component)panel, "Center");
        this.mainPanel.revalidate();
        this.mainPanel.repaint();
    }

    private void proceedToProgramSelectionAfterFetch() {
        CardLayout cardLayout = new CardLayout();
        JPanel contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);
        Runnable onOpenSettings = () -> cardLayout.show(contentPanel, "Ustawienia");
        Runnable onBackToMain = () -> cardLayout.show(contentPanel, "Wyb\u00f3r programu");
        if (this.programSelectionPanel == null) {
            this.programSelectionPanel = new ProgramSelectionPanel(this.serverRegistry, this.availableProducts, e -> {
                Product selectedProduct = this.programSelectionPanel.getSelectedProgram();
                if (selectedProduct != null) {
                    ServerInfo server = this.serverRegistry.getServers().getFirst();
                    StartupParams startupParams = new StartupParams();
                    startupParams.serverHost = server.getHost();
                    startupParams.serverPort = server.getPort();
                    startupParams.token = this.authToken;
                    startupParams.folderName = selectedProduct.getFolderName();
                    startupParams.debug = ConnectionConstants.getInstance().isDebug();
                    if (selectedProduct.isHidRequired()) {
                        HIDDeviceType hidDeviceType = RegistryUtil.getHIDDeviceType();
                        startupParams.device = hidDeviceType.name();
                    }
                    this.showBotLogPanel();
                    this.bootstrapLoader = new BootstrapLoader(this);
                    this.bootstrapLoader.downloadAndRunEncrypted(this, startupParams, selectedProduct);
                }
            }, this.userType, onOpenSettings, this::performLogout, this.username, this.subscriptionExpiry, this.pfpBytes);
        } else {
            this.programSelectionPanel.updateProducts(this.availableProducts);
        }
        this.setVisible(false);
        int width = 880;
        int height = 660;
        this.setSize(width, height);
        this.setLocationRelativeTo(null);
        contentPanel.add((Component)this.programSelectionPanel, "Wyb\u00f3r programu");
        IntegratedSettingsPanel settingsPanel = new IntegratedSettingsPanel(this.programSelectionPanel, this.authToken, onBackToMain);
        contentPanel.add((Component)settingsPanel, "Ustawienia");
        this.mainPanel.removeAll();
        this.mainPanel.add((Component)this.customTitleBar, "North");
        this.mainPanel.add((Component)contentPanel, "Center");
        this.mainPanel.revalidate();
        this.mainPanel.repaint();
        this.setVisible(true);
    }

    public void performLogout() {
        RegistryUtil.setAuthToken("");
        this.authToken = null;
        this.userType = null;
        this.username = null;
        this.subscriptionExpiry = 0L;
        this.pfpBytes = null;
        if (this.nettyClient != null) {
            this.nettyClient.disconnect();
            this.nettyClient = null;
        }
        this.mainPanel.removeAll();
        this.mainPanel.add((Component)this.customTitleBar, "North");
        this.licenseInputPanel.hideLoading();
        this.licenseInputPanel.clearPassword();
        this.mainPanel.add((Component)this.licenseInputPanel, "Center");
        this.mainPanel.revalidate();
        this.mainPanel.repaint();
        this.programSelectionPanel = null;
    }

    private boolean isSingleInstance() {
        try {
            Path lockFilePath = Paths.get(System.getProperty("user.home"), LOCK_FILE);
            File lockFile = lockFilePath.toFile();
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }
            if ((lock = (channel = FileChannel.open(lockFilePath, StandardOpenOption.WRITE)).tryLock()) == null) {
                channel.close();
                return false;
            }
            Runtime.getRuntime().addShutdownHook(new Thread(this::releaseLock));
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void releaseLock() {
        try {
            if (lock != null) {
                lock.release();
                lock = null;
            }
            if (channel != null && channel.isOpen()) {
                channel.close();
                channel = null;
            }
            Path lockFilePath = Paths.get(System.getProperty("user.home"), LOCK_FILE);
            Files.deleteIfExists(lockFilePath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ServerProductDTO {
        private int id;
        private String name;
        private String downloadUrl;
        private String fileName;
        private String folderName;
        private String description;
        private String subscriptionValidity;
        private String lastUpdate;
        private boolean authorized;
        private boolean hidRequired;
        private String currentChecksum;
        private boolean beta;

        private ServerProductDTO() {
        }

        @Generated
        public int getId() {
            return this.id;
        }

        @Generated
        public String getName() {
            return this.name;
        }

        @Generated
        public String getDownloadUrl() {
            return this.downloadUrl;
        }

        @Generated
        public String getFileName() {
            return this.fileName;
        }

        @Generated
        public String getFolderName() {
            return this.folderName;
        }

        @Generated
        public String getDescription() {
            return this.description;
        }

        @Generated
        public String getSubscriptionValidity() {
            return this.subscriptionValidity;
        }

        @Generated
        public String getLastUpdate() {
            return this.lastUpdate;
        }

        @Generated
        public boolean isAuthorized() {
            return this.authorized;
        }

        @Generated
        public boolean isHidRequired() {
            return this.hidRequired;
        }

        @Generated
        public String getCurrentChecksum() {
            return this.currentChecksum;
        }

        @Generated
        public boolean isBeta() {
            return this.beta;
        }

        @Generated
        public void setId(int id) {
            this.id = id;
        }

        @Generated
        public void setName(String name) {
            this.name = name;
        }

        @Generated
        public void setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        @Generated
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Generated
        public void setFolderName(String folderName) {
            this.folderName = folderName;
        }

        @Generated
        public void setDescription(String description) {
            this.description = description;
        }

        @Generated
        public void setSubscriptionValidity(String subscriptionValidity) {
            this.subscriptionValidity = subscriptionValidity;
        }

        @Generated
        public void setLastUpdate(String lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        @Generated
        public void setAuthorized(boolean authorized) {
            this.authorized = authorized;
        }

        @Generated
        public void setHidRequired(boolean hidRequired) {
            this.hidRequired = hidRequired;
        }

        @Generated
        public void setCurrentChecksum(String currentChecksum) {
            this.currentChecksum = currentChecksum;
        }

        @Generated
        public void setBeta(boolean beta) {
            this.beta = beta;
        }
    }
}

