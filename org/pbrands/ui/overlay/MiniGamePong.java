/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.pbrands.ui.overlay.MiniGamePong$Particle
 *  org.pbrands.ui.overlay.MiniGamePong$PowerUpType
 */
package org.pbrands.ui.overlay;

import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.pbrands.sound.SoundManager;
import org.pbrands.ui.overlay.MiniGamePong;

/*
 * Exception performing whole class analysis ignored.
 */
public class MiniGamePong {
    private static final float BOARD_WIDTH = 600.0f;
    private static final float BOARD_HEIGHT = 400.0f;
    private static final float PADDLE_WIDTH = 12.0f;
    private static final float PADDLE_HEIGHT = 80.0f;
    private static final float PADDLE_MARGIN = 20.0f;
    private static final float PADDLE_SPEED = 400.0f;
    private static final float BALL_SIZE = 14.0f;
    private static final float BALL_INITIAL_SPEED = 350.0f;
    private static final float BALL_SPEED_INCREMENT = 20.0f;
    private static final float BALL_MAX_SPEED = 900.0f;
    private Difficulty difficulty = Difficulty.NORMAL;
    private PowerUpType activePowerUp = null;
    private float powerUpTimer = 0.0f;
    private boolean powerUpForPlayer = false;
    private float powerUpX = -1.0f;
    private float powerUpY = -1.0f;
    private PowerUpType spawnedPowerUp = null;
    private float powerUpSpawnTimer = 0.0f;
    private static final float POWER_UP_SPAWN_INTERVAL = 12.0f;
    private static final float POWER_UP_SIZE = 24.0f;
    private static final int WIN_SCORE = 7;
    private float playerY;
    private float aiY;
    private float playerPaddleHeight = 80.0f;
    private float aiPaddleHeight = 80.0f;
    private float aiTargetY;
    private float aiUpdateTimer = 0.0f;
    private float ballX;
    private float ballY;
    private float ballVelX;
    private float ballVelY;
    private float ballSpeed;
    private int playerScore = 0;
    private int aiScore = 0;
    private boolean gameOver = false;
    private boolean playerWon = false;
    private boolean paused = false;
    private boolean waitingToServe = true;
    private boolean showingIntro = true;
    private float serveTimer = 0.0f;
    private int rallyCount = 0;
    private int maxRally = 0;
    private int menuSelectedOption = 0;
    private static final int MENU_OPTION_START = 0;
    private static final int MENU_OPTION_DIFFICULTY = 1;
    private static final int MENU_OPTION_SOUND = 2;
    private static final int MENU_OPTION_COUNT = 3;
    private float shakeIntensity = 0.0f;
    private float shakeOffsetX = 0.0f;
    private float shakeOffsetY = 0.0f;
    private float flashAlpha = 0.0f;
    private int flashColor = -1;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private final List<Particle> particles = new ArrayList<Particle>();
    private final Random random = new Random();
    private final List<float[]> ballTrail = new ArrayList<float[]>();
    private static final int MAX_TRAIL_LENGTH = 15;
    private final SoundManager soundManager = SoundManager.getInstance();
    private boolean soundEnabled = true;
    private static final int COLOR_BG = -15066587;
    private static final int COLOR_LINE = -12961200;
    private static final int COLOR_PLAYER = -11481264;
    private static final int COLOR_AI = -11513617;
    private static final int COLOR_BALL = -1;
    private static final int COLOR_SCORE = -2130706433;
    private int currentBallColor = -1;

    public MiniGamePong() {
        this.initGame();
    }

    public void initGame() {
        this.showingIntro = true;
        this.gameOver = false;
        this.paused = false;
        this.playerY = 200.0f;
        this.aiY = 200.0f;
        this.aiTargetY = 200.0f;
        this.aiUpdateTimer = 0.0f;
        this.playerPaddleHeight = 80.0f;
        this.aiPaddleHeight = 80.0f;
        this.ballX = 300.0f;
        this.ballY = 200.0f;
        this.ballSpeed = 350.0f;
        this.ballVelX = 0.0f;
        this.ballVelY = 0.0f;
        this.particles.clear();
        this.ballTrail.clear();
        this.activePowerUp = null;
        this.spawnedPowerUp = null;
    }

    public void startGame() {
        this.showingIntro = false;
        this.newGame();
    }

    public void newGame() {
        this.showingIntro = false;
        this.playerY = 200.0f;
        this.aiY = 200.0f;
        this.aiTargetY = 200.0f;
        this.aiUpdateTimer = 0.0f;
        this.playerScore = 0;
        this.aiScore = 0;
        this.gameOver = false;
        this.playerWon = false;
        this.paused = false;
        this.particles.clear();
        this.ballTrail.clear();
        this.activePowerUp = null;
        this.powerUpTimer = 0.0f;
        this.spawnedPowerUp = null;
        this.powerUpX = -1.0f;
        this.powerUpSpawnTimer = 5.0f;
        this.playerPaddleHeight = 80.0f * this.difficulty.playerPaddleScale;
        this.aiPaddleHeight = 80.0f;
        this.rallyCount = 0;
        this.maxRally = 0;
        this.shakeIntensity = 0.0f;
        this.flashAlpha = 0.0f;
        this.currentBallColor = -1;
        this.resetBall(true);
    }

    public boolean isShowingIntro() {
        return this.showingIntro;
    }

    private void resetBall(boolean towardsPlayer) {
        this.ballX = 300.0f;
        this.ballY = 200.0f;
        this.ballSpeed = 350.0f;
        this.waitingToServe = true;
        this.serveTimer = 0.0f;
        float angle = (this.random.nextFloat() - 0.5f) * 90.0f * (float)Math.PI / 180.0f;
        this.ballVelX = (float)Math.cos(angle) * this.ballSpeed * (float)(towardsPlayer ? -1 : 1);
        this.ballVelY = (float)Math.sin(angle) * this.ballSpeed;
        this.ballTrail.clear();
        this.currentBallColor = -1;
    }

    public void setDifficulty(Difficulty diff) {
        this.difficulty = diff;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public void cycleDifficulty() {
        Difficulty[] values = Difficulty.values();
        int next = (this.difficulty.ordinal() + 1) % values.length;
        this.difficulty = values[next];
    }

    public void update(float deltaTime) {
        float dy;
        float dx;
        if (this.gameOver || this.paused || this.showingIntro) {
            return;
        }
        if (this.shakeIntensity > 0.0f) {
            this.shakeIntensity -= deltaTime * 8.0f;
            this.shakeOffsetX = (this.random.nextFloat() - 0.5f) * this.shakeIntensity * 10.0f;
            this.shakeOffsetY = (this.random.nextFloat() - 0.5f) * this.shakeIntensity * 10.0f;
        } else {
            this.shakeOffsetY = 0.0f;
            this.shakeOffsetX = 0.0f;
        }
        if (this.flashAlpha > 0.0f) {
            this.flashAlpha -= deltaTime * 4.0f;
        }
        if (this.activePowerUp != null) {
            this.powerUpTimer -= deltaTime;
            if (this.powerUpTimer <= 0.0f) {
                this.deactivatePowerUp();
            }
        }
        if (this.spawnedPowerUp == null && !this.waitingToServe) {
            this.powerUpSpawnTimer -= deltaTime;
            if (this.powerUpSpawnTimer <= 0.0f) {
                this.spawnPowerUp();
                this.powerUpSpawnTimer = 12.0f + this.random.nextFloat() * 5.0f;
            }
        }
        if (this.waitingToServe) {
            this.serveTimer += deltaTime;
            if (this.serveTimer >= 1.0f) {
                this.waitingToServe = false;
            }
            return;
        }
        if (this.moveUp) {
            this.playerY -= 400.0f * deltaTime;
        }
        if (this.moveDown) {
            this.playerY += 400.0f * deltaTime;
        }
        this.playerY = MiniGamePong.clamp(this.playerY, this.playerPaddleHeight / 2.0f, 400.0f - this.playerPaddleHeight / 2.0f);
        this.updateAI(deltaTime);
        float speedRatio = (this.ballSpeed - 350.0f) / 550.0f;
        this.currentBallColor = this.lerpColor(-1, -11468801, speedRatio);
        this.ballTrail.add(0, new float[]{this.ballX, this.ballY, speedRatio});
        if (this.ballTrail.size() > 15) {
            this.ballTrail.remove(this.ballTrail.size() - 1);
        }
        float effectiveSpeed = 1.0f;
        if (this.activePowerUp == PowerUpType.FAST_BALL) {
            effectiveSpeed = 1.5f;
        }
        this.ballX += this.ballVelX * deltaTime * effectiveSpeed;
        this.ballY += this.ballVelY * deltaTime * effectiveSpeed;
        if (this.spawnedPowerUp != null && (dx = this.ballX - this.powerUpX) * dx + (dy = this.ballY - this.powerUpY) * dy < 361.0f) {
            this.activatePowerUp(this.spawnedPowerUp, this.ballVelX < 0.0f);
            this.spawnedPowerUp = null;
            this.spawnPowerUpParticles(this.powerUpX, this.powerUpY);
        }
        if (this.ballY - 7.0f <= 0.0f) {
            this.ballY = 7.0f;
            this.ballVelY = Math.abs(this.ballVelY);
            this.spawnWallParticles(this.ballX, 0.0f);
        } else if (this.ballY + 7.0f >= 400.0f) {
            this.ballY = 393.0f;
            this.ballVelY = -Math.abs(this.ballVelY);
            this.spawnWallParticles(this.ballX, 400.0f);
        }
        float playerPaddleX = 32.0f;
        if (this.ballVelX < 0.0f && this.ballX - 7.0f <= playerPaddleX && this.ballX + 7.0f >= 20.0f && this.ballY >= this.playerY - this.playerPaddleHeight / 2.0f && this.ballY <= this.playerY + this.playerPaddleHeight / 2.0f) {
            this.ballX = playerPaddleX + 7.0f;
            float hitPos = (this.ballY - this.playerY) / (this.playerPaddleHeight / 2.0f);
            float bounceAngle = hitPos * 60.0f * (float)Math.PI / 180.0f;
            this.ballSpeed = Math.min(900.0f, this.ballSpeed + 20.0f);
            this.ballVelX = Math.abs((float)Math.cos(bounceAngle) * this.ballSpeed);
            this.ballVelY = (float)Math.sin(bounceAngle) * this.ballSpeed;
            ++this.rallyCount;
            this.maxRally = Math.max(this.maxRally, this.rallyCount);
            this.spawnPaddleParticles(playerPaddleX, this.ballY, -11481264);
            this.playHitSound();
        }
        float aiPaddleX = 568.0f;
        if (this.ballVelX > 0.0f && this.ballX + 7.0f >= aiPaddleX && this.ballX - 7.0f <= 580.0f && this.ballY >= this.aiY - this.aiPaddleHeight / 2.0f && this.ballY <= this.aiY + this.aiPaddleHeight / 2.0f) {
            this.ballX = aiPaddleX - 7.0f;
            float hitPos = (this.ballY - this.aiY) / (this.aiPaddleHeight / 2.0f);
            float bounceAngle = hitPos * 60.0f * (float)Math.PI / 180.0f;
            this.ballSpeed = Math.min(900.0f, this.ballSpeed + 20.0f);
            this.ballVelX = -Math.abs((float)Math.cos(bounceAngle) * this.ballSpeed);
            this.ballVelY = (float)Math.sin(bounceAngle) * this.ballSpeed;
            ++this.rallyCount;
            this.spawnPaddleParticles(aiPaddleX, this.ballY, -11513617);
            this.playHitSound();
        }
        if (this.ballX < 0.0f) {
            if (this.activePowerUp == PowerUpType.SHIELD && this.powerUpForPlayer) {
                this.ballX = 0.0f;
                this.ballVelX = -this.ballVelX;
                this.triggerFlash(PowerUpType.SHIELD.color);
                this.spawnPaddleParticles(0.0f, this.ballY, PowerUpType.SHIELD.color);
                this.deactivatePowerUp();
            } else {
                ++this.aiScore;
                this.rallyCount = 0;
                this.shakeIntensity = 1.0f;
                this.spawnScoreParticles(0.0f, this.ballY, -11513617);
                this.checkWin();
                if (!this.gameOver) {
                    this.resetBall(true);
                }
            }
        } else if (this.ballX > 600.0f) {
            if (this.activePowerUp == PowerUpType.SHIELD && !this.powerUpForPlayer) {
                this.ballX = 600.0f;
                this.ballVelX = -this.ballVelX;
                this.triggerFlash(PowerUpType.SHIELD.color);
                this.spawnPaddleParticles(600.0f, this.ballY, PowerUpType.SHIELD.color);
                this.deactivatePowerUp();
            } else {
                ++this.playerScore;
                this.rallyCount = 0;
                this.shakeIntensity = 1.0f;
                this.spawnScoreParticles(600.0f, this.ballY, -11481264);
                this.checkWin();
                if (!this.gameOver) {
                    this.resetBall(false);
                }
            }
        }
        this.particles.removeIf(p -> !p.update(deltaTime));
    }

    private void spawnPowerUp() {
        PowerUpType[] types = PowerUpType.values();
        this.spawnedPowerUp = types[this.random.nextInt(types.length)];
        this.powerUpX = 150.0f + this.random.nextFloat() * 600.0f / 2.0f;
        this.powerUpY = 100.0f + this.random.nextFloat() * 400.0f / 2.0f;
    }

    private void activatePowerUp(PowerUpType type, boolean forPlayer) {
        this.activePowerUp = type;
        this.powerUpTimer = type.duration;
        this.powerUpForPlayer = forPlayer;
        float basePlayerPaddle = 80.0f * this.difficulty.playerPaddleScale;
        float baseAiPaddle = 80.0f;
        switch (type.ordinal()) {
            case 0: {
                if (forPlayer) {
                    this.playerPaddleHeight = basePlayerPaddle * 1.5f;
                    break;
                }
                this.aiPaddleHeight = baseAiPaddle * 1.5f;
                break;
            }
            case 3: {
                if (forPlayer) {
                    this.aiPaddleHeight = baseAiPaddle * 0.6f;
                    break;
                }
                this.playerPaddleHeight = basePlayerPaddle * 0.6f;
                break;
            }
            case 4: {
                break;
            }
        }
        this.triggerFlash(type.color);
    }

    private void deactivatePowerUp() {
        this.playerPaddleHeight = 80.0f * this.difficulty.playerPaddleScale;
        this.aiPaddleHeight = 80.0f;
        this.activePowerUp = null;
    }

    private void triggerFlash(int color) {
        this.flashAlpha = 0.3f;
        this.flashColor = color;
    }

    private void spawnPowerUpParticles(float x, float y) {
        for (int i = 0; i < 20; ++i) {
            float angle = this.random.nextFloat() * (float)Math.PI * 2.0f;
            float speed = 80.0f + this.random.nextFloat() * 120.0f;
            this.particles.add(new Particle(x, y, (float)Math.cos(angle) * speed, (float)Math.sin(angle) * speed, this.spawnedPowerUp != null ? this.spawnedPowerUp.color : -1, 4.0f + this.random.nextFloat() * 3.0f));
        }
    }

    private int lerpColor(int c1, int c2, float t) {
        t = MiniGamePong.clamp(t, 0.0f, 1.0f);
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * t);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int g = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private void updateAI(float deltaTime) {
        float diff;
        float speedMult = 1.0f;
        if (this.activePowerUp == PowerUpType.SLOW_AI && this.powerUpForPlayer) {
            speedMult = 0.5f;
        }
        this.aiUpdateTimer += deltaTime;
        if (this.aiUpdateTimer >= this.difficulty.updateInterval) {
            this.aiUpdateTimer = 0.0f;
            if (this.ballVelX > 0.0f) {
                float timeToReach = (568.0f - this.ballX) / this.ballVelX;
                float predictedY = this.ballY + this.ballVelY * timeToReach;
                while (predictedY < 0.0f || predictedY > 400.0f) {
                    if (predictedY < 0.0f) {
                        predictedY = -predictedY;
                        continue;
                    }
                    if (!(predictedY > 400.0f)) continue;
                    predictedY = 800.0f - predictedY;
                }
                this.aiTargetY = MiniGamePong.clamp(predictedY += (this.random.nextFloat() - 0.5f) * this.difficulty.error, this.aiPaddleHeight / 2.0f, 400.0f - this.aiPaddleHeight / 2.0f);
            } else {
                this.aiTargetY = 200.0f;
            }
        }
        if (Math.abs(diff = this.aiTargetY - this.aiY) > this.difficulty.deadZone) {
            float maxMove = this.difficulty.speed * deltaTime * speedMult;
            float moveAmount = Math.min(Math.abs(diff) * 0.1f + maxMove * 0.5f, maxMove);
            this.aiY += Math.signum(diff) * moveAmount;
        }
        this.aiY = MiniGamePong.clamp(this.aiY, this.aiPaddleHeight / 2.0f, 400.0f - this.aiPaddleHeight / 2.0f);
    }

    private void checkWin() {
        if (this.playerScore >= 7) {
            this.gameOver = true;
            this.playerWon = true;
            this.playWinSound();
        } else if (this.aiScore >= 7) {
            this.gameOver = true;
            this.playerWon = false;
            this.playLoseSound();
        }
    }

    private void playHitSound() {
        if (this.soundEnabled) {
            this.soundManager.playSound("builtin:TENNIS_BALL_HIT", 50);
        }
    }

    private void playWinSound() {
        if (this.soundEnabled) {
            this.soundManager.playSound("builtin:WIN_SOUND", 70);
        }
    }

    private void playLoseSound() {
        if (this.soundEnabled) {
            this.soundManager.playSound("builtin:PONG_FAILURE", 70);
        }
    }

    private void spawnWallParticles(float x, float y) {
        for (int i = 0; i < 8; ++i) {
            float vx = (this.random.nextFloat() - 0.5f) * 150.0f;
            float vy = (float)(y == 0.0f ? 1 : -1) * this.random.nextFloat() * 100.0f;
            this.particles.add(new Particle(x, y, vx, vy, this.currentBallColor, 2.0f + this.random.nextFloat() * 2.0f));
        }
    }

    private void spawnPaddleParticles(float x, float y, int color) {
        for (int i = 0; i < 12; ++i) {
            float angle = (this.random.nextFloat() - 0.5f) * (float)Math.PI;
            float speed = 80.0f + this.random.nextFloat() * 150.0f;
            float vx = (float)Math.cos(angle) * speed * (float)(x < 300.0f ? 1 : -1);
            float vy = (float)Math.sin(angle) * speed;
            this.particles.add(new Particle(x, y, vx, vy, color, 3.0f + this.random.nextFloat() * 3.0f));
        }
    }

    private void spawnScoreParticles(float x, float y, int color) {
        for (int i = 0; i < 25; ++i) {
            float angle = this.random.nextFloat() * (float)Math.PI * 2.0f;
            float speed = 150.0f + this.random.nextFloat() * 200.0f;
            float vx = (float)Math.cos(angle) * speed;
            float vy = (float)Math.sin(angle) * speed;
            this.particles.add(new Particle(x, y, vx, vy, color, 4.0f + this.random.nextFloat() * 4.0f));
        }
    }

    public void setMoveUp(boolean up) {
        this.moveUp = up;
    }

    public void setMoveDown(boolean down) {
        this.moveDown = down;
    }

    public void togglePause() {
        if (!this.gameOver) {
            this.paused = !this.paused;
        }
    }

    public int getRallyCount() {
        return this.rallyCount;
    }

    public int getMaxRally() {
        return this.maxRally;
    }

    public void render(float deltaTime) {
        this.update(deltaTime);
        float windowWidth = ImGui.getContentRegionAvailX();
        float totalWidth = 600.0f;
        float centerOffsetX = Math.max(0.0f, (windowWidth - totalWidth) / 2.0f);
        ImGui.beginGroup();
        ImGui.pushStyleColor(3, 0.1f, 0.1f, 0.15f, 0.9f);
        ImGui.setCursorPosX(ImGui.getCursorPosX() + centerOffsetX);
        if (ImGui.beginChild("##pong_controls", 600.0f, 50.0f, true)) {
            ImGui.textColored(0.5f, 0.8f, 1.0f, 1.0f, "Sterowanie:");
            ImGui.sameLine(100.0f);
            ImGui.textColored(0.7f, 0.7f, 0.7f, 1.0f, "W/S lub Strzalki - ruch paletka");
            ImGui.sameLine(380.0f);
            ImGui.textColored(0.7f, 0.7f, 0.7f, 1.0f, "R - restart");
            ImGui.sameLine(480.0f);
            ImGui.textColored(0.7f, 0.7f, 0.7f, 1.0f, "P - pauza");
        }
        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.spacing();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + centerOffsetX);
        ImGui.textColored(0.3f, 0.8f, 0.3f, 1.0f, "Ty: " + this.playerScore);
        ImGui.sameLine(centerOffsetX + 80.0f);
        ImGui.textColored(0.6f, 0.6f, 0.6f, 1.0f, "do 7");
        ImGui.sameLine(centerOffsetX + 140.0f);
        ImGui.textColored(0.3f, 0.3f, 0.9f, 1.0f, "AI: " + this.aiScore);
        ImGui.sameLine(centerOffsetX + 220.0f);
        ImGui.textColored(0.5f, 0.5f, 0.5f, 1.0f, "Poziom:");
        ImGui.sameLine();
        ImGui.pushStyleColor(21, 0.2f, 0.2f, 0.3f, 1.0f);
        if (ImGui.smallButton(this.difficulty.name) && !this.gameOver && !this.paused) {
            this.cycleDifficulty();
        }
        ImGui.popStyleColor();
        if (this.rallyCount > 2) {
            ImGui.sameLine(centerOffsetX + 420.0f);
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 100.0) * 0.3f + 0.7f;
            ImGui.textColored(1.0f, 0.8f * pulse, 0.2f, 1.0f, "Rally: " + this.rallyCount);
        }
        if (this.activePowerUp != null) {
            ImGui.sameLine(centerOffsetX + 520.0f);
            float pr = (float)(this.activePowerUp.color >> 16 & 0xFF) / 255.0f;
            float pg = (float)(this.activePowerUp.color >> 8 & 0xFF) / 255.0f;
            float pb = (float)(this.activePowerUp.color & 0xFF) / 255.0f;
            ImGui.textColored(pr, pg, pb, 1.0f, String.format("%.1fs", Float.valueOf(this.powerUpTimer)));
        }
        ImGui.spacing();
        ImGui.setCursorPosX(ImGui.getCursorPosX() + centerOffsetX);
        ImVec2 startPos = ImGui.getCursorScreenPos();
        float boardX = startPos.x + this.shakeOffsetX;
        float boardY = startPos.y + this.shakeOffsetY;
        ImDrawList drawList = ImGui.getWindowDrawList();
        drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, -15066587, 4.0f);
        if (this.flashAlpha > 0.0f) {
            int flashCol = (int)(this.flashAlpha * 255.0f) << 24 | this.flashColor & 0xFFFFFF;
            drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, flashCol, 4.0f);
        }
        float centerX = boardX + 300.0f;
        for (float y = 0.0f; y < 400.0f; y += 20.0f) {
            drawList.addRectFilled(centerX - 2.0f, boardY + y, centerX + 2.0f, boardY + y + 10.0f, -12961200);
        }
        if (this.spawnedPowerUp != null) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 150.0) * 0.2f + 0.8f;
            float size = 24.0f * pulse;
            drawList.addCircleFilled(boardX + this.powerUpX, boardY + this.powerUpY, size * 0.8f, 0x40000000 | this.spawnedPowerUp.color & 0xFFFFFF);
            drawList.addCircleFilled(boardX + this.powerUpX, boardY + this.powerUpY, size * 0.5f, this.spawnedPowerUp.color);
            float rot = (float)(System.currentTimeMillis() % 2000L) / 2000.0f * (float)Math.PI * 2.0f;
            for (int i = 0; i < 4; ++i) {
                float a = rot + (float)i * (float)Math.PI / 2.0f;
                float ox = (float)Math.cos(a) * size * 0.7f;
                float oy = (float)Math.sin(a) * size * 0.7f;
                drawList.addCircleFilled(boardX + this.powerUpX + ox, boardY + this.powerUpY + oy, 3.0f, this.spawnedPowerUp.color);
            }
        }
        for (int i = 0; i < this.ballTrail.size(); ++i) {
            float[] pos = this.ballTrail.get(i);
            float alpha = 1.0f - (float)i / (float)this.ballTrail.size();
            float size = 14.0f * (1.0f - (float)i / (float)this.ballTrail.size() * 0.6f);
            int trailBaseColor = pos.length > 2 ? this.lerpColor(-1, -11468801, pos[2]) : -1;
            int trailColor = (int)(alpha * 120.0f) << 24 | trailBaseColor & 0xFFFFFF;
            drawList.addCircleFilled(boardX + pos[0], boardY + pos[1], size / 2.0f, trailColor);
        }
        if (!this.waitingToServe || (int)(this.serveTimer * 4.0f) % 2 == 0) {
            drawList.addCircleFilled(boardX + this.ballX, boardY + this.ballY, 13.0f, 0x20FFFFFF);
            drawList.addCircleFilled(boardX + this.ballX, boardY + this.ballY, 10.0f, 0x40FFFFFF);
            drawList.addCircleFilled(boardX + this.ballX, boardY + this.ballY, 7.0f, this.currentBallColor);
        }
        float playerPaddleX = boardX + 20.0f;
        float playerPaddleY = boardY + this.playerY - this.playerPaddleHeight / 2.0f;
        if (this.activePowerUp == PowerUpType.BIG_PADDLE && this.powerUpForPlayer) {
            drawList.addRectFilled(playerPaddleX - 4.0f, playerPaddleY - 4.0f, playerPaddleX + 12.0f + 4.0f, playerPaddleY + this.playerPaddleHeight + 4.0f, 810614608, 6.0f);
        }
        drawList.addRectFilled(playerPaddleX, playerPaddleY, playerPaddleX + 12.0f, playerPaddleY + this.playerPaddleHeight, -11481264, 4.0f);
        drawList.addRect(playerPaddleX - 2.0f, playerPaddleY - 2.0f, playerPaddleX + 12.0f + 2.0f, playerPaddleY + this.playerPaddleHeight + 2.0f, 1079037776, 4.0f, 0, 2.0f);
        float aiPaddleX = boardX + 600.0f - 20.0f - 12.0f;
        float aiPaddleY = boardY + this.aiY - this.aiPaddleHeight / 2.0f;
        if (this.activePowerUp == PowerUpType.BIG_PADDLE && !this.powerUpForPlayer || this.activePowerUp == PowerUpType.SMALL_ENEMY && this.powerUpForPlayer) {
            int glowColor = this.activePowerUp == PowerUpType.SMALL_ENEMY ? 822038608 : 810569983;
            drawList.addRectFilled(aiPaddleX - 4.0f, aiPaddleY - 4.0f, aiPaddleX + 12.0f + 4.0f, aiPaddleY + this.aiPaddleHeight + 4.0f, glowColor, 6.0f);
        }
        drawList.addRectFilled(aiPaddleX, aiPaddleY, aiPaddleX + 12.0f, aiPaddleY + this.aiPaddleHeight, -11513617, 4.0f);
        drawList.addRect(aiPaddleX - 2.0f, aiPaddleY - 2.0f, aiPaddleX + 12.0f + 2.0f, aiPaddleY + this.aiPaddleHeight + 2.0f, 1079005423, 4.0f, 0, 2.0f);
        for (Particle p : this.particles) {
            int alpha = (int)(p.life * 255.0f);
            int color = alpha << 24 | p.color & 0xFFFFFF;
            drawList.addCircleFilled(boardX + p.x, boardY + p.y, p.size * p.life, color);
        }
        ImGui.dummy(600.0f, 400.0f);
        if (this.gameOver) {
            drawList.addRectFilled(startPos.x, startPos.y, startPos.x + 600.0f, startPos.y + 400.0f, -585491931, 4.0f);
            msg = this.playerWon ? "WYGRALES!" : "PRZEGRALES";
            int msgColor = this.playerWon ? -11481264 : -11513617;
            ImVec2 msgSize = new ImVec2();
            ImGui.calcTextSize(msgSize, msg);
            drawList.addText(startPos.x + (600.0f - msgSize.x) / 2.0f, startPos.y + 200.0f - 20.0f, msgColor, msg);
            if (this.maxRally > 3) {
                String rallyMsg = "Najdluzsze rally: " + this.maxRally;
                ImVec2 rallySize = new ImVec2();
                ImGui.calcTextSize(rallySize, rallyMsg);
                drawList.addText(startPos.x + (600.0f - rallySize.x) / 2.0f, startPos.y + 200.0f + 10.0f, -21936, rallyMsg);
            }
        } else if (this.paused) {
            drawList.addRectFilled(startPos.x, startPos.y, startPos.x + 600.0f, startPos.y + 400.0f, -1441129947, 4.0f);
            msg = "PAUZA";
            ImVec2 msgSize = new ImVec2();
            ImGui.calcTextSize(msgSize, msg);
            drawList.addText(startPos.x + (600.0f - msgSize.x) / 2.0f, startPos.y + 200.0f - 10.0f, -1, msg);
        } else if (this.waitingToServe && !this.showingIntro) {
            msg = "Przygotuj sie...";
            ImVec2 msgSize = new ImVec2();
            ImGui.calcTextSize(msgSize, msg);
            drawList.addText(startPos.x + (600.0f - msgSize.x) / 2.0f, startPos.y + 200.0f - 10.0f, -2130706433, msg);
        } else if (this.showingIntro) {
            String[] controls;
            drawList.addRectFilled(startPos.x, startPos.y, startPos.x + 600.0f, startPos.y + 400.0f, -266724827, 4.0f);
            String title = "\uf45d  PONG";
            ImVec2 titleSize = new ImVec2();
            ImGui.calcTextSize(titleSize, title);
            float titleX = startPos.x + (600.0f - titleSize.x) / 2.0f;
            float titleY = startPos.y + 50.0f;
            drawList.addText(titleX - 1.0f, titleY - 1.0f, 1079037776, title);
            drawList.addText(titleX + 1.0f, titleY + 1.0f, 1079037776, title);
            drawList.addText(titleX, titleY, -1, title);
            String subtitle = "Klasyczna gra przeciwko AI";
            ImVec2 subSize = new ImVec2();
            ImGui.calcTextSize(subSize, subtitle);
            drawList.addText(startPos.x + (600.0f - subSize.x) / 2.0f, startPos.y + 85.0f, -1716868438, subtitle);
            float menuY = startPos.y + 140.0f;
            float menuItemHeight = 45.0f;
            float menuWidth = 280.0f;
            float menuX = startPos.x + (600.0f - menuWidth) / 2.0f;
            this.renderMenuItem(drawList, menuX, menuY, menuWidth, menuItemHeight - 5.0f, "\uf04b  ROZPOCZNIJ GRE", this.menuSelectedOption == 0, -11481264);
            String diffText = "\uf012  Poziom: " + this.difficulty.name;
            this.renderMenuItem(drawList, menuX, menuY += menuItemHeight, menuWidth, menuItemHeight - 5.0f, diffText, this.menuSelectedOption == 1, -11497233);
            if (this.menuSelectedOption == 1) {
                drawList.addText(menuX - 25.0f, menuY + 10.0f, -855638017, "\uf053");
                drawList.addText(menuX + menuWidth + 10.0f, menuY + 10.0f, -855638017, "\uf054");
            }
            String soundText = "\uf028  Dzwiek: " + (this.soundEnabled ? "WLACZONY" : "WYLACZONY");
            int soundColor = this.soundEnabled ? -11481264 : -11513686;
            this.renderMenuItem(drawList, menuX, menuY += menuItemHeight, menuWidth, menuItemHeight - 5.0f, soundText, this.menuSelectedOption == 2, soundColor);
            menuY += menuItemHeight;
            menuY = startPos.y + 400.0f - 100.0f;
            float lineHeight = 18.0f;
            for (String line : controls = new String[]{"\uf062 \uf063  Nawiguj menu", "\uf060 \uf061  Zmien opcje", "ENTER/SPACJA  Wybierz"}) {
                ImVec2 lineSize = new ImVec2();
                ImGui.calcTextSize(lineSize, line);
                drawList.addText(startPos.x + (600.0f - lineSize.x) / 2.0f, menuY, -2136298838, line);
                menuY += lineHeight;
            }
            String gameControls = "W gre: W/S - ruch | P - pauza | R - restart";
            ImVec2 gcSize = new ImVec2();
            ImGui.calcTextSize(gcSize, gameControls);
            drawList.addText(startPos.x + (600.0f - gcSize.x) / 2.0f, menuY += 10.0f, 0x60888888, gameControls);
        }
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
        if (this.showingIntro && this.menuSelectedOption == 1) {
            int idx = (this.difficulty.ordinal() - 1 + Difficulty.values().length) % Difficulty.values().length;
            this.difficulty = Difficulty.values()[idx];
            this.playMenuClick();
        } else if (this.showingIntro && this.menuSelectedOption == 2) {
            this.soundEnabled = !this.soundEnabled;
            this.playMenuClick();
        }
    }

    public void menuRight() {
        if (this.showingIntro && this.menuSelectedOption == 1) {
            int idx = (this.difficulty.ordinal() + 1) % Difficulty.values().length;
            this.difficulty = Difficulty.values()[idx];
            this.playMenuClick();
        } else if (this.showingIntro && this.menuSelectedOption == 2) {
            this.soundEnabled = !this.soundEnabled;
            this.playMenuClick();
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

    public void backToMenu() {
        if (this.gameOver) {
            this.gameOver = false;
            this.playerWon = false;
            this.paused = false;
            this.playerScore = 0;
            this.aiScore = 0;
            this.initGame();
            this.showingIntro = true;
            this.menuSelectedOption = 0;
            this.playMenuClick();
        }
    }

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public boolean isGameOver() {
        return this.gameOver;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public float getBoardWidth() {
        return 600.0f;
    }

    public float getBoardHeight() {
        return 400.0f;
    }

    public void renderFullscreen(float deltaTime) {
        int i;
        this.update(deltaTime);
        ImDrawList drawList = ImGui.getWindowDrawList();
        ImVec2 contentPos = ImGui.getCursorScreenPos();
        float contentWidth = ImGui.getContentRegionAvailX();
        float boardX = contentPos.x + (contentWidth - 600.0f) / 2.0f + this.shakeOffsetX;
        float boardY = contentPos.y + 25.0f + this.shakeOffsetY;
        float hudY = contentPos.y;
        float hudCenterX = boardX + 300.0f;
        String scoreText = String.format("%d  :  %d", this.playerScore, this.aiScore);
        ImVec2 scoreSize = new ImVec2();
        ImGui.calcTextSize(scoreSize, scoreText);
        String playerLabel = "\uf007 TY";
        ImVec2 playerLabelSize = new ImVec2();
        ImGui.calcTextSize(playerLabelSize, playerLabel);
        String aiLabel = "AI \uf544";
        ImVec2 aiLabelSize = new ImVec2();
        ImGui.calcTextSize(aiLabelSize, aiLabel);
        float totalHudWidth = playerLabelSize.x + 15.0f + scoreSize.x + 15.0f + aiLabelSize.x;
        float hudStartX = hudCenterX - totalHudWidth / 2.0f;
        drawList.addText(hudStartX, hudY, -11481264, playerLabel);
        drawList.addText(hudStartX + playerLabelSize.x + 15.0f, hudY, -1, scoreText);
        drawList.addText(hudStartX + playerLabelSize.x + 15.0f + scoreSize.x + 15.0f, hudY, -11513617, aiLabel);
        int diffColor = switch (this.difficulty.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> -11481264;
            case 1 -> -11481137;
            case 2 -> -3158192;
            case 3 -> -3190704;
        };
        String diffStr = this.difficulty.name;
        ImVec2 diffSize = new ImVec2();
        ImGui.calcTextSize(diffSize, diffStr);
        float diffX = boardX + 600.0f - diffSize.x - 5.0f;
        drawList.addRectFilled(diffX - 6.0f, hudY - 2.0f, diffX + diffSize.x + 6.0f, hudY + diffSize.y + 2.0f, 0x50000000 | diffColor & 0xFFFFFF, 3.0f);
        drawList.addText(diffX, hudY, diffColor, diffStr);
        drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, -15066587, 6.0f);
        drawList.addRect(boardX - 1.0f, boardY - 1.0f, boardX + 600.0f + 1.0f, boardY + 400.0f + 1.0f, 0x30FFFFFF, 6.0f, 0, 1.0f);
        if (this.flashAlpha > 0.0f) {
            int flashCol = (int)(this.flashAlpha * 255.0f) << 24 | this.flashColor & 0xFFFFFF;
            drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, flashCol, 8.0f);
        }
        float lineCenterX = boardX + 300.0f;
        for (float y = 0.0f; y < 400.0f; y += 20.0f) {
            drawList.addRectFilled(lineCenterX - 2.0f, boardY + y, lineCenterX + 2.0f, boardY + y + 10.0f, -12961200);
        }
        if (this.spawnedPowerUp != null && !this.showingIntro) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 150.0) * 0.2f + 0.8f;
            float size = 24.0f * pulse;
            drawList.addCircleFilled(boardX + this.powerUpX, boardY + this.powerUpY, size * 0.8f, 0x40000000 | this.spawnedPowerUp.color & 0xFFFFFF);
            drawList.addCircleFilled(boardX + this.powerUpX, boardY + this.powerUpY, size * 0.5f, this.spawnedPowerUp.color);
            float rot = (float)(System.currentTimeMillis() % 2000L) / 2000.0f * (float)Math.PI * 2.0f;
            for (i = 0; i < 4; ++i) {
                float a = rot + (float)i * (float)Math.PI / 2.0f;
                float ox = (float)Math.cos(a) * size * 0.7f;
                float oy = (float)Math.sin(a) * size * 0.7f;
                drawList.addCircleFilled(boardX + this.powerUpX + ox, boardY + this.powerUpY + oy, 3.0f, this.spawnedPowerUp.color);
            }
        }
        for (int i2 = 0; i2 < this.ballTrail.size(); ++i2) {
            float[] pos = this.ballTrail.get(i2);
            float alpha = 1.0f - (float)i2 / (float)this.ballTrail.size();
            float size = 14.0f * (1.0f - (float)i2 / (float)this.ballTrail.size() * 0.6f);
            int trailBaseColor = pos.length > 2 ? this.lerpColor(-1, -11468801, pos[2]) : -1;
            int trailColor = (int)(alpha * 120.0f) << 24 | trailBaseColor & 0xFFFFFF;
            drawList.addCircleFilled(boardX + pos[0], boardY + pos[1], size / 2.0f, trailColor);
        }
        if (!(this.showingIntro || this.waitingToServe && (int)(this.serveTimer * 4.0f) % 2 != 0)) {
            drawList.addCircleFilled(boardX + this.ballX, boardY + this.ballY, 13.0f, 0x20FFFFFF);
            drawList.addCircleFilled(boardX + this.ballX, boardY + this.ballY, 10.0f, 0x40FFFFFF);
            drawList.addCircleFilled(boardX + this.ballX, boardY + this.ballY, 7.0f, this.currentBallColor);
        }
        if (this.activePowerUp == PowerUpType.SHIELD) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 200.0) * 0.3f + 0.7f;
            int shieldAlpha = (int)(pulse * 80.0f);
            int shieldColor = shieldAlpha << 24 | PowerUpType.SHIELD.color & 0xFFFFFF;
            if (this.powerUpForPlayer) {
                drawList.addRectFilled(boardX, boardY, boardX + 4.0f, boardY + 400.0f, shieldColor, 0);
                for (i = 0; i < 5; ++i) {
                    float particleY = boardY + (float)((System.currentTimeMillis() / 20L + (long)(i * 80)) % 400L);
                    drawList.addCircleFilled(boardX + 2.0f, particleY, 3.0f, PowerUpType.SHIELD.color);
                }
            } else {
                drawList.addRectFilled(boardX + 600.0f - 4.0f, boardY, boardX + 600.0f, boardY + 400.0f, shieldColor, 0);
                for (i = 0; i < 5; ++i) {
                    float particleY = boardY + (float)((System.currentTimeMillis() / 20L + (long)(i * 80)) % 400L);
                    drawList.addCircleFilled(boardX + 600.0f - 2.0f, particleY, 3.0f, PowerUpType.SHIELD.color);
                }
            }
        }
        float playerPaddleX = boardX + 20.0f;
        float playerPaddleY = boardY + this.playerY - this.playerPaddleHeight / 2.0f;
        if (this.activePowerUp == PowerUpType.BIG_PADDLE && this.powerUpForPlayer) {
            drawList.addRectFilled(playerPaddleX - 4.0f, playerPaddleY - 4.0f, playerPaddleX + 12.0f + 4.0f, playerPaddleY + this.playerPaddleHeight + 4.0f, 810614608, 6.0f);
        }
        drawList.addRectFilled(playerPaddleX, playerPaddleY, playerPaddleX + 12.0f, playerPaddleY + this.playerPaddleHeight, -11481264, 4.0f);
        drawList.addRect(playerPaddleX - 2.0f, playerPaddleY - 2.0f, playerPaddleX + 12.0f + 2.0f, playerPaddleY + this.playerPaddleHeight + 2.0f, 1079037776, 4.0f, 0, 2.0f);
        float aiPaddleX = boardX + 600.0f - 20.0f - 12.0f;
        float aiPaddleY = boardY + this.aiY - this.aiPaddleHeight / 2.0f;
        if (this.activePowerUp == PowerUpType.BIG_PADDLE && !this.powerUpForPlayer || this.activePowerUp == PowerUpType.SMALL_ENEMY && this.powerUpForPlayer) {
            int glowColor = this.activePowerUp == PowerUpType.SMALL_ENEMY ? 822038608 : 810569983;
            drawList.addRectFilled(aiPaddleX - 4.0f, aiPaddleY - 4.0f, aiPaddleX + 12.0f + 4.0f, aiPaddleY + this.aiPaddleHeight + 4.0f, glowColor, 6.0f);
        }
        drawList.addRectFilled(aiPaddleX, aiPaddleY, aiPaddleX + 12.0f, aiPaddleY + this.aiPaddleHeight, -11513617, 4.0f);
        drawList.addRect(aiPaddleX - 2.0f, aiPaddleY - 2.0f, aiPaddleX + 12.0f + 2.0f, aiPaddleY + this.aiPaddleHeight + 2.0f, 1079005423, 4.0f, 0, 2.0f);
        for (Particle p : this.particles) {
            int alpha = (int)(p.life * 255.0f);
            int color = alpha << 24 | p.color & 0xFFFFFF;
            drawList.addCircleFilled(boardX + p.x, boardY + p.y, p.size * p.life, color);
        }
        float statusY = boardY + 400.0f + 5.0f;
        float leftInfoX = boardX + 10.0f;
        if (this.rallyCount > 2 && !this.showingIntro) {
            float pulse = (float)Math.sin((double)System.currentTimeMillis() / 100.0) * 0.3f + 0.7f;
            String rallyStr = "\uf06d " + this.rallyCount;
            int rallyColor = 0xFF000000 | (int)(204.0f * pulse) << 8 | 0x50;
            drawList.addText(leftInfoX, statusY, rallyColor, rallyStr);
            ImVec2 rallySize = new ImVec2();
            ImGui.calcTextSize(rallySize, rallyStr);
            leftInfoX += rallySize.x + 15.0f;
        }
        if (this.activePowerUp != null && !this.showingIntro) {
            String powerStr = this.activePowerUp.name + " " + String.format("%.1fs", Float.valueOf(this.powerUpTimer));
            drawList.addText(leftInfoX, statusY, this.activePowerUp.color, powerStr);
        }
        String controls = "W/S ruch | P pauza | R restart";
        ImVec2 ctrlSize = new ImVec2();
        ImGui.calcTextSize(ctrlSize, controls);
        drawList.addText(boardX + 600.0f - ctrlSize.x - 5.0f, statusY, 0x60AAAAAA, controls);
        if (this.gameOver) {
            this.renderGameOverOverlay(drawList, boardX, boardY);
        } else if (this.paused && !this.showingIntro) {
            this.renderPausedOverlay(drawList, boardX, boardY);
        } else if (this.waitingToServe && !this.showingIntro) {
            String msg = "Przygotuj sie...";
            ImVec2 msgSize = new ImVec2();
            ImGui.calcTextSize(msgSize, msg);
            drawList.addText(boardX + (600.0f - msgSize.x) / 2.0f, boardY + 200.0f - 10.0f, -2130706433, msg);
        } else if (this.showingIntro) {
            this.renderMenuOverlay(drawList, boardX, boardY);
        }
    }

    private void renderGameOverOverlay(ImDrawList drawList, float boardX, float boardY) {
        drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, -585491931, 8.0f);
        String msg = this.playerWon ? "WYGRALES!" : "PRZEGRALES";
        int msgColor = this.playerWon ? -11481264 : -11513617;
        ImVec2 msgSize = new ImVec2();
        ImGui.calcTextSize(msgSize, msg);
        drawList.addText(boardX + (600.0f - msgSize.x) / 2.0f, boardY + 200.0f - 40.0f, msgColor, msg);
        String scoreMsg = this.playerScore + " : " + this.aiScore;
        ImVec2 scoreSize = new ImVec2();
        ImGui.calcTextSize(scoreSize, scoreMsg);
        drawList.addText(boardX + (600.0f - scoreSize.x) / 2.0f, boardY + 200.0f - 10.0f, -855638017, scoreMsg);
        if (this.maxRally > 3) {
            String rallyMsg = "Najdluzsze rally: " + this.maxRally;
            ImVec2 rallySize = new ImVec2();
            ImGui.calcTextSize(rallySize, rallyMsg);
            drawList.addText(boardX + (600.0f - rallySize.x) / 2.0f, boardY + 200.0f + 20.0f, -21936, rallyMsg);
        }
        float pulse = (float)Math.sin((double)System.currentTimeMillis() / 300.0) * 0.3f + 0.7f;
        String restartMsg = "R - zagraj ponownie";
        ImVec2 restartSize = new ImVec2();
        ImGui.calcTextSize(restartSize, restartMsg);
        int restartColor = (int)(pulse * 200.0f) << 24 | 0xAAAAAA;
        drawList.addText(boardX + (600.0f - restartSize.x) / 2.0f - 80.0f, boardY + 400.0f - 50.0f, restartColor, restartMsg);
        String menuMsg = "M - wroc do menu";
        ImVec2 menuSize = new ImVec2();
        ImGui.calcTextSize(menuSize, menuMsg);
        int menuColor = (int)(pulse * 200.0f) << 24 | 0xAAAAAA;
        drawList.addText(boardX + (600.0f - menuSize.x) / 2.0f + 80.0f, boardY + 400.0f - 50.0f, menuColor, menuMsg);
    }

    private void renderPausedOverlay(ImDrawList drawList, float boardX, float boardY) {
        drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, -1441129947, 8.0f);
        String msg = "\uf04c  PAUZA";
        ImVec2 msgSize = new ImVec2();
        ImGui.calcTextSize(msgSize, msg);
        drawList.addText(boardX + (600.0f - msgSize.x) / 2.0f, boardY + 200.0f - 20.0f, -1, msg);
        String hint = "Nacisnij P aby kontynuowac";
        ImVec2 hintSize = new ImVec2();
        ImGui.calcTextSize(hintSize, hint);
        drawList.addText(boardX + (600.0f - hintSize.x) / 2.0f, boardY + 200.0f + 10.0f, -1716868438, hint);
    }

    private void renderMenuOverlay(ImDrawList drawList, float boardX, float boardY) {
        drawList.addRectFilled(boardX, boardY, boardX + 600.0f, boardY + 400.0f, -267382760, 8.0f);
        String title = "\uf45d  PONG";
        ImVec2 titleSize = new ImVec2();
        ImGui.calcTextSize(titleSize, title);
        float titleX = boardX + (600.0f - titleSize.x) / 2.0f;
        float titleY = boardY + 40.0f;
        drawList.addText(titleX - 1.0f, titleY - 1.0f, 1079037776, title);
        drawList.addText(titleX + 1.0f, titleY + 1.0f, 1079037776, title);
        drawList.addText(titleX, titleY, -1, title);
        String subtitle = "Klasyczna gra przeciwko AI";
        ImVec2 subSize = new ImVec2();
        ImGui.calcTextSize(subSize, subtitle);
        drawList.addText(boardX + (600.0f - subSize.x) / 2.0f, boardY + 75.0f, -1716868438, subtitle);
        float menuY = boardY + 120.0f;
        float menuWidth = 280.0f;
        float menuX = boardX + (600.0f - menuWidth) / 2.0f;
        float menuItemHeight = 42.0f;
        this.renderMenuItemFullscreen(drawList, menuX, menuY, menuWidth, menuItemHeight - 5.0f, "\uf04b  ROZPOCZNIJ GRE", this.menuSelectedOption == 0, -11481264);
        String diffText = "\uf012  Poziom: " + this.difficulty.name;
        this.renderMenuItemFullscreen(drawList, menuX, menuY += menuItemHeight, menuWidth, menuItemHeight - 5.0f, diffText, this.menuSelectedOption == 1, -11497233);
        if (this.menuSelectedOption == 1) {
            drawList.addText(menuX - 25.0f, menuY + 10.0f, -855638017, "\uf053");
            drawList.addText(menuX + menuWidth + 10.0f, menuY + 10.0f, -855638017, "\uf054");
        }
        String soundIcon = this.soundEnabled ? "\uf028" : "\uf6a9";
        String soundText = soundIcon + "  Dzwiek: " + (this.soundEnabled ? "WLACZONY" : "WYLACZONY");
        int soundColor = this.soundEnabled ? -11481264 : -9408342;
        this.renderMenuItemFullscreen(drawList, menuX, menuY += menuItemHeight, menuWidth, menuItemHeight - 5.0f, soundText, this.menuSelectedOption == 2, soundColor);
        menuY += menuItemHeight;
        float infoY = boardY + 400.0f - 80.0f;
        String[] controls = new String[]{"\uf062 \uf063  Nawiguj", "\uf060 \uf061  Zmien opcje", "ENTER/SPACJA  Wybierz"};
        float infoX = boardX + 300.0f;
        for (String line : controls) {
            ImVec2 lineSize = new ImVec2();
            ImGui.calcTextSize(lineSize, line);
            drawList.addText(infoX - lineSize.x / 2.0f, infoY, -2136298838, line);
            infoY += 18.0f;
        }
    }

    private void renderMenuItemFullscreen(ImDrawList drawList, float x, float y, float w, float h, String text, boolean selected, int accentColor) {
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

    public static enum Difficulty {
        EASY(200.0f, 60.0f, 25.0f, 0.25f, 1.0f, "Latwy"),
        NORMAL(300.0f, 40.0f, 15.0f, 0.15f, 1.0f, "Normalny"),
        HARD(400.0f, 20.0f, 8.0f, 0.1f, 0.9f, "Trudny"),
        INSANE(500.0f, 5.0f, 3.0f, 0.05f, 0.7f, "Szalony");

        final float speed;
        final float error;
        final float deadZone;
        final float updateInterval;
        final float playerPaddleScale;
        final String name;

        private Difficulty(float speed, float error, float deadZone, float updateInterval, float paddleScale, String name) {
            this.speed = speed;
            this.error = error;
            this.deadZone = deadZone;
            this.updateInterval = updateInterval;
            this.playerPaddleScale = paddleScale;
            this.name = name;
        }
    }
}

