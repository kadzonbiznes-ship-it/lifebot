/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map;

import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Generated;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.pbrands.map.Map;
import org.pbrands.map.Marker;
import org.pbrands.map.MarkerType;
import org.pbrands.map.TextureLoader;
import org.pbrands.map.TileCache;
import org.pbrands.map.TileDownloader;
import org.pbrands.map.panel.MarkerSearchListener;
import org.pbrands.map.util.WindowIconUtil;
import org.pbrands.model.MapDefinition;
import org.pbrands.netty.handler.MapClientHandler;
import org.pbrands.util.FontAwesomeIcons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapRenderer
implements MarkerSearchListener {
    private static final Logger logger = LoggerFactory.getLogger(MapRenderer.class);
    private int windowWidth = 1280;
    private int windowHeight = 720;
    private long window;
    private static final int TILE_SIZE = 2048;
    private static final int TILE_COUNT_X = 10;
    private static final int TILE_COUNT_Y = 10;
    private int[][] tileTexIds = new int[10][10];
    private TileDownloader tileDownloader;
    private TileCache tileCache;
    private volatile int tilesDownloaded = 0;
    private volatile int tilesToDownload = 0;
    private volatile boolean tilesDownloading = false;
    private volatile String downloadStatus = "";
    private int countyOverlayTexId;
    private int districtsOverlayTexId;
    private int namesOverlayTexId;
    private final int mapWidth = 20480;
    private final int mapHeight = 20480;
    private int arrowDownTexId;
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float zoom = 1.0f;
    private boolean dragging = false;
    private double lastMouseX;
    private double lastMouseY;
    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();
    private String glslVersion;
    private final Map map;
    private int displayedMarkerCount;
    private boolean showNewMarkerPopup = false;
    private float newMarkerMapX;
    private float newMarkerMapY;
    private float newMarkerScreenX;
    private float newMarkerScreenY;
    private final ImInt newMarkerTypeIndex = new ImInt(0);
    private final ImString newMarkerDescriptionBuffer = new ImString(256);
    private final ImBoolean newMarkerUndergroundBool = new ImBoolean(false);
    private final ImInt selectedPresetIndex = new ImInt(-1);
    private boolean mapSelected = false;
    private boolean resourcesLoaded = false;
    private boolean tilesLoaded = false;

    public MapRenderer(Map map) {
        this.map = map;
        this.tileCache = new TileCache();
        map.getMapPanel().setMarkerSearchListener(this);
    }

    public void run() {
        this.init();
        this.loop();
        this.cleanup();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        GLFW.glfwWindowHint(135181, 4);
        this.glslVersion = "#version 130";
        GLFW.glfwWindowHint(139266, 3);
        GLFW.glfwWindowHint(139267, 0);
        GLFW.glfwWindowHint(131076, 0);
        GLFW.glfwWindowHint(131075, 1);
        this.window = GLFW.glfwCreateWindow(this.windowWidth, this.windowHeight, "\u0141adownie mapy...", 0L, 0L);
        if (this.window == 0L) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        WindowIconUtil.setWindowIcon(this.window, "/images/favicon.png");
        GLFW.glfwSetWindowSizeCallback(this.window, (win, w, h) -> {
            this.windowWidth = w;
            this.windowHeight = h;
            GL11.glViewport(0, 0, this.windowWidth, this.windowHeight);
            GL11.glMatrixMode(5889);
            GL11.glLoadIdentity();
            GL11.glOrtho(0.0, this.windowWidth, this.windowHeight, 0.0, -1.0, 1.0);
            GL11.glMatrixMode(5888);
        });
        GLFW.glfwSetCursorPosCallback(this.window, (win, xpos, ypos) -> {
            if (this.dragging) {
                float dx = (float)(xpos - this.lastMouseX);
                float dy = (float)(ypos - this.lastMouseY);
                this.offsetX += dx;
                this.offsetY += dy;
            }
            this.lastMouseX = xpos;
            this.lastMouseY = ypos;
        });
        GLFW.glfwSetMouseButtonCallback(this.window, (win, button, action, mods) -> {
            double[] mx = new double[1];
            double[] my = new double[1];
            GLFW.glfwGetCursorPos(this.window, mx, my);
            float screenX = (float)mx[0];
            float screenY = (float)my[0];
            if (ImGui.getIO().getWantCaptureMouse()) {
                return;
            }
            if (button == 0) {
                if (action == 1) {
                    this.dragging = true;
                    Marker newlySelected = this.findMarkerAt(screenX, screenY);
                    this.map.setSelectedMarker(newlySelected);
                } else if (action == 0) {
                    this.dragging = false;
                }
            }
            if (button == 1 && action == 1) {
                float coverScale = this.computeCoverScale();
                float finalScale = coverScale * this.zoom;
                this.newMarkerMapX = (screenX - this.offsetX) / finalScale;
                this.newMarkerMapY = (screenY - this.offsetY) / finalScale;
                this.newMarkerScreenX = screenX;
                this.newMarkerScreenY = screenY;
                this.newMarkerTypeIndex.set(0);
                this.newMarkerDescriptionBuffer.set("");
                this.newMarkerUndergroundBool.set(false);
                this.showNewMarkerPopup = true;
            }
        });
        GLFW.glfwSetScrollCallback(this.window, (win, dx, dy) -> {
            double[] mx = new double[1];
            double[] my = new double[1];
            GLFW.glfwGetCursorPos(this.window, mx, my);
            float cx = (float)mx[0];
            float cy = (float)my[0];
            float factor = 1.0f + (float)dy * 0.1f;
            float newZoom = this.zoom * factor;
            float minZoom = 0.5f;
            float maxZoom = 17.5f;
            if (newZoom < 0.5f) {
                newZoom = 0.5f;
            }
            if (newZoom > 17.5f) {
                newZoom = 17.5f;
            }
            float actualFactor = newZoom / this.zoom;
            this.offsetX += (1.0f - actualFactor) * (cx - this.offsetX);
            this.offsetY += (1.0f - actualFactor) * (cy - this.offsetY);
            this.zoom = newZoom;
        });
        GLFW.glfwMakeContextCurrent(this.window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(this.window);
        GL.createCapabilities();
        GL11.glViewport(0, 0, this.windowWidth, this.windowHeight);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, this.windowWidth, this.windowHeight, 0.0, -1.0, 1.0);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glEnable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.initImGui();
    }

    private Marker findMarkerAt(float screenX, float screenY) {
        List<Marker> markers = this.map.getVisibleMarkers();
        for (int i = markers.size() - 1; i >= 0; --i) {
            float sy;
            Marker m = markers.get(i);
            float coverScale = this.computeCoverScale();
            float finalScale = coverScale * this.zoom;
            float sx = this.offsetX + finalScale * m.getX();
            if (!(this.distance(screenX, screenY, sx, sy = this.offsetY + finalScale * m.getY()) < m.getSize() * 0.5f)) continue;
            return m;
        }
        return null;
    }

    private void initImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.getFonts().setFreeTypeRenderer(true);
        io.setIniFilename(null);
        io.addConfigFlags(1);
        io.addConfigFlags(64);
        io.setConfigViewportsNoTaskBarIcon(true);
        ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesDefault());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesCyrillic());
        rangesBuilder.addRanges(io.getFonts().getGlyphRangesJapanese());
        rangesBuilder.addRanges(new short[]{32, 255, 256, 383, 0});
        rangesBuilder.addRanges(new short[]{32, 255, 256, 383, 10004, 10004, 10060, 10060, 0});
        rangesBuilder.addRanges(FontAwesomeIcons._IconRange);
        ImFontConfig fontConfig = new ImFontConfig();
        fontConfig.setMergeMode(true);
        short[] glyphRanges = rangesBuilder.buildRanges();
        io.getFonts().addFontFromMemoryTTF(TextureLoader.loadFromResources("/Nunito-Regular.ttf"), 20.0f, glyphRanges);
        io.getFonts().addFontFromMemoryTTF(TextureLoader.loadFromResources("/NotoEmoji-Regular.ttf"), 14.0f, fontConfig, glyphRanges);
        io.getFonts().addFontFromMemoryTTF(TextureLoader.loadFromResources("/fa-regular-400.ttf"), 14.0f, fontConfig, glyphRanges);
        io.getFonts().addFontFromMemoryTTF(TextureLoader.loadFromResources("/fa-solid-900.ttf"), 14.0f, fontConfig, glyphRanges);
        io.getFonts().build();
        fontConfig.destroy();
        ImGui.styleColorsDark();
        this.imGuiGlfw.init(this.window, true);
        this.imGuiGl3.init(this.glslVersion);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void renderMapSelector() {
        ImGui.pushStyleVar(3, 10.0f);
        int sizeX = 200;
        int sizeY = 85;
        ImGui.setNextWindowPos((float)this.windowWidth / 2.0f - (float)sizeX / 2.0f, (float)this.windowHeight / 2.0f - (float)sizeY / 2.0f);
        ImGui.setNextWindowSize(sizeX, (float)sizeY);
        if (ImGui.begin("Map Selector", 163)) {
            String[] presets;
            List<MapDefinition> availableMaps;
            List<MapDefinition> list = availableMaps = this.map.getAvailableMaps();
            synchronized (list) {
                presets = new String[availableMaps.size()];
                for (int i = 0; i < availableMaps.size(); ++i) {
                    presets[i] = availableMaps.get(i).getDisplayName();
                }
            }
            ImGui.spacing();
            ImGui.text("Wyb\u00f3r mapy:");
            ImGui.spacing();
            if (ImGui.combo(" ", this.selectedPresetIndex, presets)) {
                MapDefinition selectedMap;
                this.mapSelected = true;
                List<MapDefinition> list2 = availableMaps;
                synchronized (list2) {
                    selectedMap = availableMaps.get(this.selectedPresetIndex.get());
                }
                this.map.setSelectedMap(selectedMap);
                this.map.getNettyClient().sendMapSelection(selectedMap);
                this.map.setMaxCollectibles(selectedMap.getMaxCollectibles());
                GLFW.glfwSetWindowTitle(this.window, "LifeBot - Mapa [" + selectedMap.getDisplayName() + "]");
            }
        }
        ImGui.popStyleVar();
        ImGui.end();
    }

    private void drawMapTiles(float finalScale) {
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.offsetX, this.offsetY, 0.0f);
        GL11.glScalef(finalScale, finalScale, 1.0f);
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                int texId = this.tileTexIds[i][j];
                if (texId == 0) continue;
                GL11.glBindTexture(3553, texId);
                float tileX = i * 2048;
                float tileY = j * 2048;
                GL11.glBegin(7);
                GL11.glTexCoord2f(0.0f, 0.0f);
                GL11.glVertex2f(tileX, tileY);
                GL11.glTexCoord2f(1.0f, 0.0f);
                GL11.glVertex2f(tileX + 2048.0f, tileY);
                GL11.glTexCoord2f(1.0f, 1.0f);
                GL11.glVertex2f(tileX + 2048.0f, tileY + 2048.0f);
                GL11.glTexCoord2f(0.0f, 1.0f);
                GL11.glVertex2f(tileX, tileY + 2048.0f);
                GL11.glEnd();
            }
        }
        GL11.glPopMatrix();
    }

    private void startTileLoading() {
        int missingTiles = 0;
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                if (this.tileCache.hasTile(i, j)) continue;
                ++missingTiles;
            }
        }
        if (missingTiles == 0) {
            this.loadTilesFromCache();
            this.tilesLoaded = true;
            return;
        }
        this.tilesToDownload = missingTiles;
        this.tilesDownloaded = 0;
        this.tilesDownloading = true;
        this.downloadStatus = "Pobieranie mapy...";
        if (this.map.getNettyClient() != null && this.map.getNettyClient().getChannel() != null) {
            this.tileDownloader = new TileDownloader(this.map.getNettyClient().getChannel());
            this.tileDownloader.setProgressCallback((downloaded, total) -> {
                this.tilesDownloaded = downloaded;
                this.tilesToDownload = total;
            });
            MapClientHandler.setTileDownloader(this.tileDownloader);
            CompletableFuture.runAsync(() -> {
                try {
                    this.downloadAllTiles();
                }
                catch (Exception e) {
                    logger.error("Failed to download tiles", e);
                    this.downloadStatus = "B\u0142\u0105d pobierania: " + e.getMessage();
                }
            });
        } else {
            logger.warn("NettyClient not available, loading tiles from resources as fallback");
            this.loadTilesFromResources();
            this.tilesLoaded = true;
        }
    }

    private void downloadAllTiles() {
        AtomicInteger downloaded = new AtomicInteger(0);
        int total = 100;
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                int x = i;
                int y = j;
                if (this.tileCache.hasTile(x, y)) {
                    int count;
                    this.tilesDownloaded = count = downloaded.incrementAndGet();
                    this.downloadStatus = String.format("\u0141adowanie z cache... %d/%d", count, total);
                    continue;
                }
                try {
                    int count;
                    this.downloadStatus = String.format("Pobieranie tile_%d_%d... %d/%d", x, y, downloaded.get(), total);
                    byte[] data = this.tileDownloader.getTile(x, y);
                    this.tilesDownloaded = count = downloaded.incrementAndGet();
                    this.downloadStatus = String.format("Pobrano %d/%d", count, total);
                    continue;
                }
                catch (Exception e) {
                    logger.error("Failed to download tile ({},{})", x, y, e);
                }
            }
        }
        this.downloadStatus = "\u0141adowanie tekstur...";
        this.tilesDownloading = false;
        GLFW.glfwPostEmptyEvent();
    }

    private void loadTilesFromCache() {
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                File tileFile = this.tileCache.getTileFile(i, j);
                if (tileFile.exists()) {
                    this.tileTexIds[i][j] = TextureLoader.loadTextureFromFile(tileFile.toPath());
                    continue;
                }
                String path = String.format("/images/maps/tile_%d_%d.jpg", i, j);
                this.tileTexIds[i][j] = TextureLoader.loadTexture(path);
            }
        }
    }

    private void loadTilesFromResources() {
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                String path = String.format("/images/maps/tile_%d_%d.jpg", i, j);
                this.tileTexIds[i][j] = TextureLoader.loadTexture(path);
            }
        }
    }

    private void renderTileDownloadProgress() {
        if (!this.tilesDownloading && this.tilesDownloaded >= this.tilesToDownload && this.tilesToDownload > 0) {
            this.loadTilesFromCache();
            this.tilesLoaded = true;
            return;
        }
        this.imGuiGl3.newFrame();
        this.imGuiGlfw.newFrame();
        ImGui.newFrame();
        ImGui.setNextWindowPos((float)this.windowWidth / 2.0f, (float)this.windowHeight / 2.0f, 1, 0.5f, 0.5f);
        ImGui.setNextWindowSize(400.0f, 150.0f);
        int flags = 39;
        if (ImGui.begin("##download_progress", flags)) {
            ImGui.text("Pobieranie mapy...");
            ImGui.spacing();
            float progress = this.tilesToDownload > 0 ? (float)this.tilesDownloaded / (float)this.tilesToDownload : 0.0f;
            ImGui.progressBar(progress, 380.0f, 25.0f, String.format("%.0f%%", Float.valueOf(progress * 100.0f)));
            ImGui.spacing();
            ImGui.textWrapped(this.downloadStatus);
            ImGui.spacing();
            ImGui.textDisabled(String.format("%d / %d plik\u00f3w", this.tilesDownloaded, this.tilesToDownload));
        }
        ImGui.end();
        ImGui.render();
        this.imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void loop() {
        GLFWErrorCallback.createPrint(System.err).set();
        while (!GLFW.glfwWindowShouldClose(this.window)) {
            GL11.glClearColor(0.1f, 0.09f, 0.1f, 1.0f);
            GL11.glClear(16384);
            if (!this.resourcesLoaded) {
                GLFW.glfwSwapBuffers(this.window);
                GLFW.glfwPollEvents();
                int[] maxSize = new int[1];
                GL11.glGetIntegerv(3379, maxSize);
                System.out.println("Max Texture Size: " + maxSize[0]);
                this.arrowDownTexId = TextureLoader.loadTexture("/images/markers/arrow_down.png");
                this.countyOverlayTexId = TextureLoader.loadTexture("/images/overlay/map_county.png");
                this.districtsOverlayTexId = TextureLoader.loadTexture("/images/overlay/map_districts.png");
                this.namesOverlayTexId = TextureLoader.loadTexture("/images/overlay/map_names.png");
                this.startTileLoading();
                GLFW.glfwSetWindowTitle(this.window, "Wybieranie mapy...");
                this.resourcesLoaded = true;
                continue;
            }
            if (!this.tilesLoaded) {
                this.renderTileDownloadProgress();
                GLFW.glfwSwapBuffers(this.window);
                GLFW.glfwPollEvents();
                continue;
            }
            this.imGuiGl3.newFrame();
            this.imGuiGlfw.newFrame();
            ImGui.newFrame();
            if (!this.mapSelected) {
                this.renderMapSelector();
                ImGui.render();
                this.imGuiGl3.renderDrawData(ImGui.getDrawData());
                if (ImGui.getIO().hasConfigFlags(1024)) {
                    long backupWindowPtr = GLFW.glfwGetCurrentContext();
                    ImGui.updatePlatformWindows();
                    ImGui.renderPlatformWindowsDefault();
                    GLFW.glfwMakeContextCurrent(backupWindowPtr);
                }
                GLFW.glfwSwapBuffers(this.window);
                GLFW.glfwPollEvents();
                continue;
            }
            this.map.getAvailableMarkerTypes().stream().filter(markerType -> markerType.getTextureId() == 0).forEach(type -> type.setTextureId(TextureLoader.loadTexture(type.getResourcesImagePath())));
            float coverScale = this.computeCoverScale();
            float finalScale = coverScale * this.zoom;
            this.drawMapTiles(finalScale);
            this.drawMapTiles(finalScale);
            this.drawOverlays(finalScale);
            double[] mx = new double[1];
            double[] my = new double[1];
            GLFW.glfwGetCursorPos(this.window, mx, my);
            float mouseX = (float)mx[0];
            float mouseY = (float)my[0];
            this.displayedMarkerCount = 0;
            for (Marker marker : this.map.getVisibleMarkers()) {
                ++this.displayedMarkerCount;
                float sx = this.offsetX + finalScale * marker.getX();
                float sy = this.offsetY + finalScale * marker.getY();
                float dist = this.distance(mouseX, mouseY, sx, sy);
                double scaleFactor = Math.min(Math.log10(this.zoom) + 0.95, 1.25);
                if (dist < marker.getSize() * 0.5f) {
                    marker.setTargetSize((float)(40.0 * scaleFactor));
                } else {
                    marker.setTargetSize((float)(32.0 * scaleFactor));
                }
                marker.setSize(marker.getSize() + 0.2f * (marker.getTargetSize() - marker.getSize()));
                if (marker.isCollected()) {
                    GL11.glColor4f(0.5f, 0.5f, 0.5f, 0.6f);
                } else {
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                }
                GL11.glBindTexture(3553, marker.getType().getTextureId());
                float half = marker.getSize() * 0.5f;
                GL11.glBegin(7);
                GL11.glTexCoord2f(0.0f, 0.0f);
                GL11.glVertex2f(sx - half, sy - half);
                GL11.glTexCoord2f(1.0f, 0.0f);
                GL11.glVertex2f(sx + half, sy - half);
                GL11.glTexCoord2f(1.0f, 1.0f);
                GL11.glVertex2f(sx + half, sy + half);
                GL11.glTexCoord2f(0.0f, 1.0f);
                GL11.glVertex2f(sx - half, sy + half);
                GL11.glEnd();
                if (!marker.isUnderground()) continue;
                float arrowSize = marker.getSize() * 0.6f;
                GL11.glBindTexture(3553, this.arrowDownTexId);
                float offsetVal = marker.getSize() * 0.8f;
                float offsetTop = marker.getSize() * 0.65f;
                float arrowLeft = sx + offsetVal - arrowSize;
                float arrowTop = sy - offsetTop;
                float arrowRight = sx + offsetVal;
                float arrowBottom = arrowTop + arrowSize;
                GL11.glBegin(7);
                GL11.glTexCoord2f(0.0f, 0.0f);
                GL11.glVertex2f(arrowLeft, arrowTop);
                GL11.glTexCoord2f(1.0f, 0.0f);
                GL11.glVertex2f(arrowRight, arrowTop);
                GL11.glTexCoord2f(1.0f, 1.0f);
                GL11.glVertex2f(arrowRight, arrowBottom);
                GL11.glTexCoord2f(0.0f, 1.0f);
                GL11.glVertex2f(arrowLeft, arrowBottom);
                GL11.glEnd();
            }
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.map.getMapPanel().render();
            this.renderSelectedMarker(finalScale);
            if (this.showNewMarkerPopup) {
                ImGui.pushStyleVar(3, 10.0f);
                int sizeX = 270;
                int sizeY = 230;
                ImGui.setNextWindowPos(this.newMarkerScreenX - (float)sizeX / 2.0f, this.newMarkerScreenY - (float)sizeY / 2.0f);
                ImGui.setNextWindowSize(sizeX, (float)sizeY);
                ImGui.openPopup("Nowy Marker");
                if (ImGui.beginPopupModal("Nowy Marker", null, 69)) {
                    ImGui.popStyleVar();
                    ImGui.pushStyleVar(2, 20.0f, 20.0f);
                    ImGui.beginChild("TextWithPadding", new ImVec2(0.0f, 30.0f), false, 0);
                    ImGui.text("Nowy Marker");
                    ImGui.endChild();
                    ImGui.popStyleVar();
                    List<MarkerType> markerTypes = this.map.getAvailableMarkerTypes();
                    String[] markerTypeNames = new String[markerTypes.size()];
                    for (int i = 0; i < markerTypes.size(); ++i) {
                        markerTypeNames[i] = markerTypes.get(i).getName();
                    }
                    ImGui.setNextItemWidth(150.0f);
                    ImGui.pushStyleVar(12, 8.0f);
                    ImGui.combo("Typ markera", this.newMarkerTypeIndex, markerTypeNames);
                    ImGui.popStyleVar();
                    ImGui.spacing();
                    ImGui.pushStyleVar(12, 4.0f);
                    ImGui.inputTextMultiline("Opis", this.newMarkerDescriptionBuffer, 200.0f, 70.0f, 80);
                    ImGui.popStyleVar();
                    ImGui.spacing();
                    ImGui.pushStyleVar(12, 4.0f);
                    ImGui.checkbox("Pod powierzchni\u0105", this.newMarkerUndergroundBool);
                    ImGui.popStyleVar();
                    ImGui.spacing();
                    ImGui.spacing();
                    ImGui.pushStyleVar(11, 18.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    if (ImGui.button("Dodaj") && !markerTypes.isEmpty()) {
                        MarkerType markerType2 = markerTypes.get(this.newMarkerTypeIndex.get());
                        int id = this.map.getMarkers().size() + 1;
                        Marker m = new Marker(id, true, markerType2, this.newMarkerMapX, this.newMarkerMapY, this.newMarkerDescriptionBuffer.get(), this.newMarkerUndergroundBool.get());
                        this.map.getNettyClient().sendMarkerCreate(m);
                        this.showNewMarkerPopup = false;
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.popStyleVar(2);
                    ImGui.sameLine();
                    ImGui.pushStyleVar(11, 18.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    if (ImGui.button("Anuluj")) {
                        this.showNewMarkerPopup = false;
                        ImGui.closeCurrentPopup();
                    }
                    ImGui.popStyleVar(2);
                    ImGui.endPopup();
                }
            }
            ImGui.setNextWindowSize(80.0f, 0.0f);
            ImGui.setNextWindowPos(this.windowWidth - 80, 0.0f);
            ImGui.pushStyleVar(3, 10.0f);
            ImGui.pushStyleColor(5, 0.0f, 0.0f, 0.0f, 0.0f);
            ImGui.pushStyleColor(2, 0.1f, 0.1f, 0.1f, 0.55f);
            ImGui.begin("FPS", 67);
            ImGui.text(String.format("FPS: %d", (int)ImGui.getIO().getFramerate()));
            ImGui.popStyleColor(2);
            ImGui.popStyleVar();
            ImGui.end();
            ImGui.render();
            this.imGuiGl3.renderDrawData(ImGui.getDrawData());
            if (ImGui.getIO().hasConfigFlags(1024)) {
                long backupWindowPtr = GLFW.glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                GLFW.glfwMakeContextCurrent(backupWindowPtr);
            }
            GLFW.glfwSwapBuffers(this.window);
            GLFW.glfwPollEvents();
        }
    }

    private void renderSelectedMarker(float finalScale) {
        Marker selectedMarker = this.map.getSelectedMarker();
        if (selectedMarker != null) {
            float sx = this.offsetX + finalScale * selectedMarker.getX();
            float sy = this.offsetY + finalScale * selectedMarker.getY();
            float popupX = sx + 20.0f;
            float popupY = sy - 60.0f;
            ImGui.pushStyleVar(3, 8.0f);
            ImGui.pushStyleVar(2, 10.0f, 10.0f);
            ImGui.pushStyleColor(2, 0.1f, 0.1f, 0.1f, 0.95f);
            ImGui.pushStyleColor(5, 1.0f, 1.0f, 1.0f, 0.3f);
            ImGui.setNextWindowPos(popupX, popupY, 1);
            ImGui.setNextWindowSize(0.0f, 0.0f, 8);
            if (ImGui.begin("Jajko", 99)) {
                if (selectedMarker.isCreator()) {
                    ImGui.pushStyleColor(0, 0.3f, 0.3f, 0.3f, 1.0f);
                    ImGui.text("Twoje");
                    ImGui.popStyleColor();
                    ImGui.spacing();
                }
                ImGui.text(selectedMarker.getType().getName() + " (#" + selectedMarker.getId() + ")");
                ImGui.spacing();
                if (selectedMarker.getDescription() != null && !selectedMarker.getDescription().isBlank()) {
                    ImGui.text("Opis: " + selectedMarker.getDescription());
                }
                if (selectedMarker.isUnderground()) {
                    ImGui.pushStyleColor(0, 0.3f, 0.3f, 0.3f, 1.0f);
                    ImGui.text("Pod powierzchni\u0105");
                    ImGui.popStyleColor();
                }
                ImGui.spacing();
                boolean collected = selectedMarker.isCollected();
                ImGui.pushStyleVar(12, 10.0f);
                if (ImGui.checkbox("Zebrane", collected)) {
                    selectedMarker.setCollected(!selectedMarker.isCollected());
                    this.map.getNettyClient().sendMarkerInfo(selectedMarker);
                }
                ImGui.popStyleVar();
                ImGui.spacing();
                ImGui.separator();
                ImGui.spacing();
                boolean isLike = selectedMarker.isLiked();
                boolean isDislike = selectedMarker.isDisliked();
                String likeLabel = "\u2714 [" + selectedMarker.getLikeCount() + "]";
                String dislikeLabel = "\u274c [" + selectedMarker.getDislikeCount() + "]";
                if (isLike) {
                    ImGui.pushStyleVar(11, 18.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    ImGui.pushStyleColor(21, 0.2f, 0.7f, 0.2f, 1.0f);
                    ImGui.pushStyleColor(22, 0.3f, 0.8f, 0.3f, 1.0f);
                    ImGui.pushStyleColor(23, 0.1f, 0.6f, 0.1f, 1.0f);
                    if (ImGui.button(likeLabel) && selectedMarker.like()) {
                        this.map.getNettyClient().sendMarkerInfo(selectedMarker);
                    }
                    ImGui.popStyleColor(3);
                    ImGui.popStyleVar();
                    ImGui.popStyleVar();
                } else {
                    ImGui.pushStyleVar(11, 18.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    if (ImGui.button(likeLabel) && selectedMarker.like()) {
                        this.map.getNettyClient().sendMarkerInfo(selectedMarker);
                    }
                    ImGui.popStyleVar();
                    ImGui.popStyleVar();
                }
                ImGui.sameLine();
                if (isDislike) {
                    ImGui.pushStyleVar(11, 18.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    ImGui.pushStyleColor(21, 0.8f, 0.2f, 0.2f, 1.0f);
                    ImGui.pushStyleColor(22, 0.9f, 0.3f, 0.3f, 1.0f);
                    ImGui.pushStyleColor(23, 0.7f, 0.1f, 0.1f, 1.0f);
                    if (ImGui.button(dislikeLabel) && selectedMarker.dislike()) {
                        this.map.getNettyClient().sendMarkerInfo(selectedMarker);
                    }
                    ImGui.popStyleColor(3);
                    ImGui.popStyleVar();
                    ImGui.popStyleVar();
                } else {
                    ImGui.pushStyleVar(11, 18.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    if (ImGui.button(dislikeLabel) && selectedMarker.dislike()) {
                        this.map.getNettyClient().sendMarkerInfo(selectedMarker);
                    }
                    ImGui.popStyleVar();
                    ImGui.popStyleVar();
                }
                if (selectedMarker.isCreator() && selectedMarker.getLikeCount() == 0) {
                    ImGui.sameLine();
                    ImGui.pushStyleVar(11, 26.0f, 4.0f);
                    ImGui.pushStyleVar(12, 6.0f);
                    if (ImGui.button("Usu\u0144")) {
                        this.map.getNettyClient().sendMarkerDelete(selectedMarker.getId());
                        this.map.setSelectedMarker(null);
                    }
                    ImGui.popStyleVar();
                    ImGui.popStyleVar();
                }
            }
            ImGui.end();
            ImGui.popStyleColor(1);
            ImGui.popStyleColor(1);
            ImGui.popStyleVar(2);
        }
    }

    private void drawOverlays(float finalScale) {
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.offsetX, this.offsetY, 0.0f);
        GL11.glScalef(finalScale, finalScale, 1.0f);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.5f);
        if (this.map.getMapPanel().isOverlayDistrictsEnabled()) {
            this.drawOverlayQuad(this.districtsOverlayTexId);
        }
        if (this.map.getMapPanel().isOverlayRegionsEnabled()) {
            this.drawOverlayQuad(this.countyOverlayTexId);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 0.75f);
        if (this.map.getMapPanel().isOverlayNamesEnabled()) {
            this.drawOverlayQuad(this.namesOverlayTexId);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();
    }

    private void drawOverlayQuad(int texId) {
        GL11.glBindTexture(3553, texId);
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(0.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(20480.0f, 0.0f);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(20480.0f, 20480.0f);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(0.0f, 20480.0f);
        GL11.glEnd();
    }

    private float computeCoverScale() {
        float scaleX = (float)this.windowWidth / 20480.0f;
        float scaleY = (float)this.windowHeight / 20480.0f;
        return Math.max(scaleX, scaleY);
    }

    private float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public void onMarkerSearch(Integer search) {
        for (Marker marker : this.map.getMarkers()) {
            if (marker.getId() != search.intValue()) continue;
            this.map.setSelectedMarker(marker);
            float coverScale = this.computeCoverScale();
            float finalScale = coverScale * this.zoom;
            this.offsetX = (float)this.windowWidth / 2.0f - finalScale * marker.getX();
            this.offsetY = (float)this.windowHeight / 2.0f - finalScale * marker.getY();
            break;
        }
    }

    private void cleanup() {
        this.imGuiGlfw.shutdown();
        this.imGuiGl3.shutdown();
        for (MarkerType availableMarkerType : this.map.getAvailableMarkerTypes()) {
            GL11.glDeleteTextures(availableMarkerType.getTextureId());
        }
        for (int i = 0; i < 10; ++i) {
            for (int j = 0; j < 10; ++j) {
                GL11.glDeleteTextures(this.tileTexIds[i][j]);
            }
        }
        Callbacks.glfwFreeCallbacks(this.window);
        GLFW.glfwDestroyWindow(this.window);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    @Generated
    public int getDisplayedMarkerCount() {
        return this.displayedMarkerCount;
    }
}

