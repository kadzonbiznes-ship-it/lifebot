/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map.panel;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Generated;
import org.pbrands.map.Map;
import org.pbrands.map.Marker;
import org.pbrands.map.MarkerType;
import org.pbrands.map.panel.MarkerSearchListener;
import org.pbrands.map.panel.Opinion;
import org.pbrands.map.panel.Status;

public class MapPanel {
    private final Map map;
    private final HashMap<MarkerType, ImBoolean> selectedMarkers = new HashMap();
    private final ImInt statusSelector = new ImInt(0);
    private final ImInt opinionSelector = new ImInt(0);
    private final ImBoolean overlayNames = new ImBoolean(true);
    private final ImBoolean overlayRegions = new ImBoolean(false);
    private final ImBoolean overlayDistricts = new ImBoolean(false);
    private final ImString searchMarkerId = new ImString(4);
    private MarkerSearchListener markerSearchListener;

    public boolean isOverlayNamesEnabled() {
        return this.overlayNames.get();
    }

    public boolean isOverlayRegionsEnabled() {
        return this.overlayRegions.get();
    }

    public boolean isOverlayDistrictsEnabled() {
        return this.overlayDistricts.get();
    }

    public Status getStatus() {
        return Status.getStatus(this.statusSelector.get());
    }

    public Opinion getOpinion() {
        return Opinion.getOpinion(this.opinionSelector.get());
    }

    public List<MarkerType> getSelectedMarkerTypes() {
        return this.selectedMarkers.entrySet().stream().filter(entry -> ((ImBoolean)entry.getValue()).get()).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public MapPanel(Map map) {
        this.map = map;
    }

    public void render() {
        ImGui.setNextWindowPos(10.0f, 10.0f);
        ImGui.pushStyleVar(3, 10.0f);
        ImGui.pushStyleVar(2, 20.0f, 20.0f);
        ImGui.pushStyleColor(2, 0.1f, 0.1f, 0.1f, 0.95f);
        ImGui.pushStyleColor(5, 0.0f, 0.0f, 0.0f, 0.0f);
        ImGui.begin("Panel boczny", 69);
        ImGui.popStyleVar();
        ImGui.popStyleVar();
        ImGui.popStyleColor();
        ImGui.popStyleColor();
        ImGui.text(String.format("Zebrane: %d/%d", this.map.getMarkers().stream().filter(marker -> marker.getType().getName().toLowerCase().contains("event")).filter(Marker::isLiked).count(), this.map.getMaxCollectibles()));
        ImGui.text("Markery: " + this.map.getMapRenderer().getDisplayedMarkerCount());
        ImGui.spacing();
        ImGui.text("Wyszukaj Marker po ID:");
        ImGui.setNextItemWidth(150.0f);
        ImGui.pushStyleVar(12, 4.0f);
        if (ImGui.inputText("##MarkerSearch", this.searchMarkerId, this.searchMarkerId.getLength())) {
            String typedId = this.searchMarkerId.get();
            if (this.markerSearchListener != null && typedId != null && !typedId.isBlank()) {
                this.markerSearchListener.onMarkerSearch(Integer.parseInt(typedId));
            }
        }
        ImGui.popStyleVar();
        ImGui.separator();
        ImGui.text("Typy Marker\u00f3w:");
        for (MarkerType markerType : this.map.getAvailableMarkerTypes()) {
            String name = markerType.getName();
            ImGui.pushStyleVar(12, 8.0f);
            this.selectedMarkers.putIfAbsent(markerType, new ImBoolean(true));
            ImGui.checkbox(name, this.selectedMarkers.get(markerType));
            ImGui.popStyleVar();
        }
        ImGui.separator();
        ImGui.text("Status:");
        if (ImGui.radioButton("Wszystkie##status", this.statusSelector.get() == 0)) {
            this.statusSelector.set(0);
        }
        if (ImGui.radioButton("Zebrane##status", this.statusSelector.get() == 1)) {
            this.statusSelector.set(1);
        }
        if (ImGui.radioButton("Niezebrane##status", this.statusSelector.get() == 2)) {
            this.statusSelector.set(2);
        }
        ImGui.separator();
        ImGui.text("Opinia:");
        if (ImGui.radioButton("Wszystkie##opinion", this.opinionSelector.get() == 0)) {
            this.opinionSelector.set(0);
        }
        if (ImGui.radioButton("\u2714##opinion", this.opinionSelector.get() == 1)) {
            this.opinionSelector.set(1);
        }
        if (ImGui.radioButton("\u274c##opinion", this.opinionSelector.get() == 2)) {
            this.opinionSelector.set(2);
        }
        ImGui.separator();
        ImGui.text("Nak\u0142adki:");
        ImGui.pushStyleVar(12, 8.0f);
        ImGui.checkbox("Nazwy", this.overlayNames);
        ImGui.popStyleVar();
        ImGui.pushStyleVar(12, 8.0f);
        ImGui.checkbox("Dystrykty", this.overlayDistricts);
        ImGui.popStyleVar();
        ImGui.pushStyleVar(12, 8.0f);
        ImGui.checkbox("Regiony", this.overlayRegions);
        ImGui.popStyleVar();
        ImGui.end();
    }

    @Generated
    public void setMarkerSearchListener(MarkerSearchListener markerSearchListener) {
        this.markerSearchListener = markerSearchListener;
    }
}

