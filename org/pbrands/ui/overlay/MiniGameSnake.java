/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.ui.overlay.MiniGameSnake$Particle
 *  org.pbrands.ui.overlay.MiniGameSnake$PowerUpType
 */
package org.pbrands.ui.overlay;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.pbrands.sound.SoundManager;
import org.pbrands.ui.overlay.MiniGameSnake;

/*
 * Exception performing whole class analysis ignored.
 */
public class MiniGameSnake {
    private static final int GRID_WIDTH = 28;
    private static final int GRID_HEIGHT = 20;
    private static final float CELL_SIZE = 24.0f;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean showingIntro = true;
    private PowerUpType activePowerUp = null;
    private float powerUpTimer = 0.0f;
    private int powerUpX = -1;
    private int powerUpY = -1;
    private PowerUpType spawnedPowerUp = null;
    private float powerUpSpawnTimer = 0.0f;
    private static final float POWER_UP_SPAWN_INTERVAL = 15.0f;
    private float powerUpPulse = 0.0f;
    private int menuSelectedOption = 0;
    private static final int MENU_OPTION_START = 0;
    private static final int MENU_OPTION_DIFFICULTY = 1;
    private static final int MENU_OPTION_SOUND = 2;
    private static final int MENU_OPTION_COUNT = 3;
    private static final int DIR_UP = 0;
    private static final int DIR_RIGHT = 1;
    private static final int DIR_DOWN = 2;
    private static final int DIR_LEFT = 3;
    private final LinkedList<Segment> snake = new LinkedList();
    private int direction = 1;
    private int nextDirection = 1;
    private final LinkedList<Integer> inputBuffer = new LinkedList();
    private static final int INPUT_BUFFER_SIZE = 2;
    private int appleX;
    private int appleY;
    private float appleScale = 1.0f;
    private float applePulse = 0.0f;
    private int swallowSegmentIndex = -1;
    private float swallowProgress = 0.0f;
    private boolean pendingGrowth = false;
    private float tongueTimer = 0.0f;
    private float tongueExtension = 0.0f;
    private final List<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random();
    private int score = 0;
    private int bestScore = 0;
    private boolean gameOver = false;
    private boolean paused = false;
    private float moveTimer = 0.0f;
    private float moveProgress = 0.0f;
    private float currentSpeed = 0.13f;
    private float deathTimer = 0.0f;
    private boolean growing = false;
    private static final int COLOR_GRID_BG1 = -14342864;
    private static final int COLOR_GRID_BG2 = -14013896;
    private static final int COLOR_SNAKE_HEAD = -11477152;
    private static final int COLOR_SNAKE_BODY = -12533936;
    private static final int COLOR_SNAKE_TAIL = -13590720;
    private static final int COLOR_APPLE = -13290001;
    private static final int COLOR_APPLE_SHINE = -11184641;
    private static final int COLOR_BORDER = -11908512;
    private static final int COLOR_GHOST = -2130751233;
    private static final int COLOR_OBSTACLE = -12566400;
    private static final int COLOR_OBSTACLE_HIGHLIGHT = -11513712;
    private final List<int[]> obstacles = new ArrayList<int[]>();
    private float flashAlpha = 0.0f;
    private int flashColor = -1;
    private int combo = 0;
    private float comboTimer = 0.0f;
    private static final float COMBO_TIMEOUT = 3.0f;
    private int pointMultiplier = 1;
    private final SoundManager soundManager = SoundManager.getInstance();
    private boolean soundEnabled = true;

    public MiniGameSnake() {
        this.initGame();
    }

    public void initGame() {
        this.showingIntro = true;
        this.newGame();
    }

    public void newGame() {
        this.snake.clear();
        this.particles.clear();
        int startX = 14;
        int startY = 10;
        for (int i = 0; i < 4; ++i) {
            this.snake.add(new Segment(startX - i, startY));
        }
        this.direction = 1;
        this.nextDirection = 1;
        this.inputBuffer.clear();
        this.score = 0;
        this.gameOver = false;
        this.paused = false;
        this.moveTimer = 0.0f;
        this.moveProgress = 1.0f;
        this.currentSpeed = this.difficulty.initialSpeed;
        this.deathTimer = 0.0f;
        this.growing = false;
        this.activePowerUp = null;
        this.powerUpTimer = 0.0f;
        this.spawnedPowerUp = null;
        this.powerUpX = -1;
        this.powerUpY = -1;
        this.powerUpSpawnTimer = 5.0f;
        this.combo = 0;
        this.comboTimer = 0.0f;
        this.pointMultiplier = 1;
        this.flashAlpha = 0.0f;
        this.swallowSegmentIndex = -1;
        this.swallowProgress = 0.0f;
        this.pendingGrowth = false;
        this.tongueTimer = 0.0f;
        this.tongueExtension = 0.0f;
        this.spawnObstacles();
        this.spawnApple();
    }

    private void spawnObstacles() {
        this.obstacles.clear();
        int count = this.difficulty.obstacleCount;
        if (count <= 0) {
            return;
        }
        int startX = 14;
        int startY = 10;
        block0: for (int i = 0; i < count; ++i) {
            int attempts = 0;
            while (attempts < 100) {
                int x = this.random.nextInt(28);
                int y = this.random.nextInt(20);
                if (Math.abs(x - startX) < 6 && Math.abs(y - startY) < 3) {
                    ++attempts;
                    continue;
                }
                boolean occupied = false;
                for (int[] obs : this.obstacles) {
                    if (obs[0] != x || obs[1] != y) continue;
                    occupied = true;
                    break;
                }
                if (!occupied) {
                    this.obstacles.add(new int[]{x, y});
                    continue block0;
                }
                ++attempts;
            }
        }
    }

    private void spawnApple() {
        ArrayList<int[]> freeCells = new ArrayList<int[]>();
        for (int x = 0; x < 28; ++x) {
            for (int y = 0; y < 20; ++y) {
                boolean occupied = false;
                for (Segment segment : this.snake) {
                    if (segment.gridX != x || segment.gridY != y) continue;
                    occupied = true;
                    break;
                }
                if (!occupied) {
                    for (int[] obs : this.obstacles) {
                        if (obs[0] != x || obs[1] != y) continue;
                        occupied = true;
                        break;
                    }
                }
                if (occupied) continue;
                freeCells.add(new int[]{x, y});
            }
        }
        if (!freeCells.isEmpty()) {
            int[] cell = (int[])freeCells.get(this.random.nextInt(freeCells.size()));
            this.appleX = cell[0];
            this.appleY = cell[1];
            this.appleScale = 0.3f;
        }
    }

    private void spawnEatParticles(float x, float y) {
        this.spawnEatParticles(x, y, -13290001, -11184641);
    }

    private void spawnEatParticles(float x, float y, int color1, int color2) {
        for (int i = 0; i < 12; ++i) {
            float angle = (float)(Math.random() * Math.PI * 2.0);
            float speed = 80.0f + (float)(Math.random() * 80.0);
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed - 50.0f;
            int color = this.random.nextBoolean() ? color1 : color2;
            this.particles.add(new Particle(x, y, vx, vy, color));
        }
    }

    private void spawnPowerUp() {
        ArrayList<int[]> freeCells = new ArrayList<int[]>();
        for (int x = 1; x < 27; ++x) {
            for (int y = 1; y < 19; ++y) {
                if (x == this.appleX && y == this.appleY) continue;
                boolean occupied = false;
                for (Segment segment : this.snake) {
                    if (segment.gridX != x || segment.gridY != y) continue;
                    occupied = true;
                    break;
                }
                if (occupied) continue;
                freeCells.add(new int[]{x, y});
            }
        }
        if (!freeCells.isEmpty()) {
            int[] cell = (int[])freeCells.get(this.random.nextInt(freeCells.size()));
            this.powerUpX = cell[0];
            this.powerUpY = cell[1];
            PowerUpType[] types = PowerUpType.values();
            this.spawnedPowerUp = types[this.random.nextInt(types.length)];
        }
    }

    private void activatePowerUp(PowerUpType type) {
        if (type == PowerUpType.SHRINK) {
            int toRemove = Math.min(3, this.snake.size() - 3);
            for (int i = 0; i < toRemove; ++i) {
                if (this.snake.size() <= 3) continue;
                Segment removed = this.snake.removeLast();
                this.spawnEatParticles(removed.renderX + 12.0f, removed.renderY + 12.0f, type.color, -1);
            }
            this.triggerFlash(type.color);
            return;
        }
        this.activePowerUp = type;
        this.powerUpTimer = type.duration;
        this.pointMultiplier = type == PowerUpType.DOUBLE_POINTS ? 2 : 1;
        this.triggerFlash(type.color);
    }

    private void deactivatePowerUp() {
        this.activePowerUp = null;
        this.powerUpTimer = 0.0f;
        this.pointMultiplier = 1;
    }

    private void triggerFlash(int color) {
        this.flashColor = color;
        this.flashAlpha = 0.4f;
    }

    public void update(float deltaTime) {
        if (this.showingIntro) {
            return;
        }
        if (this.flashAlpha > 0.0f) {
            this.flashAlpha -= deltaTime * 2.0f;
        }
        this.applePulse += deltaTime * 4.0f;
        this.powerUpPulse += deltaTime * 5.0f;
        if (this.appleScale < 1.0f) {
            this.appleScale = Math.min(1.0f, this.appleScale + deltaTime * 4.0f);
        }
        this.tongueTimer += deltaTime * 3.5f;
        this.tongueExtension = (float)Math.sin(this.tongueTimer) * 0.5f + 0.5f;
        if (this.swallowSegmentIndex >= 0) {
            // empty if block
        }
        this.particles.removeIf(p -> !p.update(deltaTime));
        if (this.activePowerUp != null) {
            this.powerUpTimer -= deltaTime;
            if (this.powerUpTimer <= 0.0f) {
                this.deactivatePowerUp();
            }
        }
        if (this.spawnedPowerUp == null && !this.gameOver) {
            this.powerUpSpawnTimer -= deltaTime;
            if (this.powerUpSpawnTimer <= 0.0f) {
                this.spawnPowerUp();
                this.powerUpSpawnTimer = 15.0f + this.random.nextFloat() * 5.0f;
            }
        }
        if (this.combo > 0) {
            this.comboTimer -= deltaTime;
            if (this.comboTimer <= 0.0f) {
                this.combo = 0;
            }
        }
        if (this.gameOver) {
            this.deathTimer += deltaTime;
            for (int i = 0; i < this.snake.size(); ++i) {
                Segment seg = this.snake.get(i);
                float delay = (float)i * 0.05f;
                if (!(this.deathTimer > delay)) continue;
                seg.fallVelocityY += 800.0f * deltaTime;
                seg.fallOffsetY += seg.fallVelocityY * deltaTime;
                seg.rotation += (float)(300 + i * 50) * deltaTime;
            }
            return;
        }
        if (this.paused) {
            return;
        }
        float effectiveSpeed = this.currentSpeed * this.getSpeedModifier();
        this.moveTimer += deltaTime;
        this.moveProgress = Math.min(1.0f, this.moveTimer / effectiveSpeed);
        for (Segment seg : this.snake) {
            seg.updateRender(this.moveProgress);
        }
        if (this.moveTimer >= effectiveSpeed) {
            this.moveTimer = 0.0f;
            this.moveProgress = 0.0f;
            this.move();
        }
    }

    private void move() {
        boolean hitWall;
        if (!this.inputBuffer.isEmpty()) {
            this.nextDirection = this.inputBuffer.removeFirst();
        }
        this.direction = this.nextDirection;
        Segment head = this.snake.getFirst();
        int newX = head.gridX;
        int newY = head.gridY;
        switch (this.direction) {
            case 0: {
                --newY;
                break;
            }
            case 2: {
                ++newY;
                break;
            }
            case 3: {
                --newX;
                break;
            }
            case 1: {
                ++newX;
            }
        }
        boolean bl = hitWall = newX < 0 || newX >= 28 || newY < 0 || newY >= 20;
        if (hitWall) {
            if (this.activePowerUp == PowerUpType.GHOST) {
                if (newX < 0) {
                    newX = 27;
                } else if (newX >= 28) {
                    newX = 0;
                }
                if (newY < 0) {
                    newY = 19;
                } else if (newY >= 20) {
                    newY = 0;
                }
            } else {
                this.triggerDeath();
                return;
            }
        }
        for (int i = 0; i < this.snake.size() - (this.growing ? 0 : 1); ++i) {
            Segment seg = this.snake.get(i);
            if (seg.gridX != newX || seg.gridY != newY) continue;
            this.triggerDeath();
            return;
        }
        if (this.activePowerUp != PowerUpType.GHOST) {
            for (int[] obs : this.obstacles) {
                if (obs[0] != newX || obs[1] != newY) continue;
                this.triggerDeath();
                return;
            }
        }
        int prevX = head.gridX;
        int prevY = head.gridY;
        head.moveTo(newX, newY);
        for (int i = 1; i < this.snake.size(); ++i) {
            Segment seg = this.snake.get(i);
            int tempX = seg.gridX;
            int tempY = seg.gridY;
            seg.moveTo(prevX, prevY);
            prevX = tempX;
            prevY = tempY;
        }
        if (this.swallowSegmentIndex >= 0) {
            ++this.swallowSegmentIndex;
            if (this.swallowSegmentIndex >= this.snake.size()) {
                this.swallowSegmentIndex = -1;
                if (this.pendingGrowth) {
                    Segment tail = this.snake.getLast();
                    this.snake.add(new Segment(tail.gridX, tail.gridY));
                    this.pendingGrowth = false;
                }
            }
        }
        if (newX == this.appleX && newY == this.appleY) {
            ++this.combo;
            this.comboTimer = 3.0f;
            int comboBonus = Math.min(this.combo - 1, 5) * 5;
            int points = (10 + comboBonus) * this.pointMultiplier;
            this.score += points;
            this.currentSpeed = Math.max(this.difficulty.minSpeed, this.currentSpeed - this.difficulty.speedIncrease);
            float speedMod = this.getSpeedModifier();
            float px = (float)this.appleX * 24.0f + 12.0f;
            float py = (float)this.appleY * 24.0f + 12.0f;
            this.spawnEatParticles(px, py);
            this.swallowSegmentIndex = 0;
            this.swallowProgress = 0.0f;
            this.pendingGrowth = true;
            if (this.soundEnabled) {
                this.soundManager.playSound("builtin:PICKUP", 50);
            }
            this.spawnApple();
        } else {
            this.growing = false;
        }
        if (this.spawnedPowerUp != null && newX == this.powerUpX && newY == this.powerUpY) {
            float px = (float)this.powerUpX * 24.0f + 12.0f;
            float py = (float)this.powerUpY * 24.0f + 12.0f;
            this.spawnEatParticles(px, py, this.spawnedPowerUp.color, -1);
            this.activatePowerUp(this.spawnedPowerUp);
            this.spawnedPowerUp = null;
            this.powerUpX = -1;
            this.powerUpY = -1;
            if (this.soundEnabled) {
                this.soundManager.playSound("builtin:POWERUP", 60);
            }
        }
    }

    private float getSpeedModifier() {
        if (this.activePowerUp == PowerUpType.SPEED_BOOST) {
            return 0.7f;
        }
        if (this.activePowerUp == PowerUpType.SLOW_DOWN) {
            return 1.5f;
        }
        return 1.0f;
    }

    private void triggerDeath() {
        this.gameOver = true;
        this.deathTimer = 0.0f;
        if (this.score > this.bestScore) {
            this.bestScore = this.score;
        }
        this.playDeathSound();
        this.deactivatePowerUp();
        for (Segment seg : this.snake) {
            seg.fallVelocityY = -100.0f - this.random.nextFloat() * 100.0f;
        }
    }

    private void playDeathSound() {
        if (this.soundEnabled) {
            this.soundManager.playSound("builtin:SNAKE_FAILED", 70);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return this.soundEnabled;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public void startGame() {
        this.showingIntro = false;
        this.newGame();
    }

    public void setDirection(int dir) {
        int lastDir;
        int n = lastDir = this.inputBuffer.isEmpty() ? this.nextDirection : this.inputBuffer.getLast();
        if (lastDir == 0 && dir == 2 || lastDir == 2 && dir == 0 || lastDir == 3 && dir == 1 || lastDir == 1 && dir == 3) {
            return;
        }
        if (lastDir == dir) {
            return;
        }
        if (this.inputBuffer.size() < 2) {
            this.inputBuffer.addLast(dir);
        }
    }

    public void moveUp() {
        this.setDirection(0);
    }

    public void moveDown() {
        this.setDirection(2);
    }

    public void moveLeft() {
        this.setDirection(3);
    }

    public void moveRight() {
        this.setDirection(1);
    }

    public void togglePause() {
        if (!this.gameOver && !this.showingIntro) {
            this.paused = !this.paused;
        }
    }

    public void render(float deltaTime) {
        this.update(deltaTime);
        if (this.showingIntro) {
            this.renderIntroMenu();
            return;
        }
        ImGui.beginGroup();
        ImGui.textColored(0.6f, 0.8f, 1.0f, 1.0f, "Wynik: " + this.score);
        if (this.combo > 1) {
            ImGui.sameLine();
            ImGui.textColored(1.0f, 0.5f, 0.2f, 1.0f, "  x" + this.combo + " COMBO!");
        }
        if (this.activePowerUp != null) {
            ImGui.sameLine(300.0f);
            float r = (float)(this.activePowerUp.color >> 16 & 0xFF) / 255.0f;
            float g = (float)(this.activePowerUp.color >> 8 & 0xFF) / 255.0f;
            float b = (float)(this.activePowerUp.color & 0xFF) / 255.0f;
            ImGui.textColored(r, g, b, 1.0f, this.activePowerUp.name + " " + String.format("%.1f", Float.valueOf(this.powerUpTimer)) + "s");
        }
        ImGui.sameLine(500.0f);
        ImGui.textColored(1.0f, 0.8f, 0.4f, 1.0f, "\uf091 " + this.bestScore);
        ImGui.spacing();
        ImVec2 startPos = ImGui.getCursorScreenPos();
        ImDrawList drawList = ImGui.getWindowDrawList();
        float boardWidth = 672.0f;
        float boardHeight = 480.0f;
        for (int x = 0; x < 28; ++x) {
            for (int y = 0; y < 20; ++y) {
                int color = (x + y) % 2 == 0 ? -14342864 : -14013896;
                drawList.addRectFilled(startPos.x + (float)x * 24.0f, startPos.y + (float)y * 24.0f, startPos.x + (float)(x + 1) * 24.0f, startPos.y + (float)(y + 1) * 24.0f, color);
            }
        }
        if (this.activePowerUp == PowerUpType.GHOST) {
            float pulse = (float)Math.sin(this.powerUpPulse * 2.0f) * 0.3f + 0.5f;
            int glowAlpha = (int)(pulse * 80.0f);
            int borderGlow = PowerUpType.GHOST.color & 0xFFFFFF | glowAlpha << 24;
            drawList.addRect(startPos.x - 3.0f, startPos.y - 3.0f, startPos.x + boardWidth + 3.0f, startPos.y + boardHeight + 3.0f, borderGlow, 0.0f, 0, 4.0f);
        }
        drawList.addRect(startPos.x - 1.0f, startPos.y - 1.0f, startPos.x + boardWidth + 1.0f, startPos.y + boardHeight + 1.0f, -11908512, 0.0f, 0, 2.0f);
        for (int[] obs : this.obstacles) {
            float obsX = startPos.x + (float)obs[0] * 24.0f;
            float obsY = startPos.y + (float)obs[1] * 24.0f;
            float padding = 2.0f;
            drawList.addRectFilled(obsX + padding, obsY + padding, obsX + 24.0f - padding, obsY + 24.0f - padding, -12566400, 4.0f);
            drawList.addRectFilled(obsX + padding + 2.0f, obsY + padding + 2.0f, obsX + 12.0f, obsY + 12.0f, -11513712, 2.0f);
            drawList.addRect(obsX + padding, obsY + padding, obsX + 24.0f - padding, obsY + 24.0f - padding, -13619104, 4.0f, 0, 1.5f);
        }
        if (this.spawnedPowerUp != null) {
            float puPulse = 1.0f + (float)Math.sin(this.powerUpPulse) * 0.15f;
            float puCenterX = startPos.x + (float)this.powerUpX * 24.0f + 12.0f;
            float puCenterY = startPos.y + (float)this.powerUpY * 24.0f + 12.0f;
            float puRadius = 10.0f * puPulse;
            int glowColor = this.spawnedPowerUp.color & 0xFFFFFF | 0x40000000;
            drawList.addCircleFilled(puCenterX, puCenterY, puRadius + 6.0f, glowColor);
            drawList.addCircleFilled(puCenterX, puCenterY, puRadius, this.spawnedPowerUp.color);
            drawList.addCircleFilled(puCenterX - 3.0f, puCenterY - 3.0f, puRadius / 3.0f, -2130706433);
            String icon = switch (this.spawnedPowerUp.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "\uf0e7";
                case 1 -> "\uf017";
                case 2 -> "\uf6e2";
                case 3 -> "x2";
                case 4 -> "\uf066";
            };
            ImVec2 iconSize = new ImVec2();
            ImGui.calcTextSize(iconSize, icon);
            drawList.addText(puCenterX - iconSize.x / 2.0f, puCenterY - iconSize.y / 2.0f, -1, icon);
        }
        float pulseScale = 1.0f + (float)Math.sin(this.applePulse) * 0.08f;
        float effectiveScale = this.appleScale * pulseScale;
        float appleCenterX = startPos.x + (float)this.appleX * 24.0f + 12.0f;
        float appleCenterY = startPos.y + (float)this.appleY * 24.0f + 12.0f;
        float appleRadius = 10.0f * effectiveScale;
        float glowPulse = (float)Math.sin(this.applePulse * 1.5f) * 0.3f + 0.5f;
        int glowAlpha = (int)(glowPulse * 60.0f);
        drawList.addCircleFilled(appleCenterX, appleCenterY, appleRadius + 6.0f, glowAlpha << 24 | 0x4040FF);
        drawList.addCircleFilled(appleCenterX - 1.0f, appleCenterY + 1.0f, appleRadius, -13290001);
        drawList.addCircleFilled(appleCenterX + 1.0f, appleCenterY + 1.0f, appleRadius, -13290001);
        drawList.addCircleFilled(appleCenterX - 3.0f, appleCenterY - 2.0f, appleRadius / 2.5f, -11184641);
        drawList.addCircleFilled(appleCenterX, appleCenterY - appleRadius + 2.0f, 3.0f, -14342705);
        drawList.addLine(appleCenterX, appleCenterY - appleRadius + 1.0f, appleCenterX + 2.0f, appleCenterY - appleRadius - 5.0f, -14659552, 2.5f);
        float leafX = appleCenterX + 3.0f;
        float leafY = appleCenterY - appleRadius - 3.0f;
        drawList.addCircleFilled(leafX + 4.0f, leafY, 4.0f, -13590480);
        drawList.addCircleFilled(leafX + 7.0f, leafY + 1.0f, 3.0f, -13590480);
        for (Particle p : this.particles) {
            int alpha = (int)(p.life * 255.0f);
            int color = p.color & 0xFFFFFF | alpha << 24;
            drawList.addCircleFilled(startPos.x + p.x, startPos.y + p.y, 3.0f * p.life, color);
        }
        this.drawConnectedSnake(drawList, startPos);
        if (this.flashAlpha > 0.0f) {
            int alpha = (int)(this.flashAlpha * 255.0f);
            int flashFinal = this.flashColor & 0xFFFFFF | alpha << 24;
            drawList.addRectFilled(startPos.x, startPos.y, startPos.x + boardWidth, startPos.y + boardHeight, flashFinal);
        }
        ImGui.dummy(boardWidth, boardHeight);
        if (this.gameOver || this.paused) {
            float overlayAlpha = this.gameOver ? Math.min(1.0f, this.deathTimer * 2.0f) : 1.0f;
            int overlayColor = this.gameOver ? (int)(overlayAlpha * 170.0f) << 24 | 0x3A3A5A : -1439016390;
            drawList.addRectFilled(startPos.x, startPos.y, startPos.x + boardWidth, startPos.y + boardHeight, overlayColor, 4.0f);
            if (this.gameOver && this.deathTimer > 0.8f) {
                String message = "\uf54c  GAME OVER";
                ImVec2 msgSize = new ImVec2();
                ImGui.calcTextSize(msgSize, message);
                drawList.addText(startPos.x + (boardWidth - msgSize.x) / 2.0f, startPos.y + boardHeight / 2.0f - 60.0f, -40864, message);
                String scoreText = "Wynik: " + this.score;
                ImVec2 scoreSize = new ImVec2();
                ImGui.calcTextSize(scoreSize, scoreText);
                drawList.addText(startPos.x + (boardWidth - scoreSize.x) / 2.0f, startPos.y + boardHeight / 2.0f - 25.0f, -1, scoreText);
                if (this.score == this.bestScore && this.score > 0) {
                    String newBest = "\uf005 NOWY REKORD!";
                    ImVec2 newBestSize = new ImVec2();
                    ImGui.calcTextSize(newBestSize, newBest);
                    drawList.addText(startPos.x + (boardWidth - newBestSize.x) / 2.0f, startPos.y + boardHeight / 2.0f + 5.0f, -13232, newBest);
                }
                float btnWidth = 180.0f;
                float btnHeight = 35.0f;
                ImGui.setCursorScreenPos(startPos.x + (boardWidth - btnWidth) / 2.0f, startPos.y + boardHeight / 2.0f + 40.0f);
                ImGui.pushStyleColor(21, -14000096);
                ImGui.pushStyleColor(22, -12946128);
                ImGui.pushStyleColor(23, -15052784);
                if (ImGui.button("\uf01e  ZAGRAJ PONOWNIE", btnWidth, btnHeight)) {
                    this.initGame();
                }
                ImGui.popStyleColor(3);
            } else if (this.paused) {
                String message = "\uf04c  PAUZA";
                ImVec2 msgSize = new ImVec2();
                ImGui.calcTextSize(msgSize, message);
                drawList.addText(startPos.x + (boardWidth - msgSize.x) / 2.0f, startPos.y + boardHeight / 2.0f - 25.0f, -1, message);
                String hint = "Spacja - wznow gre";
                ImVec2 hintSize = new ImVec2();
                ImGui.calcTextSize(hintSize, hint);
                drawList.addText(startPos.x + (boardWidth - hintSize.x) / 2.0f, startPos.y + boardHeight / 2.0f + 10.0f, -7829368, hint);
            }
        }
        ImGui.endGroup();
    }

    private void drawConnectedSnake(ImDrawList drawList, ImVec2 startPos) {
        if (this.snake.isEmpty()) {
            return;
        }
        if (this.gameOver) {
            this.drawDeathAnimation(drawList, startPos);
            return;
        }
        float bodyWidth = 20.0f;
        int n = this.snake.size();
        float[] px = new float[n];
        float[] py = new float[n];
        float[] widths = new float[n];
        for (int i = 0; i < n; ++i) {
            float bulgePos;
            float dist;
            Segment seg = this.snake.get(i);
            px[i] = startPos.x + seg.renderX + 12.0f;
            py[i] = startPos.y + seg.renderY + 12.0f;
            float w = bodyWidth;
            if (i == 0) {
                w = bodyWidth + 6.0f;
            } else if (i >= n - 3) {
                int fromEnd = n - 1 - i;
                w = bodyWidth * (0.35f + (float)fromEnd * 0.22f);
            }
            if (this.swallowSegmentIndex >= 0 && (dist = Math.abs((float)i - (bulgePos = (float)this.swallowSegmentIndex + this.moveProgress))) < 1.5f) {
                float intensity = 1.0f - dist / 1.5f;
                float bulge = (float)Math.sin((double)intensity * Math.PI) * 0.6f;
                w += bulge * 12.0f;
            }
            widths[i] = w;
        }
        int bodyColor = -12533936;
        if (this.activePowerUp == PowerUpType.GHOST) {
            float pulse = (float)Math.sin(this.powerUpPulse * 3.0f) * 0.2f + 0.6f;
            int alpha = (int)(pulse * 255.0f);
            bodyColor = bodyColor & 0xFFFFFF | alpha << 24;
        } else if (this.activePowerUp == PowerUpType.SPEED_BOOST) {
            bodyColor = MiniGameSnake.lerpColor(bodyColor, PowerUpType.SPEED_BOOST.color, 0.3f);
        }
        int shadowColor = -15724520;
        if (this.activePowerUp == PowerUpType.GHOST) {
            shadowColor = 1611665432;
        }
        this.drawSnakeShape(drawList, px, py, widths, n, shadowColor, 3.0f);
        this.drawSnakeShape(drawList, px, py, widths, n, bodyColor, 0.0f);
        int highlightColor = MiniGameSnake.lerpColor(bodyColor, -1, 0.25f);
        this.drawSnakeSpine(drawList, px, py, widths, n, highlightColor);
        this.drawSnakeHead(drawList, px[0], py[0], widths[0], bodyColor);
        if (this.swallowSegmentIndex >= 0 && this.swallowSegmentIndex < n) {
            float bulgePos = (float)this.swallowSegmentIndex + this.moveProgress;
            int segIdx = (int)bulgePos;
            float segFrac = bulgePos - (float)segIdx;
            if (segIdx < n && segIdx >= 0) {
                float br;
                float by;
                float bx;
                if (segIdx < n - 1) {
                    bx = MiniGameSnake.lerp(px[segIdx], px[segIdx + 1], segFrac);
                    by = MiniGameSnake.lerp(py[segIdx], py[segIdx + 1], segFrac);
                    br = MiniGameSnake.lerp(widths[segIdx], widths[segIdx + 1], segFrac) / 2.0f * 0.5f;
                } else {
                    bx = px[segIdx];
                    by = py[segIdx];
                    br = widths[segIdx] / 2.0f * 0.5f;
                }
                int appleGlow = MiniGameSnake.lerpColor(bodyColor, -13290001, 0.4f);
                drawList.addCircleFilled(bx, by, br, appleGlow);
            }
        }
    }

    private void drawSnakeShape(ImDrawList drawList, float[] px, float[] py, float[] widths, int n, int color, float expand) {
        int i;
        if (n < 1) {
            return;
        }
        for (i = 0; i < n - 1; ++i) {
            float x1 = px[i];
            float y1 = py[i];
            float x2 = px[i + 1];
            float y2 = py[i + 1];
            float w1 = widths[i] / 2.0f + expand;
            float w2 = widths[i + 1] / 2.0f + expand;
            float dx = x2 - x1;
            float dy = y2 - y1;
            float len = (float)Math.sqrt(dx * dx + dy * dy);
            if (len < 0.1f) continue;
            float nx = -dy / len;
            float ny = dx / len;
            drawList.addQuadFilled(x1 + nx * w1, y1 + ny * w1, x1 - nx * w1, y1 - ny * w1, x2 - nx * w2, y2 - ny * w2, x2 + nx * w2, y2 + ny * w2, color);
        }
        for (i = 0; i < n; ++i) {
            float r = widths[i] / 2.0f + expand;
            drawList.addCircleFilled(px[i], py[i], r, color);
        }
    }

    private void drawSnakeSpine(ImDrawList drawList, float[] px, float[] py, float[] widths, int n, int color) {
        for (int i = 0; i < n; ++i) {
            float r = widths[i] / 4.0f;
            float fade = 1.0f - (float)i / (float)n * 0.7f;
            int alpha = (int)(fade * 80.0f);
            int c = color & 0xFFFFFF | alpha << 24;
            drawList.addCircleFilled(px[i], py[i] - r * 0.3f, r, c);
        }
    }

    private void drawDeathAnimation(ImDrawList drawList, ImVec2 startPos) {
        float bodyWidth = 20.0f;
        for (int i = this.snake.size() - 1; i >= 0; --i) {
            Segment seg = this.snake.get(i);
            float cx = startPos.x + seg.renderX + 12.0f;
            float cy = startPos.y + seg.renderY + 12.0f + seg.fallOffsetY;
            float t = 1.0f - (float)i / (float)this.snake.size();
            int color = MiniGameSnake.lerpColor(-13590720, -11477152, t);
            float w = bodyWidth;
            if (i == 0) {
                w = bodyWidth + 4.0f;
            } else if (i >= this.snake.size() - 3) {
                int fromEnd = this.snake.size() - 1 - i;
                w = bodyWidth * (0.4f + (float)fromEnd * 0.2f);
            }
            if (seg.fallOffsetY > 0.0f) {
                this.drawRotatedRect(drawList, cx, cy, w, w, seg.rotation, color);
            } else {
                drawList.addCircleFilled(cx, cy, w / 2.0f, color);
            }
            if (i != 0 || !(this.deathTimer < 0.5f)) continue;
            this.drawDeadEyes(drawList, cx, cy, w);
        }
    }

    private void drawDeadEyes(ImDrawList drawList, float cx, float cy, float size) {
        float eye2Y;
        float eye2X;
        float eye1Y;
        float eye1X;
        float eyeSpacing = 6.0f;
        float eyeOffset = 3.0f;
        float offsetX = 0.0f;
        float offsetY = 0.0f;
        switch (this.direction) {
            case 0: {
                offsetY = -eyeOffset;
                break;
            }
            case 2: {
                offsetY = eyeOffset;
                break;
            }
            case 3: {
                offsetX = -eyeOffset;
                break;
            }
            case 1: {
                offsetX = eyeOffset;
            }
        }
        if (this.direction == 0 || this.direction == 2) {
            eye1X = cx - eyeSpacing + offsetX;
            eye1Y = cy + offsetY;
            eye2X = cx + eyeSpacing + offsetX;
            eye2Y = cy + offsetY;
        } else {
            eye1X = cx + offsetX;
            eye1Y = cy - eyeSpacing + offsetY;
            eye2X = cx + offsetX;
            eye2Y = cy + eyeSpacing + offsetY;
        }
        int xColor = -1;
        drawList.addLine(eye1X - 3.0f, eye1Y - 3.0f, eye1X + 3.0f, eye1Y + 3.0f, xColor, 2.0f);
        drawList.addLine(eye1X + 3.0f, eye1Y - 3.0f, eye1X - 3.0f, eye1Y + 3.0f, xColor, 2.0f);
        drawList.addLine(eye2X - 3.0f, eye2Y - 3.0f, eye2X + 3.0f, eye2Y + 3.0f, xColor, 2.0f);
        drawList.addLine(eye2X + 3.0f, eye2Y - 3.0f, eye2X - 3.0f, eye2Y + 3.0f, xColor, 2.0f);
    }

    private void drawSnakeHead(ImDrawList drawList, float cx, float cy, float size, int color) {
        float elongation = 4.0f;
        float elongX = 0.0f;
        float elongY = 0.0f;
        switch (this.direction) {
            case 0: {
                elongY = -elongation;
                break;
            }
            case 2: {
                elongY = elongation;
                break;
            }
            case 3: {
                elongX = -elongation;
                break;
            }
            case 1: {
                elongX = elongation;
            }
        }
        drawList.addCircleFilled(cx + elongX * 0.5f, cy + elongY * 0.5f, size / 2.0f + 1.0f, this.applyPowerUpColor(-11477152, 0));
        if (!this.gameOver && this.tongueExtension > 0.05f) {
            this.drawTongue(drawList, cx + elongX, cy + elongY, size, this.tongueExtension);
        }
        this.drawEyes(drawList, cx, cy, size);
    }

    private void drawTongue(ImDrawList drawList, float cx, float cy, float size, float extension) {
        float maxLength = 14.0f;
        float tongueLength = maxLength * extension;
        float tongueWidth = 2.0f;
        int tongueColor = -12566273;
        float startX = cx;
        float startY = cy;
        float forkSize = 4.0f * extension;
        float easedExt = extension * extension * (3.0f - 2.0f * extension);
        tongueLength = maxLength * easedExt;
        forkSize = 5.0f * easedExt;
        switch (this.direction) {
            case 0: {
                float baseY = cy - size / 2.0f - 2.0f;
                float tipY = baseY - tongueLength;
                if (!(tongueLength > 2.0f)) break;
                drawList.addLine(startX, baseY, startX, tipY + forkSize, tongueColor, tongueWidth);
                if (!(forkSize > 1.0f)) break;
                drawList.addLine(startX, tipY + forkSize, startX - forkSize, tipY, tongueColor, tongueWidth);
                drawList.addLine(startX, tipY + forkSize, startX + forkSize, tipY, tongueColor, tongueWidth);
                break;
            }
            case 2: {
                float baseY = cy + size / 2.0f + 2.0f;
                float tipY = baseY + tongueLength;
                if (!(tongueLength > 2.0f)) break;
                drawList.addLine(startX, baseY, startX, tipY - forkSize, tongueColor, tongueWidth);
                if (!(forkSize > 1.0f)) break;
                drawList.addLine(startX, tipY - forkSize, startX - forkSize, tipY, tongueColor, tongueWidth);
                drawList.addLine(startX, tipY - forkSize, startX + forkSize, tipY, tongueColor, tongueWidth);
                break;
            }
            case 3: {
                float baseX = cx - size / 2.0f - 2.0f;
                float tipX = baseX - tongueLength;
                if (!(tongueLength > 2.0f)) break;
                drawList.addLine(baseX, startY, tipX + forkSize, startY, tongueColor, tongueWidth);
                if (!(forkSize > 1.0f)) break;
                drawList.addLine(tipX + forkSize, startY, tipX, startY - forkSize, tongueColor, tongueWidth);
                drawList.addLine(tipX + forkSize, startY, tipX, startY + forkSize, tongueColor, tongueWidth);
                break;
            }
            case 1: {
                float baseX = cx + size / 2.0f + 2.0f;
                float tipX = baseX + tongueLength;
                if (!(tongueLength > 2.0f)) break;
                drawList.addLine(baseX, startY, tipX - forkSize, startY, tongueColor, tongueWidth);
                if (!(forkSize > 1.0f)) break;
                drawList.addLine(tipX - forkSize, startY, tipX, startY - forkSize, tongueColor, tongueWidth);
                drawList.addLine(tipX - forkSize, startY, tipX, startY + forkSize, tongueColor, tongueWidth);
            }
        }
    }

    private void drawEyes(ImDrawList drawList, float cx, float cy, float size) {
        float eye2Y;
        float eye2X;
        float eye1Y;
        float eye1X;
        float eyeSize = 5.0f;
        float eyeSpacing = 7.0f;
        float eyeOffset = 4.0f;
        float offsetX = 0.0f;
        float offsetY = 0.0f;
        switch (this.direction) {
            case 0: {
                offsetY = -eyeOffset;
                break;
            }
            case 2: {
                offsetY = eyeOffset;
                break;
            }
            case 3: {
                offsetX = -eyeOffset;
                break;
            }
            case 1: {
                offsetX = eyeOffset;
            }
        }
        if (this.direction == 0 || this.direction == 2) {
            eye1X = cx - eyeSpacing + offsetX;
            eye1Y = cy + offsetY;
            eye2X = cx + eyeSpacing + offsetX;
            eye2Y = cy + offsetY;
        } else {
            eye1X = cx + offsetX;
            eye1Y = cy - eyeSpacing + offsetY;
            eye2X = cx + offsetX;
            eye2Y = cy + eyeSpacing + offsetY;
        }
        drawList.addCircleFilled(eye1X, eye1Y, eyeSize, -1);
        drawList.addCircleFilled(eye2X, eye2Y, eyeSize, -1);
        float pupilOffset = 2.0f;
        float pupilOffX = 0.0f;
        float pupilOffY = 0.0f;
        switch (this.direction) {
            case 0: {
                pupilOffY = -pupilOffset;
                break;
            }
            case 2: {
                pupilOffY = pupilOffset;
                break;
            }
            case 3: {
                pupilOffX = -pupilOffset;
                break;
            }
            case 1: {
                pupilOffX = pupilOffset;
            }
        }
        drawList.addCircleFilled(eye1X + pupilOffX, eye1Y + pupilOffY, eyeSize * 0.5f, -16777216);
        drawList.addCircleFilled(eye2X + pupilOffX, eye2Y + pupilOffY, eyeSize * 0.5f, -16777216);
    }

    private int applyPowerUpColor(int color, int segmentIndex) {
        if (this.activePowerUp == PowerUpType.GHOST) {
            float pulse = (float)Math.sin(this.powerUpPulse * 3.0f + (float)segmentIndex * 0.3f) * 0.2f + 0.6f;
            int alpha = (int)(pulse * 255.0f);
            color = color & 0xFFFFFF | alpha << 24;
        }
        if (this.activePowerUp == PowerUpType.SPEED_BOOST && segmentIndex > 0) {
            color = MiniGameSnake.lerpColor(color, PowerUpType.SPEED_BOOST.color, 0.3f);
        }
        return color;
    }

    private void drawRotatedRect(ImDrawList drawList, float cx, float cy, float w, float h, float angle, int color) {
        float rad = (float)Math.toRadians(angle);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);
        float hw = w / 2.0f;
        float hh = h / 2.0f;
        float[] px = new float[4];
        float[] py = new float[4];
        float[][] corners = new float[][]{{-hw, -hh}, {hw, -hh}, {hw, hh}, {-hw, hh}};
        for (int i = 0; i < 4; ++i) {
            px[i] = cx + corners[i][0] * cos - corners[i][1] * sin;
            py[i] = cy + corners[i][0] * sin + corners[i][1] * cos;
        }
        drawList.addQuadFilled(px[0], py[0], px[1], py[1], px[2], py[2], px[3], py[3], color);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float easeOutQuad(float t) {
        return 1.0f - (1.0f - t) * (1.0f - t);
    }

    private static int lerpColor(int c1, int c2, float t) {
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)MiniGameSnake.lerp(a1, a2, t);
        int r = (int)MiniGameSnake.lerp(r1, r2, t);
        int g = (int)MiniGameSnake.lerp(g1, g2, t);
        int b = (int)MiniGameSnake.lerp(b1, b2, t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private void renderIntroMenu() {
        float boardWidth = 672.0f;
        float boardHeight = 480.0f;
        ImGui.beginGroup();
        ImVec2 startPos = ImGui.getCursorScreenPos();
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(startPos.x, startPos.y, startPos.x + boardWidth, startPos.y + boardHeight, -15066587, 8.0f);
        drawList.addRect(startPos.x, startPos.y, startPos.x + boardWidth, startPos.y + boardHeight, -11908512, 8.0f, 0, 2.0f);
        float contentStartY = startPos.y + 40.0f;
        String title = "\uf11b  SNAKE";
        ImVec2 titleSize = new ImVec2();
        ImGui.calcTextSize(titleSize, title);
        drawList.addText(startPos.x + (boardWidth - titleSize.x) / 2.0f, contentStartY, -11477152, title);
        if (this.bestScore > 0) {
            String bestText = "\uf091 Najlepszy wynik: " + this.bestScore;
            ImVec2 bestSize = new ImVec2();
            ImGui.calcTextSize(bestSize, bestText);
            drawList.addText(startPos.x + (boardWidth - bestSize.x) / 2.0f, contentStartY + 35.0f, -13232, bestText);
        }
        float menuY = contentStartY + 80.0f;
        float menuWidth = 250.0f;
        float menuHeight = 40.0f;
        float menuSpacing = 15.0f;
        float menuX = startPos.x + (boardWidth - menuWidth) / 2.0f;
        this.renderMenuItem(drawList, menuX, menuY, menuWidth, menuHeight, "\uf04b  ROZPOCZNIJ GRE", this.menuSelectedOption == 0, -11477152);
        menuY += menuHeight + menuSpacing;
        String diffText = "Poziom: " + this.difficulty.name;
        if (this.menuSelectedOption == 1) {
            diffText = "< " + diffText + " >";
        }
        int diffColor = switch (this.difficulty.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> -11481264;
            case 1 -> -11481137;
            case 2 -> -3158192;
            case 3 -> -3190704;
        };
        this.renderMenuItem(drawList, menuX, menuY, menuWidth, menuHeight, diffText, this.menuSelectedOption == 1, diffColor);
        menuY += menuHeight + menuSpacing;
        String soundText = "Dzwiek: " + (this.soundEnabled ? "Wlaczony" : "Wylaczony");
        if (this.menuSelectedOption == 2) {
            soundText = "< " + soundText + " >";
        }
        this.renderMenuItem(drawList, menuX, menuY, menuWidth, menuHeight, soundText, this.menuSelectedOption == 2, this.soundEnabled ? -11481089 : -8355712);
        float infoStartY = menuY + menuHeight + 30.0f;
        String infoLabel = "Power-upy:";
        ImVec2 infoLabelSize = new ImVec2();
        ImGui.calcTextSize(infoLabelSize, infoLabel);
        drawList.addText(startPos.x + (boardWidth - infoLabelSize.x) / 2.0f, infoStartY, -5592406, infoLabel);
        float powerUpY = infoStartY + 25.0f;
        float col1X = startPos.x + 80.0f;
        float col2X = startPos.x + boardWidth / 2.0f + 40.0f;
        this.drawPowerUpInfo(drawList, col1X, powerUpY, PowerUpType.SPEED_BOOST, "Przyspieszenie");
        this.drawPowerUpInfo(drawList, col1X, powerUpY + 22.0f, PowerUpType.SLOW_DOWN, "Spowolnienie");
        this.drawPowerUpInfo(drawList, col1X, powerUpY + 44.0f, PowerUpType.GHOST, "Przechodzenie scian");
        this.drawPowerUpInfo(drawList, col2X, powerUpY, PowerUpType.DOUBLE_POINTS, "Podwojne punkty");
        this.drawPowerUpInfo(drawList, col2X, powerUpY + 22.0f, PowerUpType.SHRINK, "Skrocenie ogona");
        float ctrlY = powerUpY + 85.0f;
        String ctrlLabel = "Sterowanie w menu: Strzalki / WASD + Enter/Spacja";
        ImVec2 ctrlSize = new ImVec2();
        ImGui.calcTextSize(ctrlSize, ctrlLabel);
        drawList.addText(startPos.x + (boardWidth - ctrlSize.x) / 2.0f, ctrlY, -10461088, ctrlLabel);
        ImGui.dummy(boardWidth, boardHeight);
        ImGui.endGroup();
    }

    private void renderMenuItem(ImDrawList drawList, float x, float y, float w, float h, String text, boolean selected, int accentColor) {
        int bgColor = selected ? 0x40FFFFFF : 0x20FFFFFF;
        drawList.addRectFilled(x, y, x + w, y + h, bgColor, 6.0f);
        if (selected) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 200.0) * 0.3f + 0.7f;
            int borderAlpha = (int)(pulse * 200.0f);
            int borderColor = borderAlpha << 24 | accentColor & 0xFFFFFF;
            drawList.addRect(x, y, x + w, y + h, borderColor, 6.0f, 0, 2.0f);
            drawList.addRectFilled(x + 4.0f, y + 8.0f, x + 8.0f, y + h - 8.0f, accentColor, 2.0f);
        }
        ImVec2 textSize = new ImVec2();
        ImGui.calcTextSize(textSize, text);
        float textX = x + (w - textSize.x) / 2.0f;
        float textY = y + (h - textSize.y) / 2.0f;
        int textColor = selected ? -1 : -858993460;
        drawList.addText(textX, textY, textColor, text);
    }

    public void menuUp() {
        if (this.showingIntro) {
            this.menuSelectedOption = (this.menuSelectedOption - 1 + 3) % 3;
            this.playMenuClick();
        }
    }

    public void menuDown() {
        if (this.showingIntro) {
            this.menuSelectedOption = (this.menuSelectedOption + 1) % 3;
            this.playMenuClick();
        }
    }

    public void menuLeft() {
        if (this.showingIntro) {
            if (this.menuSelectedOption == 1) {
                int idx = (this.difficulty.ordinal() - 1 + Difficulty.values().length) % Difficulty.values().length;
                this.difficulty = Difficulty.values()[idx];
                this.playMenuClick();
            } else if (this.menuSelectedOption == 2) {
                this.soundEnabled = !this.soundEnabled;
                this.playMenuClick();
            }
        }
    }

    public void menuRight() {
        if (this.showingIntro) {
            if (this.menuSelectedOption == 1) {
                int idx = (this.difficulty.ordinal() + 1) % Difficulty.values().length;
                this.difficulty = Difficulty.values()[idx];
                this.playMenuClick();
            } else if (this.menuSelectedOption == 2) {
                this.soundEnabled = !this.soundEnabled;
                this.playMenuClick();
            }
        }
    }

    public void menuSelect() {
        if (this.showingIntro) {
            switch (this.menuSelectedOption) {
                case 0: {
                    this.startGame();
                    break;
                }
                case 1: {
                    this.menuRight();
                    break;
                }
                case 2: {
                    this.soundEnabled = !this.soundEnabled;
                    this.playMenuClick();
                }
            }
        }
    }

    private void playMenuClick() {
        if (this.soundEnabled) {
            this.soundManager.playSound("builtin:SELECT_MENU", 50);
        }
    }

    private void drawPowerUpInfo(ImDrawList drawList, float x, float y, PowerUpType type, String desc) {
        float r = (float)(type.color >> 16 & 0xFF) / 255.0f;
        float g = (float)(type.color >> 8 & 0xFF) / 255.0f;
        float b = (float)(type.color & 0xFF) / 255.0f;
        drawList.addCircleFilled(x, y + 7.0f, 6.0f, type.color);
        drawList.addText(x + 15.0f, y, -3355444, desc);
    }

    public int getScore() {
        return this.score;
    }

    public int getBestScore() {
        return this.bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public boolean isShowingIntro() {
        return this.showingIntro;
    }

    public static enum Difficulty {
        EASY(0.18f, 0.002f, 0.1f, "Latwy", 0),
        NORMAL(0.15f, 0.003f, 0.08f, "Normalny", 4),
        HARD(0.12f, 0.004f, 0.06f, "Trudny", 8),
        INSANE(0.1f, 0.005f, 0.05f, "Szalony", 12);

        final float initialSpeed;
        final float speedIncrease;
        final float minSpeed;
        final String name;
        final int obstacleCount;

        private Difficulty(float initialSpeed, float speedIncrease, float minSpeed, String name, int obstacleCount) {
            this.initialSpeed = initialSpeed;
            this.speedIncrease = speedIncrease;
            this.minSpeed = minSpeed;
            this.name = name;
            this.obstacleCount = obstacleCount;
        }
    }

    private static class Segment {
        int gridX;
        int gridY;
        int prevGridX;
        int prevGridY;
        float renderX;
        float renderY;
        float fallVelocityY = 0.0f;
        float fallOffsetY = 0.0f;
        float rotation = 0.0f;
        float bulge = 0.0f;
        float bulgeFade = 0.0f;

        Segment(int x, int y) {
            this.gridX = x;
            this.gridY = y;
            this.prevGridX = x;
            this.prevGridY = y;
            this.renderX = (float)x * 24.0f;
            this.renderY = (float)y * 24.0f;
        }

        void moveTo(int newX, int newY) {
            this.prevGridX = this.gridX;
            this.prevGridY = this.gridY;
            this.gridX = newX;
            this.gridY = newY;
        }

        void updateRender(float progress) {
            this.renderX = MiniGameSnake.lerp((float)this.prevGridX * 24.0f, (float)this.gridX * 24.0f, MiniGameSnake.easeOutQuad(progress));
            this.renderY = MiniGameSnake.lerp((float)this.prevGridY * 24.0f, (float)this.gridY * 24.0f, MiniGameSnake.easeOutQuad(progress));
        }

        void snapToGrid() {
            this.prevGridX = this.gridX;
            this.prevGridY = this.gridY;
            this.renderX = (float)this.gridX * 24.0f;
            this.renderY = (float)this.gridY * 24.0f;
        }

        void updateBulge(float dt) {
            if (this.bulgeFade > 0.0f) {
                this.bulgeFade -= dt;
                if (this.bulgeFade <= 0.0f) {
                    this.bulge = 0.0f;
                    this.bulgeFade = 0.0f;
                }
            }
        }
    }
}

