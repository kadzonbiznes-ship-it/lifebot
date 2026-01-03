/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.ui.overlay.MiniGameWindow$GameType
 */
package org.pbrands.ui.overlay;

import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.type.ImBoolean;
import org.pbrands.ui.overlay.MiniGamePong;
import org.pbrands.ui.overlay.MiniGameSnake;
import org.pbrands.ui.overlay.MiniGameWindow;

public class MiniGameWindow {
    private boolean windowOpen = false;
    private boolean windowFocused = false;
    private GameType currentGame = null;
    private final ImBoolean windowOpenBool = new ImBoolean(true);
    private boolean needsCenter = false;
    private final MiniGameSnake gameSnake = new MiniGameSnake();
    private final MiniGamePong gamePong = new MiniGamePong();
    private static final int VK_LEFT = 37;
    private static final int VK_UP = 38;
    private static final int VK_RIGHT = 39;
    private static final int VK_DOWN = 40;
    private static final int VK_W = 87;
    private static final int VK_A = 65;
    private static final int VK_S = 83;
    private static final int VK_D = 68;
    private static final int VK_M = 77;
    private static final int VK_P = 80;
    private static final int VK_R = 82;
    private static final int VK_SPACE = 32;
    private static final int VK_ENTER = 13;
    private static final int VK_ESCAPE = 27;

    public void openGame(GameType game) {
        this.currentGame = game;
        this.windowOpen = true;
        this.windowOpenBool.set(true);
        this.needsCenter = true;
        if (game == GameType.SNAKE) {
            this.gameSnake.initGame();
        } else if (game == GameType.PONG) {
            this.gamePong.initGame();
        }
    }

    public void close() {
        this.windowOpen = false;
        this.windowFocused = false;
    }

    public boolean isOpen() {
        return this.windowOpen;
    }

    public boolean isFocused() {
        return this.windowFocused;
    }

    public boolean shouldCaptureKey(int vkCode) {
        if (!this.windowOpen || !this.windowFocused) {
            return false;
        }
        return vkCode == 37 || vkCode == 38 || vkCode == 39 || vkCode == 40 || vkCode == 87 || vkCode == 65 || vkCode == 83 || vkCode == 68 || vkCode == 77 || vkCode == 80 || vkCode == 82 || vkCode == 32 || vkCode == 13 || vkCode == 27;
    }

    public boolean handleKeyPress(int vkCode) {
        if (!this.windowOpen || !this.windowFocused || this.currentGame == null) {
            return false;
        }
        if (vkCode == 27) {
            this.close();
            return true;
        }
        return switch (this.currentGame.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.handleKeySnake(vkCode);
            case 1 -> this.handleKeyPong(vkCode, true);
        };
    }

    private boolean handleKeySnake(int vkCode) {
        if (this.gameSnake.isShowingIntro()) {
            switch (vkCode) {
                case 38: 
                case 87: {
                    this.gameSnake.menuUp();
                    return true;
                }
                case 40: 
                case 83: {
                    this.gameSnake.menuDown();
                    return true;
                }
                case 37: 
                case 65: {
                    this.gameSnake.menuLeft();
                    return true;
                }
                case 39: 
                case 68: {
                    this.gameSnake.menuRight();
                    return true;
                }
                case 13: 
                case 32: {
                    this.gameSnake.menuSelect();
                    return true;
                }
            }
            return false;
        }
        if (vkCode == 82) {
            this.gameSnake.initGame();
            return true;
        }
        if (vkCode == 32) {
            this.gameSnake.togglePause();
            return true;
        }
        if (this.gameSnake.isGameOver() || this.gameSnake.isPaused()) {
            return false;
        }
        switch (vkCode) {
            case 37: 
            case 65: {
                this.gameSnake.moveLeft();
                return true;
            }
            case 39: 
            case 68: {
                this.gameSnake.moveRight();
                return true;
            }
            case 38: 
            case 87: {
                this.gameSnake.moveUp();
                return true;
            }
            case 40: 
            case 83: {
                this.gameSnake.moveDown();
                return true;
            }
        }
        return false;
    }

    private boolean handleKeyPong(int vkCode, boolean pressed) {
        if (vkCode == 27 && pressed) {
            this.close();
            return true;
        }
        if (this.gamePong.isShowingIntro() && pressed) {
            switch (vkCode) {
                case 38: 
                case 87: {
                    this.gamePong.menuUp();
                    return true;
                }
                case 40: 
                case 83: {
                    this.gamePong.menuDown();
                    return true;
                }
                case 37: 
                case 65: {
                    this.gamePong.menuLeft();
                    return true;
                }
                case 39: 
                case 68: {
                    this.gamePong.menuRight();
                    return true;
                }
                case 13: 
                case 32: {
                    this.gamePong.menuSelect();
                    return true;
                }
            }
            return true;
        }
        if (vkCode == 82 && pressed) {
            this.gamePong.newGame();
            return true;
        }
        if (vkCode == 77 && pressed) {
            this.gamePong.backToMenu();
            return true;
        }
        if (vkCode == 80 && pressed) {
            this.gamePong.togglePause();
            return true;
        }
        if (vkCode == 32 && pressed) {
            this.gamePong.togglePause();
            return true;
        }
        switch (vkCode) {
            case 38: 
            case 87: {
                this.gamePong.setMoveUp(pressed);
                return true;
            }
            case 40: 
            case 83: {
                this.gamePong.setMoveDown(pressed);
                return true;
            }
        }
        return vkCode == 13;
    }

    public boolean handleRelease(int vkCode) {
        if (!this.windowOpen || !this.windowFocused || this.currentGame == null) {
            return false;
        }
        if (this.currentGame == GameType.PONG) {
            return this.handleKeyPong(vkCode, false);
        }
        return false;
    }

    public void render(float deltaTime) {
        if (!this.windowOpen || this.currentGame == null) {
            return;
        }
        if (this.currentGame == GameType.PONG) {
            this.renderPongFullscreen(deltaTime);
            return;
        }
        ImGui.pushStyleColor(2, 0.12f, 0.12f, 0.15f, 0.98f);
        ImGui.pushStyleColor(10, 0.08f, 0.08f, 0.1f, 1.0f);
        ImGui.pushStyleColor(11, 0.15f, 0.15f, 0.2f, 1.0f);
        ImGui.pushStyleVar(3, 8.0f);
        ImGui.pushStyleVar(2, 12.0f, 12.0f);
        float windowWidth = this.currentGame.windowWidth;
        float windowHeight = this.currentGame.windowHeight;
        if (this.needsCenter) {
            ImGuiViewport viewport = ImGui.getMainViewport();
            float centerX = viewport.getWorkCenterX() - windowWidth / 2.0f;
            float centerY = viewport.getWorkCenterY() - windowHeight / 2.0f;
            ImGui.setNextWindowPos(centerX, centerY, 1);
            this.needsCenter = false;
        }
        ImGui.setNextWindowSize(windowWidth, windowHeight, 1);
        String windowTitle = this.currentGame.displayName + "###MiniGameWindow";
        int windowFlags = 42;
        this.windowOpenBool.set(true);
        if (ImGui.begin(windowTitle, this.windowOpenBool, windowFlags)) {
            this.windowFocused = ImGui.isWindowFocused();
            if (this.currentGame == GameType.SNAKE) {
                this.gameSnake.render(deltaTime);
            }
        }
        if (!this.windowOpenBool.get()) {
            this.close();
        }
        ImGui.end();
        ImGui.popStyleVar(2);
        ImGui.popStyleColor(3);
    }

    private void renderPongFullscreen(float deltaTime) {
        float gameWidth = this.gamePong.getBoardWidth();
        float gameHeight = this.gamePong.getBoardHeight();
        float windowWidth = gameWidth + 24.0f;
        float windowHeight = gameHeight + 90.0f;
        if (this.needsCenter) {
            ImGuiViewport viewport = ImGui.getMainViewport();
            float centerX = viewport.getWorkCenterX() - windowWidth / 2.0f;
            float centerY = viewport.getWorkCenterY() - windowHeight / 2.0f;
            ImGui.setNextWindowPos(centerX, centerY, 1);
            this.needsCenter = false;
        }
        ImGui.setNextWindowSize(windowWidth, windowHeight, 1);
        ImGui.pushStyleColor(2, 0.08f, 0.08f, 0.12f, 0.98f);
        ImGui.pushStyleColor(10, 0.06f, 0.06f, 0.1f, 1.0f);
        ImGui.pushStyleColor(11, 0.1f, 0.1f, 0.16f, 1.0f);
        ImGui.pushStyleVar(3, 10.0f);
        ImGui.pushStyleVar(2, 12.0f, 8.0f);
        int windowFlags = 42;
        String windowTitle = "\uf45d Pong###PongGame";
        this.windowOpenBool.set(true);
        if (ImGui.begin(windowTitle, this.windowOpenBool, windowFlags)) {
            this.windowFocused = ImGui.isWindowFocused();
            this.gamePong.renderFullscreen(deltaTime);
        }
        if (!this.windowOpenBool.get()) {
            this.close();
        }
        ImGui.end();
        ImGui.popStyleVar(2);
        ImGui.popStyleColor(3);
    }

    public MiniGameSnake getGameSnake() {
        return this.gameSnake;
    }

    public MiniGamePong getGamePong() {
        return this.gamePong;
    }

    public GameType getCurrentGame() {
        return this.currentGame;
    }
}

