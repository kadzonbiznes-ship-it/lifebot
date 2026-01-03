/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package org.pbrands.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import lombok.Generated;
import org.pbrands.map.MapRenderer;
import org.pbrands.map.Marker;
import org.pbrands.map.MarkerType;
import org.pbrands.map.panel.MapPanel;
import org.pbrands.map.panel.Opinion;
import org.pbrands.map.panel.Status;
import org.pbrands.model.MapDefinition;
import org.pbrands.netty.NettyClient;

public class Map {
    private final List<MarkerType> availableMarkerTypes = Collections.synchronizedList(new ArrayList());
    private final List<MapDefinition> availableMaps = Collections.synchronizedList(new ArrayList());
    private final java.util.Map<Integer, Marker> markers = Collections.synchronizedMap(new HashMap());
    private int maxCollectibles;
    private Marker selectedMarker;
    private final MapPanel mapPanel = new MapPanel(this);
    private final MapRenderer mapRenderer = new MapRenderer(this);
    private NettyClient nettyClient;
    private MapDefinition selectedMap;
    private static int markerId = 100;

    public void render() {
        this.mapRenderer.run();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<Marker> getMarkers() {
        java.util.Map<Integer, Marker> map = this.markers;
        synchronized (map) {
            return new ArrayList<Marker>(this.markers.values());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<Marker> getVisibleMarkers() {
        java.util.Map<Integer, Marker> map = this.markers;
        synchronized (map) {
            List<MarkerType> selectedMarkerTypes = this.mapPanel.getSelectedMarkerTypes();
            Status status = this.mapPanel.getStatus();
            Opinion opinion = this.mapPanel.getOpinion();
            return this.markers.values().stream().filter(marker -> selectedMarkerTypes.contains(marker.getType())).filter(marker -> status != Status.NOT_COLLECTED || !marker.isCollected()).filter(marker -> status != Status.COLLECTED || marker.isCollected()).filter(marker -> opinion != Opinion.LIKE || marker.isLiked()).filter(marker -> opinion != Opinion.DISLIKE || marker.isDisliked()).toList();
        }
    }

    public void addMarker(Marker marker) {
        if (this.markers.containsKey(marker.getId())) {
            Marker existing = this.markers.get(marker.getId());
            existing.setLikeCount(marker.getLikeCount());
            existing.setDislikeCount(marker.getDislikeCount());
            return;
        }
        this.markers.put(marker.getId(), marker);
    }

    public void removeMarker(int markerId) {
        this.markers.remove(markerId);
    }

    public void addMarkerType(MarkerType markerType) {
        for (int i = 0; i < this.availableMarkerTypes.size(); ++i) {
            MarkerType existing = this.availableMarkerTypes.get(i);
            if (existing.getId() != markerType.getId()) continue;
            this.availableMarkerTypes.set(i, markerType);
            return;
        }
        this.availableMarkerTypes.add(markerType);
    }

    public MarkerType getMarkerTypeById(int id) {
        for (MarkerType type : this.availableMarkerTypes) {
            if (type.getId() != id) continue;
            return type;
        }
        return null;
    }

    public List<MarkerType> getAvailableMarkerTypes() {
        return new ArrayList<MarkerType>(this.availableMarkerTypes);
    }

    @Generated
    public List<MapDefinition> getAvailableMaps() {
        return this.availableMaps;
    }

    @Generated
    public int getMaxCollectibles() {
        return this.maxCollectibles;
    }

    @Generated
    public void setMaxCollectibles(int maxCollectibles) {
        this.maxCollectibles = maxCollectibles;
    }

    @Generated
    public void setSelectedMarker(Marker selectedMarker) {
        this.selectedMarker = selectedMarker;
    }

    @Generated
    public Marker getSelectedMarker() {
        return this.selectedMarker;
    }

    @Generated
    public MapPanel getMapPanel() {
        return this.mapPanel;
    }

    @Generated
    public MapRenderer getMapRenderer() {
        return this.mapRenderer;
    }

    @Generated
    public void setNettyClient(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Generated
    public NettyClient getNettyClient() {
        return this.nettyClient;
    }

    @Generated
    public MapDefinition getSelectedMap() {
        return this.selectedMap;
    }

    @Generated
    public void setSelectedMap(MapDefinition selectedMap) {
        this.selectedMap = selectedMap;
    }
}

